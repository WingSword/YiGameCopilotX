package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.walks.gamecopilot.ui.widget.DiceFace

/**
 *  Created by Wing at 16:41 on 2025/3/28
 *
 */
@Composable
fun DiceAnimation(modifier: Modifier = Modifier,value:Int=(1..6).random()) {
    var isRolling by remember { mutableStateOf(false) }
    var currentValue by remember { mutableIntStateOf(1) }
    val rotationX = remember { Animatable(0f) } // 改为 val
    val rotationY = remember { Animatable(0f) } // 改为 val
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clickable(enabled = !isRolling) {
                scope.launch {
                    isRolling = true
                    rollDiceAnimation(rotationX, rotationY)
                    currentValue = value
                    rotationX.snapTo(0f) // 重置旋转角度
                    rotationY.snapTo(0f)
                    isRolling = false
                }
            }
            .graphicsLayer {
                this.rotationX = (rotationX.value)
                this.rotationY = (rotationY.value)
                rotationZ = rotationY.value * 0.3f // 增加Z轴旋转增强立体感
                transformOrigin = TransformOrigin.Center
                cameraDistance = 8f * density
                shadowElevation = if (isRolling) 16.dp.toPx() else 8.dp.toPx()

            }
    ) {
        DiceFace(value = currentValue)
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
                targetValue = (1080..1440).random().toFloat(),
                animationSpec = tween(2000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rotationY.animateTo(
                targetValue = (720..1080).random().toFloat(),
                animationSpec = tween(1400, easing = FastOutSlowInEasing)
            )
        }
    }
}
