package org.walks.gamecopilot.werewolf

import org.walks.gamecopilot.werewolf.data.*
import kotlin.random.Random

/**
 * 一夜终极狼人 - 核心游戏逻辑
 *
 * 夜间行动核心设计：
 * - 物理传递顺序：所有玩家随机打乱（防泄密）
 * - 逻辑执行顺序：按 nightOrder 优先级（保正确性）
 * - 记录所有交换操作，查看身份时按 nightOrder 回溯计算
 * - 夜间结束时统一结算所有交换，计算最终 currentRole
 */
object WerewolfGameLogic {

    // ===== 初始化 =====

    /**
     * 初始化游戏：洗牌分配角色，随机打乱夜间传递顺序
     */
    fun initializeGame(preset: WerewolfPreset, nicknames: List<String>): WerewolfGameState {
        val shuffledRoles = preset.roles.shuffled(Random.Default)
        val totalCards = preset.playerCount + 3

        require(shuffledRoles.size == totalCards) {
            "角色牌数(${shuffledRoles.size})必须等于 玩家数+3(${totalCards})"
        }

        val players = (0 until preset.playerCount).map { i ->
            WerewolfPlayer(
                id = i,
                nickname = nicknames.getOrElse(i) { "玩家${i + 1}" },
                initialRole = shuffledRoles[i],
                currentRole = shuffledRoles[i]
            )
        }

        val centerCards = (0 until 3).map { i ->
            CenterCard(
                index = i,
                role = shuffledRoles[preset.playerCount + i]
            )
        }

        // 所有玩家随机打乱顺序（包含无夜间行动的玩家）
        val nightOrder = computeNightActionOrder(players)

        return WerewolfGameState(
            phase = WerewolfGamePhase.DEAL_CARDS,
            playerCount = preset.playerCount,
            players = players,
            centerCards = centerCards,
            nightActionOrder = nightOrder,
            dealCardPlayerIndex = 0,
            dealCardRevealed = false
        )
    }

    /**
     * 计算夜间传递顺序（所有玩家，随机打乱）
     *
     * 关键设计：
     * - 所有玩家都参与传递，无行动者看到"没有夜间行动"快速跳过
     * - 顺序随机打乱，旁观者无法通过"第几个拿到设备"推断角色
     */
    private fun computeNightActionOrder(players: List<WerewolfPlayer>): List<Int> {
        return players.map { it.id }.shuffled(Random.Default)
    }

    // ===== 核心：按 nightOrder 回溯计算身份 =====

    /**
     * 根据已记录的交换操作，按 nightOrder 顺序重放，计算指定范围内的身份状态
     *
     * @param players 玩家列表（使用 initialRole 作为基准）
     * @param centerCards 底牌列表
     * @param swapActions 要应用的交换操作列表
     * @return Pair<玩家角色列表, 底牌角色列表>
     */
    private fun computeRolesAfterSwaps(
        players: List<WerewolfPlayer>,
        centerCards: List<CenterCard>,
        swapActions: List<NightSwapAction>
    ): Pair<List<WerewolfRole>, List<WerewolfRole>> {
        val sortedSwaps = swapActions.sortedBy { it.actorNightOrder }
        val playerRoles = players.map { it.initialRole }.toMutableList()
        val centerRoles = centerCards.map { it.role }.toMutableList()

        for (swap in sortedSwaps) {
            when (swap.type) {
                "doppelganger" -> {
                    // 化身幽灵：将 actor 的角色变为目标玩家的角色
                    val targetId = swap.targetPlayerId ?: continue
                    playerRoles[swap.actorPlayerId] = playerRoles[targetId]
                }
                "robber" -> {
                    val targetId = swap.targetPlayerId ?: continue
                    val temp = playerRoles[swap.actorPlayerId]
                    playerRoles[swap.actorPlayerId] = playerRoles[targetId]
                    playerRoles[targetId] = temp
                }
                "troublemaker" -> {
                    val t1 = swap.targetPlayerId ?: continue
                    val t2 = swap.targetPlayerId2 ?: continue
                    val temp = playerRoles[t1]
                    playerRoles[t1] = playerRoles[t2]
                    playerRoles[t2] = temp
                }
                "drunk" -> {
                    val centerIdx = swap.targetCenterIndex ?: continue
                    val temp = playerRoles[swap.actorPlayerId]
                    playerRoles[swap.actorPlayerId] = centerRoles[centerIdx]
                    centerRoles[centerIdx] = temp
                }
            }
        }

        return playerRoles.toList() to centerRoles.toList()
    }

