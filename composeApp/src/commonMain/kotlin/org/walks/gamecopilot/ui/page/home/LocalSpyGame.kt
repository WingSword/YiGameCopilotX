package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yi.yigamecopilot.android.theme.MorandiColorList
import kotlinx.coroutines.launch
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.clickableWithoutRipple
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.badge.WeBadge
import org.walks.gamecopilot.ui.button.CommonButton
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker
import org.walks.gamecopilot.ui.widget.FlipCard

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
    // 状态控制：控制人数选择器弹窗的显示状态
    var showNumberPicker by remember { mutableStateOf(false) }
    // 协程作用域：用于处理动画等异步操作
    val scope = rememberCoroutineScope()
    // 旋转动画：刷新按钮的旋转动画控制
    val rotation = remember { Animatable(0f) }

    // 游戏状态控制：用于强制刷新游戏问候视图的key值
    var gameTimeState by remember { mutableIntStateOf(0) }
    // 可选的游玩人数范围（4-12人）
    val numberList = (4..12).map { it.toString() }
    // 从ViewModel获取当前游戏状态数据
    val gameStateList = viewmodel.gameEntity.collectAsState().value.timeEntityList
    // 当前玩家总数，默认取最近一次记录或4人
    val playerNum = gameStateList.lastOrNull()?.totalPlayerNumber ?: 4

    LaunchedEffect(key1 = gameTimeState) {
        if (gameTimeState > 0) {
            PlatformHelper.getInstance().vibrateLongMethod()
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // 游玩人数选择区
        Row {
            CommonButton("选择游玩人数 $playerNum", onClick = {
                showNumberPicker = true
            })
        }

        // 当玩家数>=6时显示进阶配置选项
        if (playerNum >= 6) {
            // 状态控制：当前显示的卧底/空白卡选择器类型（0=隐藏，1=卧底，2=空白卡）
            var showSpyNumberPicker by remember { mutableStateOf(0) }
            // 最大卧底数计算（总人数的三分之一）
            val maxSpyList = (1..playerNum / 3).map { "$it" }

            // 从游戏状态获取当前配置值
            val spyNumber = gameStateList.lastOrNull()?.spyNum ?: 1
            val blackNum = gameStateList.lastOrNull()?.blackNum ?: 0

            Spacer(Modifier.height(8.dp))
            Row {
                // 卧底数量选择按钮
                CommonButton("选择卧底人数 $spyNumber", onClick = {
                    showSpyNumberPicker = 1
                }, backColor = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.size(8.dp))
                // 空白卡数量选择按钮
                CommonButton("空白卡数量 $blackNum", onClick = {
                    showSpyNumberPicker = 2
                }, backColor = MaterialTheme.colorScheme.secondary)
            }

            // 数值选择器组件（卧底/空白卡）
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
                            // 更新卧底数量时自动修正空白卡数值不超过新卧底数
                            viewmodel.handleLocalGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = maxSpyList[it].toInt(),
                                    blackNum = if (blackNum <= spyNumber) blackNum else 0
                                )
                            )
                        }

                        2 -> {
                            // 直接更新空白卡数量
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

        // 游戏数据显示区
        if (gameStateList.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("选择编号长按查看", color = MaterialTheme.colorScheme.secondary)
                // 刷新按钮：带旋转动画的重新开始功能
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
            // 游戏结果显示组件，key控制强制刷新
            GameGreetingView(
                key = gameTimeState,
                gameStateList.last(),
            )
        }
    }

    // 主人数选择器组件
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


