package org.walks.gamecopilot.lan

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.lan.client.LANClient
import org.walks.gamecopilot.lan.client.createLANClient
import org.walks.gamecopilot.lan.data.*
import org.walks.gamecopilot.lan.discovery.ServiceDiscovery
import org.walks.gamecopilot.lan.discovery.createServiceDiscovery
import org.walks.gamecopilot.lan.server.LANHostServer
import org.walks.gamecopilot.lan.server.createLANHostServer

class LANRoomManager {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var discovery: ServiceDiscovery = createServiceDiscovery()
    private var hostServer: LANHostServer = createLANHostServer()
    private var client: LANClient = createLANClient()
    
    private val _discoveredRooms = MutableStateFlow<List<LANRoomInfo>>(emptyList())
    val discoveredRooms: StateFlow<List<LANRoomInfo>> = _discoveredRooms.asStateFlow()
    
    private val _currentRoom = MutableStateFlow<LANRoomState?>(null)
    val currentRoom: StateFlow<LANRoomState?> = _currentRoom.asStateFlow()
    
    private val _connectionState = MutableStateFlow(LANConnectionState(ConnectionStatus.DISCONNECTED))
    val connectionState: StateFlow<LANConnectionState> = _connectionState.asStateFlow()
    
    private val _players = MutableStateFlow<List<LANPlayer>>(emptyList())
    val players: StateFlow<List<LANPlayer>> = _players.asStateFlow()
    
    private val _errors = MutableSharedFlow<LANError>()
    val errors: Flow<LANError> = _errors.asSharedFlow()
    
    private val _gameStateUpdates = MutableSharedFlow<LANGameState>()
    val gameStateUpdates: Flow<LANGameState> = _gameStateUpdates.asSharedFlow()
    
    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()
    
    private var currentRoomPassword: String = ""
    private var messageHandlerJob: Job? = null
    
    val isConnected: Boolean get() = _connectionState.value.status == ConnectionStatus.CONNECTED
    val currentRoomInfo: LANRoomInfo? get() = _currentRoom.value?.roomInfo
    val currentPlayerId: String?
        get() = if (_isHost.value) {
            _players.value.firstOrNull { it.isHost }?.id
        } else {
            client.currentPlayer?.id
        }
    
    init {
        setupMessageHandlers()
    }
    
