package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.ui.input.CommonTextField


/**
 *  Created by Wing at 17:31 on 2025/4/10
 *  快速游戏设置
 */

@Composable
fun QuickSetting() {
    var quickGameKeyword by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        CommonTextField(
            value = quickGameKeyword,
            onValueChange = {
                quickGameKeyword = it
            },
            label = "请输入快速口令"
        )
        PickerDemo()

    }
}

@Composable
fun PickerDemo() {
    val provinces = listOf("北京", "上海", "广东")
    val cities = listOf("市辖区", "黄浦区", "天河区")
    val districts = listOf("朝阳区", "徐汇区", "越秀区")

    var selected by remember { mutableStateOf(listOf("", "", "")) }

    Column {

        Text("当前选择：${selected.joinToString("-")}")
    }
}