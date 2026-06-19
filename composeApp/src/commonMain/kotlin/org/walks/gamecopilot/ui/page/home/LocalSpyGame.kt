package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.badge.WeBadge
import org.walks.gamecopilot.ui.components.common.LocalSpySwipeableIdentityCard
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

/**
 *  Created by Wing at 20:53 on 2025/3/25
 *  本地卧底游戏
 */

const val IDENTITY_DISMISS = 0
const val IDENTITY_SHOW = -1
const val IDENTITY_SHOW_ALL = -3


/**
 * 本地卧底游戏主界面组件
 * 处理玩家人数选择、卧底数量配置及游戏数据显示等功能
 *
 * @param viewmodel MainViewmodel - 游戏主视图模型，用于处理业务逻辑和数据存储
 */
@Composable
fun LocalSpyGame(viewmodel: MainViewmodel) {
    // 游戏状态控制：用于强制刷新游戏问候视图的key值
    var gameTimeState by remember { mutableIntStateOf(0) }

    // 从ViewModel获取当前游戏状态数据
    val gameEntity = viewmodel.gameEntity.collectAsState().value
    val currentGame = gameEntity.currentGame

    // 当前玩家总数，默认取当前游戏配置或4人
    val playerNum = currentGame.totalPlayerNumber
    // 状态控制：控制人数选择器弹窗的显示状态
    var showNumberPicker by remember { mutableStateOf(false) }
    // 可选的游玩人数范围（4-12人）
    val numberList = (4..16).map { it.toString() }


    LaunchedEffect(gameTimeState) {
        if (gameTimeState > 0) {
            PlatformHelper.getInstance().vibrateLongMethod()
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // 游玩人数选择区
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp))
                .fillMaxWidth().border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                ).padding(12.dp)
        ) {
            Text("选择游玩人数 $playerNum", modifier = Modifier.clickable {
                showNumberPicker = true
            }, color = MaterialTheme.colorScheme.secondary)
            // 当玩家数>=6时显示进阶配置选项

            // 状态控制：当前显示的卧底/空白卡选择器类型（0=隐藏，1=卧底，2=空白卡）
            var showSpyNumberPicker by remember { mutableStateOf(0) }
            // 最大卧底数计算（总人数的三分之一）
            val maxSpyList = (1..playerNum / 3).map { "$it" }

            // 从游戏状态获取当前配置值
            val spyNumber = currentGame.spyNum
            val blackNum = currentGame.blackNum

            Spacer(Modifier.weight(1f))

            // 卧底数量选择按钮
            Text("卧底人数 $spyNumber", modifier = Modifier.clickable {
                showSpyNumberPicker = 1
            }, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.weight(1f))
            // 空白卡数量选择按钮
            Text(text = "空白卡数量 $blackNum", modifier = Modifier.clickable {
                showSpyNumberPicker = 2
            })


            // 数值选择器组件（卧底/空白卡）
            WeSingleColumnPicker(
                visible = showSpyNumberPicker != 0,
                title = if (showSpyNumberPicker == 1) "选择卧底人数" else "选择空白卡片数量",
                range = if (showSpyNumberPicker == 1) maxSpyList else (0..currentGame.spyNum).map { "$it" },
                onCancel = { showSpyNumberPicker = 0 },
                onChange = {
                    gameTimeState++
                    when (showSpyNumberPicker) {
                        1 -> {
                            // 更新卧底数量时自动修正空白卡数值不超过新卧底数
                            viewmodel.handleGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = maxSpyList[it].toInt(),
                                    blackNum = if (blackNum <= spyNumber) blackNum else 0
                                )
                            )
                        }

                        2 -> {
                            // 直接更新空白卡数量
                            viewmodel.handleGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = spyNumber,
                                    blackNum = it
                                )
                            )
                        }
                    }
                },
                value = if (showSpyNumberPicker == 1) currentGame.spyNum else currentGame.blackNum
            )

            // 主人数选择器组件
            WeSingleColumnPicker(
                visible = showNumberPicker,
                title = "选择游玩人数",
                range = numberList,
                onCancel = { showNumberPicker = false },
                onChange = {
                    gameTimeState++
                    viewmodel.handleGameIntent(GameIntent.RefreshPlayerNumber(numberList[it].toInt()))
                },
                value = numberList.indexOf(playerNum.toString())
            )

        }

        // 游戏数据显示区
        Spacer(Modifier.height(8.dp))

        // 游戏结果显示组件，key控制强制刷新
        GameGreetingView(
            key = gameTimeState,
            gameState = currentGame,
            onStart = {
                viewmodel.handleGameIntent(GameIntent.StartGame)
                gameTimeState++
            }
        )
    }
}


