package org.walks.gamecopilot.werewolf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import org.walks.gamecopilot.data.GameStatsManager
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.ui.components.AppCard
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
                AppCard {
                    AppSectionHeader(title = "选择人数", subtitle = "人数确定后，自动配好角色与三张中央底牌")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WerewolfPresets.presets.forEach { preset ->
                            FilterChip(
                                selected = playerCount == preset.playerCount,
                                onClick = {
                                    playerCount = preset.playerCount
                                    nicknames.clear()
                                    repeat(preset.playerCount) { nicknames.add("玩家${it + 1}") }
                                },
                                label = { Text("${preset.playerCount} 人") },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                    Text(currentPreset.description, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Spacer(modifier = Modifier.height(design.spacing.sm))
                AppSectionHeader(
                    title = "本局角色",
                    subtitle = "${currentPreset.playerCount} 张玩家牌 + 3 张中央底牌"
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
                GameStatsManager.recordGameStart(GameMode.ONE_NIGHT_WEREWOLF, playerCount)
                navi.navigate(NaviRoute.ONE_NIGHT_WEREWOLF_GAME.route)
            },
            supportingText = "将手机交给第 1 位玩家，每次看完请先隐藏身份。"
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
            Text("同机游玩说明")
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
            "所有玩家看完后，设备按规则夜间顺序提示各角色行动。",
            "夜间结束后自由讨论并投票，设备会协助判断胜负。",
            "交接设备前滑回隐藏面，避免旁人看到身份。"
        ),
        onDismiss = { showGuideDialog = false }
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
                    "猎人出局会带走自己投票指向的人。皮匠与狼人同时出局时，皮匠和村民可同时获胜；场上无狼人时还需按爪牙和是否有人出局判定，可能无人获胜。"
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
