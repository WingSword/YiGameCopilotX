package org.walks.gamecopilot.lan.discovery

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANMessageType
import org.walks.gamecopilot.lan.data.LANRoomInfo
import platform.Foundation.*
import platform.darwin.*

@OptIn(ExperimentalForeignApi::class)
actual class IOSServiceDiscovery(
    private val port: Int = DEFAULT_DISCOVERY_PORT
) : ServiceDiscovery {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var broadcastJob: Job? = null
    private var discoveryJob: Job? = null
    private var receiveJob: Job? = null
    
    private val _discoveredRooms = MutableSharedFlow<LANRoomInfo>(replay = 0)
    override val discoveredRooms: Flow<LANRoomInfo> = _discoveredRooms.asSharedFlow()
    
    private val _receivedMessages = MutableSharedFlow<Pair<String, LANMessage>>(replay = 0)
    override val receivedMessages: Flow<Pair<String, LANMessage>> = _receivedMessages.asSharedFlow()
    
    @Volatile
    private var _isDiscovering = false
    override val isDiscovering: Boolean get() = _isDiscovering
    
    private var currentRoomInfo: LANRoomInfo? = null
    private var udpSocket: CFSocketRef? = null
    private var socketContext: CFSocketContext = CFSocketContext()
    
    override suspend fun startDiscovery(gameTypeFilter: String?) {
        if (_isDiscovering) return
        
        _isDiscovering = true
        
        try {
            setupSocket()
            
            receiveJob = CoroutineScope(Dispatchers.Default).launch {
                startReceiving()
            }
            
            discoveryJob = CoroutineScope(Dispatchers.Default).launch {
                while (_isDiscovering) {
                    sendDiscoveryRequest()
                    delay(DISCOVERY_BROADCAST_INTERVAL)
                }
            }
            
            GameLogger.info("iOS: 开始发现服务，端口: $port")
        } catch (e: Exception) {
            GameLogger.error("iOS: 启动发现服务失败", e)
            _isDiscovering = false
        }
    }
    
    override suspend fun stopDiscovery() {
        _isDiscovering = false
        discoveryJob?.cancel()
        receiveJob?.cancel()
        cleanupSocket()
        GameLogger.info("iOS: 停止发现服务")
    }
    
    override suspend fun broadcastPresence(roomInfo: LANRoomInfo) {
        currentRoomInfo = roomInfo
        
        if (broadcastJob?.isActive == true) return
        
        broadcastJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                try {
                    val message = LANMessage(
                        type = LANMessageType.DISCOVERY_BROADCAST,
                        roomId = roomInfo.roomId,
                        payload = json.encodeToString(roomInfo)
                    )
                    broadcastMessage(message)
                } catch (e: Exception) {
                    GameLogger.error("iOS: 广播房间信息失败", e)
                }
                delay(DISCOVERY_BROADCAST_INTERVAL)
            }
        }
        
        GameLogger.info("iOS: 开始广播房间: ${roomInfo.roomName}")
    }
    
    override suspend fun stopBroadcasting() {
        broadcastJob?.cancel()
        broadcastJob = null
        currentRoomInfo = null
        GameLogger.info("iOS: 停止广播房间")
    }
    
    override suspend fun sendDiscoveryRequest() {
        try {
            val requestMessage = LANMessage(
                type = LANMessageType.DISCOVERY_BROADCAST,
                payload = "DISCOVERY_REQUEST"
            )
            broadcastMessage(requestMessage)
        } catch (e: Exception) {
            GameLogger.error("iOS: 发送发现请求失败", e)
        }
    }
    
    override suspend fun sendDiscoveryResponse(roomInfo: LANRoomInfo, targetAddress: String) {
        try {
            val responseMessage = LANMessage(
                type = LANMessageType.DISCOVERY_RESPONSE,
                roomId = roomInfo.roomId,
                payload = json.encodeToString(roomInfo)
            )
            sendMessage(responseMessage, targetAddress, port)
        } catch (e: Exception) {
            GameLogger.error("iOS: 发送发现响应失败", e)
        }
    }
    
    override suspend fun sendMessage(message: LANMessage, targetAddress: String, port: Int) {
        try {
            val data = json.encodeToString(message).encodeToByteArray()
            val nsData = NSData.dataWithBytes(data.toCValues(), data.size.toULong())
            val address = getSocketAddress(targetAddress, port)
            
            address?.useContents {
                val socketRef = CFSocketCreate(
                    kCFAllocatorDefault,
                    AF_INET.toUByte(),
                    SOCK_DGRAM.toUByte(),
                    IPPROTO_UDP.toUByte(),
                    0u,
                    null,
                    null
                )
                
                val sendData = CFDataCreate(
                    kCFAllocatorDefault,
                    reinterpret(),
                    sizeOf<sockaddr_in>().toULong()
                )
                
                if (socketRef != null && sendData != null) {
                    CFSocketSendData(socketRef, sendData, nsData, 0.0)
                    CFRelease(socketRef)
                }
            }
        } catch (e: Exception) {
            GameLogger.error("iOS: 发送消息失败", e)
        }
    }
    
    override suspend fun broadcastMessage(message: LANMessage) {
        sendMessage(message, "255.255.255.255", port)
    }
    
    private fun setupSocket() {
        memScoped {
            socketContext = CFSocketContext(
                version = 0u,
                info = StableRef.create(this@IOSServiceDiscovery).asCPointer(),
                retain = null,
                release = null,
                copyDescription = null
            )
            
            udpSocket = CFSocketCreate(
                kCFAllocatorDefault,
                AF_INET.toUByte(),
                SOCK_DGRAM.toUByte(),
                IPPROTO_UDP.toUByte(),
                kCFSocketNoCallBack,
                null,
                null
            )
            
            val yes: IntVar = alloc()
            yes.value = 1
            
            setsockopt(
                CFSocketGetNative(udpSocket),
                SOL_SOCKET,
                SO_BROADCAST,
                yes.ptr,
                sizeOf<IntVar>().toULong().convert()
            )
            
            val addr = alloc<sockaddr_in>()
            addr.sin_len = sizeOf<sockaddr_in>().toUByte()
            addr.sin_family = AF_INET.toUByte()
            addr.sin_port = port.toUShort().bigEndian
            addr.sin_addr.s_addr = INADDR_ANY
            
            val addrData = CFDataCreate(
                kCFAllocatorDefault,
                addr.ptr.reinterpret(),
                sizeOf<sockaddr_in>().toULong()
            )
            
            CFSocketSetAddress(udpSocket, addrData)
        }
    }
    
    private fun cleanupSocket() {
        udpSocket?.let {
            CFSocketInvalidate(it)
            CFRelease(it)
        }
        udpSocket = null
    }
    
    private suspend fun startReceiving() {
        val buffer = ByteArray(4096)
        var sockAddr = ByteArray(sizeOf<sockaddr_in>())
        
        while (_isDiscovering) {
            try {
                val nativeSocket = CFSocketGetNative(udpSocket)
                if (nativeSocket == -1) {
                    delay(100)
                    continue
                }
                
                val received = buffer.usePinned { bufferPin ->
                    sockAddr.usePinned { addrPin ->
                        memScoped {
                            val addrLen = alloc<UIntVar>()
                            addrLen.value = sizeOf<sockaddr_in>().toULong()
                            
                            platform.posix.recvfrom(
                                nativeSocket,
                                bufferPin.addressOf(0),
                                buffer.size.toULong(),
                                0u,
                                addrPin.addressOf(0).reinterpret(),
                                addrLen.ptr
                            )
                        }
                    }
                }
                
                if (received > 0) {
                    val data = buffer.decodeToString(0, received.toInt())
                    val message = json.decodeFromString<LANMessage>(data)
                    
                    val senderAddress = memScoped {
                        val addr = sockAddr.toCValues().reinterpret<sockaddr_in>().pointed
                        val ip = platform.posix.inet_ntoa(addr.sin_addr)
                        ip?.toKString() ?: ""
                    }
                    
                    handleMessage(message, senderAddress)
                }
            } catch (e: Exception) {
                if (_isDiscovering) {
                    GameLogger.error("iOS: 接收消息失败", e)
                }
            }
            delay(50)
        }
    }
    
    private suspend fun handleMessage(message: LANMessage, senderAddress: String) {
        when (message.type) {
            LANMessageType.DISCOVERY_BROADCAST -> {
                if (message.payload == "DISCOVERY_REQUEST") {
                    currentRoomInfo?.let { info ->
                        sendDiscoveryResponse(info, senderAddress)
                    }
                } else {
                    try {
                        val roomInfo = json.decodeFromString<LANRoomInfo>(message.payload)
                        if (roomInfo.roomId != currentRoomInfo?.roomId) {
                            _discoveredRooms.emit(roomInfo)
                        }
                    } catch (e: Exception) {
                        GameLogger.error("iOS: 解析房间信息失败", e)
                    }
                }
            }
            LANMessageType.DISCOVERY_RESPONSE -> {
                try {
                    val roomInfo = json.decodeFromString<LANRoomInfo>(message.payload)
                    _discoveredRooms.emit(roomInfo)
                } catch (e: Exception) {
                    GameLogger.error("iOS: 解析发现响应失败", e)
                }
            }
            else -> {
                _receivedMessages.emit(Pair(senderAddress, message))
            }
        }
    }
    
    private fun getSocketAddress(address: String, port: Int): CValuesRef<sockaddr_in>? {
        return memScoped {
            val addr = alloc<sockaddr_in>()
            addr.sin_len = sizeOf<sockaddr_in>().toUByte()
            addr.sin_family = AF_INET.toUByte()
            addr.sin_port = port.toUShort().bigEndian
            
            val addrData = address.encodeToByteArray()
            platform.posix.inet_pton(AF_INET, address, addr.sin_addr.ptr)
            
            addr.ptr
        }
    }
    
    override fun dispose() {
        CoroutineScope(Dispatchers.Default).launch {
            stopDiscovery()
            stopBroadcasting()
        }
    }
}

actual fun createServiceDiscovery(port: Int): ServiceDiscovery {
    return IOSServiceDiscovery(port)
}
