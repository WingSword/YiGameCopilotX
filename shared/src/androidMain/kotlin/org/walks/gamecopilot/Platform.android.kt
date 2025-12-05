package org.walks.gamecopilot

import android.os.Build
import org.walks.gamecopilot.mmkv.MMKVDelegate

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun initMMKV(context: Any?) {

}

fun MMKVDelegate(): MMKVDelegate = MMKVDelegate()