package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random


/**
 * 硬币翻转动画组件
 * @param onFlipComplete 翻转完成回调，返回布尔值表示正面(true)或反面(false)
 * @param modifier 修饰符
 * @param frontText 硬币正面文字
 * @param backText 硬币反面文字
 */
@Composable
fun RollCoinAnimation(
    onFlipComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isRolling: Boolean = false,
    frontText: String = "正",
    backText: String = "反"
) {
    val isHeads = remember { Random.nextBoolean() }
    val rotationY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRolling) {
        if (isRolling) {
            scope.launch {
                flipCoinAnimation(rotationY)
                onFlipComplete(isHeads)
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                this.rotationY = rotationY.value
                transformOrigin = TransformOrigin.Center
                cameraDistance = 8f * density
            }
    ) {
        // 正面（0度）
        CoinFace(
            isHeads = true,
            text = frontText,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 当旋转超过90度时隐藏正面
                    alpha = if (rotationY.value <= 90f || rotationY.value >= 270f) 1f else 0f
                }
        )

        // 反面（180度）
        CoinFace(
            isHeads = false,
            text = backText,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 当旋转在90-270度之间时显示反面
                    alpha = if (rotationY.value > 90f && rotationY.value < 270f) 1f else 0f
                    // 反面硬币需要初始旋转180度
                    this.rotationY = 180f
                }
        )
    }
}

/**
 * 硬币翻转动画
 */
private suspend fun flipCoinAnimation(rotationY: Animatable<Float, AnimationVector1D>) {
    // 快速翻转2-3圈
    rotationY.animateTo(
        targetValue = (720..1080).random().toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    // 计算最终角度：确保回正到0度或180度
    val currentAngle = rotationY.value % 360f
    val targetAngle = if (currentAngle < 180f) {
        if (currentAngle < 90f) 0f else 180f
    } else {
        if (currentAngle < 270f) 180f else 0f
    }

    // 缓慢停止到最终位置
    rotationY.animateTo(
        targetValue = targetAngle,
        animationSpec = tween(200)
    )
}

/**
 * 硬币面组件
 */
@Composable
fun CoinFace(
    isHeads: Boolean,
    text: String = if (isHeads) "正" else "反",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(8.dp, CircleShape)
            .background(
                if (isHeads) {
                    Color(0xFFFFD700) // 金色
                } else {
                    Color(0xFFC0C0C0) // 银色
                },
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHeads) Color(0xFF8B4513) else Color(0xFF2F4F4F)
        )
    }
}