/**
 * 游戏身份展示视图组件
 *
 * @param key 重组标识键，用于控制派生状态和记忆值的更新时机
 * @param gameState 当前游戏状态实体，包含玩家身份信息和游戏配置
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameGreetingView(key: Int, gameState: LocalSpyEntity) {
    // 当前选中玩家索引（1-based）
    var currentSelectPlayer by remember { mutableIntStateOf(1) }
    // 身份显示状态机（默认隐藏）
    val identityDisPlayState = remember { mutableStateOf(IDENTITY_DISMISS) }

    // 派生游戏状态（根据key变化重置）
    val realGameState by remember(key) {
        derivedStateOf { gameState }
    }
    // 玩家查看次数记录列表
    var watchedTimeList = remember(key) {
        MutableList(13) { 0 }
    }

    /* 当key变化时重置游戏状态 */
    LaunchedEffect(key1 = key) {
        identityDisPlayState.value = IDENTITY_DISMISS
        watchedTimeList.map {
            watchedTimeList[it] = 0
        }
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
                        realGameState.optIdentity(currentSelect),
                        size = 12.dp,
                        fontSize = 10,
                        color = if (realGameState.spies.contains(currentSelect)) MaterialTheme.colorScheme.error
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
            onClick = { _ -> /* 点击事件占位 */ }
        ) { currentSelect ->
            // 长按查看该号码玩家身份并增加查看次数
            currentSelectPlayer = currentSelect
            watchedTimeList[currentSelect] += 1
            identityDisPlayState.value = IDENTITY_SHOW
            PlatformHelper.getInstance().vibrateMethod()
        }

        Spacer(Modifier.weight(1f))
        // 长按提示条件判断
        AnimatedVisibility(
            (watchedTimeList.filter { it > 0 }).size >= gameState.totalPlayerNumber
                    && identityDisPlayState.value != IDENTITY_SHOW_ALL
        ) {
            Text(
                text = "长按公布所有身份",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.combinedClickable(onLongClick = {
                    PlatformHelper.getInstance().vibrateLongMethod()
                    identityDisPlayState.value = IDENTITY_SHOW_ALL
                    PlatformHelper.getInstance().vibrateLongMethod()
                }) {
                    /* 点击事件占位 */
                })
        }
        Spacer(Modifier.weight(1f))
    }

    // 身份卡片动画显示逻辑
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = identityDisPlayState.value == IDENTITY_SHOW,
        // 组合动画+物理效果
        enter = slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
            initialOffsetY = { it }
        ) + fadeIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = slideOutVertically() + fadeOut()) {
        Dialog(
            onDismissRequest = { identityDisPlayState.value = IDENTITY_DISMISS },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // 关键属性
            )
        ) {
            LocalSpyIdentityCard(
                gameState = gameState,
                currentSelectPlayer = currentSelectPlayer,
            )
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
                        .width(66.dp)
                        .clip(CircleShape)
                        .background(
                            color = when {
                                interactionState.value is PressInteraction.Press ->
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

                                watchedTime >= 1 -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shape = CircleShape
                        )
                        .border(BorderStroke(2.dp, borderColor), CircleShape)
                        .combinedClickable(
                            onLongClick = { onLongClick(currentPlayer) },
                            onClick = { onClick(currentPlayer) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // 玩家序号文字显示，根据观看状态改变颜色
                    Text(
                        text = currentPlayer.toString(),
                        color = if (watchedTime >= 1) MaterialTheme.colorScheme.onTertiaryContainer
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
) {
    val flipState = remember { mutableStateOf(false) }
    // 主容器：包含卡片布局和交互效果
    FlipCard(
        modifier = Modifier.height(200.dp).width(140.dp).clip(RoundedCornerShape(20.dp))
            .clickable { flipState.value = !flipState.value },
        backContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentSelectPlayer.toString(),
                    fontSize = 90.sp,
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (gameState.isSpy(currentSelectPlayer)) "[卧底]" else "[平民]",
                    textAlign = TextAlign.Center,
                    color = if (gameState.isSpy(currentSelectPlayer)) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraLight,
                    modifier = Modifier.fillMaxWidth()
                )
                // 操作提示文本：根据显示状态切换提示语
                Text(
                    "点击卡片查看身份词",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },
        frontContent = {
            Box(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.secondaryContainer
                ).border(
                    BorderStroke(
                        width = 4.dp,
                        color = MorandiColorList[(0..7).random()] // 随机生成边框颜色
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        gameState.optIdentity(currentSelectPlayer),
                        textAlign = TextAlign.Center,
                        color = if (gameState.isSpy(currentSelectPlayer)) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.weight(1f))

                }
            }

        },
        isFlipped = !flipState.value,
        onFlipComplete = {

        }
    )
}
