import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestPipeline
import kotlinx.serialization.json.*
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.online.CloudRoomClient as Cloud
import org.walks.gamecopilot.online.CloudRoom
import org.walks.gamecopilot.online.CloudInvitation

fun main() = runBlocking {
    val outputJson = Json { encodeDefaults = true }
    // Test-only injection into the production client's request pipeline. No test
    // transport hooks are shipped in the app, and successful calls still use HTTP.
    val transport = (Cloud.javaClass.getDeclaredField("client\$delegate").apply { isAccessible = true }.get(Cloud) as Lazy<*>).value as HttpClient
    var injectedFailure: Throwable? = null
    var requestsBeforeFailure = 0
    transport.requestPipeline.intercept(HttpRequestPipeline.Before) {
        val failure = injectedFailure
        if (failure != null) {
            if (requestsBeforeFailure > 0) requestsBeforeFailure--
            else { injectedFailure = null; throw failure }
        }
    }
    generateSequence(::readlnOrNull).forEach { line ->
        val command = Json.parseToJsonElement(line).jsonObject
        fun text(key: String) = command[key]?.jsonPrimitive?.content.orEmpty()
        val body = command["body"]?.jsonObject ?: buildJsonObject {}
        if (text("networkFailure").isNotEmpty()) {
            injectedFailure = if (text("networkFailure") == "cancel") CancellationException("Test cancellation") else Error("Fail to fetch")
            requestsBeforeFailure = command["failureAfter"]?.jsonPrimitive?.int ?: 0
        }
        var cancelled = false
        var invitation: CloudInvitation? = null
        val result = try {
          val successful = when (text("op")) {
            "enter" -> Cloud.enter(text("server"), body.getValue("nickname").jsonPrimitive.content,
                text("code"), body.getValue("roomKey").jsonPrimitive.content,
                command.getValue("create").jsonPrimitive.boolean,
                body["maxPlayers"]?.jsonPrimitive?.int ?: 12,
                body["spyCount"]?.jsonPrimitive?.int ?: 1,
                body["blankCount"]?.jsonPrimitive?.int ?: 0,
                body["gameType"]?.jsonPrimitive?.content ?: "spy",
                body["roles"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                body["ledgerPreset"]?.jsonPrimitive?.content ?: "electronic", body["initialBalance"]?.jsonPrimitive?.long ?: 1500000,
                body["witchCount"]?.jsonPrimitive?.int ?: 1,
                body["inviteToken"]?.jsonPrimitive?.content.orEmpty())
            "ledger" -> Cloud.ledgerAction(if(command.containsKey("body")) body else null)
            "draw" -> Cloud.drawAction(if(command.containsKey("body")) body else null)
            "action" -> Cloud.action(text("name"), body)
            "close" -> Cloud.closeRoom()
            "refresh" -> { Cloud.refresh(); true }
            "invite" -> true
            else -> Cloud.restore()
          }
          if (text("op") == "invite") invitation = Cloud.invitation(text("webUrl"))
          successful
        } catch (e: CancellationException) { cancelled = true; false }
        println(buildJsonObject {
            put("result", result); put("joined", Cloud.joined.value); put("error", Cloud.error.value)
            put("busy", Cloud.busy.value); put("cancelled", cancelled)
            put("pendingEntryId", runCatching { Json.parseToJsonElement(MMKVUtils.getString("cloud_room_pending_entry_v1", "")).jsonObject["requestId"]?.jsonPrimitive?.content }.getOrNull().orEmpty())
            put("compatibilityMessage", Cloud.compatibilityMessage)
            put("supportsHostView", Cloud.room.value?.supportsHostView ?: false)
            put("pendingLedger", Cloud.pendingLedger.value)
            put("pendingDraw", Cloud.pendingDraw.value)
            put("server", Cloud.serverAddress); put("delay", Cloud.pollDelayMillis); put("key", Cloud.roomKey)
            put("room", Cloud.room.value?.let { outputJson.encodeToJsonElement(CloudRoom.serializer(), it) } ?: JsonNull)
            put("invitation", invitation?.let { outputJson.encodeToJsonElement(CloudInvitation.serializer(), it) } ?: JsonNull)
        })
    }
}
