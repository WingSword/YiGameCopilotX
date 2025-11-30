package org.walks.gamecopilot.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

/**
 * 通用游戏配置项数据类
 * @param title 配置项标题
 * @param value 当前值
 * @param options 可选值列表
 * @param color 显示颜色
 * @param onValueChange 值变化回调
 */
data class GameConfigItem(
    val title: String,
    val value: String,
    val options: List<String>,
    val color: Color,
    val onValueChange: (String) -> Unit
)

/**
 * 通用游戏配置组件
 * 支持多个配置项的显示和选择，使用统一的样式和交互
 *
 * @param title 配置区域标题
 * @param configItems 配置项列表
 * @param modifier 修饰符
 */
@Composable
fun GameConfigSection(
    title: String,
    configItems: List<GameConfigItem>,
    modifier: Modifier = Modifier
) {
    // 当前显示的选择器状态
    var showPickerIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 区域标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 配置按钮列
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                configItems.forEachIndexed { index, config ->
                    SmallConfigButton(
                        title = config.title,
                        value = config.value,
                        onClick = { showPickerIndex = index },
                        color = config.color
                    )
                }
            }
        }
    }

    // 选择器弹窗
    configItems.forEachIndexed { index, config ->
        WeSingleColumnPicker(
            visible = showPickerIndex == index,
            title = "选择${config.title}",
            range = config.options,
            value = config.options.indexOf(config.value).takeIf { it >= 0 } ?: 0,
            onCancel = { showPickerIndex = -1 },
            onChange = { selectedIndex ->
                config.onValueChange(config.options[selectedIndex])
                showPickerIndex = -1
            }
        )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}