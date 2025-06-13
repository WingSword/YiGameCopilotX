package org.walks.gamecopilot.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.MorandiBlue
import com.yi.yigamecopilot.android.theme.MorandiGreen
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_coin_back
import yigamecopilotx.composeapp.generated.resources.icon_coin_front
import yigamecopilotx.composeapp.generated.resources.icon_moon
import yigamecopilotx.composeapp.generated.resources.icon_sun
import kotlin.random.Random

/**
 *  Created by Wing at 17:11 on 2025/6/13
 *
 */

// 在文件中添加以下代码
private const val COIN_FLIP_DURATION = 1000 // 硬币翻转动画时长

enum class CoinFace {
    HEADS, TAILS
}

@Composable
fun CoinFlipAnimation(
    onFlipComplete: (CoinFace) -> Unit,
    modifier: Modifier = Modifier,
    roll: Boolean = false,
    frontText: String? = null,
    backText: String? = null
) {
    var isFlipping by remember { mutableStateOf(false) }
    var resultFace by remember { mutableStateOf<CoinFace?>(null) }
    LaunchedEffect(roll) {
        isFlipping = true
        resultFace = if (Random.nextBoolean()) CoinFace.HEADS else CoinFace.TAILS
    }
    val rotation = animateFloatAsState(
        targetValue = if (isFlipping) 1800f else 0f,
        animationSpec = tween(
            durationMillis = COIN_FLIP_DURATION,
            easing = LinearEasing
        ),
        label = "coinRotation"
    )

    val density = LocalDensity.current.density
    val cameraDistance = 12f * density

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            delay(COIN_FLIP_DURATION.toLong())
            isFlipping = false
            resultFace?.let { onFlipComplete(it) }
        }
    }

    Column {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clickable {
                    if (!isFlipping) {
                        isFlipping = true
                        resultFace = if (Random.nextBoolean()) CoinFace.HEADS else CoinFace.TAILS
                    }
                }
                .graphicsLayer {
                    rotationY = rotation.value
                    this.cameraDistance = cameraDistance
                },
            contentAlignment = Alignment.Center
        ) {

            val normalizedRotation = (rotation.value % 360f + 360f) % 360f

            when {
                isFlipping -> {
                    // 翻转状态：根据角度动态显示
                    if (normalizedRotation < 90f || normalizedRotation > 270f) {
                        CoinFaceContent(
                            icon = Res.drawable.icon_sun,
                            color = MorandiGreen,
                            backgroundRes = Res.drawable.icon_coin_front,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = if (rotation.value > 180f) 180f else 0f
                                }
                        )
                    }
                    if (normalizedRotation in 90f..270f) {
                        CoinFaceContent(
                            icon = Res.drawable.icon_moon,
                            color = MorandiBlue,
                            backgroundRes = Res.drawable.icon_coin_back,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = 180f
                                }
                        )
                    }
                }

                else -> {
                    // 非翻转状态：根据结果固定显示
                    if (resultFace == CoinFace.HEADS) {
                        CoinFaceContent(
                            icon = Res.drawable.icon_sun,
                            color = MorandiGreen,
                            backgroundRes = Res.drawable.icon_coin_front,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = if (rotation.value > 180f) 180f else 0f
                                }
                        )
                    } else {
                        CoinFaceContent(
                            icon = Res.drawable.icon_moon,
                            color = MorandiBlue,
                            backgroundRes = Res.drawable.icon_coin_back,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = 180f
                                }
                        )
                    }
                }
            }

        }
        AnimatedVisibility(!isFlipping) {
            Text(
                if (resultFace == CoinFace.HEADS) frontText ?: "正面" else backText ?: "反面",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

        }
    }

}

@Composable
private fun CoinFaceContent(
    icon: DrawableResource,
    color: Color,
    backgroundRes: DrawableResource,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.75f),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = Color.Unspecified
        )
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.5f), tint = Color.Unspecified
        )
    }
}
