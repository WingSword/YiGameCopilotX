package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yi.yigamecopilot.android.theme.MorandiColorList
import kotlinx.coroutines.launch
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.badge.WeBadge
import org.walks.gamecopilot.ui.button.CommonButton
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

/**
 *  Created by Wing at 20:53 on 2025/3/25
 *  本地卧底游戏
 */

const val IDENTITY_DISMISS = 0
const val IDENTITY_SHOW = -1
const val IDENTITY_SHOW_ONE = -2
const val IDENTITY_SHOW_ALL = -3


@Composable
fun LocalSpyGame(viewmodel: MainViewmodel) {
    var showNumberPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    var gameTimeState by remember { mutableIntStateOf(0) }
    val numberList = (4..12).map { it.toString() }
    val gameStateList = viewmodel.gameEntity.collectAsState().value.timeEntityList
    val playerNum = gameStateList.lastOrNull()?.totalPlayerNumber ?: 4
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            CommonButton("选择游玩人数 $playerNum", onClick = {
                showNumberPicker = true
            })
        }
        if (playerNum >= 6) {
            var showSpyNumberPicker by remember { mutableStateOf(0) }
            val maxSpyList = (1..playerNum / 3).map { "$it" }

            val spyNumber = gameStateList.lastOrNull()?.spyNum ?: 1
            val blackNum = gameStateList.lastOrNull()?.blackNum ?: 0
            Spacer(Modifier.height(8.dp))
            Row {
                CommonButton("选择卧底人数 $spyNumber", onClick = {
                    showSpyNumberPicker = 1
                }, backColor = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.size(8.dp))
                CommonButton("空白卡数量 $blackNum", onClick = {
                    showSpyNumberPicker = 2
                }, backColor = MaterialTheme.colorScheme.secondary)

            }
            WeSingleColumnPicker(
                visible = showSpyNumberPicker != 0,
                title = if (showSpyNumberPicker == 1) "选择卧底人数" else "选择空白卡片数量",
                range = if (showSpyNumberPicker == 1) maxSpyList else (0..(gameStateList.lastOrNull()?.spyNum
                    ?: 1)).map { "$it" },
                onCancel = { showSpyNumberPicker = 0 },
                onChange = {
                    gameTimeState++
                    when (showSpyNumberPicker) {
                        1 -> {
                            viewmodel.handleLocalGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = maxSpyList[it].toInt(),
                                    blackNum = if (blackNum <= spyNumber) blackNum else 0
                                )
                            )
                        }

                        2 -> {
                            viewmodel.handleLocalGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = spyNumber,
                                    blackNum = it
                                )
                            )
                        }
                    }

                },
                value = if (showSpyNumberPicker == 1) gameStateList.lastOrNull()?.spyNum
                    ?: 1 else gameStateList.lastOrNull()?.blackNum ?: 0
            )

        }

        if (gameStateList.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "选择编号长按查看", color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "重新开始",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.rotate(rotation.value).clickable {
                        viewmodel.handleLocalGameIntent(GameIntent.StartGame)
                        gameTimeState++
                        scope.launch {
                            rotation.animateTo(
                                targetValue = 360f,
                                animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                            )
                            rotation.snapTo(0f) // 重置角度准备下次旋转
                        }
                    })
            }
            GameGreetingView(
                key = gameTimeState,
                gameStateList.last(),
            )
        }
    }


    WeSingleColumnPicker(
        visible = showNumberPicker,
        title = "选择游玩人数",
        range = numberList,
        onCancel = { showNumberPicker = false },
        onChange = {
            gameTimeState++
            viewmodel.handleLocalGameIntent(GameIntent.RefreshPlayerNumber(numberList[it].toInt()))
        },
        value = numberList.indexOf(playerNum.toString())
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGreetingView(key: Int, gameState: LocalSpyEntity) {
    var showIdentityCard by remember { mutableStateOf(false) }
    var currentSelectPlayer by remember { mutableIntStateOf(1) }
    val gameWordDismiss = remember { mutableStateOf(IDENTITY_DISMISS) }
    val realGameState by remember(key) {
        derivedStateOf { gameState }
    }
    var watchedTimeList = remember(key) {
        mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    LaunchedEffect(key1 = key) {
        gameWordDismiss.value = IDENTITY_DISMISS
        watchedTimeList.map {
            watchedTimeList[it] = 0
        }
    }

    LocalPlayerSelectArea(
        playerNum = realGameState.totalPlayerNumber,
        getWatchedTime = { watchedTimeList[it] },
        badge = { currentSelect ->
            if (gameWordDismiss.value == IDENTITY_SHOW_ALL) {
                WeBadge(
                    realGameState.optIdentity(currentSelect),
                    size = 12.dp,
                    fontSize = 10,
                    color = if (realGameState.spies.contains(currentSelect)) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primaryContainer
                )
            } else if ((watchedTimeList[currentSelect]) > 1) {
                WeBadge("查看" + watchedTimeList[currentSelect] + "次", size = 12.dp, fontSize = 10)
            }
        }, onClick = { _ ->
            //no need onClick currently
        }) { currentSelect ->
        currentSelectPlayer = currentSelect
        watchedTimeList[currentSelect] += 1
        gameWordDismiss.value = IDENTITY_SHOW
    }

    Spacer(Modifier.height(8.dp))
    if ((watchedTimeList.filter { it > 0 }).size >= gameState.totalPlayerNumber) {
        Text(
            text = "长按公布所有身份",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.combinedClickable(onLongClick = {
                gameWordDismiss.value = IDENTITY_SHOW_ALL
            }) {
                //no need to do anything
            })
    }
    Spacer(Modifier.height(8.dp))
    AnimatedVisibility(gameWordDismiss.value == IDENTITY_SHOW || gameWordDismiss.value == IDENTITY_SHOW_ONE) {
        Dialog(onDismissRequest = { gameWordDismiss.value = IDENTITY_DISMISS }) {
            LocalSpyIdentityCard(
                gameState = gameState,
                identityDismiss = gameWordDismiss.value,
                currentSelectPlayer = currentSelectPlayer,
                onTap = {
                    if (gameWordDismiss.value == IDENTITY_SHOW_ONE) {
                        gameWordDismiss.value = IDENTITY_DISMISS
                    } else {
                        gameWordDismiss.value = IDENTITY_SHOW_ONE
                    }
                })
        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlayerSelectArea(
    playerNum: Int = 4,
    getWatchedTime: (Int) -> Int,
    badge: @Composable (Int) -> Unit = {},
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(playerNum) { pos ->
            val currentPlayer = pos + 1
            val watchedTime = getWatchedTime(currentPlayer)
            val interactionState = remember { mutableStateOf<PressInteraction?>(null) }
            val borderColor by animateColorAsState(
                targetValue = when (interactionState.value) {
                    is PressInteraction.Press -> MaterialTheme.colorScheme.primary
                    else -> Color.LightGray
                },
                animationSpec = tween(200)
            )
            Box(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(66.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (interactionState.value) {
                                is PressInteraction.Press -> MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.1f
                                )

                                else -> if (watchedTime >= 1) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.primary
                            },
                            shape = CircleShape
                        )
                        .border(
                            BorderStroke(2.dp, borderColor),
                            shape = CircleShape
                        )
                        .combinedClickable(
                            onLongClick = {
                                onLongClick(currentPlayer)
                            },
                            onClick = {
                                onClick(currentPlayer)
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = currentPlayer.toString(),
                        color = if (watchedTime >= 1) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                badge(currentPlayer)
            }
        }
    }
}


@Composable
fun LocalSpyIdentityCard(
    gameState: LocalSpyEntity,
    identityDismiss: Int,
    currentSelectPlayer: Int = 1,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier.height(160.dp).width(120.dp).clip(RoundedCornerShape(20.dp))
            .clickable {
                onTap()
            }.background(
                color = if (identityDismiss == IDENTITY_SHOW_ONE) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer,

                ).border(
                BorderStroke(
                    width = 4.dp,
                    color = MorandiColorList[(0..7).random()]
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = currentSelectPlayer.toString(),
            modifier = Modifier.rotate(-30f),
            fontSize = 90.sp,
            fontWeight = FontWeight.W900,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
            textAlign = TextAlign.Right
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                if (identityDismiss == IDENTITY_SHOW_ONE) gameState.optIdentity(currentSelectPlayer)
                else if (gameState.isSpy(currentSelectPlayer)) "卧底" else "平民",
                textAlign = TextAlign.Center,
                color = if (gameState.isSpy(currentSelectPlayer)) MaterialTheme.colorScheme.error else if (identityDismiss == IDENTITY_SHOW_ONE) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                if (identityDismiss == IDENTITY_SHOW_ONE) "再次点击关闭身份牌" else "点击卡片查看身份词",
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))

        }

    }

}