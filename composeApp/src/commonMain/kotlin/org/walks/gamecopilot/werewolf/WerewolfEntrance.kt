package org.walks.gamecopilot.werewolf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.common.OfflinePassingGuideDialog
import org.walks.gamecopilot.werewolf.data.WerewolfFaction
import org.walks.gamecopilot.werewolf.data.WerewolfPreset
import org.walks.gamecopilot.werewolf.data.WerewolfPresets
import org.walks.gamecopilot.werewolf.data.WerewolfRole
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 * 一夜终极狼人 - 入口配置页
 */
@Composable
fun WerewolfEntrance(viewmodel: MainViewmodel, navi: NavHostController) {
    var showRulesDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var playerCount by remember { mutableIntStateOf(5) }
    val nicknames = remember { mutableStateListOf<String>().apply {
        val preset = WerewolfPresets.getPresetForPlayerCount(5)
        repeat(preset.playerCount) { add("玩家${it + 1}") }
    }}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "一夜终极狼人",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "标准版 · 3~10人 · 约10分钟",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            OutlinedButton(
                onClick = { showRulesDialog = true },
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.icon_info),
                    contentDescription = "规则",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("游戏规则", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 人数选择标题
        Text(
            text = "选择人数",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 预设列表 — 使用普通 Column（仅8项，无需 Lazy）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WerewolfPresets.presets.forEach { preset ->
                PresetCard(
                    preset = preset,
                    isSelected = playerCount == preset.playerCount,
                    onClick = {
                        playerCount = preset.playerCount
                        nicknames.clear()
                        repeat(preset.playerCount) { i ->
                            nicknames.add("玩家${i + 1}")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 角色预览
        val currentPreset = WerewolfPresets.getPresetForPlayerCount(playerCount)
        Text(
            text = "角色配置",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 角色标签 — 使用 FlowRow 风格手动换行
        val roleGroups = currentPreset.roles.groupBy { it }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            roleGroups.entries.chunked(4).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowItems.forEach { (role, count) ->
                        Surface(
                            color = getRoleColor(role).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${role.displayName}${if (count.size > 1) "×${count.size}" else ""}",
                                fontSize = 13.sp,
                                color = getRoleColor(role),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 开始按钮
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp)
                .clickable {
                    viewmodel.prepareOneNightWerewolfGame(playerCount, nicknames.toList())
                    viewmodel.handleGameIntent(
                        org.walks.gamecopilot.intent.GameIntent.SwitchGameMode(GameMode.ONE_NIGHT_WEREWOLF.ordinal)
                    )
                    navi.navigate(NaviRoute.ONE_NIGHT_WEREWOLF_GAME.route)
                },
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开始游戏",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 传机说明按钮
        OutlinedButton(
            onClick = { showGuideDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Text("单机传机流程说明", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // 规则弹窗
    if (showRulesDialog) {
        WerewolfRulesDialog(onDismiss = { showRulesDialog = false })
    }

    // 单机传机引导弹窗
    OfflinePassingGuideDialog(
        show = showGuideDialog,
        gameTitle = "一夜终极狼人",
        steps = listOf(
            "设备将作为主持人辅助工具，依次传递给每位玩家",
            "发牌阶段：每位玩家翻开设备查看自己的身份牌",
            "夜间阶段：设备按随机顺序传递，各角色执行夜间行动",
            "白天阶段：自由讨论后进入投票",
            "确保传递时其他人看不到屏幕内容"
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
    val bgColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 人数标识
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${preset.playerCount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = preset.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * 获取角色对应颜色
 * 狼人 → error，村民 → primary，独立 → tertiary
 */
private fun getRoleColor(role: WerewolfRole): Color {
    return when (role.faction) {
        WerewolfFaction.WEREWOLF -> Color(0xFFE74C3C)
        WerewolfFaction.VILLAGER -> Color(0xFF3498DB)
        WerewolfFaction.INDEPENDENT -> Color(0xFFE67E22)
    }
}

@Composable
private fun WerewolfRulesDialog(onDismiss: () -> Unit) {
    AppDialog(
        title = "一夜终极狼人规则",
        subtitle = "一夜一白天，无淘汰长流程，适合快速身份推理。",
        onDismiss = onDismiss,
        actions = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "知道了",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.height(400.dp)
        ) {
            item {
                RuleBlock(
                    "基础信息", """
人数：3~10人，无需淘汰，1夜+1白天，约10分钟
总牌数 = 玩家数 + 3（3张中央底牌）
阵营：村民阵营、狼人阵营、皮匠（独立）
                        """.trimIndent())
            }
            item {
                RuleBlock(
                    "夜间行动（按顺序）", """
①化身幽灵：查看1名玩家身份并复制
②狼人：互认队友；独狼可看1张底牌
③爪牙/守夜人：爪牙认狼，守夜人互认
④预言家：验1人 或 查看2张底牌
⑤强盗：交换身份并看新牌
⑥捣蛋鬼：互换两人身份，不看牌
⑦酒鬼：与底牌交换，不看新牌
⑧失眠者：查看自己最终身份
                        """.trimIndent())
            }
            item {
                RuleBlock(
                    "白天阶段", """
• 自由讨论约5分钟，可撒谎，禁止亮牌
• 倒数321同时投票，手指指向1名玩家
• 最高票出局并亮明身份；平票全部出局
• 全员互投（无人超1票）则无人出局
                        """.trimIndent())
            }
            item {
                RuleBlock(
                    "胜负判定", """
皮匠胜利（最高优先级）：皮匠被投票出局
村民胜利：至少1名狼人出局 或 场上无狼人且无人出局
狼人胜利：所有狼人存活 且 有玩家出局
                        """.trimIndent())
            }
        }
    }
}

@Composable
private fun RuleBlock(title: String, content: String) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = content,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
