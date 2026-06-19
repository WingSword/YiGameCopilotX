package org.walks.gamecopilot.service.ai

/**
 * AI 服务配置数据类
 * 包含 AI 服务所需的所有配置参数
 *
 * @param provider AI 服务提供商
 * @param apiKey API 密钥
 * @param baseUrl API 基础 URL
 * @param isEnabled 是否启用 AI 功能
 * @param aiStyle AI 回复风格
 * @param timeoutMs 请求超时时间（毫秒）
 */
data class AiConfig(
    val provider: AiProvider = AiProvider.FALLBACK,
    val apiKey: String = "",
    val baseUrl: String = AiProvider.DEEP_SEEK.defaultBaseUrl,
    val isEnabled: Boolean = false,
    val aiStyle: AiStyle = AiStyle.HUMOROUS,
    val timeoutMs: Long = 10000L
)
