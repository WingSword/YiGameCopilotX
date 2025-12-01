package org.walks.gamecopilot

import org.walks.gamecopilot.mmkv.MMKVDelegate
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun initMMKV(context: Any?) {

}

fun MMKVDelegate(): MMKVDelegate = MMKVDelegate()