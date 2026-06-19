package org.walks.gamecopilot

import kotlin.time.TimeSource

// commonMain
// commonMain/PlatformHelper.kt
actual class PlatformHelper {
    actual companion object {
        private var instance: PlatformHelper? = null
        actual fun init(context: Any) {
        }

        actual fun getInstance(): PlatformHelper {
            return instance ?: throw IllegalStateException("PlatformHelper not initialized")

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

// iOS 使用 kotlin.time 获取时间戳
actual fun currentTimeMillis(): Long =
    TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
