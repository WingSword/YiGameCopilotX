package org.walks.gamecopilot

import org.walks.gamecopilot.mmkv.MMKVDelegate

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()
actual fun initMMKV(context: Any?) {

}

fun MMKVDelegate(): MMKVDelegate = MMKVDelegate()