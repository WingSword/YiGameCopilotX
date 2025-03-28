package org.walks.gamecopilot

import android.content.Context

// commonMain
// commonMain/PlatformHelper.kt
// androidMain/PlatformHelper.android.kt
actual class PlatformHelper private constructor(private val context: Context) {
    actual companion object {
        private var instance: PlatformHelper? = null

        actual fun init(context: Any) {
            instance = PlatformHelper(context as Context)
        }

        actual fun getInstance(): PlatformHelper {
            return instance ?: throw IllegalStateException("PlatformHelper not initialized")
        }
    }

    actual fun vibrateMethod() {
        context.vibrateShort()
    }

    actual fun vibrateLongMethod() {
        context.vibrateLong()
    }
}
