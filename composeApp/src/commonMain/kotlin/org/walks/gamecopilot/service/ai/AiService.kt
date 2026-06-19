package org.walks.gamecopilot.service.ai

/**
 * AI 请求参数
 *
 * @param prompt 用户输入的提示文本
 * @param systemPrompt 系统提示词（用于设定 AI 角色）
 * @param maxTokens 最大生成 token 数
 * @param temperature 生成温度（0.0 ~ 2.0，越高越随机）
 */
data class AiRequest(
    val prompt: String,
    val systemPrompt: String = "",
    val maxTokens: Int = 500,
    val temperature: Float = 0.8f
)

/**
 * AI 响应结果
 *
 * @param content AI 生成的文本内容
 * @param isSuccess 请求是否成功
 * @param errorMessage 错误信息（仅在失败时有值）
 * @param provider 实际响应的 AI 提供商
 */
data class AiResponse(
    val content: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val provider: AiProvider = AiProvider.FALLBACK
)

/**
 * AI 服务接口
 * 定义 AI 服务的基本操作契约
 */
interface AiService {
    /**
     * 发送聊天请求并获取 AI 回复
     * @param request AI 请求参数
     * @return AI 响应结果
     */
    suspend fun chat(request: AiRequest): AiResponse

    /**
     * 检查 AI 服务是否可用
     * @return 服务是否可用
     */
    fun isAvailable(): Boolean

    /**
     * 获取 AI 服务提供商名称
     * @return 提供商名称字符串
     */
    fun getProviderName(): String
}
