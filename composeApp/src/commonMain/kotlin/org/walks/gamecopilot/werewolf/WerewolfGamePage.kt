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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.intent.AiIntent
import org.walks.gamecopilot.ui.components.AiMessageBubble
import org.walks.gamecopilot.werewolf.components.WerewolfIdentityCard
import org.walks.gamecopilot.werewolf.components.getRoleColor
import org.walks.gamecopilot.werewolf.components.getRoleIcon
import org.walks.gamecopilot.werewolf.data.NightActionSubStep
import org.walks.gamecopilot.werewolf.data.WerewolfFaction
import org.walks.gamecopilot.werewolf.data.WerewolfGamePhase
import org.walks.gamecopilot.werewolf.data.WerewolfGameState
import org.walks.gamecopilot.werewolf.data.WerewolfPlayer
import org.walks.gamecopilot.werewolf.data.WerewolfPresets
import org.walks.gamecopilot.werewolf.data.WerewolfRole
import org.walks.gamecopilot.werewolf.theme.WerewolfColors

/**
 * 一夜终极狼人 - 游戏主页面
 * 单机模式：设备作为主持人辅助工具，隐私传递模式
 *
 * 核心隐私保护：
 * - 所有传递屏幕只显示玩家昵称，不显示角色信息
 * - 角色信息仅在玩家个人确认身份后显示
 * - 旁观者只能看到"请将设备递给XX"
 */
