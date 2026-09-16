package org.walks.gamecopilot

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

    actual fun getAppVersionName(): String = "1.5"
    actual fun getAppVersionCode(): Int = 9
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun epochMillis(): Double
actual fun currentTimeMillis(): Long = epochMillis().toLong()
