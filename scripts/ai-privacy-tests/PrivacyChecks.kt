import kotlinx.coroutines.runBlocking
import org.walks.gamecopilot.distribution.AppDistribution
import org.walks.gamecopilot.service.ai.*

fun main() = runBlocking {
    val configured = AiConfig(provider = AiProvider.DEEP_SEEK, apiKey = "test-only-key", isEnabled = true)
    check(AiServiceFactory.create(configured) is FallbackAiService) // Previously saved settings have no consent.
    if (!AppDistribution.onlineAiEnabled) {
        check(AiServiceFactory.create(configured.copy(onlineConsent = true)) is FallbackAiService)
        check(runCatching { DeepSeekProvider(configured.copy(onlineConsent = true)) }.isFailure)
    } else {
        // Calling the provider directly cannot bypass consent or send to an unapproved destination.
        for (candidate in listOf(configured, configured.copy(onlineConsent = true, baseUrl = "http://127.0.0.1:1"),
            configured.copy(onlineConsent = true, baseUrl = "https://unrelated.example"))) {
            check(AiServiceFactory.create(candidate) is FallbackAiService)
            val response = DeepSeekProvider(candidate).chat(AiRequest("test context"))
            check(!response.isSuccess && response.errorMessage == "请先在设置中确认联网提示说明")
        }
        check(AiServiceFactory.create(configured.copy(onlineConsent = true)) is DeepSeekProvider)
        check(AiServiceFactory.create(configured.copy(onlineConsent = false)) is FallbackAiService)
        check(AiServiceFactory.create(configured.copy(onlineConsent = true, isEnabled = false)) is FallbackAiService)
    }
    println("PASS ${AppDistribution.channel.id}: consent, destination, revocation and direct-call checks")
}
