package org.walks.gamecopilot.ui.page.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.KeepScreenOn
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
fun PrepairPage(playerNum: Int, isOwner: Boolean, startGame: () -> Unit) {
    val design = LocalAppDesign.current
    var tips by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "startButtonScale"
    )

    val canStart = playerNum >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(design.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(visible = tips.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(design.cornerRadius.md)
            ) {
                Text(
                    tips,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(design.spacing.lg)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .scale(scale)
                .shadow(
                    elevation = if (canStart) design.elevation.lg else design.elevation.none,
                    shape = RoundedCornerShape(design.cornerRadius.xxxl)
                )
                .clip(RoundedCornerShape(design.cornerRadius.xxxl))
                .background(
                    brush = if (canStart) {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
                .border(
                    width = if (canStart) 2.dp else 1.dp,
                    color = if (canStart)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(design.cornerRadius.xxxl)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = canStart
                ) { startGame() }
                .padding(horizontal = design.spacing.xxl, vertical = design.spacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$playerNum",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canStart) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(design.spacing.sm))
                    Text(
                        text = "人已加入",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (canStart) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(design.spacing.md))

                if (isOwner) {
                    if (canStart) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(design.spacing.sm))
                            Text(
                                "点击开始游戏",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            "还需 ${2 - playerNum} 人可开始",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        "请等待房主开始游戏",
                        color = if (canStart) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun RoomPage(viewmodel: MainViewmodel) {
    val design = LocalAppDesign.current
    KeepScreenOn()
    var animVisible by remember { mutableStateOf(false) }
    val roomState = viewmodel.roomEntityState.collectAsState()
    val memberList = viewmodel.roomEntityState.collectAsState().value.users

    val markedMemberList = memberList?.map { user ->
        if (user.index == roomState.value.index) {
            user.copy(isMine = true)
        } else {
            user
        }
    }

    if (roomState.value.role.isNullOrBlank() || roomState.value.role == "NOROLE") {
        PrepairPage(markedMemberList?.size ?: 0, roomState.value.isRoomOwner, startGame = {
            viewmodel.handleRoomIntent(GameRoomIntent.StartGame)
        })
    } else {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(design.spacing.xxl)
        ) {
            Text(
                roomState.value.index.toString(),
                fontWeight = FontWeight.W900,
                fontSize = 199.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.End
            )
            AnimatedVisibility(
                visible = animVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                MemberList(memberList ?: listOf(), roomState.value.isRoomOwner)
            }
            FlopArea(roomState.value.assignedWord ?: "", roomState.value.updateTime)
        }

        LaunchedEffect(Unit) {
            animVisible = true
        }
    }
}

