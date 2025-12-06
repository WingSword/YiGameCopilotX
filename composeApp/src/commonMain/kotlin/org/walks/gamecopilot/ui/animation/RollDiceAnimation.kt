package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.clickableWithoutRipple
import org.walks.gamecopilot.ui.widget.DiceFace
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_dice_roll

/**
 *  Created by Wing at 16:41 on 2025/3/28
 *
 */
@Composable
fun DiceAnimation(modifier: Modifier = Modifier, range: IntRange = 1..6,isRollingDice:Boolean) {
    var isRolling by remember { mutableStateOf(false) }
    var currentValue by remember { mutableIntStateOf(range.random()) }
    var displayValue by remember { mutableIntStateOf(currentValue) }
    val rotationX = remember { Animatable(0f) } // 改为 val
    val rotationY = remember { Animatable(0f) } // 改为 val
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRollingDice){
        if(isRollingDice)
        currentValue = range.random()
    }
    LaunchedEffect(currentValue) {
        scope.launch {
            isRolling = true
            rollDiceAnimation(rotationX, rotationY)
            rotationX.snapTo(0f) // 重置旋转角度
            rotationY.snapTo(0f)
            isRolling = false
            displayValue = currentValue
        }
    }
    Box(
        modifier = modifier
            .clickableWithoutRipple {
                currentValue = range.random()
            }
            .graphicsLayer {
                this.rotationX = (rotationX.value)
                this.rotationY = (rotationY.value)
                rotationZ = rotationY.value * 0.3f // 增加Z轴旋转增强立体感
                transformOrigin = TransformOrigin.Center
                cameraDistance = 8f * density
                // shadowElevation = if (isRolling) 16.dp.toPx() else 8.dp.toPx()
            }
    ) {
        DiceFace(value = displayValue)
    }
}


@Composable
fun DiceAnimationImage(modifier: Modifier = Modifier, range: IntRange = 1..6) {
    var isRolling by remember { mutableStateOf(false) }
    var currentValue by remember { mutableIntStateOf(1) }
    var displayValue by remember { mutableIntStateOf(currentValue) }
    val rotationX = remember { Animatable(0f) } // 改为 val
    val rotationY = remember { Animatable(0f) } // 改为 val
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentValue) {
        scope.launch {
            isRolling = true
            rollDiceAnimation(rotationX, rotationY) // 确保调用动画方法
            rotationX.snapTo(0f)
            rotationY.snapTo(0f)
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
    rotationY: Animatable<Float, AnimationVector1D>
) {
    // 使用协程并行执行动画
    coroutineScope {
        launch {
            rotationX.animateTo(
                targetValue = (360 * 3..360 * 5).random() // 3-5圈
                    .toFloat()
                    .avoidCriticalAngle(), // 应用角度修正
                animationSpec = tween(1200, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rotationY.animateTo(
                targetValue = (360 * 2..360 * 4).random() // 2-4圈
                    .toFloat()
                    .avoidCriticalAngle(),
                animationSpec = tween(800, easing = FastOutSlowInEasing)
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
