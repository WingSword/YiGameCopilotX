package org.walks.gamecopilot.intent

import org.walks.gamecopilot.service.ai.AiConfig
import org.walks.gamecopilot.service.ai.AiProvider
import org.walks.gamecopilot.service.ai.AiStyle

/**
 * AI 相关意图
 * 用于 MVI 架构中处理 AI 功能的用户操作
 */
sealed class AiIntent {
    /** 发送消息给 AI */
    data class SendMessage(val gameType: String, val context: String) : AiIntent()

    /** 更新 AI 配置 */
    data class UpdateConfig(val config: AiConfig) : AiIntent()

    /** 切换 AI 开关 */
    data class ToggleAi(val enabled: Boolean) : AiIntent()

    /** 更新 API Key */
    data class UpdateApiKey(val key: String) : AiIntent()

    /** 更新 AI 提供商 */
    data class UpdateProvider(val provider: AiProvider) : AiIntent()

    /** 更新 AI 风格 */
    data class UpdateStyle(val style: AiStyle) : AiIntent()

    /** 清除 AI 消息 */
    data object ClearMessage : AiIntent()
}
