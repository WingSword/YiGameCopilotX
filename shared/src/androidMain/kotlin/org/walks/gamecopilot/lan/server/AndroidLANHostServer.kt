package org.walks.gamecopilot.lan.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANMessageType
import org.walks.gamecopilot.lan.data.LANPlayer
import org.walks.gamecopilot.lan.data.LANConstants.HEARTBEAT_INTERVAL
import org.walks.gamecopilot.lan.data.LANConstants.CONNECTION_TIMEOUT
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap

class AndroidLANHostServer : LANHostServer {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private var serverJob: Job? = null
    private var heartbeatJob: Job? = null
    
    @Volatile
    private var _isRunning = false
    override val isRunning: Boolean get() = _isRunning
    
    @Volatile
    private var _port = DEFAULT_SERVER_PORT
    override val port: Int get() = _port
    
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val playerSessions = ConcurrentHashMap<String, LANPlayer>()
    
    private val _connectedPlayers = MutableStateFlow<List<LANPlayer>>(emptyList())
    override val connectedPlayers: Flow<List<LANPlayer>> = _connectedPlayers.asStateFlow()
    
    private val _receivedMessages = MutableSharedFlow<Pair<String, LANMessage>>()
    override val receivedMessages: Flow<Pair<String, LANMessage>> = _receivedMessages.asSharedFlow()
    
    private val lastHeartbeat = ConcurrentHashMap<String, Long>()
    
    override suspend fun start(port: Int): Boolean {
        if (_isRunning) return true
        
        return withContext(Dispatchers.IO) {
            try {
                _port = port
                
                server = embeddedServer(Netty, port = port) {
                    install(WebSockets)
                    
                    routing {
                        webSocket("/lan") {
                            val playerId = call.request.queryParameters["playerId"] ?: generatePlayerId()
                            val playerName = call.request.queryParameters["playerName"] ?: "Player"
                            
                            handleConnection(playerId, playerName, this)
                        }
                    }
                }.start(wait = false)
                
                _isRunning = true
                
                startHeartbeatChecker()
                
                GameLogger.info("主机服务器启动成功，端口: $port")
                true
            } catch (e: Exception) {
                GameLogger.error("启动主机服务器失败", e)
                false
            }
        }
    }
    
    private suspend fun handleConnection(playerId: String, playerName: String, session: WebSocketSession) {
        connections[playerId] = session
        val player = LANPlayer(
            id = playerId,
            name = playerName,
            playerIndex = connections.size - 1,
            connectedAt = System.currentTimeMillis()
        )
        playerSessions[playerId] = player
        lastHeartbeat[playerId] = System.currentTimeMillis()
        
        updateConnectedPlayers()
        
        GameLogger.info("玩家连接: $playerName ($playerId)")
        
        try {
            val welcomeMessage = LANMessage(
                type = LANMessageType.JOIN_RESPONSE,
                playerId = playerId,
                payload = json.encodeToString(player)
            )
            session.send(Frame.Text(json.encodeToString(welcomeMessage)))
            
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val message = json.decodeFromString<LANMessage>(text)
                        lastHeartbeat[playerId] = System.currentTimeMillis()
                        
                        when (message.type) {
                            LANMessageType.HEARTBEAT -> {
                                val pong = LANMessage(
                                    type = LANMessageType.HEARTBEAT,
                                    playerId = playerId
                                )
                                session.send(Frame.Text(json.encodeToString(pong)))
                            }
                            else -> {
                                _receivedMessages.emit(Pair(playerId, message))
                            }
                        }
                    } catch (e: Exception) {
                        GameLogger.error("解析消息失败: $text", e)
                    }
                }
            }
        } catch (e: Exception) {
            GameLogger.error("连接异常: $playerId", e)
        } finally {
            disconnectPlayer(playerId)
        }
    }
    
    private suspend fun disconnectPlayer(playerId: String) {
        connections.remove(playerId)
        playerSessions.remove(playerId)
        lastHeartbeat.remove(playerId)
        
        updateConnectedPlayers()
        
        val leaveMessage = LANMessage(
            type = LANMessageType.PLAYER_LEFT,
            playerId = playerId
        )
        broadcast(leaveMessage)
        
        GameLogger.info("玩家断开: $playerId")
    }
    
    override suspend fun stop() {
        _isRunning = false
        heartbeatJob?.cancel()
        
        connections.values.forEach { session ->
            try {
                session.close()
            } catch (e: Exception) {
                GameLogger.error("关闭连接失败", e)
            }
        }
        
        connections.clear()
        playerSessions.clear()
        lastHeartbeat.clear()
        
        server?.stop(1000, 2000)
        server = null
        
        GameLogger.info("主机服务器已停止")
    }
    
    override suspend fun broadcast(message: LANMessage) {
        val data = json.encodeToString(message)
        val frame = Frame.Text(data)
        
        connections.values.forEach { session ->
            try {
                session.send(frame)
            } catch (e: Exception) {
                GameLogger.error("广播消息失败", e)
            }
        }
    }
    
    override suspend fun sendToPlayer(playerId: String, message: LANMessage) {
        val session = connections[playerId] ?: return
        val data = json.encodeToString(message)
        session.send(Frame.Text(data))
    }
    
    override suspend fun kickPlayer(playerId: String, reason: String) {
        val session = connections[playerId] ?: return
        
        val kickMessage = LANMessage(
            type = LANMessageType.ERROR,
            playerId = playerId,
            payload = reason
        )
        sendToPlayer(playerId, kickMessage)
        
        session.close()
        disconnectPlayer(playerId)
    }
    
    override fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress()) {
                        if (networkInterface.name.startsWith("wlan") || 
                            networkInterface.name.startsWith("eth") ||
                            networkInterface.name.startsWith("en")) {
                            return address.hostAddress ?: ""
                        }
                    }
                }
            }
        } catch (e: Exception) {
            GameLogger.error("获取本地IP失败", e)
        }
        return "127.0.0.1"
    }
    
    private fun updateConnectedPlayers() {
        _connectedPlayers.value = playerSessions.values.toList()
    }
    
    private fun startHeartbeatChecker() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (_isRunning) {
                val now = System.currentTimeMillis()
                val timeoutPlayers = lastHeartbeat.filter { (_, lastTime) ->
                    now - lastTime > CONNECTION_TIMEOUT
                }.keys
                
                timeoutPlayers.forEach { playerId ->
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
        CoroutineScope(Dispatchers.IO).launch {
            stop()
        }
    }
}

actual fun createLANHostServer(): LANHostServer {
    return AndroidLANHostServer()
}
