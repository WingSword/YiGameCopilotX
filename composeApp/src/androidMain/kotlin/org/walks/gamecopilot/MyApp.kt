package org.walks.gamecopilot

import android.app.Application

/**
 *  Created by Wing at 15:46 on 2025/3/28
 *
 */
// 在 Application 类中初始化
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformHelper.init(applicationContext) // 初始化时注入 Context
        initMMKV(applicationContext)
    }
}