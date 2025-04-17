package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

/**
 *  Created by Wing at 17:31 on 2025/4/10
 *  快速游戏设置
 */

@Composable
fun QuickSetting() {
    var quickGameKeyword by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = quickGameKeyword,
            onValueChange = {
                quickGameKeyword = it
            },
            modifier = Modifier.fillMaxWidth(),

            placeholder = {
                Text(
                    text = "请输入快速口令",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                    fontSize = 40.sp,
                )
            }
        )
    }
}