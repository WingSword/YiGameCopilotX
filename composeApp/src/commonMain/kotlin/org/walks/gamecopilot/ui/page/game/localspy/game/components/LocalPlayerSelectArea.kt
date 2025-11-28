package org.walks.gamecopilot.ui.page.game.localspy.game.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.ui.page.game.localspy.game.IDENTITY_SHOW_ALL
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_cardindex
import yigamecopilotx.composeapp.generated.resources.icon_cardindex_notitle

/**
 * 本地播放器选择区域组件，用于展示可交互的玩家选择网格
 * 
 * 功能说明：
 * - 显示玩家卡片网格（4列布局）
 * - 支持点击查看身份和长按操作
 * - 根据不同状态显示卡片样式和身份信息
 * - 记录玩家查看次数和身份状态
 *
 * @param playerNum 总玩家数量，默认4个玩家
 * @param getWatchedTime 获取指定玩家观看时长的回调函数（参数：玩家序号，返回：观看时间单位数）
 * @param gameState 当前游戏状态实体，包含玩家身份信息和游戏配置
 * @param identityDisplayState 身份显示状态
 * @param playerIdentityState 单个玩家身份展示状态列表
 * @param onClick 玩家头像点击事件回调（参数：被点击的玩家序号）
 * @param onLongClick 玩家头像长按事件回调（参数：被长按的玩家序号）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalPlayerSelectArea(
    playerNum: Int = 4,
    getWatchedTime: (Int) -> Int,
    gameState: LocalSpyEntity,
    identityDisplayState: Int,
    playerIdentityState: List<Int>,
    onClick: (Int) -> Unit,
    onLongClick: (Int) -> Unit
) {
    // 创建4列的垂直网格布局
    LazyVerticalGrid(
        GridCells.Fixed(4),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        items(playerNum) { pos ->
            val currentPlayer = pos + 1
            val watchedTime = getWatchedTime(currentPlayer)

            Box(modifier = Modifier.padding(top = 8.dp)) {
                // 玩家头像容器，包含点击交互和状态显示
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .combinedClickable(
                            onLongClick = { onLongClick(currentPlayer) },
                            onClick = { onClick(currentPlayer) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        painter = painterResource(
                            if (watchedTime < 1) Res.drawable.icon_cardindex_notitle
                            else Res.drawable.icon_cardindex
                        ),
                        modifier = Modifier.rotate(180f),
                        contentDescription = "Card index icon",
                        alpha = if (watchedTime >= 1) 0.6f else 1f
                    )

                    // 身份显示区域 - 在卡片内部显示身份信息
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 根据不同状态显示身份信息
                        when {
                            identityDisplayState == IDENTITY_SHOW_ALL -> {
                                Text(
                                    text = gameState.optIdentity(currentPlayer),
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = if (gameState.isSpy(currentPlayer))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            playerIdentityState.contains(currentPlayer) -> {
                                Text(
                                    text = if (gameState.isSpy(currentPlayer)) "卧底" else "平民",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = if (gameState.isSpy(currentPlayer))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            watchedTime > 1 -> {
                                Text(
                                    text = "查看${watchedTime}次",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            else -> {
                                Text(
                                    text = "",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 玩家序号文字显示，根据观看状态改变颜色
                    Text(
                        text = currentPlayer.toString(),
                        color = MaterialTheme.colorScheme.secondary.copy(if (watchedTime >= 1) 0.5f else 1f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 20.dp, top = 5.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}