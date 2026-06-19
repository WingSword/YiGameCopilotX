package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.WheelItem
import org.walks.gamecopilot.getOrNull
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 真正的转盘组件 - 圆形转盘形式
 * @param items 转盘选项列表
 * @param onItemsChange 选项变更回调
 * @param onTriggerRandom 触发随机回调
 */
@Composable
fun WheelRandomComponent(
    items: List<WheelItem>,
    onItemsChange: (List<WheelItem>) -> Unit,
    onTriggerRandom: () -> Unit
) {
    // 转盘旋转角度
    val rotationAngle = remember { Animatable(0f) }

    // 是否正在旋转
    var isSpinning by remember { mutableStateOf(false) }

    // 中奖索引
    var winnerIndex by remember { mutableStateOf(-1) }

    // 显示结果对话框
    var showResultDialog by remember { mutableStateOf(false) }

    // 协程作用域
    val coroutineScope = rememberCoroutineScope()

    val themedWheelPalette = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
    )
    val displayItems = items.mapIndexed { index, item ->
        item.copy(color = themedWheelPalette[index % themedWheelPalette.size])
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IndustrialResultBar(
            text = displayItems.getOrNull(winnerIndex)?.text ?: "READY / WAIT FOR SPIN"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 转盘容器
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            // 转盘（旋转）
            WheelCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                items = displayItems,
                rotationAngle = rotationAngle.value
            )

            // 指针（固定位置）
            PointerCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            // 开始按钮（中间）
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        CircleShape
                    )
                    .clickable(enabled = !isSpinning && items.isNotEmpty()) {
                        if (!isSpinning) {
                            isSpinning = true
                            // 重置旋转角度到0度，确保每次都能正常旋转

                            coroutineScope.launch {
                                rotationAngle.snapTo(0f)
                                startSpinning(rotationAngle, items) { index ->
                                    isSpinning = false
                                    winnerIndex = index
                                    showResultDialog = true
                                    onTriggerRandom()
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSpinning) "转动中" else "开始",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSpinning) "转盘减速中..." else "点击中心按钮开始",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }

}

/**
 * 转盘Canvas组件
 */
@Composable
fun WheelCanvas(
    modifier: Modifier = Modifier,
    items: List<WheelItem>,
    rotationAngle: Float = 0f
) {
    val textMeasurer = rememberTextMeasurer()
    // 在 @Composable 上下文中预先获取颜色，传给 DrawScope 函数
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Canvas(modifier = modifier) {
        if (items.isNotEmpty()) {
            rotate(rotationAngle) {
                drawWheel(items, size, textMeasurer, textColor, borderColor)
            }
        }
    }
}

/**
 * 绘制转盘
 */
private fun DrawScope.drawWheel(
    items: List<WheelItem>,
    size: Size,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textColor: Color,
    borderColor: Color
) {
    // 转盘尺寸（使用Canvas的最小边）
    val wheelDiameter = min(size.width, size.height)
    val radius = wheelDiameter / 2
    val centerX = size.width / 2
    val centerY = size.height / 2

    // 计算总权重
    var totalWeight = 0.0
    items.forEach { totalWeight += it.weight.toDouble() }
    val totalWeightFloat = totalWeight.toFloat()

    // 计算每个选项的起始角度
    var currentAngle = -90f // 从顶部开始

    items.forEachIndexed { index, item ->
        // 根据权重计算每个选项的扇形角度
        val sweepAngle = 360f * (item.weight / totalWeightFloat)
        val startAngle = currentAngle
        
        // 绘制扇形
        drawArc(
            color = item.color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(wheelDiameter, wheelDiameter)
        )

        // 保存当前选项的角度信息，用于后续绘制文本
        val itemAngle = startAngle + sweepAngle / 2

        // 绘制分隔线 - 改为长条直线
        val startAngleRad = startAngle * PI.toFloat() / 180f

        // 直线长度（从中心到边缘）
        radius

        // 计算直线起点（中心点）
        val lineStart = Offset(centerX, centerY)

        // 计算直线终点（转盘边缘）
        val lineEnd = Offset(
            centerX + radius * cos(startAngleRad),
            centerY + radius * sin(startAngleRad)
        )

        // 绘制直线分隔线
        drawLine(
            color = Color.White.copy(alpha = 0.65f),
            start = lineStart,
            end = lineEnd,
            strokeWidth = 1.6f
        )

        // 绘制选项文本 - 使用计算出的角度和大小
        drawWheelItemText(
            item = item,
            itemAngle = itemAngle,
            radius = radius,
            centerX = centerX,
            centerY = centerY,
            sweepAngle = sweepAngle,
            textMeasurer = textMeasurer,
            textColor = textColor,
            borderColor = borderColor
        )

        // 更新当前角度为下一个选项的起始角度
        currentAngle += sweepAngle
    }

    // 外圈描边和高光，提升主题化质感
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(
                Color.White.copy(alpha = 0.3f),
                Color.Transparent,
                Color.White.copy(alpha = 0.14f),
                Color.Transparent
            )
        ),
        radius = radius * 0.98f,
        center = Offset(centerX, centerY),
        style = Stroke(width = 6f)
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.26f),
        radius = radius * 0.98f,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1.5f)
    )
}


/**
 * 绘制转盘选项文本
 * 根据选项的大小（比例）调整字体大小和位置
 */
