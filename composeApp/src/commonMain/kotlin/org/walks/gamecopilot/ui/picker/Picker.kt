package org.walks.gamecopilot.ui.picker

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.PrimaryColor
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.ui.button.ButtonType
import org.walks.gamecopilot.ui.button.WeButton
import org.walks.gamecopilot.ui.popup.WePopup

/**
 * 增强版选择器组件配置
 */
data class PickerConfig(
    val height: Int = 280,
    val itemHeight: Int = 56,
    val animationDuration: Int = 150,
    val showDivider: Boolean = true,
    val enableVibration: Boolean = true,
    val confirmText: String = "确定",
    val cancelText: String = "取消"
)

@Composable
fun WePicker(
    visible: Boolean,
    ranges: Array<List<String>>,
    values: Array<Int>,
    title: String? = null,
    config: PickerConfig = PickerConfig(),
    onCancel: () -> Unit,
    onColumnValueChange: ((column: Int, value: Int, values: Array<Int>) -> Unit)? = null,
    onValuesChange: (Array<Int>) -> Unit
) {
    val localValues = remember(visible) { values.copyOf() }

    WePopup(
        visible,
        title = title,
        enterTransition = fadeIn(tween(config.animationDuration)) + slideInVertically(tween(config.animationDuration)) { it / 3 },
        exitTransition = fadeOut(tween(config.animationDuration)) + slideOutVertically(tween(config.animationDuration)) { it / 3 },
        draggable = false,
        onClose = onCancel
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(config.height.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .drawIndicator()
            ) {
                // 可选列表
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    ranges.forEachIndexed { index, options ->
                        ColumnItem(
                            options = options,
                            index = localValues[index],
                            config = config
                        ) {
                            if (config.enableVibration) {
                                PlatformHelper.getInstance().vibrateMethod()
                            }
                            localValues[index] = it
                            onColumnValueChange?.invoke(index, it, localValues.copyOf())
                        }
                    }
                }
                // 遮罩层
                Mask(config)
            }

            Spacer(modifier = Modifier.height(24.dp))
            // 操作栏
            ActionBar(config, onCancel) {
                onValuesChange(localValues)
                onCancel()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.ColumnItem(
    options: List<String>,
    index: Int,
    config: PickerConfig,
    onChange: (Int) -> Unit
) {
    val itemHeight = config.itemHeight.dp
    val verticalPadding = remember { (config.height.dp - itemHeight) / 2 }
    val listState = rememberLazyListState(index)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect {
                onChange(it)
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        flingBehavior = rememberSnapFlingBehavior(listState)
    ) {
        items(options) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun Mask(config: PickerConfig) {
    @Composable
    fun ColumnScope.MaskItem() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
        )
    }

    Column {
        MaskItem()
        Box(modifier = Modifier.height(config.itemHeight.dp))
        MaskItem()
    }
}

@Composable
private fun Modifier.drawIndicator() = this.drawBehind {
    drawRoundRect(
        color = PrimaryColor.copy(alpha = 0.1f),
        topLeft = Offset(0f, size.height / 2 - 56.dp.toPx() / 2),
        size = Size(size.width, 56.dp.toPx()),
        cornerRadius = CornerRadius(0.dp.toPx())
    )
    // 添加选中指示器边框
    drawRoundRect(
        color = PrimaryColor.copy(alpha = 0.3f),
        topLeft = Offset(0f, size.height / 2 - 56.dp.toPx() / 2),
        size = Size(size.width, 56.dp.toPx()),
        cornerRadius = CornerRadius(0.dp.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
    )
}

@Composable
private fun ActionBar(config: PickerConfig, onCancel: () -> Unit, onConfirm: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        WeButton(
            text = config.cancelText, 
            type = ButtonType.PLAIN, 
            modifier = Modifier.weight(1f)
        ) {
            onCancel()
        }
        WeButton(
            text = config.confirmText, 
            modifier = Modifier.weight(1f)
        ) {
            onConfirm()
        }
    }
}