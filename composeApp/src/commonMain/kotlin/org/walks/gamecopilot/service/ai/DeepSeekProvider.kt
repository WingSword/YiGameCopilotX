package org.walks.gamecopilot.service.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * DeepSeek AI 服务提供商
 * 使用 Ktor Client 调用 DeepSeek API
 *
 * @param config AI 配置参数
 */
class DeepSeekProvider(private val config: AiConfig) : AiService {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeoutMs
            connectTimeoutMillis = 5000L
        }
    }

    /**
     * DeepSeek API 请求体
     */
    @Serializable
    private data class ChatRequest(
        val model: String = "deepseek-chat",
        val messages: List<Message>,
        @SerialName("max_tokens") val maxTokens: Int = 500,
        val temperature: Float = 0.8f
    )

    /**
     * 聊天消息结构
     */
    @Serializable
    private data class Message(
        val role: String,
        val content: String
    )

    /**
     * DeepSeek API 响应体
     */
    @Serializable
    private data class ChatResponse(
        val choices: List<Choice> = emptyList(),
        val error: ApiError? = null
    )

    @Serializable
    private data class Choice(
        val message: Message = Message("", "")
    )

    @Serializable
    private data class ApiError(
        val message: String = ""
    )

    override suspend fun chat(request: AiRequest): AiResponse {
        return try {
            val messages = buildList {
                if (request.systemPrompt.isNotBlank()) {
                    add(Message(role = "system", content = request.systemPrompt))
                }
                add(Message(role = "user", content = request.prompt))
            }

            val chatRequest = ChatRequest(
                model = "deepseek-chat",
                messages = messages,
                maxTokens = request.maxTokens,
                temperature = request.temperature
            )

            val response: ChatResponse = httpClient.post("${config.baseUrl}/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ChatRequest.serializer(), chatRequest))
            }.body()

            // 检查 API 错误
            if (response.error != null) {
                return AiResponse(
                    content = "",
                    isSuccess = false,
                    errorMessage = response.error.message,
                    provider = AiProvider.DEEP_SEEK
                )
            }

            // 提取回复内容
            val content = response.choices.firstOrNull()?.message?.content.orEmpty()
            if (content.isBlank()) {
                return AiResponse(
                    content = "",
                    isSuccess = false,
                    errorMessage = "AI 返回了空内容",
                    provider = AiProvider.DEEP_SEEK
                )
            }

            AiResponse(
                content = content.trim(),
                isSuccess = true,
                provider = AiProvider.DEEP_SEEK
            )
        } catch (e: Exception) {
            AiResponse(
                content = "",
                isSuccess = false,
                errorMessage = e.message ?: "请求 DeepSeek 失败",
                provider = AiProvider.DEEP_SEEK
            )
        }
    }

    override fun isAvailable(): Boolean {
        return config.apiKey.isNotBlank() && config.isEnabled
    }

    override fun getProviderName(): String {
        return AiProvider.DEEP_SEEK.displayName
    }
}