private fun DrawScope.drawWheelItemText(
    item: WheelItem,
    itemAngle: Float,
    radius: Float,
    centerX: Float,
    centerY: Float,
    sweepAngle: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textColor: Color,
    borderColor: Color
) {
    // 根据扇形大小调整文本位置
    // 扇形越大，文本越靠近边缘；扇形越小，文本越靠近中心
    val textRadius = radius * (0.6f + 0.3f * (sweepAngle / 360f))

    // 根据扇形大小调整字体大小
    val fontSize = (8f + 4f * (sweepAngle / 360f)).sp

    // 测量文本
    val textLayoutResult = textMeasurer.measure(
        text = "  " + item.text + "  ",
        style = TextStyle(
            color = textColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    )

    // 计算文本在旋转前的位置
    val textAngleRad = itemAngle * PI.toFloat() / 180f
    val textX = centerX + textRadius * cos(textAngleRad)
    val textY = centerY + textRadius * sin(textAngleRad)

    // 平移到文本位置，绘制文本
    translate(left = textX, top = textY) {
        // 绘制工业风标签框
        val backgroundPadding = 8f
        val backgroundSize = Size(
            width = textLayoutResult.size.width + backgroundPadding * 2,
            height = textLayoutResult.size.height + backgroundPadding * 2
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(
                -backgroundSize.width / 2f,
                -backgroundSize.height / 2f
            ),
            size = backgroundSize,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(
                -backgroundSize.width / 2f,
                -backgroundSize.height / 2f
            ),
            size = backgroundSize,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(width = 1.2f)
        )

        // 绘制文本
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                -textLayoutResult.size.width / 2f,
                -textLayoutResult.size.height / 2f
            )
        )
    }
}

/**
 * 指针Canvas组件
 */
@Composable
fun PointerCanvas(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        drawPointer(size, primaryColor)
    }
}

@Composable
private fun IndustrialResultBar(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 绘制指针
 */
private fun DrawScope.drawPointer(size: Size, primaryColor: Color) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    // 使用相对尺寸，避免不同容器尺寸下比例失衡
    val radius = min(size.width, size.height) / 2f
    val pointerLength = radius * 0.7f
    val pointerBaseWidth = radius * 0.15f

    // 绘制三角形指针
    drawPath(
        path = Path().apply {
            moveTo(centerX, centerY - pointerLength)  // 三角形顶点
            lineTo(centerX - pointerBaseWidth / 2, centerY)  // 左下角
            lineTo(centerX + pointerBaseWidth / 2, centerY)  // 右下角
            close()
        },
        color = primaryColor.copy(alpha = 0.88f)
    )
}

/**
 * 开始旋转转盘
 */
@OptIn(ExperimentalTime::class)
private suspend fun startSpinning(
    rotationAngle: Animatable<Float, *>,
    items: List<WheelItem>,
    onComplete: (Int) -> Unit
) {
    // 计算总权重
    var totalWeight = 0.0
    items.forEach { totalWeight += it.weight.toDouble() }

    // 随机选择中奖选项，考虑权重
    val random = kotlin.random.Random.nextFloat() * totalWeight.toFloat()
    var accumulatedWeight = 0f
    var winnerIndex = 0

    for (i in items.indices) {
        accumulatedWeight += items[i].weight
        if (random <= accumulatedWeight) {
            winnerIndex = i
            break
        }
    }
    
    // 动画时长
    val duration = 5000

    // 计算每个选项的起始角度和扇形角度
    var currentAngle = -90f // 从顶部开始
    val itemSweepAngles = mutableListOf<Float>()
    val itemStartAngles = mutableListOf<Float>()

    for (item in items) {
        val sweepAngle = 360f * (item.weight / totalWeight.toFloat())
        itemStartAngles.add(currentAngle)
        itemSweepAngles.add(sweepAngle)
        currentAngle += sweepAngle
    }
    
    // 计算最终停止角度
    // 指针在顶部(12点钟方向)，我们想让转盘停止时指针指向选中选项
    // 要让指针指向第N个选项，转盘需要旋转到使该选项的中心点位于顶部
    val winnerStartAngle = itemStartAngles[winnerIndex]
    val winnerSweepAngle = itemSweepAngles[winnerIndex]
    val winnerCenterAngle = winnerStartAngle + winnerSweepAngle / 2

    // 计算目标角度：让选中选项的中心点位于顶部(270度方向)
    val targetAngle = 360f * 5 + (270f - winnerCenterAngle)

    // 创建协程作用域并并行执行震动和动画
    kotlinx.coroutines.coroutineScope {
        // 启动震动协程
        launch {
            var lastVibrateTime = 0L
            val startTime = Clock.System.now().toEpochMilliseconds()

            while (true) {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val elapsed = currentTime - startTime

                if (elapsed >= duration) break

                // 计算进度(0到1)
                val progress = elapsed.toFloat() / duration

                // 根据进度计算震动间隔，随时间线性增加
                // 开始时30ms，结束时300ms
                val vibrateInterval = (30 + progress * 270).toLong()

                // 检查是否应该震动
                if (currentTime - lastVibrateTime >= vibrateInterval) {
                    PlatformHelper.getInstance().vibrateMethod()
                    lastVibrateTime = currentTime
                }

                kotlinx.coroutines.delay(16)
            }
        }

        // 执行旋转动画
        rotationAngle.animateTo(
            targetValue = targetAngle,
            animationSpec = tween<Float>(
                durationMillis = duration,
                easing = androidx.compose.animation.core.EaseOutCubic
            )
        )
    }

    // 完成后回调
    onComplete(winnerIndex)
}