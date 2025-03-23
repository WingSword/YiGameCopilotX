package org.walks.gamecopilot.http


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.RoomDataModel


val baseJsonConf = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}
const val HOST = "http://116.198.196.244:8080/api"
const val CREATE_ROOM_URL = "/createRoom"


@Serializable
data class PostData(
    val key: String,
    val value: String
)

val client by lazy {
    HttpClient {
        install(ContentNegotiation) {
            json(baseJsonConf)
        }
    }
}

fun getUrl(url: String?): String {
    return HOST + url
}

// 定义常量以避免硬编码
private const val ROOM_ID_KEY = "roomId"
private const val PASSWORD_KEY = "passWord"

suspend fun createRoomRequest(roomId: String, passwd: String): RoomDataModel? {
    // 输入校验
    if (roomId.isBlank() || passwd.isBlank()) {
        println("Invalid input: account or password cannot be blank.")
        return null
    }

    try {
        val response: HttpResponse = client.get(getUrl(CREATE_ROOM_URL)) {
            contentType(ContentType.Application.Json)
            url {
                parameters.append(ROOM_ID_KEY , roomId) // 添加查询参数
                parameters.append(PASSWORD_KEY , passwd)
            }
        }

        // 打印响应头
        println("Response headers: ${response.headers}")

        // 打印服务器返回的原始 JSON 字符串
        val responseBody = response.body<String>()
        println("Server response: $responseBody")

        // 解析响应体并返回
        return baseJsonConf.decodeFromString<RoomDataModel>(responseBody)
//    } catch (e: HttpClientCallException) {
//        // 捕获网络请求异常
//        println("Network error in POST request: ${e.message}")
//    } catch (e: JsonDecodingException) {
//        // 捕获 JSON 解析异常
//        println("JSON decoding error: ${e.message}")
    } catch (e: Exception) {
        // 捕获其他未知异常
        println("Unexpected error in POST request: ${e.message}")
    } finally {
        // 避免重复关闭 client，确保其生命周期由外部管理
        // client.close() // 移除此行，避免不必要的关闭
    }
    return null
}

suspend fun joinARoomRequest(roomId: String, passwd: String): RoomDataModel? {
    try {
        val response = client.post(getUrl("/joinRoom")) {
            contentType(ContentType.Application.Json)
            val map = mapOf(
                "roomId" to roomId,
                "passWord" to passwd,
            )
            setBody(
                map
            )
        }
       // val responseBody: RoomDataModel = baseJsonConf.decodeFromString(response.body())

        // 解析响应体并返回
        return baseJsonConf.decodeFromString<RoomDataModel>(response.body())

    } catch (e: Exception) {
        println("Error in POST request: ${e.message}")
    } finally {
        client.close()
    }
    return null
}



suspend fun safeGetRequest(url: String): Response {
    return try {
        val response = client.get(url)
        when (response.status) {
            HttpStatusCode.OK -> {
                println("Success: ${response.body<String>()}")
                Response.Success(response)
            }
            else -> {
                println("Server error: ${response.status}")
                Response.Error(response.status.description)
            }
        }
    } catch (e: ClientRequestException) {
        println("4xx Error: ${e.message}")
        Response.Error("Client error")
    } catch (e: ServerResponseException) {
        println("5xx Error: ${e.message}")
        Response.Error("Server error")
    } catch (e: Exception) {
        println("Network error: ${e.message}")
        Response.Error("Connection failed")
    }
}

sealed class Response {
    data class Success(val data: Any) : Response()
    data class Error(val message: String) : Response()
}



