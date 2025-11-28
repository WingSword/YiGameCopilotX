package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.data.entity.WordGroupManager
import org.walks.gamecopilot.getWordMapBySelectedGroups
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 * 游戏配置区域组件
 * 包含词库选择、游戏设置和游戏控制等功能
 * 
 * @param gameTimeState 游戏时间状态
 * @param currentGame 当前游戏实体
 * @param playerNum 玩家人数
 * @param showNumberPicker 是否显示人数选择器
 * @param numberList 可选人数列表
 * @param selectedWordGroups 选中的词组
 * @param currentWords 当前词汇
 * @param isWordLibraryExpanded 词库是否展开
 * @param showWordsDialog 是否显示词汇弹窗
 * @param showAllIdentities 是否显示所有身份
 * @param allPlayersViewed 所有玩家是否查看过身份
 * @param onNumberPickerChange 人数选择器状态变化回调
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
    showNumberPicker: Boolean,
    numberList: List<String>,
    selectedWordGroups: Set<String>,
    currentWords: Map<String, String>,
    isWordLibraryExpanded: Boolean,
    showWordsDialog: Boolean,
    showAllIdentities: Boolean,
    allPlayersViewed: Boolean,
    onNumberPickerChange: (Boolean) -> Unit,
    onWordsDialogChange: (Boolean) -> Unit,
    onWordLibraryToggle: (Boolean) -> Unit,
    onShowAllIdentities: (Boolean) -> Unit,
    onAllPlayersViewed: (Boolean) -> Unit,
    onGameIntent: (GameIntent) -> Unit,
    onGameTimeStateChange: (Int) -> Unit
) {
    // 游戏状态控制：当前显示的卧底/空白卡选择器类型（0=隐藏，1=卧底，2=空白卡）
    var showSpyNumberPicker by remember { mutableStateOf(0) }
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

            // 游戏设置和控制区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左侧：配置按钮列
                GameSettingsSection(
                    modifier = Modifier.weight(3f),
                    gameTimeState = gameTimeState,
                    playerNum = playerNum,
                    spyNumber = spyNumber,
                    blackNum = blackNum,
                    maxSpyList = maxSpyList,
                    showSpyNumberPicker = showSpyNumberPicker,
                    onSpyNumberPickerChange = { showSpyNumberPicker = it },
                    onGameTimeStateChange = onGameTimeStateChange
                )

                // 右侧：游戏控制区域
                GameControlSection(
                    modifier = Modifier.weight(2f),
                    gameTimeState = gameTimeState,
                    showAllIdentities = showAllIdentities,
                    allPlayersViewed = allPlayersViewed,
                    currentWords = currentWords,
                    onGameIntent = onGameIntent,
                    onGameTimeStateChange = onGameTimeStateChange,
                    onShowAllIdentities = onShowAllIdentities
                )
            }

            // 主人数选择器组件
            WeSingleColumnPicker(
                visible = showNumberPicker,
                title = "选择游玩人数",
                range = numberList,
                onCancel = { onNumberPickerChange(false) },
                onChange = { index ->
                    onGameTimeStateChange(gameTimeState + 1)
                    onGameIntent(GameIntent.RefreshPlayerNumber(numberList[index].toInt()))
                },
                value = numberList.indexOf(playerNum.toString())
            )

            // 数值选择器组件（卧底/空白卡）
            WeSingleColumnPicker(
                visible = showSpyNumberPicker != 0,
                title = if (showSpyNumberPicker == 1) "选择卧底人数" else "选择空白卡片数量",
                range = if (showSpyNumberPicker == 1) maxSpyList else (0..currentGame.spyNum).map { "$it" },
                onCancel = { showSpyNumberPicker = 0 },
                onChange = { index ->
                    onGameTimeStateChange(gameTimeState + 1)
                    when (showSpyNumberPicker) {
                        1 -> {
                            // 更新卧底数量时自动修正空白卡数值不超过新卧底数
                            onGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = maxSpyList[index].toInt(),
                                    blackNum = if (blackNum <= spyNumber) blackNum else 0
                                )
                            )
                        }

                        2 -> {
                            // 直接更新空白卡数量
                            onGameIntent(
                                GameIntent.RefreshSpyNumber(
                                    spyNum = spyNumber,
                                    blackNum = index
                                )
                            )
                        }
                    }
                },
                value = if (showSpyNumberPicker == 1) currentGame.spyNum else currentGame.blackNum
            )
        }
    }
}