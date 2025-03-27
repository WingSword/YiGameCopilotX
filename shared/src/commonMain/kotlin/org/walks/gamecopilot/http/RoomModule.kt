package org.walks.gamecopilot.http

import io.ktor.client.HttpClient
import org.walks.gamecopilot.data.RoomDataModel
import org.walks.gamecopilot.data.RoomInfoEntity

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

    suspend fun createRoom(roomId: String, password: String): RoomDataModel<String> {
        return (httpClient.safeGetRequest<RoomDataModel<String>>(
            url = getUrl(CREATE_ROOM_URL),
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
            data ?: RoomDataModel(code = 0, msg = "创建房间失败")
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

    suspend fun deleteRoom(roomId: String, password: String,userId: String): RoomDataModel<String> {
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
}
