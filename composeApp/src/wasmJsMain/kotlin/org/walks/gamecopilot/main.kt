import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.walks.gamecopilot.App
import org.walks.gamecopilot.online.CloudRoomClient
import org.walks.gamecopilot.online.CloudInvitations

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { try { const key = '__yigame_storage_probe'; localStorage.setItem(key, '1'); localStorage.removeItem(key); return true; } catch (e) { return false; } }")
private external fun storageAvailable(): Boolean

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    if(!storageAvailable()) {
        document.body?.textContent = "请允许此网站保存本地数据后刷新页面，以便保留房间身份和游戏记录。"
        return
    }
    // HTTPS deployments proxy /api on this origin; native builds keep the original default.
    if(window.location.protocol == "https:") CloudRoomClient.configureDefaultServer(window.location.origin)
    CloudInvitations.configureWeb(window.location.origin + window.location.pathname)
    fun receiveInvitation() {
        if(window.location.hash.isNotEmpty()) {
            CloudInvitations.receive(window.location.hash)
            // The invitation is persisted before removing it from the address bar.
            window.history.replaceState(null, "", window.location.pathname + window.location.search)
        }
    }
    receiveInvitation()
    window.addEventListener("hashchange", { receiveInvitation() })
    ComposeViewport(document.body!!) {
        App()
    }
}
