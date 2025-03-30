package org.walks.gamecopilot.ui.widget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 *  Created by Wing at 17:55 on 2025/3/28
 *
 */
@Composable
fun FlipCard(
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit,
    isFlipped: Boolean,
    onFlipComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    //val animationSpec = tween(600, easing = FastOutSlowInEasing)

    val elevation by animateDpAsState(
        if (pressed) 4.dp else 8.dp,
        animationSpec = tween(200)
    )

    // modifier.shadow(elevation, RoundedCornerShape(16.dp))


    val rotation = remember { Animatable(0f) }
    val cameraDistance = with(LocalDensity.current) { 12.dp.toPx() }

    LaunchedEffect(isFlipped) {
        rotation.animateTo(
            targetValue = if (isFlipped) 180f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
                // 新增可见性阈值
                visibilityThreshold = 0.1f
            )
        )
        onFlipComplete()
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp))
            // 将样式应用在graphicsLayer之前 ↓↓↓

            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                rotationY = rotation.value
                this.cameraDistance = cameraDistance
            }

    ) {
        // 正面内容
        CardContent(
            content = frontContent,
            visible = rotation.value <= 90f,
            rotation = rotation.value
        )

        // 背面内容
        CardContent(
            content = backContent,
            visible = rotation.value > 90f,
            rotation = rotation.value - 180f, // 动态计算旋转角度
            isBack = true // 新增标识
        )
    }
}

@Composable
private fun CardContent(
    content: @Composable () -> Unit,
    visible: Boolean,
    rotation: Float,
    isBack: Boolean = false
) {
    Box(
        modifier = CardStyle( Modifier // 移除CardStyle的重复应用
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation
                alpha = if (visible) 1f else 0f
                scaleX = if (isBack) -1f else 1f
            })
    ) {
        content()
    }
}

@Composable
fun CardStyle(modifier: Modifier = Modifier) = modifier
    .clip(RoundedCornerShape(16.dp))
    .background(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
    .border(
        width = 2.dp,
        color = MaterialTheme.colorScheme.outline,
        shape = RoundedCornerShape(16.dp)
    )
    // 增加动画过渡 ↓↓↓
