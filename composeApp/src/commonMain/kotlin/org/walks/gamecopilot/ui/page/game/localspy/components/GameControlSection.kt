package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.components.AppPrimaryAction

/**
 * 游戏控制区域组件
 * 包含开始/重新开始按钮和游戏状态显示
 * 
 * @param modifier 修饰符
 * @param gameTimeState 游戏时间状态
 * @param showAllIdentities 是否显示所有身份（游戏结束状态）
 * @param allPlayersViewed 所有玩家是否查看过身份
 * @param currentWords 当前可用词汇映射
 * @param onGameIntent 游戏意图处理回调
 * @param onGameTimeStateChange 游戏时间状态变化回调
 * @param onShowAllIdentities 显示所有身份状态变化回调
 */
@Composable
fun GameControlSection(
    modifier: Modifier = Modifier,
    gameTimeState: Int,
    showAllIdentities: Boolean,
    allPlayersViewed: Boolean,
    currentWords: Map<String, String>,
    onGameIntent: (GameIntent) -> Unit,
    onGameTimeStateChange: (Int) -> Unit,
    onShowAllIdentities: (Boolean) -> Unit
) {
    // 游戏控制区域
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 开始游戏按钮 - 大尺寸
        val canStartGame = currentWords.isNotEmpty()
        AppPrimaryAction(
            text = if (gameTimeState == 0) "开始游戏" else "重新开始",
            onClick = {
                if (gameTimeState == 0) {
                    onGameTimeStateChange(gameTimeState + 1)
                }
                onGameIntent(GameIntent.StartGame)
                if (gameTimeState > 0) {
                    onGameTimeStateChange(gameTimeState + 2)
                }
                // 重新开始时重置身份公布状态
                onShowAllIdentities(false)
            },
            enabled = canStartGame,
            icon = Icons.Rounded.PlayArrow
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 词汇状态提示
        if (currentWords.isEmpty()) {
            Text(
                text = "请先选择词库",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 游戏状态提示区域 - 统一的状态显示组件
        GameStatusDisplay(
            gameTimeState = gameTimeState,
            allPlayersViewed = allPlayersViewed,
            showAllIdentities = showAllIdentities,
            onShowAllIdentities = onShowAllIdentities
        )
    }
}
/**
 * 游戏状态显示组件
 * 根据不同游戏状态显示相应的提示信息
 * 
 * @param gameTimeState 游戏时间状态
 * @param allPlayersViewed 所有玩家是否查看过身份
 * @param showAllIdentities 是否显示所有身份
 * @param onShowAllIdentities 显示所有身份状态变化回调
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameStatusDisplay(
    gameTimeState: Int,
    allPlayersViewed: Boolean,
    showAllIdentities: Boolean,
    onShowAllIdentities: (Boolean) -> Unit
) {
    // 游戏状态提示区域 - 统一的状态显示组件
    AnimatedVisibility(
        visible = gameTimeState > 0,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .let { modifier ->
                    if (allPlayersViewed && !showAllIdentities) {
                        modifier.combinedClickable(
                            onLongClick = {
                                PlatformHelper.getInstance().vibrateLongMethod()
                                onShowAllIdentities(true) // 长按后立即设置游戏结束状态
                            },
                            onClick = {
                                PlatformHelper.getInstance().vibrateMethod()
                            }
                        )
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(
                                    alpha = 0.1f
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(
                                    alpha = 0.3f
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                    } else {
                        modifier.background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.3f
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // 根据状态显示不同的图标和文字
                when {
                    allPlayersViewed && !showAllIdentities -> {
                        // 长按公布所有身份状态
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "公布所有身份",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "长按公布所有身份",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    showAllIdentities -> {
                        // 游戏结束状态
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "游戏已结束",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    else -> {
                        // 游戏进行中状态
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "游戏进行中",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // 游戏未开始时的提示
    AnimatedVisibility(
        visible = gameTimeState == 0,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        Text(
            text = "配置完成，开始游戏",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}
