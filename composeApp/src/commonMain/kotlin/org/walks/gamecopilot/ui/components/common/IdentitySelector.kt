package org.walks.gamecopilot.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yi.yigamecopilot.android.theme.MorandiColorList
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.page.home.IDENTITY_DISMISS
import org.walks.gamecopilot.ui.page.home.IDENTITY_SHOW
import org.walks.gamecopilot.ui.widget.FlipCard
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_asterisk
import yigamecopilotx.composeapp.generated.resources.icon_cardindex
import yigamecopilotx.composeapp.generated.resources.icon_cardindex_notitle
import yigamecopilotx.composeapp.generated.resources.icon_star

/**
 * 通用身份选择器组件
 * 支持多种游戏的身份查看、卡片翻转和昵称设置功能
 *
 * @param refreshKey 重组标识键，用于控制状态重置
 * @param playerNum 玩家总数
 * @param identities 玩家身份列表（索引从0开始，对应玩家1-N）
 * @param nicknames 玩家昵称列表（索引从0开始，对应玩家1-N）
 * @param onNicknameChange 昵称修改回调 (playerIndex: Int, newNickname: String) -> Unit
 * @param customIdentityCard 自定义身份卡片组件，如果为null则使用默认卡片
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdentitySelector(
    refreshKey: Int,
    playerNum: Int,
    identities: List<String>,
    nicknames: List<String>,
    onNicknameChange: (Int, String) -> Unit,
    onRefreshIdentities: (() -> Unit)? = null,
    customIdentityCard: @Composable ((playerNumber: Int, identity: String, nickname: String, onClose: () -> Unit) -> Unit)? = null,
) {
    var currentSelectPlayer by remember(refreshKey) { mutableIntStateOf(0) }
    val identityDisplayState = remember(refreshKey) { mutableStateOf(IDENTITY_DISMISS) }
    val nicknameEditState = remember(refreshKey) { mutableStateOf(false) }
    val nicknameText = remember(refreshKey) { mutableStateOf("") }
    var forceRefresh by remember(refreshKey) { mutableStateOf(0) }
    val displayNicknames = remember(playerNum, nicknames) {
        List(playerNum) { index ->
            nicknames.getOrNull(index)?.takeIf { it.isNotBlank() } ?: "${index + 1}"
        }
    }
    val displayIdentities = remember(playerNum, identities) {
        List(playerNum) { index ->
            identities.getOrNull(index)?.takeIf { it.isNotBlank() } ?: "未知身份"
        }
    }

    // 单个玩家身份展示状态列表，记录已查看过身份的玩家
    val playerIdentityState = remember(refreshKey) {
        mutableStateListOf<Int>()
    }

    // 玩家查看次数记录列表
    val watchedTimeList = remember(refreshKey) {
        mutableStateListOf(*(Array(playerNum + 1) { 0 }))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerSelectArea(
            playerNum = playerNum,
            nicknames = displayNicknames,
            getWatchedTime = { watchedTimeList[it] },
            identityDisplayState = identityDisplayState.value,
            playerIdentityState = playerIdentityState,
            identities = displayIdentities,
            forceRefresh = forceRefresh,
            refreshKey = refreshKey,
            onRefreshIdentities = onRefreshIdentities,
            onClick = { currentSelect ->
                currentSelectPlayer = currentSelect - 1
                watchedTimeList[currentSelect] += 1
                identityDisplayState.value = IDENTITY_SHOW
                PlatformHelper.getInstance().vibrateMethod()
            }
        ) { currentSelect ->
            currentSelectPlayer = currentSelect - 1
            val currentNickname = displayNicknames[currentSelectPlayer]
            nicknameText.value =
                if (currentNickname == currentSelect.toString()) "" else currentNickname
            nicknameEditState.value = true
        }
    }

    // 身份卡片动画显示逻辑
    var dialogVisible by remember(refreshKey) { mutableStateOf(false) }
    LaunchedEffect(identityDisplayState.value) {
        if (identityDisplayState.value == IDENTITY_SHOW) {
            dialogVisible = true
        }
    }

    if (identityDisplayState.value == IDENTITY_SHOW || dialogVisible) {
        Dialog(
            onDismissRequest = { dialogVisible = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val cardTransitionState = remember { MutableTransitionState(false) }
            LaunchedEffect(dialogVisible) {
                cardTransitionState.targetState = dialogVisible
            }
            LaunchedEffect(cardTransitionState.currentState, cardTransitionState.targetState) {
                if (!cardTransitionState.currentState && !cardTransitionState.targetState) {
                    identityDisplayState.value = IDENTITY_DISMISS
                    dialogVisible = false
                }
            }
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visibleState = cardTransitionState,
                enter = fadeIn(tween(120)) +
                        scaleIn(
                            initialScale = 0.82f,
                            animationSpec = tween(220)
                        ) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(220)
                        ),
                exit = fadeOut(tween(100)) +
                        scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(180)
                        ) +
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(180)
                        )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (customIdentityCard != null) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            customIdentityCard(
                                currentSelectPlayer + 1,
                                displayIdentities[currentSelectPlayer],
                                displayNicknames[currentSelectPlayer],
                                { dialogVisible = false }
                            )
                        }
                    } else {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            IdentityCard(
                                playerNumber = currentSelectPlayer + 1,
                                identity = displayIdentities[currentSelectPlayer],
                                nickname = displayNicknames[currentSelectPlayer]
                            )
                        }
                    }
                }
            }
        }
    }

    // 昵称编辑弹窗
    if (nicknameEditState.value) {
        NicknameEditDialog(
            playerNumber = currentSelectPlayer + 1,
            currentNickname = nicknameText.value,
            onNicknameChange = { newNickname ->
                nicknameText.value = newNickname
                onNicknameChange(currentSelectPlayer, newNickname)
            },
            onDismiss = { nicknameEditState.value = false }
        )
    }
}

/**
 * 玩家选择区域组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerSelectArea(
    playerNum: Int,
    nicknames: List<String>,
    getWatchedTime: (Int) -> Int,
    identityDisplayState: Int,
    playerIdentityState: List<Int>,
    identities: List<String>,
    forceRefresh: Int,
    refreshKey: Int,
    onRefreshIdentities: (() -> Unit)? = null,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(playerNum, key = { pos -> pos + refreshKey }) { pos ->
                val currentPlayer = pos + 1
                val watchedTime = getWatchedTime(currentPlayer)

                Box(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    // 可点击的卡片区域
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .combinedClickable(
                                onLongClick = { onLongClick(currentPlayer) },
                                onClick = { onClick(currentPlayer) },
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            )
                    ) {
                        Image(
                            painter = painterResource(
                                if (watchedTime < 1) Res.drawable.icon_cardindex_notitle
                                else Res.drawable.icon_cardindex
                            ),
                            modifier = Modifier.rotate(180f).align(Alignment.TopEnd),
                            contentDescription = "Card index icon",
                            alpha = if (watchedTime >= 1) 0.6f else 1f
                        )

                        // 玩家序号显示
                        Text(
                            text = currentPlayer.toString(),
                            modifier = Modifier.align(Alignment.TopEnd)
                                .padding(end = 20.dp, top = 5.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(if (watchedTime >= 1) 0.5f else 1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (nicknames[currentPlayer - 1].isNotEmpty() &&
                        nicknames[currentPlayer - 1] != currentPlayer.toString()
                    ) {
                        Text(
                            text = nicknames[currentPlayer - 1],
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-8).dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        when {
                            playerIdentityState.contains(currentPlayer) -> {
                                Text(
                                    text = identities[currentPlayer - 1],
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            watchedTime > 1 -> {
                                Text(
                                    text = "查看${watchedTime}次",
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                Text(
                                    text = "",
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

/**
 * 身份卡片组件
 */