    /**
     * 获取某个玩家在指定 nightOrder 点的身份
     * 只应用 nightOrder < viewerNightOrder 的交换操作
     */
    fun getRoleForViewer(state: WerewolfGameState, targetPlayerId: Int, viewerNightOrder: Int): WerewolfRole {
        val applicableSwaps = state.nightSwapActions.filter { it.actorNightOrder < viewerNightOrder }
        val (playerRoles, _) = computeRolesAfterSwaps(state.players, state.centerCards, applicableSwaps)
        return playerRoles[targetPlayerId]
    }

    /**
     * 获取某张底牌在指定 nightOrder 点的角色
     */
    fun getCenterCardForViewer(state: WerewolfGameState, centerIndex: Int, viewerNightOrder: Int): WerewolfRole {
        val applicableSwaps = state.nightSwapActions.filter { it.actorNightOrder < viewerNightOrder }
        val (_, centerRoles) = computeRolesAfterSwaps(state.players, state.centerCards, applicableSwaps)
        return centerRoles[centerIndex]
    }

    // ===== 夜间行动：辅助查询 =====

    fun getCurrentNightActionPlayer(state: WerewolfGameState, stepIndex: Int): WerewolfPlayer? {
        val playerId = state.nightActionOrder.getOrNull(stepIndex) ?: return null
        return state.players.getOrNull(playerId)
    }

    fun getCurrentNightActionRole(state: WerewolfGameState, stepIndex: Int): WerewolfRole? {
        return getCurrentNightActionPlayer(state, stepIndex)?.initialRole
    }

    fun isLoneWolf(state: WerewolfGameState, playerId: Int): Boolean {
        val player = state.players.getOrNull(playerId) ?: return false
        if (player.initialRole != WerewolfRole.WEREWOLF) return false
        val wolfCount = state.players.count { it.initialRole == WerewolfRole.WEREWOLF }
        return wolfCount == 1
    }

    fun getWolfTeammates(state: WerewolfGameState, playerId: Int): List<WerewolfPlayer> {
        return state.players.filter {
            it.initialRole == WerewolfRole.WEREWOLF && it.id != playerId
        }
    }

    fun getMasonTeammates(state: WerewolfGameState, playerId: Int): List<WerewolfPlayer> {
        val player = state.players.getOrNull(playerId) ?: return emptyList()
        return state.players.filter {
            (it.initialRole == WerewolfRole.MASON_A || it.initialRole == WerewolfRole.MASON_B) && it.id != player.id
        }
    }

    /**
     * 清除化身幽灵的待行动状态（在执行完被复制角色的行动后调用）
     */
    fun clearDoppelgangerPendingAction(state: WerewolfGameState): WerewolfGameState {
        return state.copy(doppelgangerPendingAction = false)
    }

    // ===== 夜间行动执行（记录交换 + 按 nightOrder 计算视图） =====

