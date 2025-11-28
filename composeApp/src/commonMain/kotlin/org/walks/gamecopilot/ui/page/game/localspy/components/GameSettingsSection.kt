package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 游戏设置区域组件
 * 包含游玩人数、卧底人数、空白卡数量等配置选项
 * 
 * @param modifier 修饰符
 * @param gameTimeState 游戏时间状态
 * @param playerNum 玩家人数
 * @param spyNumber 卧底人数
 * @param blackNum 空白卡数量
 * @param maxSpyList 最大卧底数列表
 * @param showSpyNumberPicker 是否显示卧底/空白卡选择器
 * @param onSpyNumberPickerChange 卧底/空白卡选择器状态变化回调
 * @param onGameTimeStateChange 游戏时间状态变化回调
 */
@Composable
fun GameSettingsSection(
    modifier: Modifier = Modifier,
    gameTimeState: Int,
    playerNum: Int,
    spyNumber: Int,
    blackNum: Int,
    maxSpyList: List<String>,
    showSpyNumberPicker: Int,
    onSpyNumberPickerChange: (Int) -> Unit,
    onGameTimeStateChange: (Int) -> Unit
) {
    // 配置按钮列
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 配置按钮列
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 游玩人数按钮 - 小尺寸
            SmallConfigButton(
                title = "游玩人数",
                value = playerNum.toString(),
                onClick = { /* 人数选择器由父组件控制 */ },
                color = MaterialTheme.colorScheme.primary
            )

            // 卧底人数按钮 - 小尺寸
            SmallConfigButton(
                title = "卧底人数",
                value = spyNumber.toString(),
                onClick = { onSpyNumberPickerChange(1) },
                color = MaterialTheme.colorScheme.error
            )

            // 空白卡数量按钮 - 小尺寸
            SmallConfigButton(
                title = "空白卡",
                value = blackNum.toString(),
                onClick = { onSpyNumberPickerChange(2) },
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * 小尺寸配置按钮组件
 * 用于垂直排列的配置选项
 * 
 * @param title 按钮标题
 * @param value 显示值
 * @param onClick 点击回调
 * @param color 按钮颜色
 */
@Composable
fun SmallConfigButton(
    title: String,
    value: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }

            .background(
                color = color.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}