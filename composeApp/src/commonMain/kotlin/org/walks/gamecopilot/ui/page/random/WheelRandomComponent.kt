package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
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


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 选项说明
        Text(
            text = items.getOrNull(winnerIndex)?.text ?: "点击开始",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 转盘容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            // 转盘（旋转）
            WheelCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                items = items,
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
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
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
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
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

    Canvas(modifier = modifier) {
        if (items.isNotEmpty()) {
            rotate(rotationAngle) {
                drawWheel(items, size, textMeasurer)
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
    textMeasurer: androidx.compose.ui.text.TextMeasurer
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
        
        // 绘制分隔线 - 改为底边向外的细长等腰三角形
        val startAngleRad = startAngle * PI.toFloat() / 180f

        // 三角形底边宽度 - 增加到原来的两倍
        val triangleBaseWidth = 16f  // 固定宽度为16像素

        // 计算垂直于径向的方向
        val perpAngle = startAngleRad + PI.toFloat() / 2f

        // 计算顶点到中心的距离（稍微留出一些空间，避免与指针重叠）
        val innerRadius = radius * 0.3f  // 使用固定比例，确保所有三角形一致
        val tipPoint = Offset(
            centerX + innerRadius * cos(startAngleRad),
            centerY + innerRadius * sin(startAngleRad)
        )

        // 计算底边两端点（在转盘边缘）
        val basePoint1 = Offset(
            centerX + radius * cos(startAngleRad) - triangleBaseWidth * cos(perpAngle),
            centerY + radius * sin(startAngleRad) - triangleBaseWidth * sin(perpAngle)
        )
        val basePoint2 = Offset(
            centerX + radius * cos(startAngleRad) + triangleBaseWidth * cos(perpAngle),
            centerY + radius * sin(startAngleRad) + triangleBaseWidth * sin(perpAngle)
        )

        // 绘制三角形 - 底边在转盘边缘，顶点朝内
        drawPath(
            path = Path().apply {
                // 从底边一端开始
                moveTo(basePoint1.x, basePoint1.y)
                // 连接到底边另一端
                lineTo(basePoint2.x, basePoint2.y)
                // 连接到顶点
                lineTo(tipPoint.x, tipPoint.y)
                // 闭合三角形
                close()
            },
            color = Color.White
        )

        // 绘制选项文本 - 使用计算出的角度和大小
        drawWheelItemText(
            item = item,
            itemAngle = itemAngle,
            radius = radius,
            centerX = centerX,
            centerY = centerY,
            sweepAngle = sweepAngle,
            textMeasurer = textMeasurer
        )

        // 更新当前角度为下一个选项的起始角度
        currentAngle += sweepAngle
    }
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
    textMeasurer: androidx.compose.ui.text.TextMeasurer
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
            color = Color.White,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    )

    // 计算文本在旋转前的位置
    val textAngleRad = itemAngle * PI.toFloat() / 180f
    val textX = centerX + textRadius * cos(textAngleRad)
    val textY = centerY + textRadius * sin(textAngleRad)

    // 平移到文本位置，绘制文本
    translate(left = textX, top = textY) {
        // 绘制半透明圆角背景
        val backgroundPadding = 8f
        val backgroundSize = Size(
            width = textLayoutResult.size.width + backgroundPadding * 2,
            height = textLayoutResult.size.height + backgroundPadding * 2
        )

        // 绘制圆角矩形背景
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(
                -backgroundSize.width / 2f,
                -backgroundSize.height / 2f
            ),
            size = backgroundSize,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
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

/**
 * 绘制指针
 */
private fun DrawScope.drawPointer(size: Size, primaryColor: Color) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    // 绘制等腰三角形指针 - 恢复合适大小
    val pointerLength = 200f  // 增加指针长度
    val pointerBaseWidth = 80f  // 增加三角形底边宽度，使指针更粗更明显

    // 绘制三角形指针
    drawPath(
        path = Path().apply {
            moveTo(centerX, centerY - pointerLength)  // 三角形顶点
            lineTo(centerX - pointerBaseWidth / 2, centerY)  // 左下角
            lineTo(centerX + pointerBaseWidth / 2, centerY)  // 右下角
            close()
        },
        color = primaryColor  // 使用传入的主题色
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
    // 计算每个选项的权重和角度
    var totalWeight = 0.0
    items.forEach { totalWeight += it.weight.toDouble() }
    val totalWeightFloat = totalWeight.toFloat()

    // 随机选择中奖选项，考虑权重
    var tempTotalWeight = 0.0
    items.forEach { tempTotalWeight += it.weight.toDouble() }
    val random = kotlin.random.Random.nextFloat() * tempTotalWeight.toFloat()
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

    // 计算每个选项的起始角度
    var currentAngle = -90f // 从顶部开始
    val itemAngles = mutableListOf<Float>()

    for (item in items) {
        val sweepAngle = 360f * (item.weight / totalWeightFloat)
        val itemCenterAngle = currentAngle + sweepAngle / 2
        itemAngles.add(itemCenterAngle)
        currentAngle += sweepAngle
    }
    
    // 计算最终停止角度
    // 指针在顶部(12点钟方向)，我们想让转盘停止时指针指向选中选项
    // 要让指针指向第N个选项，转盘需要旋转到使该选项的中心点位于顶部
    val targetAngle = 360f * 5 + itemAngles[winnerIndex] + 90f

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

    // 计算最终结果
    // 转盘停止后的角度
    val finalAngle = (targetAngle % 360 + 360) % 360

    // 转盘顺时针旋转，指针固定在顶部(270度)
    val pointerAngle = 270f
    var finalIndex = 0
    var minDiff = Float.MAX_VALUE

    // 计算每个选项的起始角度和中心角度
    var currentAngleFinal = -90f // 从顶部开始
    var totalWeightFinal = 0.0
    items.forEach { totalWeightFinal += it.weight.toDouble() }
    val totalWeightFloatFinal = totalWeightFinal.toFloat()
    val itemAnglePairs = mutableListOf<Pair<Float, Float>>() // 每个选项的起始角度和中心角度

    for (item in items) {
        val sweepAngle = 360f * (item.weight / totalWeightFloatFinal)
        val startAngle = currentAngleFinal
        val centerAngle = currentAngleFinal + sweepAngle / 2
        itemAnglePairs.add(Pair(startAngle, centerAngle))
        currentAngleFinal += sweepAngle
    }

    // 找到最接近指针的选项
    for (i in itemAnglePairs.indices) {
        val (_, centerAngle) = itemAnglePairs[i]
        // 计算选项i的中心角度在转盘上的绝对角度
        val optionAngle = (centerAngle + finalAngle) % 360
        // 计算与指针的角度差
        val diff = kotlin.math.abs((pointerAngle - optionAngle + 360) % 360)

        if (diff < minDiff) {
            minDiff = diff
            finalIndex = i
        }
    }

    // 完成后回调
    onComplete(finalIndex)
}