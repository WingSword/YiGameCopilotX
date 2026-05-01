package org.walks.gamecopilot

// commonMain
// commonMain/PlatformHelper.kt
expect class PlatformHelper {
    companion object {
        fun init(context: Any) // 声明初始化方法
        fun getInstance(): PlatformHelper
    }

    fun vibrateMethod()

    fun vibrateLongMethod()

    fun startPersistentAlert()

    fun stopPersistentAlert()

    fun getAppVersionName(): String
    fun getAppVersionCode(): Int
}

