package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.currentTimeMillis
import org.walks.gamecopilot.data.WordImportEngine
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.data.entity.WordGroup
import org.walks.gamecopilot.data.entity.WordGroupManager
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.registerCustomSpyWords
import org.walks.gamecopilot.ui.components.common.WordImportDialog
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 * 词库选择区域组件
 * 可折叠的词库选择界面，包含词组选择、词汇查看和自定义词库导入功能
 *
 * @param selectedWordGroups 选中的词组ID集合
 * @param currentWords 当前词汇映射
 * @param currentGame 当前游戏实体
 * @param isExpanded 是否展开状态
 * @param onWordsDialogChange 词汇弹窗状态变化回调
 * @param onWordLibraryToggle 词库展开状态切换回调
 * @param onGameIntent 游戏意图处理回调
 */
@Composable
fun WordLibrarySection(
    selectedWordGroups: Set<String>,
    currentWords: Map<String, String>,
    currentGame: LocalSpyEntity,
    isExpanded: Boolean,
    onWordsDialogChange: (Boolean) -> Unit,
    onWordLibraryToggle: (Boolean) -> Unit,
    onGameIntent: (GameIntent) -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }

    // 词库选择区域 - 可折叠
    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = tween(durationMillis = 400, easing = EaseOutQuart)
        ) + fadeIn(animationSpec = tween(durationMillis = 400, easing = EaseOutQuart)),
        exit = slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = tween(durationMillis = 300, easing = EaseInQuart)
        ) + fadeOut(animationSpec = tween(durationMillis = 300, easing = EaseInQuart))
    ) {
        Column(
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "词库选择",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 导入自定义词库按钮
                    Text(
                        text = "导入",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                PlatformHelper.getInstance().vibrateMethod()
                                showImportDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 词汇查看按钮
                    Icon(
                        painter = painterResource(Res.drawable.icon_info),
                        contentDescription = "查看词汇",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp).clickable {
                            onWordsDialogChange(true)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 词库选择内容
            WordGroupSelectorContent(
                selectedGroupIds = selectedWordGroups,
                onGroupsChanged = { groupIds ->
                    onGameIntent(GameIntent.RefreshWordGroups(groupIds))
                }
            )
        }
    }

    // 自定义词库导入弹窗
    if (showImportDialog) {
        WordImportDialog(
            title = "导入卧底词库",
            isSpyMode = true,
            onDismiss = { showImportDialog = false },
            onImportSuccess = { text ->
                val result = WordImportEngine.parseSpyPairs(text)
                if (result is WordImportEngine.ImportResult.SpyPairs) {
                    val groupId = "custom_spy_${currentTimeMillis()}"
                    val groupName = "自定义${WordGroupManager.getCustomGroups().size + 1}"
                    // 注册词组
                    WordGroupManager.addGroup(WordGroup(groupId, groupName, isBuiltIn = false))
                    // 注册词汇
                    registerCustomSpyWords(groupId, result.pairs)
                    // 自动选中新增词组
                    onGameIntent(GameIntent.RefreshWordGroups(selectedWordGroups + groupId))
                }
            }
        )
    }
}

/**
 * 词库选择内容组件（不包含外层容器和标题）
 * 用于整合到其他组件中
 *
 * @param selectedGroupIds 选中的词组ID集合
 * @param onGroupsChanged 词组选择变化回调
 */
@Composable
fun WordGroupSelectorContent(
    selectedGroupIds: Set<String>,
    onGroupsChanged: (Set<String>) -> Unit
) {
    val allGroups = WordGroupManager.getAllGroups()

    // 词组选择区域
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allGroups.values.toList()) { group ->
            WordGroupItem(
                group = group,
                isSelected = selectedGroupIds.contains(group.id),
                onToggle = { groupId ->
                    val newSelection = if (selectedGroupIds.contains(groupId)) {
                        selectedGroupIds - groupId
                    } else {
                        selectedGroupIds + groupId
                    }
                    onGroupsChanged(newSelection)
                }
            )
        }
    }
}

/**
 * 词组项组件
 * 显示单个词组的选择状态和基本信息
 *
 * @param group 词组实体
 * @param isSelected 是否选中状态
 * @param onToggle 切换选中状态回调
 */
@Composable
private fun WordGroupItem(
    group: WordGroup,
    isSelected: Boolean,
    onToggle: (String) -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onToggle(group.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = group.displayName,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        if (group.isBuiltIn) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "内置",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        } else {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "自定",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}
