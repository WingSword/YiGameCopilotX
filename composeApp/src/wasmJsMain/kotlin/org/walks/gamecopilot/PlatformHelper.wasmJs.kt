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

    actual fun getAppVersionName(): String = "1.3"
    actual fun getAppVersionCode(): Int = 4
}