package org.walks.gamecopilot.ui.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.walks.gamecopilot.PlatformHelper
import kotlin.math.abs

/** 身份卡片通用配色（主题感知） */
@Composable
fun rememberIdentityCardColors(): IdentityCardColors {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        IdentityCardColors(
            cardBackground = scheme.surface,
            border = scheme.outline,
            divider = scheme.outline,
            accent = scheme.primary,
            sideBar = scheme.outline,
            progressBg = scheme.outline
        )
    }
}

data class IdentityCardColors(
    val cardBackground: Color,
    val border: Color,
    val divider: Color,
    val accent: Color,
    val sideBar: Color,
    val progressBg: Color
)

/**
 * 可滑动切换身份的基础卡片壳
 * 供阿瓦隆、谁是卧底等游戏共用：动画、滑动手势、关闭按钮、进度条、侧边线
 *
 * @param resetKey 重置标识，切换玩家时重置状态
 * @param cardWidth 卡片宽度
 * @param cardHeight 卡片高度
 * @param dragLimit 拖拽上限（左右）
 * @param onClose 关闭回调，切换至少一次后显示关闭按钮
 * @param hiddenContent 未揭示时内容
 * @param visibleContent 已揭示时内容
 * @param showProgressBar 是否显示底部进度条
 * @param showSideBorders 是否显示左右侧边线
 */
@Composable
fun SwipeableIdentityCardShell(
    resetKey: Any,
    cardWidth: Dp,
    cardHeight: Dp,
    modifier: Modifier = Modifier,
    dragLimit: Float = 220f,
    rotateTarget: Float = 360f,
    snapOffset: Float = 300f,
    onClose: () -> Unit = {},
    showProgressBar: Boolean = true,
    showSideBorders: Boolean = true,
    hiddenContent: @Composable () -> Unit,
    visibleContent: @Composable () -> Unit,
    colors: IdentityCardColors = rememberIdentityCardColors()
) {
    val scope = rememberCoroutineScope()
    var showIdentity by remember(resetKey) { mutableStateOf(false) }
    var hasSwitched by remember(resetKey) { mutableStateOf(false) }
    var dragOffset by remember(resetKey) { mutableFloatStateOf(0f) }
    var isSwitchAnimating by remember(resetKey) { mutableStateOf(false) }
    val cardSlide = remember(resetKey) { Animatable(0f) }
    val enterScale = remember(resetKey) { Animatable(0.84f) }
    val enterOffsetY = remember(resetKey) { Animatable(100f) }
    val enterRotationX = remember(resetKey) { Animatable(-12f) }

    LaunchedEffect(resetKey) {
        hasSwitched = false
        isSwitchAnimating = false
        dragOffset = 0f
        cardSlide.snapTo(0f)
        enterScale.snapTo(0.84f)
        enterOffsetY.snapTo(100f)
        enterRotationX.snapTo(-12f)
        launch {
            enterScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            enterOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            enterRotationX.animateTo(
                targetValue = 0f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = modifier
                .height(cardHeight)
                .width(cardWidth)
                .graphicsLayer {
                    translationX = cardSlide.value + dragOffset
                    translationY = enterOffsetY.value
                    scaleX = enterScale.value
                    scaleY = enterScale.value
                    rotationX = enterRotationX.value
                    cameraDistance = 18f * density
                }
                .pointerInput(resetKey) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragOffset = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!isSwitchAnimating) {
                                dragOffset = (dragOffset + dragAmount).coerceIn(-dragLimit, dragLimit)
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                cardSlide.snapTo(dragOffset)
                                dragOffset = 0f
                                cardSlide.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onDragEnd = {
                            if (!isSwitchAnimating && abs(dragOffset) > 56f) {
                                val direction = if (dragOffset > 0f) 1 else -1
                                scope.launch {
                                    try {
                                        isSwitchAnimating = true
                                        cardSlide.snapTo(dragOffset)
                                        dragOffset = 0f
                                        cardSlide.animateTo(
                                            targetValue = direction * rotateTarget,
                                            animationSpec = tween(115, easing = FastOutSlowInEasing)
                                        )
                                        PlatformHelper.getInstance().vibrateMethod()
                                        showIdentity = !showIdentity
                                        hasSwitched = true
                                        cardSlide.snapTo(-direction * snapOffset)
                                        cardSlide.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    } finally {
                                        isSwitchAnimating = false
                                    }
                                }
                            } else {
                                scope.launch {
                                    cardSlide.snapTo(dragOffset)
                                    dragOffset = 0f
                                    cardSlide.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
                .border(
                    BorderStroke(2.dp, colors.border),
                    RectangleShape
                ),
            shape = RectangleShape,
            color = colors.cardBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!showIdentity) hiddenContent() else visibleContent()

                if (showProgressBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(colors.progressBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (showIdentity) 1f else 0.35f)
                                .height(6.dp)
                                .background(colors.accent)
                        )
                    }
                }

                if (showSideBorders) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(colors.sideBar)
                        )
                        Box(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(colors.sideBar)
                        )
                    }
                }

                if (hasSwitched) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 底部滑动提示
        if (!hasSwitched) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Swipe,
                    contentDescription = null,
                    tint = colors.accent.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "左右滑动翻看",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent.copy(alpha = 0.6f)
                )
            }
        }
    }
}