/**
 * 游戏身份展示视图组件
 *
 * @param key 重组标识键，用于控制派生状态和记忆值的更新时机
 * @param gameState 当前游戏状态实体，包含玩家身份信息和游戏配置
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGreetingView(key: Int, gameState: LocalSpyEntity, onStart: () -> Unit) {
    // 当前选中玩家索引（1-based）
    var currentSelectPlayer by remember { mutableIntStateOf(1) }
    // 身份显示状态机（默认隐藏）
    val identityDisPlayState = remember { mutableStateOf(IDENTITY_DISMISS) }
    //单个玩家身份展示状态
    var playerIdentityState = remember {
        mutableStateListOf<Int>()
    }
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }

    // 派生游戏状态（根据key变化重置）
    val realGameState by remember(key) {
        derivedStateOf { gameState }
    }
    // 玩家查看次数记录列表
    var watchedTimeList = remember(key) {
        MutableList(17) { 0 }
    }

    /* 当key变化时重置游戏状态 */
    LaunchedEffect(key1 = realGameState.gameWord) {
        identityDisPlayState.value = IDENTITY_DISMISS
        watchedTimeList.indices.forEach {
            watchedTimeList[it] = 0
        }
        playerIdentityState.clear()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if ((watchedTimeList.filter { it > 0 }).size >= gameState.totalPlayerNumber
                && identityDisPlayState.value != IDENTITY_SHOW_ALL
            ) "长按公布所有身份" else "选择编号查看",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.combinedClickable(onLongClick = {
                PlatformHelper.getInstance().vibrateLongMethod()
                identityDisPlayState.value = IDENTITY_SHOW_ALL
                PlatformHelper.getInstance().vibrateLongMethod()
            }) {
                /* 点击事件占位 */
            })
        // 刷新按钮：带旋转动画的重新开始功能
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = "重新开始",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.rotate(rotation.value).clickable {
                onStart()
                scope.launch {
                    rotation.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    )
                    rotation.snapTo(0f) // 重置角度准备下次旋转
                }
            })
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        /* 玩家选择区域布局 */
        LocalPlayerSelectArea(
            playerNum = realGameState.totalPlayerNumber,
            getWatchedTime = { watchedTimeList[it] },
            badge = { currentSelect ->
                // 不同状态下的徽章显示逻辑
                if (identityDisPlayState.value == IDENTITY_SHOW_ALL) {
                    WeBadge(
                        if (identityDisPlayState.value > 0) "" else realGameState.optIdentity(
                            currentSelect
                        ),
                        size = 12.dp,
                        fontSize = 10,
                        color = if (realGameState.isSpy(currentSelect)) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                } else if (playerIdentityState.contains(currentSelect)) {
                    WeBadge(
                        if (realGameState.isSpy(currentSelect)) "卧底" else "平民",
                        size = 12.dp,
                        fontSize = 10,
                        color = if (realGameState.isSpy(currentSelect)) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                } else if ((watchedTimeList[currentSelect]) > 1) {
                    WeBadge(
                        "查看" + watchedTimeList[currentSelect] + "次",
                        size = 12.dp,
                        fontSize = 10
                    )
                }
            },
            onClick = { currentSelect ->
                currentSelectPlayer = currentSelect
                watchedTimeList[currentSelect] += 1
                identityDisPlayState.value = IDENTITY_SHOW
                PlatformHelper.getInstance().vibrateMethod()
            }
        ) { currentSelect ->
            playerIdentityState.add(currentSelect)
        }
    }

    // 身份卡片动画显示逻辑
    var dialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(identityDisPlayState.value) {
        if (identityDisPlayState.value == IDENTITY_SHOW) {
            dialogVisible = true
        }
    }

    if (identityDisPlayState.value == IDENTITY_SHOW || dialogVisible) {
        Dialog(
            onDismissRequest = { dialogVisible = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // 关键属性
            )
        ) {
            val cardTransitionState = remember { MutableTransitionState(false) }
            LaunchedEffect(dialogVisible) {
                cardTransitionState.targetState = dialogVisible
            }
            LaunchedEffect(cardTransitionState.currentState, cardTransitionState.targetState) {
                if (!cardTransitionState.currentState && !cardTransitionState.targetState) {
                    identityDisPlayState.value = IDENTITY_DISMISS
                    dialogVisible = false
                }
            }

            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visibleState = cardTransitionState,
                enter = fadeIn(tween(120)) +
                        scaleIn(
                            initialScale = 0.82f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                exit = fadeOut(tween(100)) +
                        scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        ) +
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LocalSpyIdentityCard(
                        modifier = Modifier.align(Alignment.Center),
                        gameState = gameState,
                        currentSelectPlayer = currentSelectPlayer,
                        onClose = { dialogVisible = false }
                    )
                }
            }
        }
    }
}

