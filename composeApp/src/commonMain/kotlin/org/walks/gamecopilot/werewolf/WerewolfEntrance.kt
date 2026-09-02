package org.walks.gamecopilot.werewolf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.ui.components.AppChoiceRow
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppDialogActions
import org.walks.gamecopilot.ui.components.AppPrimaryAction
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.ui.components.common.GameSetupScreen
import org.walks.gamecopilot.ui.components.common.OfflinePassingGuideDialog
import org.walks.gamecopilot.werewolf.data.WerewolfFaction
import org.walks.gamecopilot.werewolf.data.WerewolfPreset
import org.walks.gamecopilot.werewolf.data.WerewolfPresets
import org.walks.gamecopilot.werewolf.data.WerewolfRole

/** 一夜终极狼人入口配置页。 */
@Composable
fun WerewolfEntrance(viewmodel: MainViewmodel, navi: NavHostController) {
    var showRulesDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var playerCount by remember { mutableIntStateOf(5) }
    val nicknames = remember {
        mutableStateListOf<String>().apply {
            repeat(WerewolfPresets.getPresetForPlayerCount(5).playerCount) {
                add("玩家${it + 1}")
            }
        }
    }
    val design = LocalAppDesign.current
    val currentPreset = WerewolfPresets.getPresetForPlayerCount(playerCount)

    GameSetupScreen(
        title = "一夜终极狼人",
        subtitle = "3-10人 · 一夜一白天 · 约10分钟",
        onBack = { navi.navigateUp() },
        onShowRules = { showRulesDialog = true }
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = design.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(design.spacing.md)
        ) {
            item {
                AppSectionHeader(
                    title = "选择人数",
                    subtitle = "预设会自动匹配玩家牌与三张中央底牌"
                )
            }
            items(WerewolfPresets.presets.size) { index ->
                val preset = WerewolfPresets.presets[index]
                PresetCard(
                    preset = preset,
                    isSelected = playerCount == preset.playerCount,
                    onClick = {
                        playerCount = preset.playerCount
                        nicknames.clear()
                        repeat(preset.playerCount) { playerIndex ->
                            nicknames.add("玩家${playerIndex + 1}")
                        }
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(design.spacing.sm))
                AppSectionHeader(
                    title = "本局角色",
                    subtitle = "${currentPreset.roles.size} 张玩家牌 + 3 张中央底牌"
                )
            }
            item {
                RolePreview(preset = currentPreset)
            }
        }

        AppPrimaryAction(
            text = "开始传机发牌",
            onClick = {
                viewmodel.prepareOneNightWerewolfGame(playerCount, nicknames.toList())
                viewmodel.handleGameIntent(
                    GameIntent.SwitchGameMode(GameMode.ONE_NIGHT_WEREWOLF.ordinal)
                )
                navi.navigate(NaviRoute.ONE_NIGHT_WEREWOLF_GAME.route)
            },
            supportingText = "进入后直接从第 1 位玩家开始查看身份，不再重复配置人数。"
        )

        OutlinedButton(
            onClick = { showGuideDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(design.cornerRadius.button)
        ) {
            Icon(
                imageVector = Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看单机传机流程")
        }
    }

    if (showRulesDialog) {
        WerewolfRulesDialog(onDismiss = { showRulesDialog = false })
    }

    OfflinePassingGuideDialog(
        show = showGuideDialog,
        gameTitle = "一夜终极狼人",
        steps = listOf(
            "设备按玩家顺序传递，每位玩家翻开并记住自己的身份牌。",
            "所有玩家看完后，设备按随机夜间顺序提示各角色行动。",
            "夜间结束后自由讨论并投票，设备会协助判断胜负。",
            "交接设备前滑回隐藏面，避免旁人看到身份。"
        ),
        onDismiss = { showGuideDialog = false }
    )
}

@Composable
private fun PresetCard(
    preset: WerewolfPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AppChoiceRow(
        title = preset.name,
        description = preset.description,
        selected = isSelected,
        onClick = onClick,
        leadingContent = {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(LocalAppDesign.current.cornerRadius.md),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = preset.playerCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun RolePreview(preset: WerewolfPreset) {
    val roleGroups = preset.roles.groupingBy { it }.eachCount().entries.toList()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        roleGroups.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (role, count) ->
                    val roleColor = roleColor(role)
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = roleColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(LocalAppDesign.current.cornerRadius.sm)
                    ) {
                        Text(
                            text = "${role.displayName}${if (count > 1) " ×$count" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = roleColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                            maxLines = 1
                        )
                    }
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun roleColor(role: WerewolfRole): Color = when (role.faction) {
    WerewolfFaction.WEREWOLF -> MaterialTheme.colorScheme.error
    WerewolfFaction.VILLAGER -> MaterialTheme.colorScheme.tertiary
    WerewolfFaction.INDEPENDENT -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun WerewolfRulesDialog(onDismiss: () -> Unit) {
    AppDialog(
        title = "一夜终极狼人规则",
        subtitle = "一夜一白天，无淘汰长流程，适合快速身份推理。",
        onDismiss = onDismiss,
        actions = {
            AppDialogActions(
                confirmText = "知道了",
                onConfirm = onDismiss
            )
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(400.dp)
        ) {
            item {
                RuleBlock(
                    "基础信息",
                    "人数：3-10人；总牌数为玩家数加三张中央底牌。阵营包括村民、狼人和独立阵营。"
                )
            }
            item {
                RuleBlock(
                    "夜间行动",
                    "设备会按化身幽灵、狼人、爪牙/守夜人、预言家、强盗、捣蛋鬼、酒鬼、失眠者的顺序提示行动。"
                )
            }
            item {
                RuleBlock(
                    "白天阶段",
                    "自由讨论后同时投票。最高票玩家出局并亮明身份；平票时最高票玩家全部出局。"
                )
            }
            item {
                RuleBlock(
                    "胜负判定",
                    "皮匠被投出时独立获胜；至少一名狼人出局或场上无狼人且无人出局时村民获胜；其余情况狼人获胜。"
                )
            }
        }
    }
}

@Composable
private fun RuleBlock(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
