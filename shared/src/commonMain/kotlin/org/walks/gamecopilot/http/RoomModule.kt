package org.walks.gamecopilot.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.util.logging.Logger
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.io.IOException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.data.RoomDataModel
import org.walks.gamecopilot.data.RoomInfoEntity
import org.walks.gamecopilot.data.WsDataEntity
import org.walks.gamecopilot.data.WsMessage

// 新建文件：RoomService.kt
class RoomModule(private val httpClient: HttpClient) {
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

    suspend fun createRoom(roomId: String, password: String): WsDataEntity? {
        if (webSocketSession == null) {
            connect()
        }
        val session = webSocketSession
        // 发送创建房间请求
        session?.send(
            Frame.Text(
                Json.encodeToString(
                    WsMessage(
                        type = "CREATE_ROOM",
                        roomId = roomId,
                        passWord = password
                    )
                )
            )
        )

        // 接收响应
        return try {
            val response = session?.incoming?.receive() as? Frame.Text
                ?: throw IOException("Invalid response type")

            Json.decodeFromString<WsDataEntity>(response.readText())
        } catch (e: Exception) {
            GameLogger.error("创建房间失败: ${e.message}")
            null
        }
    }

    suspend fun joinRoom(roomId: String, password: String): RoomDataModel<String> {
        return (httpClient.safeGetRequest<RoomDataModel<String>>(
            url = getUrl(JOIN_ROOM_URL),
            params = mapOf(ROOM_ID_KEY to roomId, PASSWORD_KEY to password)
        )).let {
            val data = when (it) {
                is Response.Success<*> -> {
                    it.data as RoomDataModel<String>
                }

                is Response.Error -> {
                    null
                }
            }
            data ?: RoomDataModel(code = 0, msg = "加入房间失败")
        }
    }

    suspend fun getRoomInfo(roomId: String, password: String): RoomDataModel<RoomInfoEntity> {
        return (httpClient.safeGetRequest<RoomDataModel<RoomInfoEntity>>(
            url = getUrl(ROOM_INFO_URL),
            params = mapOf(ROOM_ID_KEY to roomId, PASSWORD_KEY to password)
        )).let {
            val data = when (it) {
                is Response.Success<*> -> {
                    it.data as? RoomDataModel<RoomInfoEntity>
                }

                is Response.Error -> {
                    null
                }
            }
            data ?: RoomDataModel(code = 0, msg = "获取房间信息失败")
        }
    }

    suspend fun leaveRoom(roomId: String, password: String, userId: String): RoomDataModel<String> {
        return (httpClient.safeGetRequest<RoomDataModel<String>>(
            url = getUrl(LEAVE_ROOM_URL),
            params = mapOf(ROOM_ID_KEY to roomId, PASSWORD_KEY to password, USER_ID to userId)
        )).let {
            val data = when (it) {
                is Response.Success<*> -> {
                    it.data as? RoomDataModel<String>
                }

                is Response.Error -> {
                    null
                }
            }
            data ?: RoomDataModel(code = 0, msg = "离开房间失败")
        }
    }

    suspend fun startGame(
        roomId: String,
        password: String,
        userId: String
    ): RoomDataModel<String?> {
        return (httpClient.safeGetRequest<RoomDataModel<String>>(
            url = getUrl(START_GAME_URL),
            params = mapOf(ROOM_ID_KEY to roomId, PASSWORD_KEY to password, USER_ID to userId)
        )).let {
            val data = when (it) {
                is Response.Success<*> -> {
                    it.data as? RoomDataModel<String?>
                }

                is Response.Error -> {
                    null
                }
            }
            data ?: RoomDataModel(code = 0, msg = "开始游戏失败")
        }
    }

    suspend fun deleteRoom(
        roomId: String,
        password: String,
        userId: String
    ): RoomDataModel<String> {
        return (httpClient.safeGetRequest<RoomDataModel<String>>(
            url = getUrl(DELETE_ROOM_URL),
            params = mapOf(ROOM_ID_KEY to roomId, PASSWORD_KEY to password, USER_ID to userId)
        )).let {
            val data = when (it) {
                is Response.Success<*> -> {
                    it.data as? RoomDataModel<String>
                }

                is Response.Error -> {
                    null
                }
            }
            data ?: RoomDataModel(code = 0, msg = "删除房间失败")
        }
    }


    // 新增WebSocket会话
    private var webSocketSession: WebSocketSession? = null

    // 初始化连接
    suspend fun connect() {
        webSocketSession = wsClient.webSocketSession {
            url(WSHOST) // 根据实际WebSocket端点调整
        }
    }

    // 关闭连接
    suspend fun disconnect() {
        webSocketSession?.close()
        webSocketSession = null
    }


}