/**
 * 本地播放器选择区域组件，用于展示可交互的玩家选择网格
 *
 * @param playerNum 总玩家数量，默认4个玩家
 * @param getWatchedTime 获取指定玩家观看时长的回调函数（参数：玩家序号，返回：观看时间单位数）
 * @param badge 在玩家头像右上角显示的徽章组件（参数：玩家序号），默认不显示
 * @param onClick 玩家头像点击事件回调（参数：被点击的玩家序号）
 * @param onLongClick 玩家头像长按事件回调（参数：被长按的玩家序号）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlayerSelectArea(
    playerNum: Int = 4,
    getWatchedTime: (Int) -> Int,
    badge: @Composable (Int) -> Unit = {},
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit
) {
    // 创建4列的垂直网格布局
    LazyVerticalGrid(
        GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(playerNum) { pos ->
            val currentPlayer = pos + 1
            val watchedTime = getWatchedTime(currentPlayer)

            // 交互状态管理，用于处理按压效果
            val interactionState = remember { mutableStateOf<PressInteraction?>(null) }

            // 动态边框颜色动画，按压时显示主题色，默认浅灰色
            val borderColor by animateColorAsState(
                targetValue = when (interactionState.value) {
                    is PressInteraction.Press -> MaterialTheme.colorScheme.primary
                    else -> Color.LightGray
                },
                animationSpec = tween(200)
            )

            Box(modifier = Modifier.padding(top = 8.dp)) {
                // 玩家头像容器，包含点击交互和状态显示
                Box(
                    modifier = Modifier
                        .width(66.dp).height(66.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            color = when {
                                interactionState.value is PressInteraction.Press ->
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

                                watchedTime >= 1 -> MaterialTheme.colorScheme.surface
                                else -> MaterialTheme.colorScheme.primary
                            },

                            )
                        .border(BorderStroke(4.dp, borderColor), RoundedCornerShape(16.dp))
                        .combinedClickable(
                            onLongClick = { onLongClick(currentPlayer) },
                            onClick = { onClick(currentPlayer) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 玩家序号文字显示，根据观看状态改变颜色
                    Text(
                        text = currentPlayer.toString(),
                        color = if (watchedTime >= 1) Color.LightGray
                        else MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // 显示玩家对应的徽章组件
                badge(currentPlayer)
            }
        }
    }
}


/**
 * 身份卡片组件，用于显示玩家身份信息及操作提示
 *
 * @param gameState 当前游戏状态实体，包含玩家身份数据及操作逻辑
 * @param currentSelectPlayer 当前选择的玩家编号（默认1号玩家）
 */
@Composable
fun LocalSpyIdentityCard(
    gameState: LocalSpyEntity,
    currentSelectPlayer: Int = 1,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    val identityText = gameState.optIdentity(currentSelectPlayer)
    Box(modifier = modifier) {
        LocalSpySwipeableIdentityCard(
            resetKey = "legacy-local-spy-$currentSelectPlayer-$identityText",
            playerNumber = currentSelectPlayer,
            nickname = gameState.nicknames.getOrNull(currentSelectPlayer - 1)
                ?.takeIf { it.isNotBlank() }
                ?: "玩家$currentSelectPlayer",
            identity = identityText,
            isSpy = gameState.isSpy(currentSelectPlayer),
            onClose = onClose
        )
    }
}