@Composable
fun IdentityCard(
    playerNumber: Int,
    identity: String,
    nickname: String
) {
    val flipState = remember { mutableStateOf(false) }
    val accentColor = remember(playerNumber) {
        MorandiColorList[playerNumber % MorandiColorList.size]
    }

    FlipCard(
        modifier = Modifier.height(200.dp).width(140.dp).clip(RoundedCornerShape(12.dp))
            .clickable { flipState.value = !flipState.value },
        backContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(Res.drawable.icon_asterisk),
                        alpha = 0.33f,
                        modifier = Modifier.size(100.dp),
                        contentDescription = ""
                    )
                    Text(
                        text = playerNumber.toString(),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                        textAlign = TextAlign.Right
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (nickname.isNotEmpty()) nickname else "玩家$playerNumber",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "点击卡片查看身份",
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
                    color = MaterialTheme.colorScheme.surface
                ).border(
                    width = 4.dp,
                    color = accentColor,
                    shape = RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(Res.drawable.icon_star),
                        alpha = 0.33f,
                        modifier = Modifier.size(120.dp).rotate(-30f),
                        contentDescription = ""
                    )

                    Text(
                        text = playerNumber.toString(),
                        modifier = Modifier.rotate(-30f),
                        fontSize = 90.sp,
                        fontWeight = FontWeight.W900,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.33f),
                        textAlign = TextAlign.Right
                    )
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        identity,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (nickname.isNotEmpty()) {
                        Text(
                            nickname,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        },
        isFlipped = !flipState.value
    )
}

/**
 * 昵称编辑弹窗
 */
@Composable
fun NicknameEditDialog(
    playerNumber: Int,
    currentNickname: String,
    onNicknameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }

    AppDialog(
        title = "设置玩家$playerNumber 昵称",
        subtitle = "昵称会显示在玩家卡片顶部，留空则使用默认编号",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            Button(
                onClick = {
                    onNicknameChange(nickname)
                    onDismiss()
                }
            ) {
                Text("确定")
            }
        }
    ) {
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
