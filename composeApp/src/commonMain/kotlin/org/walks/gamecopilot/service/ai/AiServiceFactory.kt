package org.walks.gamecopilot.service.ai

/**
 * AI 服务工厂
 * 根据配置创建对应的 AI 服务实例
 * 当外部 AI 不可用时自动降级为本地预设服务
 */
object AiServiceFactory {

    /**
     * 创建 AI 服务实例
     * @param config AI 配置参数
     * @return 对应的 AI 服务实现
     */
    fun create(config: AiConfig): AiService {
        return when (config.provider) {
            AiProvider.DEEP_SEEK -> {
                if (config.apiKey.isNotBlank() && config.isEnabled) {
                    DeepSeekProvider(config)
                } else {
                    // API Key 为空或未启用时降级为本地预设
                    FallbackAiService()
                }
            }

            AiProvider.FALLBACK -> FallbackAiService()
        }
    }
}
