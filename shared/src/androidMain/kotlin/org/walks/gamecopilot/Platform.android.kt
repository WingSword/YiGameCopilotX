package org.walks.gamecopilot

import android.content.Context
import android.os.Build
import com.tencent.mmkv.MMKV

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun initMMKV(context: Any?) {
    MMKV.initialize(context as Context)
}