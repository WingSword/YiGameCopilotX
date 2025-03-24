package org.walks.gamecopilot.http

import io.ktor.client.HttpClient
import org.walks.gamecopilot.RoomDataModel
import org.walks.gamecopilot.data.RoomInfoEntity

// 新建文件：RoomService.kt
class RoomModule(private val httpClient: HttpClient) {
    // 定义常量以避免硬编码
    companion object{
        private const val ROOM_ID_KEY = "roomId"
        private const val PASSWORD_KEY = "passWord"
        private const val CREATE_ROOM_URL = "/createRoom"
        private const val JOIN_ROOM_URL = "/joinRoom"
        private const val ROOM_INFO_URL = "/getRoomInfo"
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
                    throw Exception(it.message)
                }
            }
            data
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
                    throw Exception(it.message)
                }
            }
            data
        }
    }

    suspend fun getRoomInfo(roomId: String, password: String): RoomDataModel<RoomInfoEntity> {
        return (httpClient.safeGetRequest<RoomDataModel<RoomInfoEntity>>(
            url = getUrl(ROOM_INFO_URL),
            params = mapOf(ROOM_ID_KEY to roomId, PASSWORD_KEY to password)
        )).let {
            val data = when (it) {
                is Response.Success<*> -> {
                    it.data as RoomDataModel<RoomInfoEntity>
                }

                is Response.Error -> {
                    throw Exception(it.message)
                }
            }
            data
        }
    }
}