@Composable
fun WerewolfGamePage(
    viewmodel: org.walks.gamecopilot.MainViewmodel,
    onBack: () -> Unit
) {
    val configuredPlayerCount by viewmodel.oneNightWerewolfPlayerCount.collectAsState()
    val configuredNicknames by viewmodel.oneNightWerewolfNicknames.collectAsState()

    fun createInitialGameState(): WerewolfGameState {
        val preset = WerewolfPresets.getPresetForPlayerCount(configuredPlayerCount)
        return WerewolfGameLogic.initializeGame(preset, configuredNicknames)
    }

    var gameState by remember(configuredPlayerCount, configuredNicknames) {
        mutableStateOf(createInitialGameState())
    }

    val votes = remember { mutableStateListOf<Pair<Int, Int>>() }

    // AI 旁白状态
    val aiMessage by viewmodel.aiMessage.collectAsState()
    val isLoadingAi by viewmodel.isLoadingAi.collectAsState()

    // 构建当前阶段的 AI 上下文
    val aiContext = when (gameState.phase) {
        WerewolfGamePhase.NIGHT_START -> "一夜终极狼人游戏：天黑了，所有玩家闭眼。请作为旁白播报氛围描述。"
        WerewolfGamePhase.NIGHT_ACTION -> "一夜终极狼人游戏：夜间行动进行中，步骤${gameState.currentNightStep + 1}/${gameState.nightActionOrder.size}。请简短描述夜晚氛围。"
        WerewolfGamePhase.DAY_DISCUSSION -> "一夜终极狼人游戏：天亮了，${gameState.players.filter { it.isAlive }.size}名存活玩家进入讨论。请给出推理线索提示。"
        WerewolfGamePhase.DAY_VOTING -> "一夜终极狼人游戏：投票阶段，请作为旁白营造紧张氛围。"
        WerewolfGamePhase.GAME_OVER -> {
            val winner = when (gameState.winner) {
                WerewolfFaction.VILLAGER -> "村民阵营"
                WerewolfFaction.WEREWOLF -> "狼人阵营"
                WerewolfFaction.INDEPENDENT -> "皮匠"
                else -> "未知"
            }
            "一夜终极狼人游戏结束，${winner}胜利！请总结这场对局。"
        }

        else -> "一夜终极狼人游戏进行中，请作为旁白简短点评。"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WerewolfColors.background)
    ) {
        // 暗色顶部栏 - 保持沉浸感
        WerewolfTopBar(
            title = when (gameState.phase) {
                WerewolfGamePhase.SETUP -> "一夜终极狼人"
                WerewolfGamePhase.DEAL_CARDS -> "查看身份"
                WerewolfGamePhase.NIGHT_START -> "夜幕降临"
                WerewolfGamePhase.NIGHT_ACTION -> "夜间行动"
                WerewolfGamePhase.DAY_DISCUSSION -> "白天讨论"
                WerewolfGamePhase.DAY_VOTING -> "投票阶段"
                WerewolfGamePhase.VOTE_RESULT -> "投票结果"
                WerewolfGamePhase.HUNTER_ACTION -> "猎人行动"
                WerewolfGamePhase.GAME_OVER -> "游戏结束"
            },
            onBack = onBack
        )

        Box(modifier = Modifier.weight(1f)) {
            when (gameState.phase) {
                WerewolfGamePhase.SETUP -> {
                    LaunchedEffect(configuredPlayerCount, configuredNicknames) {
                        gameState = createInitialGameState()
                    }
                }

                WerewolfGamePhase.DEAL_CARDS -> {
                    DealCardsPhase(
                        gameState = gameState,
                        onRevealCard = {
                            gameState = gameState.copy(dealCardRevealed = true)
                        },
                        onConfirmCard = {
                            val nextIndex = gameState.dealCardPlayerIndex + 1
                            if (nextIndex >= gameState.playerCount) {
                                gameState = gameState.copy(
                                    phase = WerewolfGamePhase.NIGHT_START,
                                    dealCardPlayerIndex = 0,
                                    dealCardRevealed = false
                                )
                            } else {
                                gameState = gameState.copy(
                                    dealCardPlayerIndex = nextIndex,
                                    dealCardRevealed = false
                                )
                            }
                        }
                    )
                }

                WerewolfGamePhase.NIGHT_START -> {
                    NightStartPhase(
                        onContinue = {
                            gameState = gameState.copy(
                                phase = WerewolfGamePhase.NIGHT_ACTION,
                                currentNightStep = 0,
                                nightSubStep = NightActionSubStep.HAND_OFF
                            )
                        }
                    )
                }

            WerewolfGamePhase.NIGHT_ACTION -> {
                NightActionPhase(
                    gameState = gameState,
                    onSubStepChange = { subStep ->
                        gameState = gameState.copy(nightSubStep = subStep)
                    },
                    onActionComplete = { newState, resultText ->
                        gameState = newState.copy(
                            nightSubStep = NightActionSubStep.RESULT,
                            nightActionResultText = resultText
                        )
                        PlatformHelper.getInstance().vibrateMethod()
                    },
                    onResultConfirmed = {
                        // 如果化身幽灵有待执行的额外行动，回到 ACTION 子步骤
                        if (gameState.doppelgangerPendingAction) {
                            gameState = WerewolfGameLogic.clearDoppelgangerPendingAction(gameState)
                            gameState = gameState.copy(nightSubStep = NightActionSubStep.ACTION)
                        } else {
                            advanceNightStep(gameState) { newState -> gameState = newState }
                        }
                    },
                    onSkipAction = {
                        advanceNightStep(gameState) { newState -> gameState = newState }
                    }
                )
            }

            WerewolfGamePhase.DAY_DISCUSSION -> {
                DayDiscussionPhase(
                    gameState = gameState,
                    onStartVoting = {
                        gameState = gameState.copy(
                            phase = WerewolfGamePhase.DAY_VOTING,
                            currentVoterIndex = 0
                        )
                        votes.clear()
                    }
                )
            }

            WerewolfGamePhase.DAY_VOTING -> {
                VotingPhase(
                    gameState = gameState,
                    votes = votes,
                    onVote = { voterId, targetId ->
                        votes.add(voterId to targetId)
                        val nextVoter = gameState.currentVoterIndex + 1
                        if (nextVoter >= gameState.players.size) {
                            val updatedPlayers = gameState.players.map { player ->
                                val myVote = votes.find { it.first == player.id }?.second
                                player.copy(voteTarget = myVote)
                            }
                            gameState = gameState.copy(players = updatedPlayers)
                            gameState = WerewolfGameLogic.resolveVotes(gameState)

                            if (gameState.hunterPending) {
                                gameState = gameState.copy(phase = WerewolfGamePhase.HUNTER_ACTION)
                            } else {
                                val winner = WerewolfGameLogic.determineWinner(gameState)
                                gameState = gameState.copy(
                                    phase = WerewolfGamePhase.GAME_OVER,
                                    winner = winner
                                )
                            }
                        } else {
                            gameState = gameState.copy(currentVoterIndex = nextVoter)
                        }
                    }
                )
            }

            WerewolfGamePhase.VOTE_RESULT -> {
                // 已合并到 GAME_OVER
            }

            WerewolfGamePhase.HUNTER_ACTION -> {
                HunterActionPhase(
                    gameState = gameState,
                    onHunterShoot = { targetId ->
                        gameState = WerewolfGameLogic.hunterShoot(gameState, targetId)
                        val winner = WerewolfGameLogic.determineWinner(gameState)
                        gameState = gameState.copy(
                            phase = WerewolfGamePhase.GAME_OVER,
                            winner = winner
                        )
                    }
                )
            }

            WerewolfGamePhase.GAME_OVER -> {
                GameOverPhase(
                    gameState = gameState,
                    onRestart = {
                        gameState = createInitialGameState()
                        votes.clear()
                    },
                    onBack = onBack
                )
            }
        }
        }

        // AI 旁白消息气泡
        AiMessageBubble(
            message = aiMessage,
            isLoading = isLoadingAi,
            onRefresh = {
                viewmodel.handleAiIntent(AiIntent.SendMessage("werewolf", aiContext))
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

/**
 * 推进夜间行动到下一步
 */
private fun advanceNightStep(gameState: WerewolfGameState, setState: (WerewolfGameState) -> Unit) {
    val nextStep = gameState.currentNightStep + 1
    if (nextStep >= gameState.nightActionOrder.size) {
        // 夜间行动全部完成 → 统一结算所有交换，计算最终 currentRole
        val finalizedState = WerewolfGameLogic.finalizeNightActions(gameState)
        setState(finalizedState.copy(phase = WerewolfGamePhase.DAY_DISCUSSION))
    } else {
        setState(gameState.copy(
            currentNightStep = nextStep,
            nightSubStep = NightActionSubStep.HAND_OFF,
            nightActionResultText = ""
        ))
    }
}

// ===== 配置阶段 =====
@Composable
private fun SetupPhase(
    playerCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    nicknames: List<String>,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "游戏配置",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WerewolfColors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "选择人数后开始游戏，设备将作为主持人辅助",
                fontSize = 14.sp,
                color = WerewolfColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "选择人数",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WerewolfColors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            (3..10).chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { count ->
                        val selected = playerCount == count
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { onPlayerCountChange(count) },
                            color = if (selected) WerewolfColors.primary else WerewolfColors.surfaceContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${count}人",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) WerewolfColors.onPrimary else WerewolfColors.onSurface
                                )
                            }
                        }
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val preset = WerewolfPresets.getPresetForPlayerCount(playerCount)
            Text(
                text = "角色配置 (${preset.name})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WerewolfColors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                preset.roles.groupBy { it }.forEach { (role, instances) ->
                    Surface(
                        color = getRoleColor(role).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${role.displayName}${if (instances.size > 1) "×${instances.size}" else ""}",
                            fontSize = 13.sp,
                            color = getRoleColor(role),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "玩家列表",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WerewolfColors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            nicknames.forEachIndexed { idx, name ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    color = WerewolfColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(WerewolfColors.surfaceContainerHighest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 13.sp, color = WerewolfColors.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = name, fontSize = 15.sp, color = WerewolfColors.onSurface)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onStart),
            color = WerewolfColors.primary,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "开始游戏",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WerewolfColors.onPrimary
                )
            }
        }
    }
}

