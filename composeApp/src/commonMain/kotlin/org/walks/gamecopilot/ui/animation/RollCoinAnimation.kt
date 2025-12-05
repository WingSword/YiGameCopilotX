package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.random.Random


/**
 * 硬币翻转动画组件
 * @param onFlipComplete 翻转完成回调，返回布尔值表示正面(true)或反面(false)
 * @param modifier 修饰符
 */
@Composable
fun RollCoinAnimation(
    onFlipComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHeads = remember { Random.nextBoolean() }
    val rotationY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            flipCoinAnimation(rotationY)
            onFlipComplete(isHeads)
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
        CoinFace(isHeads = isHeads)
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

    // 缓慢停止到最终位置
    rotationY.animateTo(
        targetValue = rotationY.value % 360f,
        animationSpec = tween(200)
    )
}

/**
 * 硬币面组件
 */
@Composable
fun CoinFace(isHeads: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 根据是否是正面显示不同的颜色
                    if (isHeads) {
                        // 正面 - 金色
                        this.alpha = 1f
                    } else {
                        // 反面 - 银色
                        this.alpha = 0.8f
                    }
                }
        ) {
            // 硬币基本样式
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
            ) {
                // 硬币主体
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    // 正面和反面的不同样式
                    if (isHeads) {
                        // 正面样式（金色）
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            // 可以添加正面图案或文字
                        }
                    } else {
                        // 反面样式（银色）
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            // 可以添加反面图案或文字
                        }
                    }
                }
            }
        }
    }
}