    /**
     * 化身幽灵：复制目标玩家身份
     * nightOrder=1，在所有交换之前，所以看到的一定是 initialRole
     *
     * 修复：记录 NightSwapAction(doppelganger)，使 finalizeNightActions 时
     * 化身幽灵的 currentRole 被正确更新为被复制者的角色
     */
    fun executeDoppelgangerAction(state: WerewolfGameState, playerId: Int, targetPlayerId: Int): Pair<WerewolfGameState, String> {
        val targetPlayer = state.players.getOrNull(targetPlayerId)
            ?: return state to "错误：找不到目标玩家"

        // 化身幽灵 nightOrder=1，所有交换都在后面，直接看 initialRole
        val copiedRole = getRoleForViewer(state, targetPlayerId, WerewolfRole.DOPPELGANGER.nightOrder)

        // 记录 doppelganger 交换操作，使 computeRolesAfterSwaps 能正确更新角色
        val swapAction = NightSwapAction(
            actorPlayerId = playerId,
            actorNightOrder = WerewolfRole.DOPPELGANGER.nightOrder,
            type = "doppelganger",
            targetPlayerId = targetPlayerId
        )

        // 判断被复制角色是否需要额外交互操作（需要用户选择的行动）
        val needsPendingAction = copiedRole.hasNightAction && copiedRole.nightOrder > WerewolfRole.DOPPELGANGER.nightOrder

        val resultText = "你复制了 ${targetPlayer.nickname} 的身份：${copiedRole.displayName}" +
            if (needsPendingAction) "\n接下来你将执行 ${copiedRole.displayName} 的夜间行动" else ""

        return state.copy(
            doppelgangerTargetId = targetPlayerId,
            doppelgangerCopiedRole = copiedRole,
            doppelgangerPendingAction = needsPendingAction,
            nightSwapActions = state.nightSwapActions + swapAction
        ) to resultText
    }

    /**
     * 狼人查看底牌（独狼模式）
     * nightOrder=2，在强盗/捣蛋鬼/酒鬼之前，底牌尚未被交换
     */
    fun executeWerewolfPeekCenter(state: WerewolfGameState, centerIndex: Int): Pair<WerewolfGameState, String> {
        val nightOrder = WerewolfRole.WEREWOLF.nightOrder
        val role = getCenterCardForViewer(state, centerIndex, nightOrder)
        val resultText = "底牌${centerIndex + 1} 是：${role.displayName}"
        return state to resultText
    }

    /**
     * 爪牙：看到狼人是谁
     * 基于初始角色（initialRole），不受交换影响
     */
    fun getMinionWolvesText(state: WerewolfGameState): String {
        val wolves = state.players.filter { it.initialRole == WerewolfRole.WEREWOLF }
        return if (wolves.isEmpty()) {
            "场上没有狼人！你不知道谁是你的队友"
        } else {
            "狼人：${wolves.joinToString("、") { it.nickname }}"
        }
    }

    /**
     * 预言家查看1名玩家
     * nightOrder=4，只应用 nightOrder<4 的交换（化身幽灵/狼人/爪牙/守夜人——都不交换玩家身份）
     * 所以预言家看到的就是 initialRole（除非未来有新角色加入前置交换）
     */
    fun executeSeerViewPlayer(state: WerewolfGameState, targetPlayerId: Int): Pair<WerewolfGameState, String> {
        val target = state.players.getOrNull(targetPlayerId)
            ?: return state to "错误：找不到目标玩家"

        val nightOrder = WerewolfRole.SEER.nightOrder
        val targetRole = getRoleForViewer(state, targetPlayerId, nightOrder)

        val resultText = "${target.nickname} 的身份是：${targetRole.displayName}"
        return state.copy(
            seerActionType = 1,
            seerTargetPlayerId = targetPlayerId
        ) to resultText
    }

    /**
     * 预言家查看2张底牌
     * nightOrder=4，底牌在此时只可能被化身幽灵/狼人（独狼查底牌但查不是换）影响
     */
    fun executeSeerViewCenter(state: WerewolfGameState, idx1: Int, idx2: Int): Pair<WerewolfGameState, String> {
        if (idx1 == idx2) return state to "错误：不能选择同一张底牌"

        val nightOrder = WerewolfRole.SEER.nightOrder
        val role1 = getCenterCardForViewer(state, idx1, nightOrder)
        val role2 = getCenterCardForViewer(state, idx2, nightOrder)

        val resultText = "底牌${idx1 + 1}：${role1.displayName}\n底牌${idx2 + 1}：${role2.displayName}"
        return state.copy(
            seerActionType = 2,
            seerTargetCenter1 = idx1,
            seerTargetCenter2 = idx2
        ) to resultText
    }

