package org.walks.gamecopilot

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformHelper.init(applicationContext)
        initMMKV(applicationContext)
    }
}
