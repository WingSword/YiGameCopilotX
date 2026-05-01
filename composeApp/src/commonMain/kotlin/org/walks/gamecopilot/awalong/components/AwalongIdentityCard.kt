package org.walks.gamecopilot.awalong.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yi.yigamecopilot.android.theme.AiLv
import com.yi.yigamecopilot.android.theme.Chi
import kotlinx.coroutines.launch
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.GOOD_PERSON
import kotlin.math.abs

@Composable
fun AwalongIdentityCard(
    playerNumber: Int,
    role: AwalongRole,
    nickname: String,
    allRoles: List<AwalongRole>,
    allNicknames: List<String>,
    onClose: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var showIdentity by remember(role.title, nickname) { mutableStateOf(false) }
    var hasSwitched by remember(role.title, nickname) { mutableStateOf(false) }
    var dragOffset by remember(role.title, nickname) { mutableFloatStateOf(0f) }
    var isSwitchAnimating by remember(role.title, nickname) { mutableStateOf(false) }
    val cardSlide = remember(role.title, nickname) { Animatable(0f) }
    val enterScale = remember(role.title, nickname) { Animatable(0.84f) }
    val enterOffsetY = remember(role.title, nickname) { Animatable(100f) }
    val enterRotationX = remember(role.title, nickname) { Animatable(-12f) }

    LaunchedEffect(role.title, nickname) {
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
            modifier = Modifier
                .height(470.dp)
                .width(300.dp)
                .graphicsLayer {
                    translationX = cardSlide.value + dragOffset
                    translationY = enterOffsetY.value
                    scaleX = enterScale.value
                    scaleY = enterScale.value
                    rotationX = enterRotationX.value
                    cameraDistance = 18f * density
                }
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), RectangleShape)
                .pointerInput(playerNumber, role.title) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragOffset = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!isSwitchAnimating) {
                                dragOffset = (dragOffset + dragAmount).coerceIn(-220f, 220f)
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
                                            targetValue = direction * 360f,
                                            animationSpec = tween(115, easing = FastOutSlowInEasing)
                                        )
                                        showIdentity = !showIdentity
                                        hasSwitched = true
                                        cardSlide.snapTo(-direction * 300f)
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
                },
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!showIdentity) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PLAYER $playerNumber",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (nickname.isNotEmpty()) nickname else "玩家$playerNumber",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "身份已隐藏",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text = role.title,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            fontSize = 36.sp,
                            color = if (role.roleType == GOOD_PERSON) AiLv.copy(0.95f) else Chi.copy(
                                0.95f
                            )
                        )
                        Text(
                            text = "[${if (role.roleType == GOOD_PERSON) "好人" else "坏人"}阵营]",
                            color = if (role.roleType == GOOD_PERSON) AiLv else Chi,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            role.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )

                        val checkList = role.checkSkills(allRoles)
                        if (checkList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "你能看到的其他玩家：",
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            LazyColumn(modifier = Modifier.height(150.dp)) {
                                items(checkList.keys.toList()) { playerIndex ->
                                    val targetNickname =
                                        if (playerIndex < allNicknames.size) allNicknames[playerIndex] else ""
                                    val showText =
                                        "[${playerIndex + 1}${if (targetNickname.isNotEmpty()) " ($targetNickname)" else ""} 的身份 ${if (role != AwalongRole.PAIXIWEIWEIER) checkList[playerIndex]?.title else "可能是梅林"}]"
                                    Text(
                                        text = showText,
                                        modifier = Modifier.padding(vertical = 3.dp).fillMaxWidth(),
                                        color = if (role == AwalongRole.PAIXIWEIWEIER || checkList[playerIndex]?.roleType == GOOD_PERSON) AiLv else Chi,
                                        textAlign = TextAlign.Start,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (showIdentity) 1f else 0.35f)
                                .height(6.dp)
                                .background(Color(0xFFF2C72B))
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
