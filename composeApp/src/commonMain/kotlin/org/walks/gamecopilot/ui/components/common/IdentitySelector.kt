package org.walks.gamecopilot.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yi.yigamecopilot.android.theme.MorandiColorList
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.PlatformHelper
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
 * @param key 重组标识键，用于控制状态重置
 * @param playerNum 玩家总数
 * @param identities 玩家身份列表（索引从0开始，对应玩家1-N）
 * @param nicknames 玩家昵称列表（索引从0开始，对应玩家1-N）
 * @param onNicknameChange 昵称修改回调 (playerIndex: Int, newNickname: String) -> Unit
 * @param customIdentityCard 自定义身份卡片组件，如果为null则使用默认卡片
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IdentitySelector(
    key: Int,
    playerNum: Int,
    identities: List<String>,
    nicknames: List<String>,
    onNicknameChange: (Int, String) -> Unit,
    onRefreshIdentities: (() -> Unit)? = null,
    customIdentityCard: @Composable ((playerNumber: Int, identity: String, nickname: String) -> Unit)? = null
) {
    // 当前选中玩家索引（0-based）
    var currentSelectPlayer by remember { mutableIntStateOf(0) }
    // 身份显示状态机
    val identityDisplayState = remember { mutableStateOf(IDENTITY_DISMISS) }
    // 昵称编辑状态
    val nicknameEditState = remember { mutableStateOf(false) }
    // 昵称编辑文本
    val nicknameText = remember { mutableStateOf("") }
    // 强制刷新状态
    var forceRefresh by remember { mutableStateOf(0) }
    // 昵称更新状态

    // 单个玩家身份展示状态列表，记录已查看过身份的玩家
    val playerIdentityState = remember {
        mutableStateListOf<Int>()
    }

    // 玩家查看次数记录列表
    val watchedTimeList = remember(key) {
        mutableStateListOf(*(Array(playerNum + 1) { 0 }))
    }

    /* 当key变化时重置游戏状态 */
    LaunchedEffect(key1 = key) {
        identityDisplayState.value = IDENTITY_DISMISS
        repeat(watchedTimeList.size) { index ->
            watchedTimeList[index] = 0
        }
        playerIdentityState.clear()
    }

    // 监听昵称变化，强制刷新UI
    LaunchedEffect(nicknames) {
        // 昵称列表变化时触发重组以立即显示昵称变化
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* 玩家选择区域布局 */
        PlayerSelectArea(
            playerNum = playerNum,
            nicknames = nicknames,
            getWatchedTime = { watchedTimeList[it] },
            identityDisplayState = identityDisplayState.value,
            playerIdentityState = playerIdentityState,
            identities = identities,
            forceRefresh = forceRefresh,
            onRefreshIdentities = onRefreshIdentities,
            onClick = { currentSelect ->
                currentSelectPlayer = currentSelect - 1 // 转换为0-based索引
                watchedTimeList[currentSelect] += 1
                identityDisplayState.value = IDENTITY_SHOW
                PlatformHelper.getInstance().vibrateMethod()
            }
        ) { currentSelect ->
            // 长按编辑昵称
            currentSelectPlayer = currentSelect - 1 // 转换为0-based索引
            // 如果昵称等于数字号码，则显示空字符串
            val currentNickname = nicknames[currentSelectPlayer]
            nicknameText.value =
                if (currentNickname == currentSelect.toString()) "" else currentNickname
            nicknameEditState.value = true
        }
    }

    // 身份卡片动画显示逻辑
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = identityDisplayState.value == IDENTITY_SHOW,
        enter = slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            ),
            initialOffsetY = { it }
        ) + fadeIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = slideOutVertically() + fadeOut()
    ) {
        Dialog(
            onDismissRequest = { identityDisplayState.value = IDENTITY_DISMISS },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            if (customIdentityCard != null) {
                customIdentityCard(
                    currentSelectPlayer + 1,
                    identities[currentSelectPlayer],
                    nicknames[currentSelectPlayer]
                )
            } else {
                IdentityCard(
                    playerNumber = currentSelectPlayer + 1,
                    identity = identities[currentSelectPlayer],
                    nickname = nicknames[currentSelectPlayer]
                )
            }
        }
    }

    // 昵称编辑弹窗
    if (nicknameEditState.value) {
        Dialog(
            onDismissRequest = { nicknameEditState.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
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
    onRefreshIdentities: (() -> Unit)? = null,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit
) {
    // 列容器，包含刷新按钮和玩家网格
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
        items(playerNum) { pos ->
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

                // 昵称显示在卡片上方，但位置更合理
                if (nicknames[currentPlayer - 1].isNotEmpty() &&
                    nicknames[currentPlayer - 1] != currentPlayer.toString()
                ) {
                    Text(
                        text = nicknames[currentPlayer - 1],
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-8).dp) // 减少偏移，使昵称更靠近卡片
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

                // 身份显示区域 - 回到卡片内部底部，紧贴卡片
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    when {
                        playerIdentityState.contains(currentPlayer) -> {
                            Text(
                                text = identities[currentPlayer - 1],
                                modifier = Modifier.padding(bottom = 2.dp), // 减少底部间距
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        watchedTime > 1 -> {
                            Text(
                                text = "查看${watchedTime}次",
                                modifier = Modifier.padding(bottom = 2.dp), // 减少底部间距，紧贴卡片
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
private fun IdentityCard(
    playerNumber: Int,
    identity: String,
    nickname: String
) {
    val flipState = remember { mutableStateOf(false) }

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
                    color = MorandiColorList[(0..7).random()],
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
private fun NicknameEditDialog(
    playerNumber: Int,
    currentNickname: String,
    onNicknameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }

    androidx.compose.material3.Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "设置玩家$playerNumber 昵称",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                TextButton(
                    onClick = {
                        onNicknameChange(nickname)
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            }
        }
    }
}

// 身份显示状态常量
private const val IDENTITY_DISMISS = 0
private const val IDENTITY_SHOW = 1
private const val IDENTITY_SHOW_ALL = 2