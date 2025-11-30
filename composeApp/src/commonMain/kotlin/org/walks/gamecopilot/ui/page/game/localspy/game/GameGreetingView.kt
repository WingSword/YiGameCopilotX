package org.walks.gamecopilot.ui.page.game.localspy.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.ui.page.game.localspy.game.components.LocalPlayerSelectArea
import org.walks.gamecopilot.ui.page.home.LocalSpyIdentityCard

/**
 * 游戏身份展示视图组件
 * 使用通用的LocalSpyIdentitySelector组件来管理玩家身份查看
 *
 * @param key 重组标识键，用于控制状态重置
 * @param gameState 当前游戏状态实体，包含玩家身份信息和游戏配置
 * @param showAllIdentities 是否显示所有身份（游戏结束状态）
 * @param onShowAllIdentitiesChange 显示所有身份状态变化回调
 * @param onAllPlayersViewed 所有玩家查看状态变化回调
 */
@Composable
fun GameGreetingView(
    key: Int,
    gameState: LocalSpyEntity,
    showAllIdentities: Boolean = false,
    onShowAllIdentitiesChange: (Boolean) -> Unit = {},
    onAllPlayersViewed: (Boolean) -> Unit = {}
) {
    // 当前选中玩家索引（1-based）
    var currentSelectPlayer by remember { mutableIntStateOf(1) }
    // 身份显示状态机（默认隐藏，但可以由外部控制）
    val identityDisPlayState = remember { mutableStateOf(IDENTITY_DISMISS) }

    // 监听外部showAllIdentities状态，当长按公布身份时立即更新显示状态
    LaunchedEffect(showAllIdentities) {
        if (showAllIdentities) {
            identityDisPlayState.value = IDENTITY_SHOW_ALL
        }
    }
    
    // 单个玩家身份展示状态列表，记录已查看过身份的玩家
    val playerIdentityState = remember {
        mutableStateListOf<Int>()
    }

    // 派生游戏状态（根据key变化重置）
    val realGameState by remember(key) {
        derivedStateOf { gameState }
    }
    
    // 玩家查看次数记录列表，用于统计查看次数和判断是否所有玩家都已查看
    val watchedTimeList = remember(key) {
        mutableStateListOf(*(Array(17) { 0 }))
    }

    /* 当key变化时重置游戏状态 */
    LaunchedEffect(key1 = realGameState.gameWord) {
        identityDisPlayState.value = IDENTITY_DISMISS
        // 重置watchedTimeList
        repeat(watchedTimeList.size) { index ->
            watchedTimeList[index] = 0
        }
        playerIdentityState.clear()
        onAllPlayersViewed(false)
        onShowAllIdentitiesChange(false) // 重置游戏结束状态
    }

    // 监听玩家查看状态，当所有玩家都查看过身份时通知外部
    LaunchedEffect(watchedTimeList.size, watchedTimeList.sum()) {
        val allViewed = (watchedTimeList.filter { it > 0 }).size >= gameState.totalPlayerNumber
        onAllPlayersViewed(allViewed)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        /* 玩家选择区域布局 */
        LocalPlayerSelectArea(
            playerNum = realGameState.totalPlayerNumber,
            getWatchedTime = { watchedTimeList[it] },
            gameState = realGameState,
            identityDisplayState = identityDisPlayState.value,
            playerIdentityState = playerIdentityState,
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