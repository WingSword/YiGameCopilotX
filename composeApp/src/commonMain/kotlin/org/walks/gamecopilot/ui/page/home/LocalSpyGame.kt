package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
const val IDENTITY_SHOW = 1
const val IDENTITY_SHOW_ALL = 2


@Composable
fun LocalSpyGame(viewmodel: MainViewmodel) {
    var showNumberPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    var gameTimeState by remember { mutableIntStateOf(0) }
    val numberList = (4..10).map { it.toString() }
    val gameStateList = viewmodel.gameEntity.collectAsState().value.timeEntityList
    val playerNum = gameStateList.lastOrNull()?.totalPlayerNumber ?: 4
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            CommonButton("选择游玩人数 $playerNum", onClick = {
                showNumberPicker = true
            })

            if (playerNum >= 6) {
                var showSpyNumberPicker by remember { mutableStateOf(false) }
                val maxSpyList = (1..playerNum / 3).map { "$it" }
                val spyNumber = gameStateList.lastOrNull()?.spyNum ?: 1
                val blackNum= gameStateList.lastOrNull()?.blackNum ?: 0
                Spacer(Modifier.width(8.dp))
                CommonButton("选择卧底人数 $spyNumber", onClick = {
                    showSpyNumberPicker = true
                }, backColor = MaterialTheme.colorScheme.secondary)

                WeSingleColumnPicker(
                    visible = showSpyNumberPicker,
                    title = "选择卧底人数",
                    range = maxSpyList,
                    onCancel = { showSpyNumberPicker = false },
                    onChange = {
                        gameTimeState++
                        viewmodel.handleIntent(GameIntent.RefreshSpyNumber(maxSpyList[it].toInt()))
                    },
                    value = spyNumber
                )
            }
        }

        if (gameStateList.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "选择查看编号", color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "重新开始",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.rotate(rotation.value).clickable {
                        viewmodel.handleIntent(GameIntent.StartGame)
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
            viewmodel.handleIntent(GameIntent.RefreshPlayerNumber(numberList[it].toInt()))
        },
        value = numberList.indexOf(playerNum.toString())
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGreetingView(key: Int, gameState: LocalSpyEntity) {
    var currentSelectPlayer by remember { mutableIntStateOf(1) }
    val gameWordDismiss = remember { mutableStateOf(IDENTITY_DISMISS) }
    val realGameState by remember(key) {
        derivedStateOf { gameState }
    }
    var watchedTimeList = remember(key) {
        mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
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
        }) { currentSelect ->
        gameWordDismiss.value = IDENTITY_DISMISS
        currentSelectPlayer = currentSelect
    }

    Spacer(Modifier.height(8.dp))
    if ((watchedTimeList.filter { it > 0 }).size >= gameState.totalPlayerNumber) {
        Text(
            text = "长按公布所有身份",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.combinedClickable(onLongClick = {
                gameWordDismiss.value = IDENTITY_SHOW_ALL
            }) {

            })
    }
    Spacer(Modifier.height(8.dp))
    LocalSpyIdentityCard(
        gameState = gameState,
        identityDismiss = gameWordDismiss.value,
        currentSelectPlayer = currentSelectPlayer,
        onLongClick = {
            watchedTimeList[currentSelectPlayer] += 1
            gameWordDismiss.value = IDENTITY_SHOW
        },
        onTap = {
            if (gameWordDismiss.value == IDENTITY_SHOW)
                gameWordDismiss.value = IDENTITY_DISMISS
        })

}

@Composable
fun LocalPlayerSelectArea(
    playerNum: Int = 4,
    getWatchedTime: (Int) -> (Int),
    badge: @Composable (Int) -> Unit = {},
    onClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(playerNum) { pos ->
            val currentPlayer = pos + 1
            val watchedTime = getWatchedTime.invoke(currentPlayer)
            Box(
                modifier = Modifier.padding(top = 8.dp), contentAlignment = Alignment.TopCenter
            ) {
                TextButton(
                    onClick = {
                        onClick(currentPlayer)
                    },
                    shape = CircleShape,
                    border = BorderStroke(2.dp, color = Color.LightGray),
                    colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = if (watchedTime >= 1) Color.LightGray else MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
                        disabledContentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = (currentPlayer).toString())
                }
                badge(currentPlayer)

            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalSpyIdentityCard(
    gameState: LocalSpyEntity,
    identityDismiss: Int,
    currentSelectPlayer: Int = 1,
    onLongClick: () -> Unit,
    onTap: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.height(160.dp).width(120.dp).clip(RoundedCornerShape(20.dp)).border(
            width = 4.dp,
            color = MorandiColorList[(0..7).random()],
            shape = RoundedCornerShape(20.dp)
        ).background(
            if (identityDismiss == IDENTITY_DISMISS) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondaryContainer
        ).shadow(1.dp).combinedClickable(onLongClick = {
            onLongClick()
        }) {
            onTap()
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                if (identityDismiss == IDENTITY_DISMISS) "$currentSelectPlayer 号\n长按查看身份词"
                else gameState.optIdentity(currentSelectPlayer),
                textAlign = TextAlign.Center,
                color = if (identityDismiss == IDENTITY_DISMISS) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            if (identityDismiss == IDENTITY_SHOW) {
                Text(
                    "再次点击关闭身份牌",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

    }
}