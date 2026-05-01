package org.walks.gamecopilot.lan.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.lan.data.*

class IOSLANClient : LANClient {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var client: HttpClient? = null
    private var session: WebSocketSession? = null
    private var receiveJob: Job? = null
    private var heartbeatJob: Job? = null
    
    private val _connectionState = MutableStateFlow(LANConnectionState(ConnectionStatus.DISCONNECTED))
    override val connectionState: Flow<LANConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedMessages = MutableSharedFlow<LANMessage>()
    override val receivedMessages: Flow<LANMessage> = _receivedMessages.asSharedFlow()
    
    @Volatile
    private var _isConnected = false
    override val isConnected: Boolean get() = _isConnected
    
    private var _currentPlayer: LANPlayer? = null
    override val currentPlayer: LANPlayer? get() = _currentPlayer
    
    private var currentRoomId: String = ""
    
    override suspend fun connect(hostAddress: String, port: Int, playerName: String): Boolean {
        if (_isConnected) return true
        
        _connectionState.value = LANConnectionState(ConnectionStatus.CONNECTING)
        
        return withContext(Dispatchers.Default) {
            try {
                client = HttpClient {
                    install(WebSockets) {
                        pingInterval = HEARTBEAT_INTERVAL
                    }
                }
                
                session = client?.webSocketSession {
                    url {
                        protocol = URLProtocol.WS
                        host = hostAddress
                        this.port = port
                        path("lan")
                        parameter("playerName", playerName)
                    }
                }
                
                _isConnected = true
                _connectionState.value = LANConnectionState(ConnectionStatus.CONNECTED)
                
                startReceiving()
                startHeartbeat()
                
                GameLogger.info("iOS: 已连接到主机: $hostAddress:$port")
                true
            } catch (e: Exception) {
                _connectionState.value = LANConnectionState(
                    ConnectionStatus.ERROR,
                    "连接失败: ${e.message}"
                )
                GameLogger.error("iOS: 连接主机失败", e)
                false
            }
        }
    }
    
    private suspend fun startReceiving() {
        receiveJob = CoroutineScope(Dispatchers.Default).launch {
            session?.incoming?.consumeAsFlow()?.collect { frame ->
                when (frame) {
                    is Frame.Text -> {
                        try {
                            val text = frame.readText()
                            val message = json.decodeFromString<LANMessage>(text)
                            
                            when (message.type) {
                                LANMessageType.JOIN_RESPONSE -> {
                                    _currentPlayer = json.decodeFromString<LANPlayer>(message.payload)
                                    GameLogger.info("iOS: 加入成功: ${_currentPlayer?.name}")
                                }
                                LANMessageType.HEARTBEAT -> {
                                    // 心跳响应，忽略
                                }
                                else -> {
                                    _receivedMessages.emit(message)
                                }
                            }
                        } catch (e: Exception) {
                            GameLogger.error("iOS: 解析消息失败", e)
                        }
                    }
                    is Frame.Close -> {
                        disconnect()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.Default).launch {
            while (_isConnected) {
                try {
                    val heartbeat = LANMessage(type = LANMessageType.HEARTBEAT)
                    sendMessage(heartbeat)
                } catch (e: Exception) {
                    GameLogger.error("iOS: 发送心跳失败", e)
                }
                delay(HEARTBEAT_INTERVAL)
            }
        }
    }
    
    override suspend fun disconnect() {
        _isConnected = false
        heartbeatJob?.cancel()
        receiveJob?.cancel()
        
        try {
            session?.close()
        } catch (e: Exception) {
            GameLogger.error("iOS: 关闭会话失败", e)
        }
        
        try {
            client?.close()
        } catch (e: Exception) {
            GameLogger.error("iOS: 关闭客户端失败", e)
        }
        
        session = null
        client = null
        _currentPlayer = null
        currentRoomId = ""
        
        _connectionState.value = LANConnectionState(ConnectionStatus.DISCONNECTED)
        GameLogger.info("iOS: 已断开连接")
    }
    
    override suspend fun sendMessage(message: LANMessage) {
        if (!_isConnected || session == null) {
            GameLogger.warning("iOS: 未连接，无法发送消息")
            return
        }
        
        try {
            val data = json.encodeToString(message)
            session?.send(Frame.Text(data))
        } catch (e: Exception) {
            GameLogger.error("iOS: 发送消息失败", e)
            _connectionState.value = LANConnectionState(
                ConnectionStatus.ERROR,
                "发送失败: ${e.message}"
            )
        }
    }
    
    override suspend fun joinRoom(roomId: String, password: String?): Boolean {
        if (!_isConnected) return false
        
        currentRoomId = roomId
        
        val joinMessage = LANMessage(
            type = LANMessageType.JOIN_ROOM,
            roomId = roomId,
            payload = password ?: ""
        )
        sendMessage(joinMessage)
        
        return true
    }
    
    override suspend fun leaveRoom() {
        if (!_isConnected || currentRoomId.isEmpty()) return
        
        val leaveMessage = LANMessage(
            type = LANMessageType.LEAVE_ROOM,
            roomId = currentRoomId
        )
        sendMessage(leaveMessage)
        
        currentRoomId = ""
    }
    
    override fun dispose() {
        CoroutineScope(Dispatchers.Default).launch {
            disconnect()
        }
    }
}

actual fun createLANClient(): LANClient {
    return IOSLANClient()
}
