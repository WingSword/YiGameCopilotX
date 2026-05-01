package org.walks.gamecopilot.lan.server

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANMessageType
import org.walks.gamecopilot.lan.data.LANPlayer
import platform.Foundation.*
import platform.darwin.*
import kotlin.native.concurrent.freeze

@OptIn(ExperimentalForeignApi::class)
actual class IOSLANHostServer : LANHostServer {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var serverJob: Job? = null
    private var heartbeatJob: Job? = null
    
    @Volatile
    private var _isRunning = false
    override val isRunning: Boolean get() = _isRunning
    
    @Volatile
    private var _port = DEFAULT_SERVER_PORT
    override val port: Int get() = _port
    
    private val connections = mutableMapOf<String, NSStream>()
    private val playerSessions = mutableMapOf<String, LANPlayer>()
    private val lastHeartbeat = mutableMapOf<String, Long>()
    
    private val _connectedPlayers = MutableStateFlow<List<LANPlayer>>(emptyList())
    override val connectedPlayers: Flow<List<LANPlayer>> = _connectedPlayers.asStateFlow()
    
    private val _receivedMessages = MutableSharedFlow<Pair<String, LANMessage>>()
    override val receivedMessages: Flow<Pair<String, LANMessage>> = _receivedMessages.asSharedFlow()
    
    private var listener: CFSocketRef? = null
    
    override suspend fun start(port: Int): Boolean {
        if (_isRunning) return true
        
        return withContext(Dispatchers.Default) {
            try {
                _port = port
                
                val socket = CFSocketCreate(
                    kCFAllocatorDefault,
                    PF_INET.toUByte(),
                    SOCK_STREAM.toUByte(),
                    IPPROTO_TCP.toUByte(),
                    kCFSocketAcceptCallBack,
                    staticCFunction { _, type, address, data, info ->
                        handleNewConnection(type, address, data, info)
                    },
                    null
                )
                
                if (socket == null) {
                    GameLogger.error("iOS: 创建Socket失败")
                    return@withContext false
                }
                
                val yes: IntVar = memScoped { alloc() }
                yes.value = 1
                
                setsockopt(
                    CFSocketGetNative(socket),
                    SOL_SOCKET,
                    SO_REUSEADDR,
                    yes.ptr,
                    sizeOf<IntVar>().toULong().convert()
                )
                
                memScoped {
                    val addr = alloc<sockaddr_in>()
                    addr.sin_len = sizeOf<sockaddr_in>().toUByte()
                    addr.sin_family = AF_INET.toUByte()
                    addr.sin_port = _port.toUShort().bigEndian
                    addr.sin_addr.s_addr = INADDR_ANY
                    
                    val addrData = CFDataCreate(
                        kCFAllocatorDefault,
                        addr.ptr.reinterpret(),
                        sizeOf<sockaddr_in>().toULong()
                    )
                    
                    val error = CFSocketSetAddress(socket, addrData)
                    if (error != kCFSocketSuccess) {
                        GameLogger.error("iOS: 绑定端口失败")
                        CFRelease(socket)
                        return@withContext false
                    }
                }
                
                val runLoopSource = CFSocketCreateRunLoopSource(kCFAllocatorDefault, socket, 0)
                CFRunLoopAddSource(CFRunLoopGetCurrent(), runLoopSource, kCFRunLoopDefaultMode)
                
                listener = socket
                _isRunning = true
                
                startHeartbeatChecker()
                
                GameLogger.info("iOS: 主机服务器启动成功，端口: $port")
                true
            } catch (e: Exception) {
                GameLogger.error("iOS: 启动主机服务器失败", e)
                false
            }
        }
    }
    
    private fun handleNewConnection(
        type: CFSocketCallBackType,
        address: CFDataRef?,
        data: UnsafeRawPointer?,
        info: UnsafeMutableRawPointer?
    ) {
        if (type != kCFSocketAcceptCallBack) return
        
        val newSocket = data?.let { 
            platform.posix.ptrToLong(it).toInt()
        } ?: return
        
        CoroutineScope(Dispatchers.Default).launch {
            handleClientConnection(newSocket)
        }
    }
    
    private suspend fun handleClientConnection(socketFd: Int) {
        val playerId = generatePlayerId()
        val buffer = ByteArray(4096)
        
        try {
            while (_isRunning) {
                val bytesRead = buffer.usePinned { pinnedBuffer ->
                    platform.posix.recv(socketFd, pinnedBuffer.addressOf(0), buffer.size.toULong(), 0)
                }
                
                if (bytesRead <= 0) break
                
                val data = buffer.decodeToString(0, bytesRead.toInt())
                
                if (data.startsWith("GET") && data.contains("Upgrade: websocket")) {
                    performWebSocketHandshake(socketFd, data)
                    continue
                }
                
                try {
                    val message = json.decodeFromString<LANMessage>(data)
                    lastHeartbeat[playerId] = System.currentTimeMillis()
                    
                    when (message.type) {
                        LANMessageType.HEARTBEAT -> {
                            val pong = LANMessage(
                                type = LANMessageType.HEARTBEAT,
                                playerId = playerId
                            )
                            sendMessageToSocket(socketFd, pong)
                        }
                        else -> {
                            _receivedMessages.emit(Pair(playerId, message))
                        }
                    }
                } catch (e: Exception) {
                    GameLogger.error("iOS: 解析消息失败", e)
                }
            }
        } catch (e: Exception) {
            GameLogger.error("iOS: 客户端连接异常", e)
        } finally {
            disconnectPlayer(playerId)
            platform.posix.close(socketFd)
        }
    }
    
    private fun performWebSocketHandshake(socketFd: Int, request: String) {
        val keyMatch = Regex("Sec-WebSocket-Key: (.+)").find(request)
        val clientKey = keyMatch?.groupValues?.get(1)?.trim() ?: return
        
        val acceptKey = generateWebSocketAcceptKey(clientKey)
        
        val response = buildString {
            appendLine("HTTP/1.1 101 Switching Protocols")
            appendLine("Upgrade: websocket")
            appendLine("Connection: Upgrade")
            appendLine("Sec-WebSocket-Accept: $acceptKey")
            appendLine()
        }
        
        response.encodeToByteArray().usePinned { pinned ->
            platform.posix.send(socketFd, pinned.addressOf(0), response.length.toULong(), 0)
        }
    }
    
    private fun generateWebSocketAcceptKey(clientKey: String): String {
        val magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val combined = clientKey + magic
        val sha1 = NSUUID().UUIDString
        return sha1.encodeToByteArray().let { 
            NSData.dataWithBytes(it.toCValues(), it.size.toULong())
        }.base64EncodedStringWithOptions(0u)
    }
    
    private fun sendMessageToSocket(socketFd: Int, message: LANMessage) {
        try {
            val data = json.encodeToString(message).encodeToByteArray()
            val frame = createWebSocketFrame(data)
            frame.usePinned { pinned ->
                platform.posix.send(socketFd, pinned.addressOf(0), frame.size.toULong(), 0)
            }
        } catch (e: Exception) {
            GameLogger.error("iOS: 发送消息失败", e)
        }
    }
    
    private fun createWebSocketFrame(data: ByteArray): ByteArray {
        val frame = mutableListOf<Byte>()
        frame.add(0x81.toByte())
        
        when {
            data.size <= 125 -> {
                frame.add(data.size.toByte())
            }
            data.size <= 65535 -> {
                frame.add(126.toByte())
                frame.add((data.size shr 8).toByte())
                frame.add(data.size.toByte())
            }
            else -> {
                frame.add(127.toByte())
                for (i in 7 downTo 0) {
                    frame.add((data.size shr (i * 8)).toByte())
                }
            }
        }
        
        frame.addAll(data.toList())
        return frame.toByteArray()
    }
    
    private suspend fun disconnectPlayer(playerId: String) {
        playerSessions.remove(playerId)
        lastHeartbeat.remove(playerId)
        updateConnectedPlayers()
        
        GameLogger.info("iOS: 玩家断开: $playerId")
    }
    
    override suspend fun stop() {
        _isRunning = false
        heartbeatJob?.cancel()
        
        listener?.let {
            CFSocketInvalidate(it)
            CFRelease(it)
        }
        listener = null
        
        connections.clear()
        playerSessions.clear()
        lastHeartbeat.clear()
        
        GameLogger.info("iOS: 主机服务器已停止")
    }
    
    override suspend fun broadcast(message: LANMessage) {
        connections.forEach { (_, _) ->
            // TODO: 实现广播
        }
    }
    
    override suspend fun sendToPlayer(playerId: String, message: LANMessage) {
        // TODO: 实现单播
    }
    
    override suspend fun kickPlayer(playerId: String, reason: String) {
        disconnectPlayer(playerId)
    }
    
    override fun getLocalIpAddress(): String {
        var address = "127.0.0.1"
        
        memScoped {
            val ifaddr = allocPointerTo<ifaddrs>()
            if (getifaddrs(ifaddr.ptr) == 0) {
                var addr = ifaddr.value
                while (addr != null) {
                    val interface = addr.pointed
                    val name = interface.ifa_name?.toKString() ?: ""
                    
                    if ((interface.ifa_flags.toInt() and IFF_LOOPBACK) == 0 &&
                        interface.ifa_addr != null) {
                        
                        val sa = interface.ifa_addr!!.pointed
                        if (sa.sa_family.toInt() == AF_INET) {
                            if (name.startsWith("en") || name.startsWith("wlan")) {
                                val addrIn = interface.ifa_addr!!.reinterpret<sockaddr_in>().pointed
                                val addrBytes = addrIn.sin_addr.s_addr.toInt()
                                
                                address = buildString {
                                    append((addrBytes and 0xFF))
                                    append(".")
                                    append((addrBytes shr 8) and 0xFF)
                                    append(".")
                                    append((addrBytes shr 16) and 0xFF)
                                    append(".")
                                    append((addrBytes shr 24) and 0xFF)
                                }
                            }
                        }
                    }
                    addr = interface.ifa_next
                }
                freeifaddrs(ifaddr.value)
            }
        }
        
        return address
    }
    
    private fun updateConnectedPlayers() {
        _connectedPlayers.value = playerSessions.values.toList()
    }
    
    private fun startHeartbeatChecker() {
        heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
            while (_isRunning) {
                val now = System.currentTimeMillis()
                lastHeartbeat.filter { (_, lastTime) ->
                    now - lastTime > CONNECTION_TIMEOUT
                }.keys.forEach { playerId ->
                    launch {
                        disconnectPlayer(playerId)
                    }
                }
                delay(HEARTBEAT_INTERVAL)
            }
        }
    }
    
    private fun generatePlayerId(): String {
        return "player_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    override fun dispose() {
        CoroutineScope(Dispatchers.Default).launch {
            stop()
        }
    }
}

actual fun createLANHostServer(): LANHostServer {
    return IOSLANHostServer()
}