    fun startDiscovery(gameType: GameType = GameType.ALL) {
        scope.launch {
            _connectionState.value = LANConnectionState(ConnectionStatus.DISCOVERING)
            
            discovery.discoveredRooms
                .onEach { room ->
                    val currentList = _discoveredRooms.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.roomId == room.roomId }
                    
                    if (existingIndex >= 0) {
                        currentList[existingIndex] = room
                    } else {
                        currentList.add(room)
                    }
                    
                    _discoveredRooms.value = currentList.sortedByDescending { it.createdAt }
                }
                .launchIn(scope)
            
            discovery.startDiscovery(gameType.displayName)
        }
    }
    
    fun stopDiscovery() {
        scope.launch {
            discovery.stopDiscovery()
            _connectionState.value = LANConnectionState(ConnectionStatus.DISCONNECTED)
        }
    }
    
    fun clearDiscoveredRooms() {
        _discoveredRooms.value = emptyList()
    }
    
    fun createRoom(
        roomName: String,
        hostName: String,
        gameType: GameType,
        maxPlayers: Int = 8,
        password: String = "",
        port: Int = org.walks.gamecopilot.lan.server.DEFAULT_SERVER_PORT
    ): Boolean {
        if (hostServer.isRunning) {
            GameLogger.warning("服务器已在运行")
            return false
        }
        
        scope.launch {
            _connectionState.value = LANConnectionState(ConnectionStatus.CONNECTING)
            
            val started = hostServer.start(port)
            if (!started) {
                _errors.emit(LANError(LANErrorCodes.NETWORK_ERROR, "无法启动服务器", false))
                _connectionState.value = LANConnectionState(ConnectionStatus.ERROR, "无法启动服务器")
                return@launch
            }
            
            val localIp = hostServer.getLocalIpAddress()
            val roomId = generateRoomId()
            
            val roomInfo = LANRoomInfo(
                roomId = roomId,
                roomName = roomName,
                hostName = hostName,
                hostAddress = localIp,
                port = hostServer.port,
                gameType = gameType,
                maxPlayers = maxPlayers,
                currentPlayers = 1,
                hasPassword = password.isNotEmpty()
            )
            
            currentRoomPassword = password
            
            val hostPlayer = LANPlayer(
                id = "host_${Clock.System.now().toEpochMilliseconds()}",
                name = hostName,
                isHost = true,
                isReady = true,
                playerIndex = 0
            )
            
            _currentRoom.value = LANRoomState(
                roomInfo = roomInfo,
                players = listOf(hostPlayer),
                gameStarted = false
            )
            
            _players.value = listOf(hostPlayer)
            _isHost.value = true
            _connectionState.value = LANConnectionState(ConnectionStatus.CONNECTED)
            
            discovery.broadcastPresence(roomInfo)
            
            hostServer.connectedPlayers
                .onEach { playerList ->
                    updatePlayerList(playerList)
                }
                .launchIn(scope)
            
            hostServer.receivedMessages
                .onEach { (playerId, message) ->
                    handleHostMessage(playerId, message)
                }
                .launchIn(scope)
            
            GameLogger.info("房间创建成功: $roomName ($roomId)")
        }
        
        return true
    }
    
    fun joinRoom(roomInfo: LANRoomInfo, playerName: String, password: String = ""): Boolean {
        if (client.isConnected) {
            GameLogger.warning("已连接到其他房间")
            return false
        }
        
        if (roomInfo.hasPassword && password.isEmpty()) {
            scope.launch {
                _errors.emit(LANError(LANErrorCodes.INVALID_PASSWORD, "需要密码", true))
            }
            return false
        }
        
        scope.launch {
            _connectionState.value = LANConnectionState(ConnectionStatus.CONNECTING)
            
            val connected = client.connect(roomInfo.hostAddress, roomInfo.port, playerName)
            if (!connected) {
                _errors.emit(LANError(LANErrorCodes.CONNECTION_TIMEOUT, "连接失败", true))
                _connectionState.value = LANConnectionState(ConnectionStatus.ERROR, "连接失败")
                return@launch
            }
            
            _isHost.value = false
            currentRoomPassword = password
            
            val joined = client.joinRoom(roomInfo.roomId, password)
            if (!joined) {
                _errors.emit(LANError(LANErrorCodes.ROOM_NOT_FOUND, "加入房间失败", true))
                disconnect()
                return@launch
            }
            
            _currentRoom.value = LANRoomState(roomInfo = roomInfo, gameStarted = false)
            _connectionState.value = LANConnectionState(ConnectionStatus.CONNECTED)
            
            client.connectionState
                .onEach { state ->
                    _connectionState.value = state
                    if (state.status == ConnectionStatus.ERROR || state.status == ConnectionStatus.DISCONNECTED) {
                        if (_currentRoom.value != null) {
                            _errors.emit(LANError(LANErrorCodes.HOST_DISCONNECTED, "与主机断开连接", false))
                        }
                    }
                }
                .launchIn(scope)
            
            client.receivedMessages
                .onEach { message ->
                    handleClientMessage(message)
                }
                .launchIn(scope)
            
            GameLogger.info("加入房间成功: ${roomInfo.roomName}")
        }
        
        return true
    }
    
    fun disconnect() {
        scope.launch {
            if (_isHost.value) {
                discovery.stopBroadcasting()
                
                val closeMessage = LANMessage(type = LANMessageType.ROOM_CLOSED)
                broadcastGameAction(closeMessage)
                
                hostServer.stop()
                _currentRoom.value = null
                _players.value = emptyList()
            } else {
                client.leaveRoom()
                client.disconnect()
            }
            
            _isHost.value = false
            _connectionState.value = LANConnectionState(ConnectionStatus.DISCONNECTED)
            GameLogger.info("已断开连接")
        }
    }
    
    fun startGame() {
        if (!_isHost.value) {
            scope.launch {
                _errors.emit(LANError(LANErrorCodes.NOT_ROOM_OWNER, "只有房主可以开始游戏", true))
            }
            return
        }
        
        scope.launch {
            val message = LANMessage(
                type = LANMessageType.START_GAME,
                roomId = _currentRoom.value?.roomInfo?.roomId ?: ""
            )
            broadcastGameAction(message)
            
            _currentRoom.value?.let { state ->
                _currentRoom.value = state.copy(gameStarted = true)
            }
            
            GameLogger.info("游戏已开始")
        }
    }
    
    fun endGame() {
        if (!_isHost.value) return
        
        scope.launch {
            val message = LANMessage(
                type = LANMessageType.END_GAME,
                roomId = _currentRoom.value?.roomInfo?.roomId ?: ""
            )
            broadcastGameAction(message)
            
            _currentRoom.value?.let { state ->
                _currentRoom.value = state.copy(gameStarted = false)
            }
            
            GameLogger.info("游戏已结束")
        }
    }
    
    fun syncGameState(gameState: Any, gameType: GameType = GameType.ALL) {
        if (!_isHost.value) return
        
        scope.launch {
            val stateJson = json.encodeToString(gameState)
            
            val lanGameState = LANGameState(
                gameType = gameType,
                rawData = stateJson
            )
            
            val message = LANMessage(
                type = LANMessageType.GAME_STATE_SYNC,
                roomId = _currentRoom.value?.roomInfo?.roomId ?: "",
                payload = json.encodeToString(lanGameState)
            )
            broadcastGameAction(message)
        }
    }
    
    fun sendGameAction(action: String, data: Any? = null) {
        scope.launch {
            val payload = if (data != null) {
                json.encodeToString(data)
            } else {
                action
            }
            
            val message = LANMessage(
                type = LANMessageType.GAME_ACTION,
                roomId = _currentRoom.value?.roomInfo?.roomId ?: "",
                playerId = if (_isHost.value) "host" else client.currentPlayer?.id ?: "",
                payload = payload
            )
            
            if (_isHost.value) {
                broadcastGameAction(message)
            } else {
                client.sendMessage(message)
            }
        }
    }
    
    fun kickPlayer(playerId: String, reason: String = "被房主移出") {
        if (!_isHost.value) return
        
        scope.launch {
            hostServer.kickPlayer(playerId, reason)
        }
    }
    
    private suspend fun broadcastGameAction(message: LANMessage) {
        hostServer.broadcast(message)
    }
    
    private fun setupMessageHandlers() {
        messageHandlerJob = scope.launch {
            combine(
                hostServer.receivedMessages,
                client.receivedMessages
            ) { hostMsg, clientMsg ->
                Pair(hostMsg, clientMsg)
            }.collect()
        }
    }
    
    private suspend fun handleHostMessage(playerId: String, message: LANMessage) {
        when (message.type) {
            LANMessageType.GAME_ACTION -> {
                broadcastGameAction(message.copy(playerId = playerId))
            }
            else -> {
                GameLogger.debug("主机收到消息: ${message.type}")
            }
        }
    }
    
    private suspend fun handleClientMessage(message: LANMessage) {
        when (message.type) {
            LANMessageType.GAME_STATE_SYNC -> {
                try {
                    val gameState = json.decodeFromString<LANGameState>(message.payload)
                    _gameStateUpdates.emit(gameState)
                } catch (e: Exception) {
                    GameLogger.error("解析游戏状态失败", e)
                }
            }
            LANMessageType.GAME_ACTION -> {
                _gameStateUpdates.emit(
                    LANGameState(
                        gameType = GameType.ALL,
                        rawData = message.payload
                    )
                )
            }
            LANMessageType.PLAYER_JOINED -> {
                val player = json.decodeFromString<LANPlayer>(message.payload)
                updateClientPlayerList(player, true)
            }
            LANMessageType.PLAYER_LEFT -> {
                val player = json.decodeFromString<LANPlayer>(message.payload)
                updateClientPlayerList(player, false)
            }
            LANMessageType.START_GAME -> {
                _currentRoom.value?.let { state ->
                    _currentRoom.value = state.copy(gameStarted = true)
                }
            }
            LANMessageType.END_GAME -> {
                _currentRoom.value?.let { state ->
                    _currentRoom.value = state.copy(gameStarted = false)
                }
            }
            LANMessageType.ROOM_CLOSED -> {
                _errors.emit(LANError(LANErrorCodes.HOST_DISCONNECTED, "房间已关闭", false))
                disconnect()
            }
            LANMessageType.ERROR -> {
                _errors.emit(LANError(LANErrorCodes.NETWORK_ERROR, message.payload, true))
            }
            else -> {
                GameLogger.debug("客户端收到消息: ${message.type}")
            }
        }
    }
    
    private fun updatePlayerList(newPlayers: List<LANPlayer>) {
        _players.value = newPlayers
        
        _currentRoom.value?.let { state ->
            _currentRoom.value = state.copy(
                players = newPlayers,
                roomInfo = state.roomInfo.copy(currentPlayers = newPlayers.size)
            )
        }
    }
    
    private fun updateClientPlayerList(player: LANPlayer, joined: Boolean) {
        val currentList = _players.value.toMutableList()
        
        if (joined) {
            if (currentList.none { it.id == player.id }) {
                currentList.add(player)
            }
        } else {
            currentList.removeAll { it.id == player.id }
        }
        
        _players.value = currentList
        
        _currentRoom.value?.let { state ->
            _currentRoom.value = state.copy(
                players = currentList,
                roomInfo = state.roomInfo.copy(currentPlayers = currentList.size)
            )
        }
    }
    
    private fun generateRoomId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
    
    fun dispose() {
        scope.launch {
            disconnect()
            discovery.dispose()
            hostServer.dispose()
            client.dispose()
            messageHandlerJob?.cancel()
        }
        scope.cancel()
    }
}

val lanRoomManager by lazy { LANRoomManager() }
