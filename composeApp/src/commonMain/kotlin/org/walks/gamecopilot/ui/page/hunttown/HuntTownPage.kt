package org.walks.gamecopilot.ui.page.hunttown

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import org.walks.gamecopilot.ui.components.CommonTopBar
import org.walks.gamecopilot.ui.components.common.OfflinePassingGuideDialog
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
    var showGuideDialog by remember { mutableStateOf(true) }
    var playerCount by remember { mutableStateOf(8) }
    var witchCount by remember { mutableStateOf(2) }
    var gameStarted by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(HuntPhase.SETUP) }
    val players = remember { mutableStateListOf<HuntPlayer>() }

    var selectedMurderTarget by remember { mutableStateOf<Int?>(null) }
    var selectedProtectTarget by remember { mutableStateOf<Int?>(null) }
    var lastNightDeath by remember { mutableStateOf<Int?>(null) }
    var winnerText by remember { mutableStateOf("") }

    LaunchedEffect(phase) {
        if (phase == HuntPhase.DAWN_ALERT) {
            PlatformHelper.getInstance().startPersistentAlert()
        } else {
            PlatformHelper.getInstance().stopPersistentAlert()
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
            subtitle = "单机主持流程",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("当前阶段：${phase.label}", fontWeight = FontWeight.Bold)
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
                        phase = HuntPhase.NIGHT_CLOSE_EYES
                        winnerText = ""
                    }
                )
            } else {
                HuntPhasePanel(
                    phase = phase,
                    players = players,
                    selectedMurderTarget = selectedMurderTarget,
                    selectedProtectTarget = selectedProtectTarget,
                    lastNightDeath = lastNightDeath,
                    onSelectMurderTarget = { selectedMurderTarget = it },
                    onSelectProtectTarget = { selectedProtectTarget = it },
                    onNext = {
                        PlatformHelper.getInstance().vibrateMethod()
                        when (phase) {
                            HuntPhase.NIGHT_CLOSE_EYES -> phase = HuntPhase.NIGHT_WITCH_OPEN
                            HuntPhase.NIGHT_WITCH_OPEN -> phase = HuntPhase.DAWN_ALERT
                            HuntPhase.DAWN_ALERT -> phase = HuntPhase.NIGHT_SHERIFF_OPEN
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

                            HuntPhase.DAY_RESULT -> phase = HuntPhase.DAY_DISCUSS
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

                            HuntPhase.SETUP -> Unit
                        }
                    },
                    onRevealPlayer = { playerId ->
                        val idx = players.indexOfFirst { it.id == playerId }
                        if (idx >= 0 && players[idx].alive) {
                            PlatformHelper.getInstance().vibrateMethod()
                            players[idx] = players[idx].copy(alive = false, revealed = true)
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("人数配置", fontWeight = FontWeight.Bold)
            NumberSelectorRow("总人数", playerCount, (4..12).toList(), onPlayerCountChange)
            NumberSelectorRow("女巫人数", witchCount, (1..maxWitches).toList(), onWitchCountChange)
            Text("警长人数固定为 1，其余为村民。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("开始猎巫镇") }
        }
    }
}

@Composable
private fun NumberSelectorRow(
    title: String,
    current: Int,
    candidates: List<Int>,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$title：$current")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            candidates.forEach { number ->
                val selected = number == current
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(number) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(number.toString(), color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
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
    onSelectMurderTarget: (Int?) -> Unit,
    onSelectProtectTarget: (Int?) -> Unit,
    onNext: () -> Unit,
    onRevealPlayer: (Int) -> Unit
) {
    val alivePlayers = players.filter { it.alive }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Text("警长睁眼：请选择要守护的玩家（可守护任意存活玩家，不能同时阻止多目标）")
                    alivePlayers.forEach { p ->
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
                    Text("白天讨论与放逐：点击一名存活玩家翻开身份并出局。")
                    alivePlayers.forEach { p ->
                        OutlinedButton(
                            onClick = { onRevealPlayer(p.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("放逐并翻开：${p.nickname}")
                        }
                    }
                }

                HuntPhase.GAME_END -> {
                    Text("已结算胜负，点击“下一步”返回配置阶段。")
                }

                HuntPhase.SETUP -> Unit
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

            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (phase == HuntPhase.GAME_END) "返回配置" else "下一步")
            }
        }
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
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
    }
}
