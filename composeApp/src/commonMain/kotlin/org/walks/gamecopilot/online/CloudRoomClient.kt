package org.walks.gamecopilot.online

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.distribution.AppDistribution
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Serializable private data class CloudEntry(val token: String, val roomKey: String = "", val room: CloudRoom)
@Serializable private data class CloudSession(val server: String, val token: String, val roomId: String, val roomKey: String)
@Serializable private data class PendingEntry(val server: String, val path: String, val body: JsonObject, val requestId: String)

/** Screen-scoped polling with a durable membership token; a network failure never creates a new player. */
object CloudRoomClient {
    const val DEFAULT_SERVER = "http://8.133.216.39:8080"
    private val json = Json { ignoreUnknownKeys = true }
    private val client by lazy { HttpClient { install(HttpTimeout) { requestTimeoutMillis = 12000; connectTimeoutMillis = 8000 } } }
    private val _room = MutableStateFlow<CloudRoom?>(null)
    val room = _room.asStateFlow()
    val compatibilityMessage get() = if (_room.value?.supportsHostView == false) CLOUD_SERVER_UPGRADE_MESSAGE else ""
    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _joined = MutableStateFlow(false)
    val joined = _joined.asStateFlow()
    private val _pendingLedger = MutableStateFlow("")
    val pendingLedger = _pendingLedger.asStateFlow()
    private val _pendingDraw = MutableStateFlow("")
    val pendingDraw = _pendingDraw.asStateFlow()
    private const val DRAW_PENDING = "cloud_draw_pending_v1"
    private const val LEDGER_PENDING = "cloud_ledger_pending_v1"
    private var session: CloudSession? = null
    private var loaded = false
    private var mutationVersion = 0L
    private var refreshing = false
    private var retryAt: TimeMark? = null
    private const val SESSION = "cloud_room_session_v1"
    private const val PENDING = "cloud_room_pending_entry_v1"
    private val retryMillis get() = retryAt?.elapsedNow()?.unaryMinus()?.inWholeMilliseconds?.coerceAtLeast(0) ?: 0L
    val pollDelayMillis get() = maxOf(if (_room.value?.drawing?.phase == "DRAWING") 800L else 2000L, retryMillis)
    // Web sets this to its HTTPS origin when served behind the room reverse proxy.
    var defaultServer: String = DEFAULT_SERVER
        private set
    fun configureDefaultServer(address: String) { defaultServer = validServer(address) }
    val serverAddress get() = MMKVUtils.getString("cloud_room_server", "").ifBlank { defaultServer }
    val nickname get() = MMKVUtils.getString("cloud_room_nickname", "")
    val roomKey get() = session?.roomKey.orEmpty()
    fun matchesInvitation(invite: CloudJoinInvitation): Boolean = session?.let { it.server == invite.server && it.roomId == invite.roomId } == true
    fun restore(): Boolean {
        if (!AppDistribution.roomsEnabled) return false
        if (!loaded) {
            loaded = true
            session = runCatching { json.decodeFromString<CloudSession>(MMKVUtils.getString(SESSION, "")) }.getOrNull()
            _pendingLedger.value = MMKVUtils.getString(LEDGER_PENDING, "")
            _pendingDraw.value = MMKVUtils.getString(DRAW_PENDING, "")
        }
        _joined.value = session != null
        return _joined.value
    }
    private fun requestId(): String = (1..48).map { "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"[Random.nextInt(62)] }.joinToString("")
    private fun validServer(raw: String): String {
        val clean = raw.trim().trimEnd('/')
        val url = Url(clean)
        require(url.protocol.name in listOf("http", "https") && url.host.isNotBlank() &&
            url.encodedPath in listOf("", "/") && url.user == null && url.password == null && url.parameters.isEmpty() && url.fragment.isEmpty()) { "请填写完整的服务器地址" }
        return clean
    }
    private suspend fun request(server: String, path: String, method: HttpMethod, body: JsonObject? = null, token: String = ""): String {
        AppDistribution.requireRooms()
        checkRateLimit()
        val response = client.request(server + "/api/v1/rooms" + path) {
            this.method = method
            if (token.isNotEmpty()) bearerAuth(token)
            if (body != null) { contentType(ContentType.Application.Json); setBody(body.toString()) }
        }
        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            val error = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull()
            if (response.status.value == 429) {
                val seconds = response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()?.coerceAtLeast(1) ?: 60L
                retryAt = TimeSource.Monotonic.markNow() + seconds.seconds
                checkRateLimit()
            }
            val code = error?.get("code")?.jsonPrimitive?.content.orEmpty()
            val unsupported = response.status.value == 404 && code == "NOT_FOUND" &&
                (path.endsWith("/invite") || path.endsWith("/hostview"))
            throw RoomFailure(response.status.value, code,
                if (unsupported) CLOUD_SERVER_UPGRADE_MESSAGE else error?.get("message")?.jsonPrimitive?.content ?: "服务响应异常，请稍后重试")
        }
        return text
    }
    suspend fun enter(server: String, nickname: String, code: String, key: String, create: Boolean,
                      maxPlayers: Int, spies: Int, blanks: Int, gameType: String = "spy", roles: List<String> = emptyList(),
                      ledgerPreset: String = "electronic", initialBalance: Long = 1_500_000, witchCount: Int = 1, inviteToken: String = ""): Boolean {
        if (!AppDistribution.roomsEnabled) {
            _error.value = AppDistribution.ROOMS_UNAVAILABLE
            return false
        }
        restore()
        if (session != null) return false
        if (_busy.value) return false
        mutationVersion++
        _busy.value = true; _error.value = ""
        return try {
            val address = validServer(server)
            require(nickname.trim().length in 1..20) { "请填写 1–20 字的昵称" }
            require(nickname.trim().none { it.code < 32 || it.code in 127..159 }) { "昵称不能包含控制字符" }
            require(create || code.matches(Regex("[0-9]{6}"))) { "请输入 6 位房间号" }
            require((!create && inviteToken.matches(Regex("[A-Za-z0-9_-]{32}"))) || (create && key.isBlank()) || key.trim().length in 6..32) { "密钥需要 6–32 个字符" }
            if (create) {
                require(gameType in listOf("spy", "werewolf", "ledger", "hunt", "avalon", "drawing")) { "请选择游戏" }
                if (gameType == "werewolf") {
                    require(maxPlayers in 3..10 && (roles.isEmpty() || roles.size == maxPlayers + 3)) { "一夜狼人需要 3–10 人与三张中央底牌" }
                } else if (gameType == "avalon") {
                    require(maxPlayers in 5..10 && (roles.isEmpty() || roles.size == maxPlayers)) { "阿瓦隆需要 5–10 人" }
                } else if (gameType == "drawing") {
                    require(maxPlayers in 3..10) { "你画我猜需要 3–10 人" }
                } else if (gameType == "hunt") {
                    require(maxPlayers in 4..12 && witchCount in 1..maxPlayers-2) { "简化猎巫镇需 4–12 人、至少 1 名女巫、1 名警长和 1 名村民" }
                } else if (gameType == "ledger") {
                    require(maxPlayers in 2..20 && ledgerPreset in LedgerScenes.ids && initialBalance in 0..999_999_999_999L) { "请检查记账人数和起始余额" }
                } else {
                require(maxPlayers in 4..16 && spies in 1..3 && blanks in 0..1) { "请检查人数和身份配置" }
                require((spies + blanks) * 2 < maxPlayers) { "卧底与白板合计需少于人数上限的一半" }
                }
            }
            val path = if (create) "" else "/$code/join"
            val body = buildJsonObject {
                put("nickname", nickname.trim()); put("roomKey", key.trim())
                if(!create && inviteToken.isNotEmpty()) put("inviteToken", inviteToken)
                if (create) {
                    put("gameType", gameType); put("maxPlayers", maxPlayers); put("spyCount", spies); put("blankCount", blanks)
                    if(gameType in listOf("werewolf", "avalon") && roles.isNotEmpty()) put("roles", JsonArray(roles.map(::JsonPrimitive)))
                    if(gameType == "ledger") { put("ledgerPreset", ledgerPreset); put("initialBalance", initialBalance) }
                    if(gameType == "hunt") put("witchCount", witchCount)
                }
            }
            val saved = runCatching { json.decodeFromString<PendingEntry>(MMKVUtils.getString(PENDING, "")) }.getOrNull()
            val pending = saved?.takeIf { it.server == address && it.path == path && it.body == body }
                ?: PendingEntry(address, path, body, requestId())
            // Persist before sending: a lost response can be retried after navigation or process death.
            MMKVUtils.put(PENDING, json.encodeToString(PendingEntry.serializer(), pending))
            val requestBody = JsonObject(body + ("requestId" to JsonPrimitive(pending.requestId)))
            val entry = json.decodeFromString<CloudEntry>(request(address, path, HttpMethod.Post, requestBody))
            session = CloudSession(address, entry.token, entry.room.roomId, entry.roomKey.ifEmpty { key.trim() })
            loaded = true
            MMKVUtils.put(SESSION, json.encodeToString(CloudSession.serializer(), session!!))
            MMKVUtils.remove(PENDING)
            MMKVUtils.put("cloud_room_server", address); MMKVUtils.put("cloud_room_nickname", nickname.trim())
            _room.value = entry.room
            _joined.value = true
            CloudInvitations.clear()
            true
        } catch (e: CancellationException) { throw e }
        // Wasm fetch rejections may be Kotlin Error rather than Exception.
        catch(e: Throwable) { _error.value = if (e is RoomFailure || e is IllegalArgumentException) e.message.orEmpty() else "连接失败，请检查服务地址和网络后重试"; false }
        finally { _busy.value = false }
    }
    suspend fun refresh() {
        restore()
        val s = session ?: return
        if (_busy.value || refreshing || retryMillis > 0) return
        val version = mutationVersion
        refreshing = true
        try {
            val next = json.decodeFromString<CloudRoom>(request(s.server,"/${s.roomId}",HttpMethod.Get,token=s.token))
            if (session == s && mutationVersion == version && !_busy.value) { _room.value = next; _error.value = "" }
        } catch (e: CancellationException) { throw e }
        catch(e: Throwable) {
            if (session != s || mutationVersion != version || _busy.value) return
            handleFailure(e, "连接中断，正在重连…")
        }
        finally { refreshing = false }
    }
    suspend fun ledgerAction(fields: JsonObject? = null): Boolean {
        if (_busy.value || session == null) return false
        if (fields != null) {
            if (_pendingLedger.value.isNotEmpty()) { _error.value = "请先确认上一笔操作的结果"; return false }
            val command = buildJsonObject { fields.forEach { (key, value) -> put(key, value) }; put("requestId", requestId()) }
            MMKVUtils.put(LEDGER_PENDING, command.toString())
            _pendingLedger.value = command.toString()
        }
        val command = runCatching { json.parseToJsonElement(_pendingLedger.value).jsonObject }.getOrNull() ?: return false
        return action("ledger", command)
    }
    private fun clearPendingLedger() { MMKVUtils.remove(LEDGER_PENDING); _pendingLedger.value = "" }
    suspend fun drawAction(fields: JsonObject? = null): Boolean {
        if (_busy.value || session == null) return false
        if (fields != null) {
            if (_pendingDraw.value.isNotEmpty()) { _error.value = "请先重试上一项画板操作"; return false }
            val command = JsonObject(fields + ("requestId" to JsonPrimitive(requestId())))
            MMKVUtils.put(DRAW_PENDING, command.toString())
            _pendingDraw.value = command.toString()
        }
        val command = runCatching { json.parseToJsonElement(_pendingDraw.value).jsonObject }.getOrNull() ?: return false
        return action("draw", command)
    }
    private fun clearPendingDraw() { MMKVUtils.remove(DRAW_PENDING); _pendingDraw.value = "" }
    suspend fun action(name: String, fields: JsonObject = buildJsonObject {}): Boolean {
        val s = session ?: return false
        if (_busy.value) return false
        mutationVersion++
        _busy.value = true
        return try {
            val response = request(s.server,"/${s.roomId}/$name",HttpMethod.Post,fields,s.token)
            if (name == "leave") forget() else _room.value = json.decodeFromString<CloudRoom>(response)
            if (name == "ledger") clearPendingLedger()
            if (name == "draw") clearPendingDraw()
            _error.value = ""; true
        } catch (e: CancellationException) { throw e }
        catch(e: Throwable) {
            if (name == "ledger" && e is RoomFailure && e.status in 400..499 && e.status != 429) clearPendingLedger()
            if (name == "draw" && e is RoomFailure && e.status in 400..499 && e.status != 429) clearPendingDraw()
            handleFailure(e, "操作未确认，请刷新后重试")
            if (e is RoomFailure && (e.status == 409 || e.code == "HOST_ONLY")) {
                // Refresh the view, but require a fresh user action against the new round.
                try {
                    val next = json.decodeFromString<CloudRoom>(request(s.server, "/${s.roomId}", HttpMethod.Get, token = s.token))
                    if (session == s) _room.value = next
                    if (e.code == "STALE_ROUND") _error.value = "局次已更新，请确认当前房间后重试"
                } catch (cancel: CancellationException) { throw cancel }
                catch (refreshError: Throwable) { handleFailure(refreshError, "连接中断，正在重连…") }
            }
            false
        }
        finally { _busy.value = false }
    }
    suspend fun invitation(webUrl: String = ""): CloudInvitation? {
        val s = session ?: return null
        if(_busy.value) return null
        mutationVersion++; _busy.value = true; _error.value = ""
        return try {
            val body = buildJsonObject { put("webUrl", webUrl.trim()); put("serverUrl", s.server) }
            json.decodeFromString<CloudInvitation>(request(s.server,"/${s.roomId}/invite",HttpMethod.Post,body,s.token))
        } catch(e: CancellationException) { throw e }
        catch(e: Throwable) { handleFailure(e,"邀请生成失败，请重试"); null }
        finally { _busy.value = false }
    }
    suspend fun closeRoom(): Boolean {
        val s = session ?: return false
        if (_busy.value) return false
        mutationVersion++
        _busy.value = true
        return try { request(s.server,"/${s.roomId}",HttpMethod.Delete,token=s.token); forget(); _error.value = ""; true }
        catch(e: CancellationException) { throw e }
        catch(e: Throwable) { handleFailure(e, "解散未确认，请重试"); false }
        finally { _busy.value = false }
    }
    fun forget() { mutationVersion++; session = null; _room.value = null; _joined.value = false; MMKVUtils.remove(SESSION); clearPendingLedger(); clearPendingDraw() }
    private fun checkRateLimit() {
        val remaining = retryMillis
        if (remaining > 0) throw RoomFailure(429, "RATE_LIMIT", "请求过于频繁，请 ${(remaining + 999) / 1000} 秒后重试")
    }
    private fun handleFailure(e: Throwable, fallback: String) {
        if (e is RoomFailure && e.code in listOf("SESSION_EXPIRED", "ROOM_NOT_FOUND", "ROOM_EXPIRED")) forget()
        _error.value = if (e is RoomFailure) e.message.orEmpty() else fallback
    }
    private class RoomFailure(val status: Int, val code: String, message: String): Exception(message)
}
