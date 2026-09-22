package org.walks.gamecopilot.ui.page.hunttown
import org.walks.gamecopilot.distribution.AppDistribution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.GameStatsManager
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.ui.components.CommonTopBar
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppPrimaryAction
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.ui.components.common.OfflinePassingGuideDialog
import org.walks.gamecopilot.theme.LocalAppDesign
import kotlin.math.max

private enum class HuntRole { WITCH, SHERIFF, VILLAGER }

private data class HuntPlayer(
    val id: Int,
    val nickname: String,
    val role: HuntRole,
    val alive: Boolean = true,
    val revealed: Boolean = false
)

private enum class HuntPhase(val label: String) {
    SETUP("配置阶段"),
    DEAL_CARDS("传机查看身份"),
    NIGHT_CLOSE_EYES("夜晚：全体闭眼"),
    NIGHT_WITCH_OPEN("夜晚：女巫睁眼"),
    DAWN_ALERT("凌晨：持续提示睁眼"),
    NIGHT_SHERIFF_OPEN("夜晚：警长守护"),
    DAY_RESULT("白天：公布结果"),
    DAY_DISCUSS("白天：讨论与放逐"),
    GAME_END("游戏结束")
}

@Composable
fun HuntTownPage(onBack: () -> Unit) {
    var showGuideDialog by remember { mutableStateOf(false) }
    val design = LocalAppDesign.current
    var playerCount by remember { mutableStateOf(8) }
    var witchCount by remember { mutableStateOf(2) }
    var gameStarted by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(HuntPhase.SETUP) }
    val players = remember { mutableStateListOf<HuntPlayer>() }

    var selectedMurderTarget by remember { mutableStateOf<Int?>(null) }
    var selectedProtectTarget by remember { mutableStateOf<Int?>(null) }
    var lastNightDeath by remember { mutableStateOf<Int?>(null) }
    var winnerText by remember { mutableStateOf("") }
    var dealIndex by remember { mutableStateOf(0) }
    var dealRevealed by remember { mutableStateOf(false) }
    var dayExiled by remember { mutableStateOf(false) }

    LaunchedEffect(phase) {
        if (phase == HuntPhase.DAWN_ALERT) {
            PlatformHelper.getInstance().startPersistentAlert()
        } else {
            PlatformHelper.getInstance().stopPersistentAlert()
        }
    }

    LaunchedEffect(phase, winnerText) {
        if (phase == HuntPhase.GAME_END && winnerText.isNotBlank()) {
            GameStatsManager.completeLatestGame(GameMode.HUNT_TOWN, winnerText)
        }
    }

    DisposableEffect(Unit) {
        onDispose { PlatformHelper.getInstance().stopPersistentAlert() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CommonTopBar(
            title = "猎巫镇",
            subtitle = "同机主持 · 单身份简化玩法",
            onBack = onBack,
            customAction = {
                TextButton(onClick = { showGuideDialog = true }) { Text("玩法") }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = design.spacing.xl)
                .padding(bottom = design.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(design.spacing.lg)
        ) {
            if (gameStarted) {
                AppCard(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(phase.label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    if (winnerText.isNotBlank()) {
                        Text(winnerText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!gameStarted) {
                SetupPanel(
                    playerCount = playerCount,
                    witchCount = witchCount,
                    onPlayerCountChange = {
                        playerCount = it
                        witchCount = witchCount.coerceAtMost(max(1, it / 3))
                    },
                    onWitchCountChange = { witchCount = it },
                    onStart = {
                        PlatformHelper.getInstance().vibrateLongMethod()
                        players.clear()
                        val roles = buildList {
                            repeat(witchCount) { add(HuntRole.WITCH) }
                            add(HuntRole.SHERIFF)
                            repeat(playerCount - witchCount - 1) { add(HuntRole.VILLAGER) }
                        }.shuffled()

                        roles.forEachIndexed { index, role ->
                            players.add(
                                HuntPlayer(
                                    id = index,
                                    nickname = "${index + 1}号",
                                    role = role
                                )
                            )
                        }
                        gameStarted = true
                        phase = HuntPhase.DEAL_CARDS
                        dealIndex = 0; dealRevealed = false; dayExiled = false
                        selectedMurderTarget = null; selectedProtectTarget = null; lastNightDeath = null
                        winnerText = ""
                        GameStatsManager.recordGameStart(GameMode.HUNT_TOWN, playerCount)
                    }
                )
            } else if (phase == HuntPhase.DEAL_CARDS) {
                AppCard {
                    val player = players[dealIndex]
                    AppSectionHeader("请将设备递给 ${player.nickname}", subtitle = "${dealIndex + 1} / ${players.size} · 查看时请避开其他玩家")
                    if (dealRevealed) {
                        Text(when(player.role) { HuntRole.WITCH -> "女巫"; HuntRole.SHERIFF -> "警长"; HuntRole.VILLAGER -> "村民" }, style = MaterialTheme.typography.headlineLarge)
                        if(player.role == HuntRole.WITCH) Text("女巫同伴：" + players.filter { it.role == HuntRole.WITCH && it.id != player.id }.joinToString("、") { it.nickname }.ifEmpty { "无" })
                    }
                    AppPrimaryAction(
                        text = if (!dealRevealed) "点击查看身份" else if (dealIndex == players.lastIndex) "隐藏并交还主持人" else "隐藏并交给下一位",
                        onClick = {
                            if (dealRevealed) {
                                dealRevealed = false
                                if (dealIndex == players.lastIndex) phase = HuntPhase.NIGHT_CLOSE_EYES else dealIndex++
                            } else dealRevealed = true
                        }
                    )
                }
            } else {
                HuntPhasePanel(
                    phase = phase,
                    players = players,
                    selectedMurderTarget = selectedMurderTarget,
                    selectedProtectTarget = selectedProtectTarget,
                    lastNightDeath = lastNightDeath,
                    dayExiled = dayExiled,
                    onSelectMurderTarget = { selectedMurderTarget = it },
                    onSelectProtectTarget = { selectedProtectTarget = if (players.any { p -> p.alive && p.role == HuntRole.SHERIFF }) it else null },
                    onNext = {
                        PlatformHelper.getInstance().vibrateMethod()
                        when (phase) {
                            HuntPhase.NIGHT_CLOSE_EYES -> phase = HuntPhase.NIGHT_WITCH_OPEN
                            HuntPhase.NIGHT_WITCH_OPEN -> phase = HuntPhase.DAWN_ALERT
                            HuntPhase.DAWN_ALERT -> {
                                selectedProtectTarget = null
                                phase = HuntPhase.NIGHT_SHERIFF_OPEN
                            }
                            HuntPhase.NIGHT_SHERIFF_OPEN -> {
                                val murdered = selectedMurderTarget
                                val protected = selectedProtectTarget
                                if (murdered != null && murdered != protected) {
                                    PlatformHelper.getInstance().vibrateLongMethod()
                                    val idx = players.indexOfFirst { it.id == murdered }
                                    if (idx >= 0 && players[idx].alive) {
                                        players[idx] = players[idx].copy(alive = false, revealed = true)
                                        lastNightDeath = murdered
                                    }
                                } else {
                                    lastNightDeath = null
                                }
                                selectedMurderTarget = null
                                selectedProtectTarget = null
                                phase = HuntPhase.DAY_RESULT
                            }

                            HuntPhase.DAY_RESULT -> {
                                dayExiled = false
                                winnerText = when {
                                    players.none { it.alive && it.role == HuntRole.WITCH } -> "村民阵营胜利：所有女巫都已被翻开。"
                                    players.none { it.alive && it.role != HuntRole.WITCH } -> "女巫阵营胜利：所有村民均已出局。"
                                    else -> ""
                                }
                                phase = if (winnerText.isEmpty()) HuntPhase.DAY_DISCUSS else HuntPhase.GAME_END
                            }
                            HuntPhase.DAY_DISCUSS -> {
                                val witchesAlive = players.count { it.alive && it.role == HuntRole.WITCH }
                                val villagersAlive = players.count { it.alive && it.role != HuntRole.WITCH }
                                when {
                                    witchesAlive == 0 -> {
                                        PlatformHelper.getInstance().vibrateLongMethod()
                                        winnerText = "村民阵营胜利：所有女巫都已被翻开。"
                                        phase = HuntPhase.GAME_END
                                    }

                                    villagersAlive == 0 -> {
                                        PlatformHelper.getInstance().vibrateLongMethod()
                                        winnerText = "女巫阵营胜利：所有村民均已出局。"
                                        phase = HuntPhase.GAME_END
                                    }

                                    else -> phase = HuntPhase.NIGHT_CLOSE_EYES
                                }
                            }

                            HuntPhase.GAME_END -> {
                                gameStarted = false
                                phase = HuntPhase.SETUP
                            }

                            HuntPhase.SETUP, HuntPhase.DEAL_CARDS -> Unit
                        }
                    },
                    onRevealPlayer = { playerId ->
                        val idx = players.indexOfFirst { it.id == playerId }
                        if (idx >= 0 && players[idx].alive && !dayExiled) {
                            PlatformHelper.getInstance().vibrateMethod()
                            players[idx] = players[idx].copy(alive = false, revealed = true)
                            dayExiled = true
                        }
                    }
                )
            }
        }
    }

    OfflinePassingGuideDialog(
        show = showGuideDialog,
        gameTitle = "猎巫镇",
        steps = listOf(
            "这是 4–12 人的单身份简化玩法，不包含原版的多张审判牌、手牌与阴谋传染。",
            "同机需要额外一位不参赛主持人。玩家先依次查看身份，再将设备交给主持人；人数不含主持人。",
            "夜间由主持人避开其他玩家的视线记录女巫目标与警长守护。" +
                if (AppDistribution.roomsEnabled) "参赛玩家请使用网络房间分别操作。" else "主持人不参与对局。",
            "主持人按阶段推进：闭眼 -> 女巫行动 -> 凌晨提示 -> 警长守护 -> 白天结算。",
            "凌晨阶段会播放持续提示音，确认“睁眼”后会停止。",
            "白天可翻开一名玩家身份作为放逐，系统自动判断阵营胜负。"
        ),
        onDismiss = { showGuideDialog = false }
    )
}

@Composable
private fun SetupPanel(
    playerCount: Int,
    witchCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    onWitchCountChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    val maxWitches = max(1, playerCount / 3)
    AppCard {
        AppSectionHeader("同机主持", subtitle = "4–12 位玩家 · 额外一位不参赛主持人")
        Text("玩家轮流查看身份后，将设备交给主持人，由主持人私下记录夜间行动。" +
            if (AppDistribution.roomsEnabled) "所有人都想参赛时，建议使用网络房间。" else "主持人不参与对局。",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("当前为单身份简化玩法，未包含原版的多张审判牌、手牌与阴谋传染。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    AppCard {
            AppSectionHeader("人数配置", subtitle = "人数不含主持人")
            NumberSelectorRow("玩家人数", playerCount, (4..12).toList(), onPlayerCountChange)
            NumberSelectorRow("女巫人数", witchCount, (1..maxWitches).toList(), onWitchCountChange)
            Text("警长人数固定为 1，其余为村民。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AppPrimaryAction(text = "开始传机发牌", onClick = onStart)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NumberSelectorRow(
    title: String,
    current: Int,
    candidates: List<Int>,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$title：$current")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            candidates.forEach { number ->
                val selected = number == current
                FilterChip(selected = selected, onClick = { onSelect(number) }, label = { Text(number.toString()) })
            }
        }
    }
}

@Composable
private fun HuntPhasePanel(
    phase: HuntPhase,
    players: List<HuntPlayer>,
    selectedMurderTarget: Int?,
    selectedProtectTarget: Int?,
    lastNightDeath: Int?,
    dayExiled: Boolean,
    onSelectMurderTarget: (Int?) -> Unit,
    onSelectProtectTarget: (Int?) -> Unit,
    onNext: () -> Unit,
    onRevealPlayer: (Int) -> Unit
) {
    val alivePlayers = players.filter { it.alive }
    AppCard {
            when (phase) {
                HuntPhase.NIGHT_CLOSE_EYES -> {
                    Text("请所有玩家闭眼，进入夜晚。")
                }

                HuntPhase.NIGHT_WITCH_OPEN -> {
                    Text("女巫睁眼：请选择本夜谋杀目标（仅可选存活玩家）")
                    alivePlayers.forEach { p ->
                        SelectablePlayerRow(
                            text = "${p.nickname}",
                            selected = selectedMurderTarget == p.id,
                            onClick = { onSelectMurderTarget(p.id) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { onSelectMurderTarget(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("跳过（本夜不杀人）")
                    }
                }

                HuntPhase.DAWN_ALERT -> {
                    Text("凌晨阶段：持续提示音已播放，请玩家睁眼确认。")
                    Text("确认后点击“下一步”停止提示音并进入警长守护。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HuntPhase.NIGHT_SHERIFF_OPEN -> {
                    Text(if (alivePlayers.any { it.role == HuntRole.SHERIFF }) "警长睁眼：请选择要守护的玩家" else "警长已出局，本夜无人守护。")
                    if (alivePlayers.any { it.role == HuntRole.SHERIFF }) alivePlayers.forEach { p ->
                        SelectablePlayerRow(
                            text = "${p.nickname}",
                            selected = selectedProtectTarget == p.id,
                            onClick = { onSelectProtectTarget(p.id) }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { onSelectProtectTarget(null) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("跳过（本夜不守护）")
                    }
                }

                HuntPhase.DAY_RESULT -> {
                    Text(
                        if (lastNightDeath == null) "昨夜无人死亡（可能被守护成功）。"
                        else "昨夜死亡：${lastNightDeath + 1}号。"
                    )
                }

                HuntPhase.DAY_DISCUSS -> {
                    Text(if (dayExiled) "本日已放逐一名玩家，请点击下一步。" else "白天讨论与放逐：点击一名存活玩家翻开身份并出局。")
                    alivePlayers.forEach { p ->
                        OutlinedButton(
                            onClick = { onRevealPlayer(p.id) },
                            enabled = !dayExiled,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("放逐并翻开：${p.nickname}")
                        }
                    }
                }

                HuntPhase.GAME_END -> {
                    Text("已结算胜负，点击“下一步”返回配置阶段。")
                }

                HuntPhase.SETUP, HuntPhase.DEAL_CARDS -> Unit
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("玩家状态", fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(players) { p ->
                    val roleText = when (p.role) {
                        HuntRole.WITCH -> "女巫"
                        HuntRole.SHERIFF -> "警长"
                        HuntRole.VILLAGER -> "村民"
                    }
                    val stateText = if (p.alive) "存活" else "出局"
                    val revealText = if (p.revealed || !p.alive) roleText else "未翻开"
                    Text("${p.nickname} · $stateText · $revealText")
                }
            }

            AppPrimaryAction(text = if (phase == HuntPhase.GAME_END) "返回配置" else "下一步", onClick = onNext)
    }
}

@Composable
private fun SelectablePlayerRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(LocalAppDesign.current.cornerRadius.button)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
    }
}
