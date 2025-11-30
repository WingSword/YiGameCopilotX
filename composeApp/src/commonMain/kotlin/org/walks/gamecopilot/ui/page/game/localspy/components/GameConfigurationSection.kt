package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.components.common.GameConfigItem
import org.walks.gamecopilot.ui.components.common.GameConfigSection

/**
 * 游戏配置区域组件
 * 包含词库选择、游戏设置和游戏控制等功能
 * 
 * @param gameTimeState 游戏时间状态
 * @param currentGame 当前游戏实体
 * @param playerNum 玩家人数
 * @param numberList 可选人数列表
 * @param selectedWordGroups 选中的词组
 * @param currentWords 当前词汇
 * @param isWordLibraryExpanded 词库是否展开
 * @param showWordsDialog 是否显示词汇弹窗
 * @param showAllIdentities 是否显示所有身份
 * @param allPlayersViewed 所有玩家是否查看过身份
 * @param onWordsDialogChange 词汇弹窗状态变化回调
 * @param onWordLibraryToggle 词库展开状态切换回调
 * @param onShowAllIdentities 显示所有身份状态变化回调
 * @param onAllPlayersViewed 所有玩家查看状态变化回调
 * @param onGameIntent 游戏意图处理回调
 * @param onGameTimeStateChange 游戏时间状态变化回调
 */
@Composable
fun GameConfigurationSection(
    gameTimeState: Int,
    currentGame: LocalSpyEntity,
    playerNum: Int,
    numberList: List<String>,
    selectedWordGroups: Set<String>,
    currentWords: Map<String, String>,
    isWordLibraryExpanded: Boolean,
    showWordsDialog: Boolean,
    showAllIdentities: Boolean,
    allPlayersViewed: Boolean,
    onWordsDialogChange: (Boolean) -> Unit,
    onWordLibraryToggle: (Boolean) -> Unit,
    onShowAllIdentities: (Boolean) -> Unit,
    onAllPlayersViewed: (Boolean) -> Unit,
    onGameIntent: (GameIntent) -> Unit,
    onGameTimeStateChange: (Int) -> Unit
) {
    // 最大卧底数计算（总人数的三分之一）
    val maxSpyList = (1..playerNum / 3).map { "$it" }
    // 从游戏状态获取当前配置值
    val spyNumber = currentGame.spyNum
    val blackNum = currentGame.blackNum

    // 游戏配置区域 - 统一的卡片样式，包含词库选择
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 区域标题
            Text(
                text = "游戏配置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 词库选择区域 - 可折叠
            WordLibrarySection(
                selectedWordGroups = selectedWordGroups,
                currentWords = currentWords,
                currentGame = currentGame,
                isExpanded = isWordLibraryExpanded,
                onWordsDialogChange = onWordsDialogChange,
                onWordLibraryToggle = onWordLibraryToggle,
                onGameIntent = onGameIntent
            )

            // 分割线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 游戏设置区域 - 使用公共组件
            GameConfigSection(
                title = "游戏设置",
                configItems = listOf(
                    GameConfigItem(
                        title = "游玩人数",
                        value = playerNum.toString(),
                        options = numberList,
                        color = MaterialTheme.colorScheme.primary,
                        onValueChange = { newValue ->
                            onGameTimeStateChange(gameTimeState + 1)
                            onGameIntent(GameIntent.RefreshPlayerNumber(newValue.toInt()))
                        }
                    ),
                    GameConfigItem(
                        title = "卧底人数",
                        value = spyNumber.toString(),
                        options = maxSpyList,
                        color = MaterialTheme.colorScheme.error,
                        onValueChange = { newValue ->
                            // 更新卧底数量时自动修正空白卡数值不超过新卧底数
                            onGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = newValue.toInt(),
                                    blackNum = if (blackNum <= spyNumber) blackNum else 0
                                )
                            )
                        }
                    ),
                    GameConfigItem(
                        title = "空白卡",
                        value = blackNum.toString(),
                        options = (0..spyNumber).map { it.toString() },
                        color = MaterialTheme.colorScheme.secondary,
                        onValueChange = { newValue ->
                            onGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = spyNumber,
                                    blackNum = newValue.toInt()
                                )
                            )
                        }
                    )
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 游戏控制区域
            GameControlSection(
                modifier = Modifier.fillMaxWidth(),
                gameTimeState = gameTimeState,
                showAllIdentities = showAllIdentities,
                allPlayersViewed = allPlayersViewed,
                currentWords = currentWords,
                onGameIntent = onGameIntent,
                onGameTimeStateChange = onGameTimeStateChange,
                onShowAllIdentities = onShowAllIdentities
            )


        }
    }
}