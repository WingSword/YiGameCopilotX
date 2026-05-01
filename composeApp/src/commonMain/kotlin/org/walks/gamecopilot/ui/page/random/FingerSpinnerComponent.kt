package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.walks.gamecopilot.PlatformHelper
import kotlin.random.Random

private val RAINBOW_RIPPLE_COLORS = listOf(
    Color(0xFFFF4D4F), // 红
    Color(0xFFFF8C42), // 橙
    Color(0xFFFFD54F), // 黄
    Color(0xFF66BB6A), // 绿
    Color(0xFF26C6DA), // 青
    Color(0xFF42A5F5), // 蓝
    Color(0xFFAB47BC)  // 紫
)

private data class FingerRipple(
    val id: Long,
    val position: Offset,
    val color: Color,
    val phaseOffset: Float,
    val isWinner: Boolean = false,
    val isFadingOut: Boolean = false
)

@Composable
fun FingerSpinnerComponent(modifier: Modifier = Modifier) {
    val ripples = remember { mutableStateMapOf<Long, FingerRipple>() }
    val usedRoundColors = remember { mutableStateMapOf<Long, Color>() }
    var selectedPointerId by remember { mutableStateOf<Long?>(null) }
    var jumpingPointerId by remember { mutableStateOf<Long?>(null) }
    var countdownSeconds by remember { mutableIntStateOf(3) }
    var participantRevision by remember { mutableLongStateOf(0L) }
    var runningProcess by remember { mutableStateOf(false) }
    var lockToWinner by remember { mutableStateOf(false) }

    val activeCount = ripples.size
    val infiniteTransition = rememberInfiniteTransition(label = "fingerRippleGlobal")
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProgress"
    )

    LaunchedEffect(participantRevision, activeCount, lockToWinner) {
        if (runningProcess || lockToWinner || activeCount < 2) {
            return@LaunchedEffect
        }

        val revisionSnapshot = participantRevision
        countdownSeconds = 3
        repeat(3) { step ->
            countdownSeconds = 3 - step
            delay(1000)
            if (revisionSnapshot != participantRevision || ripples.size < 2) {
                return@LaunchedEffect
            }
        }

        runningProcess = true
        try {
            repeat(3) {
                if (ripples.size < 2 || revisionSnapshot != participantRevision) return@LaunchedEffect
                PlatformHelper.getInstance().vibrateMethod()
                delay(90)
            }

            val candidateIds = ripples.keys.toList()
            if (candidateIds.size < 2) {
                return@LaunchedEffect
            }

            val jumpDelays = listOf(
                150L, 185L, 140L, 210L, 160L, 235L, 175L, 260L, 190L,
                285L, 205L, 315L, 225L, 340L, 245L, 370L, 265L, 400L
            )
            jumpDelays.forEachIndexed { index, jumpDelay ->
                if (ripples.size < 2 || revisionSnapshot != participantRevision) return@LaunchedEffect
                jumpingPointerId = candidateIds[Random.nextInt(candidateIds.size)]
                if (index % 2 == 0) {
                    PlatformHelper.getInstance().vibrateMethod()
                }
                delay(jumpDelay)
            }

            val winnerId = candidateIds[Random.nextInt(candidateIds.size)]
            selectedPointerId = winnerId
            jumpingPointerId = winnerId
            // 一旦确定结果立即进入锁定态，避免在淡出阶段被触摸变化打断并进入下一轮
            lockToWinner = true

            candidateIds.forEach { id ->
                val ripple = ripples[id] ?: return@forEach
                ripples[id] = ripple.copy(
                    isWinner = id == winnerId,
                    isFadingOut = id != winnerId
                )
            }

            delay(420)
            candidateIds.filter { it != winnerId }.forEach { ripples.remove(it) }
        } finally {
            if (!lockToWinner) {
                jumpingPointerId = null
            }
            runningProcess = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(lockToWinner, selectedPointerId) {
                awaitPointerEventScope {
                    var previousPressed = emptySet<Long>()
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedChanges = event.changes.filter { it.pressed }
                        val nowPressed = pressedChanges.associateBy { it.id.value }
                        val nowIds = nowPressed.keys
                        val countChanged = nowIds.size != previousPressed.size

                        // 同步删除已离开的触点
                        (ripples.keys - nowIds).forEach { removedId ->
                            // 结果锁定后即使赢家手指松开，也保留最后高亮直到全部抬手才重置
                            if (lockToWinner && removedId == selectedPointerId) {
                                return@forEach
                            }
                            ripples.remove(removedId)
                            usedRoundColors.remove(removedId)
                        }

                        pressedChanges.forEach { change ->
                            val pointerId = change.id.value
                            val ripple = ripples[pointerId]
                            if (ripple != null) {
                                val keepWinnerOnly = lockToWinner && selectedPointerId != pointerId
                                if (keepWinnerOnly) {
                                    ripples.remove(pointerId)
                                } else {
                                    ripples[pointerId] = ripple.copy(position = change.position)
                                }
                            } else if (!lockToWinner) {
                                val rippleColor = allocateDistinctColor(pointerId, usedRoundColors)
                                ripples[pointerId] = FingerRipple(
                                    id = pointerId,
                                    position = change.position,
                                    color = rippleColor,
                                    phaseOffset = Random.nextFloat()
                                )
                            }
                            change.consume()
                        }

                        if (countChanged && !lockToWinner) {
                            participantRevision += 1
                        }

                        if (nowIds.isEmpty()) {
                            ripples.clear()
                            usedRoundColors.clear()
                            runningProcess = false
                            lockToWinner = false
                            selectedPointerId = null
                            jumpingPointerId = null
                            countdownSeconds = 3
                            participantRevision += 1
                        }

                        previousPressed = nowIds
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            ripples.values.forEach { ripple ->
                val highlighted = ripple.id == jumpingPointerId
                drawFingerRipple(
                    center = ripple.position,
                    baseColor = ripple.color,
                    phaseOffset = ripple.phaseOffset,
                    highlighted = highlighted,
                    selected = ripple.isWinner,
                    fadingOut = ripple.isFadingOut,
                    waveProgress = waveProgress,
                    pulseProgress = pulseProgress
                )
            }
        }

        val guideText = when {
            runningProcess -> "正在随机选择..."
            lockToWinner && selectedPointerId != null -> "已选中，松开全部手指后重置"
            activeCount >= 2 -> "保持按住"
            else -> "两个或以上人每人陆续伸出一根手指按住屏幕"
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!runningProcess && !lockToWinner && activeCount >= 2) {
                Text(
                    text = countdownSeconds.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = guideText,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFingerRipple(
    center: Offset,
    baseColor: Color,
    phaseOffset: Float,
    highlighted: Boolean,
    selected: Boolean,
    fadingOut: Boolean,
    waveProgress: Float,
    pulseProgress: Float
) {
    val mixedWave = (waveProgress + phaseOffset) % 1f
    val secondWave = (mixedWave + 0.45f) % 1f
    val pulseScale = 0.92f + 0.16f * pulseProgress

    val alpha = when {
        fadingOut -> 0.2f
        selected -> 0.95f
        highlighted -> 0.9f
        else -> 0.75f
    }
    val scale = when {
        selected -> 1.28f
        highlighted -> 1.18f
        else -> 1f
    } * pulseScale

    val minRadius = 28f * scale
    val maxRadius = 170f * scale

    drawCircle(
        color = baseColor.copy(alpha = 0.42f * alpha),
        radius = minRadius,
        center = center
    )
    // 中心高亮，避免被手指完全遮挡时“看不见”
    drawCircle(
        color = Color.White.copy(alpha = 0.82f * alpha),
        radius = 8f * scale,
        center = center
    )
    if (highlighted) {
        drawCircle(
            color = Color.White.copy(alpha = 0.95f * alpha),
            radius = maxRadius + 22f,
            center = center,
            style = Stroke(width = 9f)
        )
    }
    drawCircle(
        color = baseColor.copy(alpha = (1f - mixedWave) * 0.82f * alpha),
        radius = minRadius + (maxRadius - minRadius) * mixedWave,
        center = center,
        style = Stroke(width = 11f)
    )
    drawCircle(
        color = baseColor.copy(alpha = (1f - secondWave) * 0.62f * alpha),
        radius = minRadius + (maxRadius - minRadius) * secondWave,
        center = center,
        style = Stroke(width = 8f)
    )
}

private fun allocateDistinctColor(
    pointerId: Long,
    usedRoundColors: MutableMap<Long, Color>
): Color {
    usedRoundColors[pointerId]?.let { return it }
    val occupied = usedRoundColors.values.toSet()
    val available = RAINBOW_RIPPLE_COLORS.filterNot { it in occupied }
    val chosen = if (available.isNotEmpty()) {
        available.random()
    } else {
        RAINBOW_RIPPLE_COLORS.random()
    }
    usedRoundColors[pointerId] = chosen
    return chosen
}