// ===== 发牌/查看身份阶段 =====
@Composable
private fun DealCardsPhase(
    gameState: WerewolfGameState,
    onRevealCard: () -> Unit,
    onConfirmCard: () -> Unit
) {
    val playerIndex = gameState.dealCardPlayerIndex
    val player = gameState.players.getOrNull(playerIndex) ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部进度
            Text(
                text = "查看身份 ${playerIndex + 1}/${gameState.playerCount}",
                fontSize = 14.sp,
                color = WerewolfColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(0.6f).height(4.dp),
                color = WerewolfColors.surfaceContainer,
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((playerIndex + 1).toFloat() / gameState.playerCount)
                        .background(WerewolfColors.primary, RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!gameState.dealCardRevealed) {
                // 未翻开 - 传递提示
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WerewolfColors.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "请将设备递给", fontSize = 16.sp, color = WerewolfColors.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = player.nickname,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = WerewolfColors.primary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "确保其他人看不到屏幕", fontSize = 13.sp, color = WerewolfColors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(52.dp)
                        .clickable(onClick = onRevealCard),
                    color = WerewolfColors.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "我是 ${player.nickname}，翻开身份",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WerewolfColors.onPrimary
                        )
                    }
                }
            } else {
                // 已翻开 - 使用公共身份卡组件
                WerewolfIdentityCard(
                    resetKey = "${player.id}-${player.initialRole}",
                    playerNumber = player.id + 1,
                    nickname = player.nickname,
                    role = player.initialRole,
                    showDescription = true,
                    onClose = onConfirmCard
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                        .clickable(onClick = onConfirmCard),
                    color = WerewolfColors.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "我记住了",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WerewolfColors.onSurface
                        )
                    }
                }
            }
        }
    }
}

// ===== 夜晚开始 =====
@Composable
private fun NightStartPhase(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(WerewolfColors.surfaceAlt, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DarkMode,
                    contentDescription = "夜晚",
                    modifier = Modifier.size(40.dp),
                    tint = WerewolfColors.onSurface
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "天黑请闭眼",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WerewolfColors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "所有人闭上眼睛\n接下来将依次传递设备执行夜间行动",
                fontSize = 14.sp,
                color = WerewolfColors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .clickable(onClick = onContinue),
                color = WerewolfColors.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("开始夜间行动", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WerewolfColors.onPrimary)
                }
            }
        }
    }
}

// ===== 夜间行动（隐私传递模式 - 按玩家编号）=====
/**
 * 夜间行动核心：
 * - 夜间行动顺序是按玩家ID排列的（内部基于角色nightOrder排序，但UI不暴露）
 * - 传递屏幕只显示"请将设备递给XX"，不显示角色信息
 * - 玩家确认身份后看到自己的角色操作界面
 * - 无夜间行动的玩家快速跳过
 */
@Composable
private fun NightActionPhase(
    gameState: WerewolfGameState,
    onSubStepChange: (NightActionSubStep) -> Unit,
    onActionComplete: (WerewolfGameState, String) -> Unit,
    onResultConfirmed: () -> Unit,
    onSkipAction: () -> Unit
) {
    val stepIndex = gameState.currentNightStep
    val player = WerewolfGameLogic.getCurrentNightActionPlayer(gameState, stepIndex)

    if (player == null) {
        onSkipAction()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部进度条 - 只显示步骤编号，不显示角色名
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "夜间行动 ${stepIndex + 1}/${gameState.nightActionOrder.size}",
                fontSize = 14.sp,
                color = WerewolfColors.onSurfaceVariant
            )
            // 不再显示角色名称！只显示步骤数
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(4.dp),
            color = WerewolfColors.surfaceContainer,
            shape = RoundedCornerShape(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((stepIndex + 1).toFloat() / gameState.nightActionOrder.size)
                    .background(WerewolfColors.primary, RoundedCornerShape(2.dp))
            )
        }

        // 子步骤内容
        when (gameState.nightSubStep) {
            NightActionSubStep.HAND_OFF -> {
                NightHandOffScreen(
                    player = player,
                    onReady = { onSubStepChange(NightActionSubStep.ACTION) },
                    onSkip = onSkipAction
                )
            }
            NightActionSubStep.ACTION -> {
                NightActionScreen(
                    player = player,
                    gameState = gameState,
                    onActionComplete = onActionComplete,
                    onSkip = onSkipAction,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
            NightActionSubStep.RESULT -> {
                NightResultScreen(
                    player = player,
                    resultText = gameState.nightActionResultText,
                    onConfirm = onResultConfirmed
                )
            }
        }
    }
}

