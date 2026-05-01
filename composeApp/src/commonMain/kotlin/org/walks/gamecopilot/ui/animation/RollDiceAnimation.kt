package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.clickableWithoutRipple
import org.walks.gamecopilot.ui.widget.DiceFace
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_dice_roll
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 *  Created by Wing at 16:41 on 2025/3/28
 *
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DiceAnimation(modifier: Modifier = Modifier, range: IntRange = 1..6,isRollingDice:Boolean) {
    var isRolling by remember { mutableStateOf(false) }
    var currentValue by remember { mutableIntStateOf(range.random()) }
    var displayValue by remember { mutableIntStateOf(currentValue) }
    val rotationX = remember { Animatable(0f) } // 改为 val
    val rotationY = remember { Animatable(0f) } // 改为 val
    val liftProgress = remember { Animatable(0f) }
    val jumpDistancePx = with(LocalDensity.current) { 26.dp.toPx() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRollingDice){
        if(isRollingDice)
        currentValue = range.random()
    }
    LaunchedEffect(currentValue) {
        scope.launch {
            isRolling = true
            // 并行执行动画和震动
            coroutineScope {
                // 启动震动协程
                launch {
                    val duration = 1200 // 动画总时长
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

                        delay(16)
                    }
                }

                // 执行骰子动画
                launch {
                    // 滚动过程中快速切换点数，增强随机感
                    repeat(10) {
                        displayValue = range.random()
                        delay(85)
                    }
                }
                rollDiceAnimation(rotationX, rotationY, liftProgress)
            }
            rotationX.snapTo(0f) // 重置旋转角度
            rotationY.snapTo(0f)
            liftProgress.snapTo(0f)
            isRolling = false
            displayValue = currentValue
        }
    }
    Box(
        modifier = modifier
            .clickableWithoutRipple {
                currentValue = range.random()
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(64.dp)
                .height(14.dp)
                .graphicsLayer {
                    // 骰子抬升时阴影缩小并变淡，营造离地感
                    val shadowT = 1f - liftProgress.value
                    scaleX = 0.7f + shadowT * 0.45f
                    scaleY = 0.6f + shadowT * 0.25f
                    alpha = 0.08f + shadowT * 0.26f
                }
                .background(Color.Black, CircleShape)
        )
        Box(
            modifier = Modifier
                .graphicsLayer {
                    this.rotationX = rotationX.value
                    this.rotationY = rotationY.value
                    rotationZ = rotationY.value * 0.18f
                    transformOrigin = TransformOrigin.Center
                    cameraDistance = 10f * density
                    translationY = -jumpDistancePx * liftProgress.value
                    // 抛起时轻微拉伸/压缩，增强动势
                    scaleX = 1f + liftProgress.value * 0.02f
                    scaleY = 1f - liftProgress.value * 0.04f
                }
        ) {
            DiceFace(value = displayValue)
        }
    }
}


@OptIn(ExperimentalTime::class)
@Composable
fun DiceAnimationImage(modifier: Modifier = Modifier, range: IntRange = 1..6) {
    var isRolling by remember { mutableStateOf(false) }
    var currentValue by remember { mutableIntStateOf(1) }
    var displayValue by remember { mutableIntStateOf(currentValue) }
    val rotationX = remember { Animatable(0f) } // 改为 val
    val rotationY = remember { Animatable(0f) } // 改为 val
    val liftProgress = remember { Animatable(0f) }
    val jumpDistancePx = with(LocalDensity.current) { 22.dp.toPx() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentValue) {
        scope.launch {
            isRolling = true
            // 并行执行动画和震动
            coroutineScope {
                // 启动震动协程
                launch {
                    val duration = 1200 // 动画总时长
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

                        delay(16)
                    }
                }

                // 执行骰子动画
                rollDiceAnimation(rotationX, rotationY, liftProgress) // 确保调用动画方法
            }
            rotationX.snapTo(0f)
            rotationY.snapTo(0f)
            liftProgress.snapTo(0f)
            isRolling = false
            displayValue = currentValue
        }
    }
    Box(
    modifier = modifier
        .clickable(enabled = !isRolling) {
            currentValue = range.random()
        }
        .graphicsLayer {
            // 核心旋转参数
            this.rotationX = rotationX.value
            this.rotationY = rotationY.value
            rotationZ = rotationY.value * 0.1f
            // 中心点设置
            transformOrigin = TransformOrigin.Center
            cameraDistance = 12 * density // 透视效果
            translationY = -jumpDistancePx * liftProgress.value
        },
    contentAlignment = Alignment.Center
) {
    Image(
        painter = painterResource(Res.drawable.icon_dice_roll),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            .size(64.dp) // 建议添加固定尺寸
    )
}

}


private suspend fun rollDiceAnimation(
    rotationX: Animatable<Float, AnimationVector1D>,
    rotationY: Animatable<Float, AnimationVector1D>,
    liftProgress: Animatable<Float, AnimationVector1D>
) {
    // 使用协程并行执行动画
    coroutineScope {
        launch {
            rotationX.animateTo(
                targetValue = (360 * 4..360 * 6).random() // 4-6圈
                    .toFloat()
                    .avoidCriticalAngle(), // 应用角度修正
                animationSpec = tween(1450, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rotationY.animateTo(
                targetValue = (360 * 3..360 * 5).random() // 3-5圈
                    .toFloat()
                    .avoidCriticalAngle(),
                animationSpec = tween(1120, easing = FastOutSlowInEasing)
            )
        }
        launch {
            // 更真实的抛起+衰减弹跳
            liftProgress.snapTo(0f)
            liftProgress.animateTo(1f, animationSpec = tween(260, easing = FastOutSlowInEasing))
            liftProgress.animateTo(0.16f, animationSpec = tween(240, easing = FastOutSlowInEasing))
            liftProgress.animateTo(0.58f, animationSpec = tween(180, easing = FastOutSlowInEasing))
            liftProgress.animateTo(0.08f, animationSpec = tween(170, easing = FastOutSlowInEasing))
            liftProgress.animateTo(0.28f, animationSpec = tween(140, easing = FastOutSlowInEasing))
            liftProgress.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
}

private fun Float.avoidCriticalAngle(): Float {
    val normalized = this % 360f
    return when {
        normalized in 80f..100f -> this + 45f // 避开90度临界点
        normalized in 170f..190f -> this + 45f // 避开180度
        normalized in 260f..280f -> this + 45f // 避开270度
        else -> this
    }
}
