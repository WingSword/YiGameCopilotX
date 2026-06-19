package org.walks.gamecopilot

import kotlin.time.TimeSource

// commonMain
// commonMain/PlatformHelper.kt
actual class PlatformHelper {
    actual companion object {
        private var instance: PlatformHelper? = null
        actual fun init(context: Any) {
            instance = PlatformHelper()
        }

        actual fun getInstance(): PlatformHelper {
            return instance ?: PlatformHelper().also { instance = it }
        }
    }

    actual fun vibrateMethod() {
    }

    actual fun vibrateLongMethod() {
    }

    actual fun startPersistentAlert() {
    }

    actual fun stopPersistentAlert() {
    }

    actual fun getAppVersionName(): String = "1.3"
    actual fun getAppVersionCode(): Int = 4
}

// Wasm 环境使用 Monotonic 时间源
// 注意：这不是墙上时钟时间，但足够用于生成唯一 ID
private val appStartTime = TimeSource.Monotonic.markNow()
actual fun currentTimeMillis(): Long = appStartTime.elapsedNow().inWholeMilliseconds