/**
 * 夜间传递屏幕：只显示玩家昵称，不显示角色信息
 */
@Composable
private fun NightHandOffScreen(
    player: WerewolfPlayer,
    onReady: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(WerewolfColors.surfaceAlt, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Smartphone,
                    contentDescription = "传递设备",
                    modifier = Modifier.size(32.dp),
                    tint = WerewolfColors.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "请将设备递给",
                fontSize = 16.sp,
                color = WerewolfColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = player.nickname,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WerewolfColors.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "确保其他人看不到屏幕",
                fontSize = 13.sp,
                color = WerewolfColors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .clickable(onClick = onReady),
                color = WerewolfColors.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "我是 ${player.nickname}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WerewolfColors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(40.dp)
                    .clickable(onClick = onSkip),
                color = WerewolfColors.surfaceContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("跳过", fontSize = 14.sp, color = WerewolfColors.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 夜间行动屏幕：玩家确认身份后，看到自己的角色操作界面
 * 只有拿到设备的玩家能看到，旁观者看不到
 */
@Composable
private fun NightActionScreen(
    player: WerewolfPlayer,
    gameState: WerewolfGameState,
    onActionComplete: (WerewolfGameState, String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val role = player.initialRole
    val roleColor = getRoleColor(role)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 角色信息 - 现在只有持有者能看到
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getRoleIcon(role),
                    contentDescription = role.displayName,
                    modifier = Modifier.size(22.dp),
                    tint = roleColor
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = role.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor
                )
                Text(
                    text = role.description,
                    fontSize = 12.sp,
                    color = WerewolfColors.onSurfaceMedium,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 角色特定操作界面
        when (role) {
            WerewolfRole.DOPPELGANGER -> {
                // 如果化身幽灵已完成复制，且有待执行的额外行动
                val copiedRole = gameState.doppelgangerCopiedRole
                if (gameState.doppelgangerPendingAction && copiedRole != null) {
                    // 显示被复制角色的行动界面
                    DoppelgangerFollowUpAction(
                        copiedRole = copiedRole,
                        player = player,
                        gameState = gameState,
                        onActionComplete = onActionComplete
                    )
                } else {
                    Text("选择要复制的玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    PlayerSelectionGrid(
                        players = gameState.players,
                        excludeIds = setOf(player.id),
                        onSelect = { targetId ->
                            val (newState, result) = WerewolfGameLogic.executeDoppelgangerAction(gameState, player.id, targetId)
                            onActionComplete(newState, result)
                        }
                    )
                }
            }

            WerewolfRole.WEREWOLF -> {
                val isLone = WerewolfGameLogic.isLoneWolf(gameState, player.id)
                if (isLone) {
                    Text("你是独狼！选择1张中央底牌查看：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (0..2).forEach { idx ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable {
                                        val (newState, result) = WerewolfGameLogic.executeWerewolfPeekCenter(gameState, idx)
                                        onActionComplete(newState, result)
                                    },
                                color = WerewolfColors.surface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("底牌${idx + 1}", fontSize = 15.sp, color = WerewolfColors.onSurface)
                                }
                            }
                        }
                    }
                } else {
                    // 多狼：展示队友信息（自动行动，用 LaunchedEffect 避免重组重复触发）
                    LaunchedEffect(gameState.currentNightStep) {
                        val teammates = WerewolfGameLogic.getWolfTeammates(gameState, player.id)
                        val resultText = if (teammates.isNotEmpty()) {
                            "狼人队友：${teammates.joinToString("、") { it.nickname }}"
                        } else {
                            "你是唯一的狼人"
                        }
                        onActionComplete(gameState, resultText)
                    }
                }
            }

            WerewolfRole.MINION -> {
                // 爪牙：自动显示狼人信息
                LaunchedEffect(gameState.currentNightStep) {
                    val resultText = WerewolfGameLogic.getMinionWolvesText(gameState)
                    onActionComplete(gameState, resultText)
                }
            }

            WerewolfRole.MASON_A, WerewolfRole.MASON_B -> {
                // 守夜人：自动显示队友信息
                LaunchedEffect(gameState.currentNightStep) {
                    val teammates = WerewolfGameLogic.getMasonTeammates(gameState, player.id)
                    val resultText = if (teammates.isNotEmpty()) {
                        "守夜人队友：${teammates.joinToString("、") { it.nickname }}"
                    } else {
                        "没有其他守夜人"
                    }
                    onActionComplete(gameState, resultText)
                }
            }

            WerewolfRole.SEER -> {
                var seerChoice by remember { mutableIntStateOf(0) }
                var seerCenter1 by remember { mutableIntStateOf(-1) }

                if (seerChoice == 0) {
                    Text("选择查看方式：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f).height(48.dp).clickable { seerChoice = 1 },
                            color = WerewolfColors.surface, shape = RoundedCornerShape(8.dp)
                        ) { Box(contentAlignment = Alignment.Center) { Text("查看1名玩家", fontSize = 14.sp, color = WerewolfColors.onSurface) } }
                        Surface(
                            modifier = Modifier.weight(1f).height(48.dp).clickable { seerChoice = 2 },
                            color = WerewolfColors.surface, shape = RoundedCornerShape(8.dp)
                        ) { Box(contentAlignment = Alignment.Center) { Text("查看2张底牌", fontSize = 14.sp, color = WerewolfColors.onSurface) } }
                    }
                } else if (seerChoice == 1) {
                    Text("选择要查看的玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    PlayerSelectionGrid(
                        players = gameState.players,
                        excludeIds = setOf(player.id),
                        onSelect = { targetId ->
                            val (newState, result) = WerewolfGameLogic.executeSeerViewPlayer(gameState, targetId)
                            onActionComplete(newState, result)
                        }
                    )
                } else {
                    if (seerCenter1 < 0) {
                        Text("选择第1张底牌：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                    } else {
                        Text("选择第2张底牌（已选底牌${seerCenter1 + 1}）：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (0..2).forEach { idx ->
                            val isSelected = idx == seerCenter1
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clickable {
                                        if (seerCenter1 < 0) {
                                            seerCenter1 = idx
                                        } else if (idx != seerCenter1) {
                                            val (newState, result) = WerewolfGameLogic.executeSeerViewCenter(gameState, seerCenter1, idx)
                                            onActionComplete(newState, result)
                                        }
                                    },
                                color = if (isSelected) WerewolfColors.primary.copy(alpha = 0.2f) else WerewolfColors.surface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        if (isSelected) "✓ 底牌${idx + 1}" else "底牌${idx + 1}",
                                        fontSize = 15.sp,
                                        color = if (isSelected) WerewolfColors.primary else WerewolfColors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            WerewolfRole.ROBBER -> {
                Text("选择要交换身份的玩家（或跳过不发动）：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                Spacer(modifier = Modifier.height(8.dp))
                PlayerSelectionGrid(
                    players = gameState.players,
                    excludeIds = setOf(player.id),
                    onSelect = { targetId ->
                        val (newState, result) = WerewolfGameLogic.executeRobberSwap(gameState, player.id, targetId)
                        onActionComplete(newState, result)
                    }
                )
            }

            WerewolfRole.TROUBLEMAKER -> {
                var t1 by remember { mutableIntStateOf(-1) }
                if (t1 < 0) {
                    Text("选择第1个玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                } else {
                    val p1 = gameState.players.getOrNull(t1)
                    Text("已选：${p1?.nickname}，选择第2个玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                PlayerSelectionGrid(
                    players = gameState.players,
                    excludeIds = setOf(player.id) + if (t1 >= 0) setOf(t1) else emptySet(),
                    selectedIds = if (t1 >= 0) setOf(t1) else emptySet(),
                    onSelect = { targetId ->
                        if (t1 < 0) {
                            t1 = targetId
                        } else {
                            val (newState, result) = WerewolfGameLogic.executeTroublemakerSwap(gameState, t1, targetId)
                            onActionComplete(newState, result)
                        }
                    }
                )
            }

            WerewolfRole.DRUNK -> {
                Text("选择1张底牌交换（不能看新牌！）：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..2).forEach { idx ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clickable {
                                    val (newState, result) = WerewolfGameLogic.executeDrunkSwap(gameState, player.id, idx)
                                    onActionComplete(newState, result)
                                },
                            color = WerewolfColors.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("底牌${idx + 1}", fontSize = 15.sp, color = WerewolfColors.onSurface)
                            }
                        }
                    }
                }
            }

            WerewolfRole.INSOMNIAC -> {
                // 失眠者：自动查看最终身份
                LaunchedEffect(gameState.currentNightStep) {
                    val (newState, result) = WerewolfGameLogic.getInsomniacResult(gameState, player.id)
                    onActionComplete(newState, result)
                }
            }

            else -> {
                // 无夜间行动的角色（村民、猎人、皮匠）- 快速跳过
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WerewolfColors.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "你的角色「${role.displayName}」没有夜间行动",
                        fontSize = 14.sp,
                        color = WerewolfColors.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                        .clickable(onClick = onSkip),
                    color = WerewolfColors.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "传递设备",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WerewolfColors.onPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // 底部跳过按钮
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(onClick = onSkip),
            color = WerewolfColors.surfaceContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("跳过（不行动）", fontSize = 14.sp, color = WerewolfColors.onSurfaceVariant)
            }
        }
    }
}

/**
 * 夜间结果屏幕：玩家查看行动结果
 */
@Composable
private fun NightResultScreen(
    player: WerewolfPlayer,
    resultText: String,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = player.nickname,
                fontSize = 14.sp,
                color = WerewolfColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WerewolfColors.surface,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = resultText,
                    fontSize = 16.sp,
                    color = WerewolfColors.primary,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .clickable(onClick = onConfirm),
                color = WerewolfColors.surfaceContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "确认，请闭眼",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WerewolfColors.onSurface
                    )
                }
            }
        }
    }
}