    /**
     * 强盗交换
     * nightOrder=5：记录交换操作，查看目标在 nightOrder=5 时的身份
     * （只有 nightOrder<5 的交换被应用，目前没有任何角色在 nightOrder<5 交换玩家身份）
     */
    fun executeRobberSwap(state: WerewolfGameState, playerId: Int, targetPlayerId: Int): Pair<WerewolfGameState, String> {
        val target = state.players.getOrNull(targetPlayerId)
            ?: return state to "错误：找不到目标玩家"

        val nightOrder = WerewolfRole.ROBBER.nightOrder
        // 查看目标在强盗行动时的身份（不包含强盗自己的交换）
        val viewedRole = getRoleForViewer(state, targetPlayerId, nightOrder)

        // 记录交换操作（不立即修改 currentRole）
        val swapAction = NightSwapAction(
            actorPlayerId = playerId,
            actorNightOrder = nightOrder,
            type = "robber",
            targetPlayerId = targetPlayerId
        )

        val resultText = "你与 ${target.nickname} 交换了身份\n你的新身份是：${viewedRole.displayName}"
        return state.copy(
            nightSwapActions = state.nightSwapActions + swapAction,
            robberTargetId = targetPlayerId
        ) to resultText
    }

    /**
     * 捣蛋鬼交换两个玩家
     * nightOrder=6：记录交换操作
     */
    fun executeTroublemakerSwap(state: WerewolfGameState, target1Id: Int, target2Id: Int): Pair<WerewolfGameState, String> {
        val t1 = state.players.getOrNull(target1Id)
            ?: return state to "错误：找不到目标1"
        val t2 = state.players.getOrNull(target2Id)
            ?: return state to "错误：找不到目标2"

        val nightOrder = WerewolfRole.TROUBLEMAKER.nightOrder
        val swapAction = NightSwapAction(
            actorPlayerId = -1, // 捣蛋鬼不参与交换本身
            actorNightOrder = nightOrder,
            type = "troublemaker",
            targetPlayerId = target1Id,
            targetPlayerId2 = target2Id
        )

        val resultText = "你交换了 ${t1.nickname} 和 ${t2.nickname} 的身份"
        return state.copy(
            nightSwapActions = state.nightSwapActions + swapAction,
            troublemakerTarget1Id = target1Id,
            troublemakerTarget2Id = target2Id
        ) to resultText
    }

    /**
     * 酒鬼交换底牌
     * nightOrder=7：记录交换操作（酒鬼不能看新牌）
     */
    fun executeDrunkSwap(state: WerewolfGameState, playerId: Int, centerIndex: Int): Pair<WerewolfGameState, String> {
        val nightOrder = WerewolfRole.DRUNK.nightOrder
        val swapAction = NightSwapAction(
            actorPlayerId = playerId,
            actorNightOrder = nightOrder,
            type = "drunk",
            targetCenterIndex = centerIndex
        )

        val resultText = "你与底牌${centerIndex + 1}交换了身份\n（不能查看新牌）"
        return state.copy(
            nightSwapActions = state.nightSwapActions + swapAction,
            drunkCenterIndex = centerIndex
        ) to resultText
    }

    /**
     * 失眠者查看最终身份
     * nightOrder=8：应用所有 nightOrder<8 的交换，即所有交换都会被应用
     */
    fun getInsomniacResult(state: WerewolfGameState, playerId: Int): Pair<WerewolfGameState, String> {
        val insomniac = state.players.getOrNull(playerId)
            ?: return state to "错误：找不到失眠者"

        val nightOrder = WerewolfRole.INSOMNIAC.nightOrder
        val finalRole = getRoleForViewer(state, playerId, nightOrder)
        val changed = finalRole != insomniac.initialRole

        val resultText = if (changed) {
            "你的身份发生了变化！\n当前身份：${finalRole.displayName}"
        } else {
            "你的身份没有变化\n当前身份：${finalRole.displayName}"
        }

        return state to resultText
    }

