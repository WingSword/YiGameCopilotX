@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
// OHOS (linuxArm64) actual for expect in commonMain/org/walks/gamecopilot/Platform.kt
package org.walks.gamecopilot

class OHOSPlatform : Platform {
    override val name: String = "HarmonyOS"
}

actual fun getPlatform(): Platform = OHOSPlatform()

actual fun initMMKV(context: Any?) {
    // OHOS: MMKV not available on linuxArm64 target.
    // multiplatform-settings (no-arg) used for preference storage instead.
}