// ===== 白天讨论 =====
@Composable
private fun DayDiscussionPhase(
    gameState: WerewolfGameState,
    onStartVoting: () -> Unit
) {
    var viewingPlayerId by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "天亮了 — 讨论时间",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = WerewolfColors.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "自由讨论，可撒谎但禁止亮牌。约5分钟后投票。",
            fontSize = 14.sp,
            color = WerewolfColors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "点击查看自己的当前身份（传递设备）",
            fontSize = 14.sp,
            color = WerewolfColors.onSurfaceMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(gameState.players.size) { idx ->
                val player = gameState.players[idx]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewingPlayerId = player.id
                        },
                    color = WerewolfColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(WerewolfColors.surfaceContainerHighest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 13.sp, color = WerewolfColors.onSurface)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(player.nickname, fontSize = 15.sp, color = WerewolfColors.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("点击查看", fontSize = 12.sp, color = WerewolfColors.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onStartVoting),
            color = WerewolfColors.primary,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("进入投票", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WerewolfColors.onPrimary)
            }
        }
    }

    // 身份查看弹窗 - 使用公共 WerewolfIdentityCard
    if (viewingPlayerId >= 0) {
        val player = gameState.players.getOrNull(viewingPlayerId)
        if (player != null) {
            Dialog(
                onDismissRequest = { viewingPlayerId = -1 },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    WerewolfIdentityCard(
                        resetKey = "day-${player.id}-${player.currentRole}",
                        playerNumber = player.id + 1,
                        nickname = player.nickname,
                        role = player.currentRole,
                        showDescription = false,
                        onClose = { viewingPlayerId = -1 }
                    )
                }
            }
        }
    }
}

