package org.walks.gamecopilot.service.ai

/**
 * AI 服务提供商枚举
 * 定义支持的 AI 提供商及其默认配置
 */
enum class AiProvider(val displayName: String, val defaultBaseUrl: String) {
    DEEP_SEEK("DeepSeek", "https://api.deepseek.com"),
    FALLBACK("本地预设", "")
}

/**
 * AI 回复风格枚举
 * 定义不同的 AI 语气风格
 */
enum class AiStyle(val displayName: String) {
    HUMOROUS("幽默风趣"),
    SERIOUS("严肃专业"),
    SARCASTIC("毒舌犀利")
}
