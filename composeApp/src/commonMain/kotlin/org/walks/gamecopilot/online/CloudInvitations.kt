package org.walks.gamecopilot.online

import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.mmkv.MMKVUtils

@Serializable data class CloudJoinInvitation(val server: String, val roomId: String, val token: String)

/** A scanned invitation never replaces an existing membership. It is consumed after joining. */
object CloudInvitations {
    private const val KEY = "cloud_join_invitation_v1"
    private val _pending = MutableStateFlow<CloudJoinInvitation?>(null)
    val pending = _pending.asStateFlow()
    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()
    var webAddress = ""
        private set
    fun configureWeb(address: String) {
        webAddress = address
        _pending.value = runCatching { Json.decodeFromString<CloudJoinInvitation>(MMKVUtils.getString(KEY, "")) }.getOrNull()
    }
    fun receive(fragment: String) {
        if(fragment.isBlank()) return
        _error.value = ""
        try {
            require(fragment.length <= 2048)
            val params = Url("https://invitation.invalid/?" + fragment.removePrefix("#")).parameters
            require(params.names() == setOf("room", "invite", "server") && params.names().all { params.getAll(it)?.size == 1 })
            val room = params["room"].orEmpty(); val token = params["invite"].orEmpty()
            val server = params["server"].orEmpty().trimEnd('/')
            val url = Url(server)
            require(room.matches(Regex("[0-9]{6}")) && token.matches(Regex("[A-Za-z0-9_-]{32}")))
            require(server.length <= 256 && url.protocol.name in listOf("http", "https") && url.host.isNotBlank() &&
                url.encodedPath in listOf("", "/") && url.user == null && url.password == null && url.parameters.isEmpty() && url.fragment.isEmpty())
            require(!webAddress.startsWith("https:") || server.startsWith("https:"))
            val invite = CloudJoinInvitation(server, room, token)
            MMKVUtils.put(KEY, Json.encodeToString(CloudJoinInvitation.serializer(), invite))
            _pending.value = invite
        } catch(_: Exception) {
            clear(); _error.value = "邀请链接不完整或服务地址不兼容，请让房主重新分享二维码。"
        }
    }
    fun clear() { _pending.value = null; _error.value = ""; MMKVUtils.remove(KEY) }
}