// ===== 投票阶段（隐私传递模式）=====
@Composable
private fun VotingPhase(
    gameState: WerewolfGameState,
    votes: List<Pair<Int, Int>>,
    onVote: (Int, Int) -> Unit
) {
    val voterIndex = gameState.currentVoterIndex
    val voter = gameState.players.getOrNull(voterIndex)

    if (voter == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "投票阶段 ${voterIndex + 1}/${gameState.players.size}",
                fontSize = 14.sp,
                color = WerewolfColors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = WerewolfColors.surfaceContainer,
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((voterIndex + 1).toFloat() / gameState.players.size)
                        .background(WerewolfColors.primary, RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WerewolfColors.surface,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "请将设备递给", fontSize = 14.sp, color = WerewolfColors.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = voter.nickname,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WerewolfColors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "请选择你要投票出局的玩家", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(gameState.players.size) { idx ->
                    val target = gameState.players[idx]
                    if (target.id == voter.id) return@items
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVote(voter.id, target.id) },
                        color = WerewolfColors.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(WerewolfColors.surfaceContainerHighest, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idx + 1}", fontSize = 13.sp, color = WerewolfColors.onSurface)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(target.nickname, fontSize = 15.sp, color = WerewolfColors.onSurface)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("投票", fontSize = 13.sp, color = WerewolfColors.primary)
                        }
                    }
                }
            }
        }
    }
}

// ===== 猎人行动 =====
@Composable
private fun HunterActionPhase(
    gameState: WerewolfGameState,
    onHunterShoot: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🎯 猎人发动技能",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = WerewolfColors.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        val hunterName = gameState.players.find { it.id == gameState.hunterPlayerId }?.nickname ?: "猎人"
        Text(
            text = "${hunterName} 被淘汰！必须带走1名玩家",
            fontSize = 14.sp,
            color = WerewolfColors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(gameState.players.size) { idx ->
                val player = gameState.players[idx]
                if (!player.isAlive) return@items
                if (player.id == gameState.hunterPlayerId) return@items
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHunterShoot(player.id) },
                    color = WerewolfColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(player.nickname, fontSize = 15.sp, color = WerewolfColors.onSurface)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("带走", fontSize = 13.sp, color = WerewolfColors.secondary)
                    }
                }
            }
        }
    }
}

