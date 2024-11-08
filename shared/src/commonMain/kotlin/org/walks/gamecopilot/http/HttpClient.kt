package org.walks.gamecopilot.http


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
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
val host = "http://116.198.196.244:8080"

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
    return host + url
}

suspend fun createRoomRequest() {
    val postData = PostData("exampleKey", "exampleValue")
    try {
        val response = client.post("http://your - api - url") {
            contentType(ContentType.Application.Json)
            setBody(postData)
        }
        val responseBody = response.body<String>()
        println("Response body: $responseBody")
    } catch (e: Exception) {
        println("Error in POST request: ${e.message}")
    } finally {
        client.close()
    }
}

suspend fun joinARoom(account: String, passwd: String): RoomDataModel? {
    try {
        val response = client.post(getUrl("/api/createRoom")) {
            contentType(ContentType.Application.Json)
            val map = mapOf(
                "roomId" to account,
                "roomPwd" to passwd,
                "masterId" to "ddd",
                "roomSize" to "3"
            )
            setBody(
                map
            )
        }
        val responseBody: RoomDataModel = baseJsonConf.decodeFromString(response.body())
        return responseBody
    } catch (e: Exception) {
        println("Error in POST request: ${e.message}")
    } finally {
        client.close()
    }
    return null
}