    // ===== 夜间结算 =====

    /**
     * 夜间行动全部完成后，统一结算所有交换，计算最终的 currentRole
     * 必须在进入 DAY_DISCUSSION 之前调用
     */
    fun finalizeNightActions(state: WerewolfGameState): WerewolfGameState {
        val (finalPlayerRoles, finalCenterRoles) = computeRolesAfterSwaps(
            state.players, state.centerCards, state.nightSwapActions
        )

        val updatedPlayers = state.players.mapIndexed { index, player ->
            player.copy(currentRole = finalPlayerRoles[index])
        }

        val updatedCenterCards = state.centerCards.mapIndexed { index, card ->
            card.copy(role = finalCenterRoles[index])
        }

        return state.copy(
            players = updatedPlayers,
            centerCards = updatedCenterCards
        )
    }

    // ===== 投票结算 =====

    fun resolveVotes(state: WerewolfGameState): WerewolfGameState {
        val voteCounts = mutableMapOf<Int, Int>()
        state.players.forEach { player ->
            player.voteTarget?.let { targetId ->
                voteCounts[targetId] = (voteCounts[targetId] ?: 0) + 1
            }
        }

        val maxVotes = voteCounts.values.maxOrNull() ?: 0
        val candidates = voteCounts.filter { it.value == maxVotes }.keys.toList()

        val allSingleVotes = voteCounts.values.all { it == 1 } && voteCounts.size == state.playerCount

        val eliminatedIds = if (allSingleVotes) {
            emptyList()
        } else if (maxVotes <= 1) {
            emptyList()
        } else {
            candidates
        }

        val updatedPlayers = state.players.map { player ->
            if (eliminatedIds.contains(player.id)) {
                player.copy(isAlive = false, isRevealed = true)
            } else player
        }

        val eliminatedPlayers = updatedPlayers.filter { !it.isAlive }
        val hunterEliminated = eliminatedPlayers.find { it.currentRole == WerewolfRole.HUNTER }

        return state.copy(
            players = updatedPlayers,
            voteResults = voteCounts,
            eliminatedPlayerIds = eliminatedIds,
            hunterPending = hunterEliminated != null,
            hunterPlayerId = hunterEliminated?.id
        )
    }

    fun hunterShoot(state: WerewolfGameState, targetId: Int): WerewolfGameState {
        val updatedPlayers = state.players.map { player ->
            if (player.id == targetId) {
                player.copy(isAlive = false, isRevealed = true)
            } else player
        }
        return state.copy(
            players = updatedPlayers,
            hunterPending = false,
            hunterTargetId = targetId
        )
    }

    // ===== 胜负判定 =====

    fun determineWinner(state: WerewolfGameState): WerewolfFaction? {
        val tannerEliminated = state.players.any {
            it.currentRole == WerewolfRole.TANNER && !it.isAlive
        }
        if (tannerEliminated) {
            return WerewolfFaction.INDEPENDENT
        }

        val wolvesOnField = state.players.filter {
            it.currentRole == WerewolfRole.WEREWOLF
        }
        val hasWolvesOnField = wolvesOnField.isNotEmpty()
        val wolvesInCenter = state.centerCards.count { it.role == WerewolfRole.WEREWOLF }

        if (hasWolvesOnField) {
            val anyWolfEliminated = wolvesOnField.any { !it.isAlive }
            return if (anyWolfEliminated) {
                WerewolfFaction.VILLAGER
            } else {
                WerewolfFaction.WEREWOLF
            }
        } else if (wolvesInCenter > 0) {
            val hasAnyEliminated = state.players.any { !it.isAlive }
            return if (hasAnyEliminated) {
                WerewolfFaction.WEREWOLF
            } else {
                WerewolfFaction.VILLAGER
            }
        } else {
            return WerewolfFaction.VILLAGER
        }
    }
}