// ===== 游戏结束 =====
@Composable
private fun GameOverPhase(
    gameState: WerewolfGameState,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            val winnerText = when (gameState.winner) {
            WerewolfFaction.VILLAGER -> "村民阵营胜利"
            WerewolfFaction.WEREWOLF -> "狼人阵营胜利"
            WerewolfFaction.INDEPENDENT -> "皮匠胜利"
                else -> "游戏结束"
            }
            val winnerColor = when (gameState.winner) {
                WerewolfFaction.VILLAGER -> WerewolfColors.info
                WerewolfFaction.WEREWOLF -> WerewolfColors.danger
                WerewolfFaction.INDEPENDENT -> WerewolfColors.secondary
                else -> WerewolfColors.onSurface
            }

            Text(
                text = winnerText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = winnerColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (gameState.eliminatedPlayerIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WerewolfColors.surface,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "出局玩家", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WerewolfColors.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        gameState.eliminatedPlayerIds.forEach { playerId ->
                            val player = gameState.players.find { it.id == playerId }
                            player?.let {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(it.nickname, fontSize = 14.sp, color = WerewolfColors.onSurface)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = it.currentRole.displayName,
                                        fontSize = 13.sp,
                                        color = getRoleColor(it.currentRole),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (gameState.hunterTargetId != null) {
                val hunterTarget = gameState.players.find { it.id == gameState.hunterTargetId }
                hunterTarget?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = WerewolfColors.surface,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎯 猎人带走：${it.nickname}", fontSize = 14.sp, color = WerewolfColors.secondary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(it.currentRole.displayName, fontSize = 13.sp, color = getRoleColor(it.currentRole))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WerewolfColors.surface,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "全部角色揭示", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WerewolfColors.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    gameState.players.forEach { player ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(player.nickname, fontSize = 14.sp, color = WerewolfColors.onSurface)
                            if (player.currentRole != player.initialRole) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(初始: ${player.initialRole.displayName})",
                                    fontSize = 11.sp,
                                    color = WerewolfColors.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = player.currentRole.displayName,
                                fontSize = 13.sp,
                                color = getRoleColor(player.currentRole),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f).height(48.dp).clickable(onClick = onRestart),
                color = WerewolfColors.surfaceContainer,
                shape = RoundedCornerShape(8.dp)
            ) { Box(contentAlignment = Alignment.Center) { Text("再来一局", fontSize = 15.sp, color = WerewolfColors.onSurface) } }
            Surface(
                modifier = Modifier.weight(1f).height(48.dp).clickable(onClick = onBack),
                color = WerewolfColors.primary,
                shape = RoundedCornerShape(8.dp)
            ) { Box(contentAlignment = Alignment.Center) { Text("返回大厅", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WerewolfColors.onPrimary) } }
        }
    }
}

// ===== 通用组件 =====

@Composable
private fun PlayerSelectionGrid(
    players: List<WerewolfPlayer>,
    excludeIds: Set<Int>,
    selectedIds: Set<Int> = emptySet(),
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        players.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { player ->
                    if (excludeIds.contains(player.id)) return@forEach
                    val isSelected = selectedIds.contains(player.id)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onSelect(player.id) },
                        color = if (isSelected) WerewolfColors.primary.copy(alpha = 0.2f) else WerewolfColors.surface,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = player.nickname,
                                fontSize = 14.sp,
                                color = if (isSelected) WerewolfColors.primary else WerewolfColors.onSurface
                            )
                        }
                    }
                }
                val visibleInRow = row.count { it.id !in excludeIds }
                if (visibleInRow == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ===== 工具函数 =====
// getRoleColor 和 getRoleIcon 已移至 components.WerewolfIdentityCard

/**
 * 化身幽灵复制后的额外行动界面
 * 当化身幽灵复制的角色有交互式夜间行动时，在此执行
 */
@Composable
private fun DoppelgangerFollowUpAction(
    copiedRole: WerewolfRole,
    player: WerewolfPlayer,
    gameState: WerewolfGameState,
    onActionComplete: (WerewolfGameState, String) -> Unit
) {
    // 显示提示
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = getRoleColor(copiedRole).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getRoleIcon(copiedRole),
                contentDescription = copiedRole.displayName,
                modifier = Modifier.size(20.dp),
                tint = getRoleColor(copiedRole)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "你现在扮演 ${copiedRole.displayName}，执行其夜间行动",
                fontSize = 13.sp,
                color = getRoleColor(copiedRole)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    when (copiedRole) {
        WerewolfRole.SEER -> {
            var seerChoice by remember { mutableIntStateOf(0) }
            var seerCenter1 by remember { mutableIntStateOf(-1) }

            if (seerChoice == 0) {
                Text("选择查看方式：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp).clickable { seerChoice = 1 },
                        color = WerewolfColors.surface, shape = RoundedCornerShape(8.dp)
                    ) { Box(contentAlignment = Alignment.Center) { Text("查看1名玩家", fontSize = 14.sp, color = WerewolfColors.onSurface) } }
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp).clickable { seerChoice = 2 },
                        color = WerewolfColors.surface, shape = RoundedCornerShape(8.dp)
                    ) { Box(contentAlignment = Alignment.Center) { Text("查看2张底牌", fontSize = 14.sp, color = WerewolfColors.onSurface) } }
                }
            } else if (seerChoice == 1) {
                Text("选择要查看的玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                Spacer(modifier = Modifier.height(8.dp))
                PlayerSelectionGrid(
                    players = gameState.players,
                    excludeIds = setOf(player.id),
                    onSelect = { targetId ->
                        val (newState, result) = WerewolfGameLogic.executeSeerViewPlayer(gameState, targetId)
                        onActionComplete(newState, result)
                    }
                )
            } else {
                if (seerCenter1 < 0) {
                    Text("选择第1张底牌：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                } else {
                    Text("选择第2张底牌（已选底牌${seerCenter1 + 1}）：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..2).forEach { idx ->
                        val isSelected = idx == seerCenter1
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clickable {
                                    if (seerCenter1 < 0) {
                                        seerCenter1 = idx
                                    } else if (idx != seerCenter1) {
                                        val (newState, result) = WerewolfGameLogic.executeSeerViewCenter(gameState, seerCenter1, idx)
                                        onActionComplete(newState, result)
                                    }
                                },
                            color = if (isSelected) WerewolfColors.primary.copy(alpha = 0.2f) else WerewolfColors.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isSelected) "✓ 底牌${idx + 1}" else "底牌${idx + 1}",
                                    fontSize = 15.sp,
                                    color = if (isSelected) WerewolfColors.primary else WerewolfColors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        WerewolfRole.ROBBER -> {
            Text("选择要交换身份的玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
            Spacer(modifier = Modifier.height(8.dp))
            PlayerSelectionGrid(
                players = gameState.players,
                excludeIds = setOf(player.id),
                onSelect = { targetId ->
                    val (newState, result) = WerewolfGameLogic.executeRobberSwap(gameState, player.id, targetId)
                    onActionComplete(newState, result)
                }
            )
        }

        WerewolfRole.TROUBLEMAKER -> {
            var t1 by remember { mutableIntStateOf(-1) }
            if (t1 < 0) {
                Text("选择第1个玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
            } else {
                val p1 = gameState.players.getOrNull(t1)
                Text("已选：${p1?.nickname}，选择第2个玩家：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            PlayerSelectionGrid(
                players = gameState.players,
                excludeIds = setOf(player.id) + if (t1 >= 0) setOf(t1) else emptySet(),
                selectedIds = if (t1 >= 0) setOf(t1) else emptySet(),
                onSelect = { targetId ->
                    if (t1 < 0) {
                        t1 = targetId
                    } else {
                        val (newState, result) = WerewolfGameLogic.executeTroublemakerSwap(gameState, t1, targetId)
                        onActionComplete(newState, result)
                    }
                }
            )
        }

        WerewolfRole.DRUNK -> {
            Text("选择1张底牌交换（不能看新牌！）：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..2).forEach { idx ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable {
                                val (newState, result) = WerewolfGameLogic.executeDrunkSwap(gameState, player.id, idx)
                                onActionComplete(newState, result)
                            },
                        color = WerewolfColors.surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("底牌${idx + 1}", fontSize = 15.sp, color = WerewolfColors.onSurface)
                        }
                    }
                }
            }
        }

        WerewolfRole.WEREWOLF -> {
            // 化身幽灵复制狼人：查看队友（多狼）或查看底牌（独狼）
            val isLone = WerewolfGameLogic.isLoneWolf(gameState, player.id)
            if (isLone) {
                Text("你是独狼！选择1张中央底牌查看：", fontSize = 14.sp, color = WerewolfColors.onSurfaceMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..2).forEach { idx ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clickable {
                                    val (newState, result) = WerewolfGameLogic.executeWerewolfPeekCenter(gameState, idx)
                                    onActionComplete(newState, result)
                                },
                            color = WerewolfColors.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("底牌${idx + 1}", fontSize = 15.sp, color = WerewolfColors.onSurface)
                            }
                        }
                    }
                }
            } else {
                // 自动展示队友信息
                LaunchedEffect(Unit) {
                    val teammates = WerewolfGameLogic.getWolfTeammates(gameState, player.id)
                    val resultText = if (teammates.isNotEmpty()) {
                        "狼人队友：${teammates.joinToString("、") { it.nickname }}"
                    } else {
                        "你是唯一的狼人"
                    }
                    onActionComplete(gameState, resultText)
                }
            }
        }

        WerewolfRole.MINION -> {
            LaunchedEffect(Unit) {
                val resultText = WerewolfGameLogic.getMinionWolvesText(gameState)
                onActionComplete(gameState, resultText)
            }
        }

        WerewolfRole.MASON_A, WerewolfRole.MASON_B -> {
            LaunchedEffect(Unit) {
                val teammates = WerewolfGameLogic.getMasonTeammates(gameState, player.id)
                val resultText = if (teammates.isNotEmpty()) {
                    "守夜人队友：${teammates.joinToString("、") { it.nickname }}"
                } else {
                    "没有其他守夜人"
                }
                onActionComplete(gameState, resultText)
            }
        }

        WerewolfRole.INSOMNIAC -> {
            // 失眠者查看最终身份 — 但此时夜间还没结束，所有交换尚未全部完成
            // 化身幽灵作为 nightOrder=1 的角色复制失眠者(nightOrder=8)，
            // 实际上它需要在所有交换结束后才能查看最终身份
            // 简化处理：标记为自动行动，在所有行动结束后统一查看
            LaunchedEffect(Unit) {
                onActionComplete(gameState, "你的身份将在天亮后揭晓（失眠者效果）")
            }
        }

        else -> {
            // 复制了无夜间行动的角色，无需额外操作
            LaunchedEffect(Unit) {
                onActionComplete(gameState, "${copiedRole.displayName}没有夜间行动")
            }
        }
    }
}

/**
 * 狼人游戏暗色顶部栏
 * 保持暗夜沉浸感，使用 WerewolfColors 配色
 */
@Composable
private fun WerewolfTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WerewolfColors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Cottage,
                contentDescription = "返回",
                tint = WerewolfColors.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = WerewolfColors.onSurface
        )
    }
}
