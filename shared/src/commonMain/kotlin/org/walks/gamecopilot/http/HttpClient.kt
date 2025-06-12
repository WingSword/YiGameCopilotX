package org.walks.gamecopilot.http


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

val baseJsonConf = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}
const val HOST = "http://116.198.196.244:8080/api"

const val WSHOST="ws://116.198.196.244:6688/api/spyGame"

val roomModule by lazy {
    RoomModule(client)
}

@Serializable
data class PostData(
    val key: String,
    val value: String
)

val client by lazy {
    HttpClient() {
        install(WebSockets) {
            // 可选配置心跳检测
            pingInterval = 20.seconds // 20秒心跳间隔

        }
    }
}
val wsClient = HttpClient(CIO) {
    // 专用于WebSocket的配置（不安装ContentNegotiation）
    install(WebSockets) {
        pingInterval = 20.seconds
    }
}

fun getUrl(url: String?): String {
    return HOST + url
}


const val DATA_SUCCESS=1000

// 修改为扩展函数，提升灵活性
suspend inline fun <reified T> HttpClient.safeGetRequest(
    url: String,
    params: Map<String, String> = mapOf()
): Response<T> {
    return try {
        val response = client.get(url) {
            url {
                for ((key, value) in params) {
                    parameters.append(key, value)
                }
            }
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                println("Success: ${response.body<String>()}")
                Response.Success(baseJsonConf.decodeFromString<T>(response.body()))
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

sealed class Response<out T> {
    data class Success<out T>(val data: T) : Response<T>()
    data class Error(val message: String) : Response<Nothing>()
}



