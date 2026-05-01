package org.walks.gamecopilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformHelper.init(applicationContext)
        initMMKV(applicationContext)
        setContent {
            App()
        }
    }
}
