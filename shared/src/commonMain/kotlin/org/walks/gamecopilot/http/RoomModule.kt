package org.walks.gamecopilot.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.data.WsMessage

// 新建文件：RoomService.kt
class RoomModule(
    private val httpClient: HttpClient, private val listener: WebSocketListener? = null
) {
    // 定义常量以避免硬编码
    companion object {
        private const val ROOM_ID_KEY = "roomId"
        private const val PASSWORD_KEY = "passWord"
        private const val USER_ID = "userId"
        private const val CREATE_ROOM_URL = "/createRoom"
        private const val JOIN_ROOM_URL = "/joinRoom"
        private const val ROOM_INFO_URL = "/getRoomInfo"
        private const val LEAVE_ROOM_URL = "/leaveRoom"
        private const val START_GAME_URL = "/startGame"
        private const val DELETE_ROOM_URL = "/deleteRoom"
    }

    // 新增WebSocket会话
    private var webSocketSession: WebSocketSession? = null

    // 新增消息通道
    private val _messageChannel = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messageChannel.receiveAsFlow()

    // 新增状态管理
    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // 修改connect方法
    suspend fun connect(): Boolean {
        _connectionState.value = ConnectionState.CONNECTING
        return try {
            webSocketSession = wsClient.webSocketSession {
                url(WSHOST)
            }
            _connectionState.value = ConnectionState.CONNECTED
            startMessageListener()
            GameLogger.info("WebSocket连接成功")
            true
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.DISCONNECTED
            GameLogger.error("连接失败", e)
            false
        }
    }

    // 新增消息监听协程
    private var messageJob: Job? = null
    private fun startMessageListener() {
        messageJob?.cancel()
        messageJob = CoroutineScope(Dispatchers.Default).launch {
            webSocketSession?.incoming?.consumeAsFlow()?.collect { frame ->
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        _messageChannel.send(text)
                    }

                    is Frame.Binary -> {
                        GameLogger.debug("收到二进制帧（暂不支持）")
                    }

                    is Frame.Close -> {
                        GameLogger.debug("连接关闭: ${frame.readReason()}")
                        disconnect()
                    }

                    else -> {}
                }
            }
        }
    }

    // 修改disconnect方法
    suspend fun disconnect() {
        messageJob?.cancel()
        webSocketSession?.close()
        webSocketSession = null
        _connectionState.value = ConnectionState.DISCONNECTED
        GameLogger.info("连接已断开")
    }

    suspend fun sendMessageWs(wsMessage: WsMessage) {
        try {
            webSocketSession?.send(
                Frame.Text(
                    Json.encodeToString(
                        wsMessage
                    )
                )
            )
        } catch (e: Exception) {
            GameLogger.error("发送消息失败: ${e.message}")
        }
    }


    suspend fun createRoom(roomId: String, password: String) {
        // 先确保连接
        if (webSocketSession == null) {
            val connected = connect()
            if (!connected) {
                GameLogger.error("连接服务器失败")
                return
            }
        }
        sendMessageWs(
            WsMessage(
                type = "CREATE_ROOM",
                roomId = roomId,
                passWord = password
            )
        )
    }

    suspend fun joinRoom(roomId: String, password: String) {
        // 先确保连接
        if (webSocketSession == null) {
            val connected = connect()
            if (!connected) {
                GameLogger.error("连接服务器失败")
                return
            }
        }
        sendMessageWs(
            WsMessage(
                type = "JOIN_ROOM",
                roomId = roomId,
                passWord = password
            )
        )
    }

    suspend fun leaveRoom(roomId: String, password: String) {
        sendMessageWs(
            WsMessage(
                type = "QUIT_ROOM",
                roomId = roomId,
                passWord = password
            )
        )
    }

    suspend fun startGame(roomId: String, password: String) {
        sendMessageWs(
            WsMessage(
                type = "START_GAME",
                roomId = roomId,
                passWord = password
            )
        )
    }

    suspend fun deleteRoom(roomId: String, password: String) {
        sendMessageWs(
            WsMessage(
                type = "DELETE_ROOM",
                roomId = roomId,
                passWord = password
            )
        )
    }

}

// 添加消息处理器接口
interface WebSocketListener {
    suspend fun onMessage(message: String)
    fun onError(e: Throwable)
}
