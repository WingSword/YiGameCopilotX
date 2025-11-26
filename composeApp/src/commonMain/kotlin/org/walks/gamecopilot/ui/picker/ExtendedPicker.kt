package org.walks.gamecopilot.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.ui.button.WeButton

/**
 * 多列选择器，支持不同列的独立配置
 */
@Composable
fun WeMultiColumnPicker(
    visible: Boolean,
    columns: List<PickerColumn>,
    title: String? = null,
    config: PickerConfig = PickerConfig(),
    onCancel: () -> Unit,
    onValuesChange: (List<Int>) -> Unit
) {
    val ranges = columns.map { it.options }.toTypedArray()
    val values = columns.map { it.selectedIndex }.toTypedArray()
    
    WePicker(
        visible = visible,
        ranges = ranges,
        values = values,
        title = title,
        config = config,
        onCancel = onCancel,
        onValuesChange = { intArray ->
            onValuesChange(intArray.toList())
        }
    )
}

/**
 * 带标签的选择器
 */
@Composable
fun WeLabeledPicker(
    visible: Boolean,
    title: String? = null,
    range: List<String>,
    value: Int,
    label: String,
    config: PickerConfig = PickerConfig(),
    onChange: (Int) -> Unit,
    onCancel: () -> Unit
) {
    WePicker(
        visible = visible,
        ranges = arrayOf(range),
        values = arrayOf(value),
        title = title,
        config = config,
        onCancel = onCancel,
        onValuesChange = { onChange(it.first()) }
    )
}

/**
 * 快速选择器 - 简化版本，用于常见场景
 */
@Composable
fun WeQuickPicker(
    visible: Boolean,
    options: List<String>,
    selectedIndex: Int,
    title: String? = null,
    onSelected: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val config = remember {
        PickerConfig(
            height = 200,
            itemHeight = 40,
            animationDuration = 100,
            confirmText = "选择",
            cancelText = "取消"
        )
    }
    
    WeSingleColumnPicker(
        visible = visible,
        title = title,
        range = options,
        value = selectedIndex,
        config = config,
        onChange = { index ->
            onSelected(index, options[index])
        },
        onCancel = onDismiss
    )
}

/**
 * 选择器列配置
 */
data class PickerColumn(
    val options: List<String>,
    val selectedIndex: Int,
    val label: String? = null
)

/**
 * 主题化选择器样式枚举
 */
enum class PickerStyle {
    DEFAULT,    // 默认样式
    COMPACT,    // 紧凑样式
    ELEGANT,    // 优雅样式
    MINIMAL     // 极简样式
}

/**
 * 样式化选择器
 */
@Composable
fun WeStyledPicker(
    visible: Boolean,
    range: List<String>,
    value: Int,
    title: String? = null,
    style: PickerStyle = PickerStyle.DEFAULT,
    onChange: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val config = remember(style) {
        when (style) {
            PickerStyle.DEFAULT -> PickerConfig()
            PickerStyle.COMPACT -> PickerConfig(
                height = 200,
                itemHeight = 40,
                animationDuration = 100
            )
            PickerStyle.ELEGANT -> PickerConfig(
                height = 320,
                itemHeight = 64,
                animationDuration = 300,
                confirmText = "确认",
                cancelText = "返回"
            )
            PickerStyle.MINIMAL -> PickerConfig(
                height = 180,
                itemHeight = 36,
                animationDuration = 50,
                showDivider = false,
                confirmText = "OK",
                cancelText = "X"
            )
        }
    }
    
    WeSingleColumnPicker(
        visible = visible,
        title = title,
        range = range,
        value = value,
        config = config,
        onChange = onChange,
        onCancel = onCancel
    )
}

/**
 * 自定义按钮样式的选择器
 */
@Composable
fun WeCustomButtonPicker(
    visible: Boolean,
    range: List<String>,
    value: Int,
    title: String? = null,
    config: PickerConfig = PickerConfig(),
    customButtons: @Composable (onConfirm: () -> Unit, onCancel: () -> Unit) -> Unit = { confirm, cancel ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeButton(
                text = "取消",
                modifier = Modifier.weight(1f),
                onClick = cancel
            )
            WeButton(
                text = "确定",
                modifier = Modifier.weight(1f),
                onClick = confirm
            )
        }
    },
    onChange: (Int) -> Unit,
    onCancel: () -> Unit
) {
    WePicker(
        visible = visible,
        ranges = arrayOf(range),
        values = arrayOf(value),
        title = title,
        config = config,
        onCancel = onCancel,
        onValuesChange = { onChange(it.first()) }
    )
}