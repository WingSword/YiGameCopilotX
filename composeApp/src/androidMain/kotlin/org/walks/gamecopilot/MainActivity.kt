package org.walks.gamecopilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 *  Created by Wing at 15:47 on 2025/3/28
 *
 */
// 或 Activity 中
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformHelper.init(applicationContext)
        setContent {
            App()
        }
    }
}
