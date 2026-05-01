package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.walks.gamecopilot.PlatformHelper
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/**
 * 硬币翻转动画组件
 * @param onFlipComplete 翻转完成回调，返回布尔值表示正面(true)或反面(false)
 * @param modifier 修饰符
 * @param frontText 硬币正面文字
 *
 * @param backText 硬币反面文字
 */
@OptIn(ExperimentalTime::class)
@Composable
fun RollCoinAnimation(
    onFlipComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isRolling: Boolean = false,
    frontText: String = "正",
    backText: String = "反"
) {
    val jumpProgress = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val tiltX = remember { Animatable(0f) }
    val edgeGlow = remember { Animatable(0f) }
    val jumpDistancePx = with(LocalDensity.current) { 34.dp.toPx() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRolling) {
        if (isRolling) {
            scope.launch {
                val isHeads = Random.nextBoolean()
                // 并行执行动画和震动
                kotlinx.coroutines.coroutineScope {
                    // 启动震动协程
                    launch {
                        val duration = 1000 // 动画总时长
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

                    // 执行翻转动画
                    launch { flipCoinAnimation(rotationY, isHeads) }
                    launch { tossJumpAnimation(jumpProgress, tiltX) }
                    launch { edgeGlowAnimation(edgeGlow) }
                }
                onFlipComplete(isHeads)
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(68.dp)
                .height(14.dp)
                .graphicsLayer {
                    val shadowT = 1f - jumpProgress.value
                    scaleX = 0.8f + shadowT * 0.45f
                    scaleY = 0.55f + shadowT * 0.25f
                    alpha = 0.07f + shadowT * 0.24f
                }
                .background(Color.Black, CircleShape)
        )
        // 正面（0度）
        CoinFace(
            isHeads = true,
            text = frontText,
            edgeGlow = edgeGlow.value,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    this.rotationX = tiltX.value
                    transformOrigin = TransformOrigin.Center
                    cameraDistance = 10f * density
                    translationY = -jumpDistancePx * jumpProgress.value
                    // 当旋转超过90度时隐藏正面
                    alpha = if (rotationY.value <= 90f || rotationY.value >= 270f) 1f else 0f
                }
        )

        // 反面（180度）
        CoinFace(
            isHeads = false,
            text = backText,
            edgeGlow = edgeGlow.value,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.rotationY = rotationY.value + 180f
                    this.rotationX = tiltX.value
                    transformOrigin = TransformOrigin.Center
                    cameraDistance = 10f * density
                    translationY = -jumpDistancePx * jumpProgress.value
                    // 当旋转在90-270度之间时显示反面
                    alpha = if (rotationY.value > 90f && rotationY.value < 270f) 1f else 0f
                }
        )
    }
}

/**
 * 硬币翻转动画
 */
private suspend fun flipCoinAnimation(
    rotationY: Animatable<Float, AnimationVector1D>,
    isHeads: Boolean
) {
    // 快速翻转2-3圈
    rotationY.animateTo(
        targetValue = (720..1080).random().toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    // 按随机结果收敛到正面(0)或反面(180)
    val targetAngle = if (isHeads) 0f else 180f

    // 缓慢停止到最终位置
    rotationY.animateTo(
        targetValue = targetAngle,
        animationSpec = tween(200)
    )
}

private suspend fun tossJumpAnimation(
    jumpProgress: Animatable<Float, AnimationVector1D>,
    tiltX: Animatable<Float, AnimationVector1D>
) {
    jumpProgress.snapTo(0f)
    tiltX.snapTo(0f)
    kotlinx.coroutines.coroutineScope {
        launch {
            jumpProgress.animateTo(1f, animationSpec = tween(260, easing = FastOutSlowInEasing))
            jumpProgress.animateTo(0.2f, animationSpec = tween(220, easing = FastOutSlowInEasing))
            jumpProgress.animateTo(0.62f, animationSpec = tween(180, easing = FastOutSlowInEasing))
            jumpProgress.animateTo(0f, animationSpec = tween(340, easing = FastOutSlowInEasing))
        }
        launch {
            tiltX.animateTo(
                (-22..22).random().toFloat(),
                animationSpec = tween(360, easing = FastOutSlowInEasing)
            )
            tiltX.animateTo(0f, animationSpec = tween(260, easing = FastOutSlowInEasing))
        }
    }
}

private suspend fun edgeGlowAnimation(edgeGlow: Animatable<Float, AnimationVector1D>) {
    edgeGlow.snapTo(0f)
    repeat(3) {
        edgeGlow.animateTo(1f, animationSpec = tween(140, easing = FastOutSlowInEasing))
        edgeGlow.animateTo(0.2f, animationSpec = tween(220, easing = FastOutSlowInEasing))
    }
    edgeGlow.animateTo(0f, animationSpec = tween(280, easing = FastOutSlowInEasing))
}

/**
 * 硬币面组件
 */
@Composable
fun CoinFace(
    isHeads: Boolean,
    text: String = if (isHeads) "正" else "反",
    edgeGlow: Float = 0f,
    modifier: Modifier = Modifier
) {
    val faceBase = if (isHeads) Color(0xFFFFD26A) else Color(0xFFD6DBE3)
    val faceDark = if (isHeads) Color(0xFFF1B938) else Color(0xFFAAB2BF)
    val edgeBrush = Brush.sweepGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f + edgeGlow * 0.35f),
            Color.White.copy(alpha = 0.02f),
            Color.White.copy(alpha = 0.12f + edgeGlow * 0.3f),
            Color.White.copy(alpha = 0.02f)
        )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(8.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(faceBase, faceDark),
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 3.dp, brush = edgeBrush, shape = CircleShape)
        )
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHeads) Color(0xFF8B4513) else Color(0xFF2F4F4F)
        )
    }
}