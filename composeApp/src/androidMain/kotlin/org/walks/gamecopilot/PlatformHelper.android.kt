package org.walks.gamecopilot

import android.content.Context
import android.content.pm.PackageManager

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

    actual fun getAppVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.3"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.3"
        }
    }

    actual fun getAppVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            4
        }
    }
}
