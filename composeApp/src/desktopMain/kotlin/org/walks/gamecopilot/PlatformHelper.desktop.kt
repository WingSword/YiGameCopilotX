package org.walks.gamecopilot

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

}