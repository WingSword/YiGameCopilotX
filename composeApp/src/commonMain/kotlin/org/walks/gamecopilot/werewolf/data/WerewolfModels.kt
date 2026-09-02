package org.walks.gamecopilot.werewolf.data

import kotlinx.serialization.Serializable

/**
 * 一夜终极狼人 - 角色枚举
 * 每个角色有阵营、技能描述、夜间行动顺序
 */
enum class WerewolfRole(
    val displayName: String,
    val faction: WerewolfFaction,
    val description: String,
    val hasNightAction: Boolean,
    val nightOrder: Int // 0=无行动，>0 按从小到大顺序行动
) {
    WEREWOLF(
        displayName = "狼人",
        faction = WerewolfFaction.WEREWOLF,
        description = "夜间与其他狼人互认队友；若独狼可查看1张中央底牌",
        hasNightAction = true,
        nightOrder = 2
    ),
    MINION(
        displayName = "爪牙",
        faction = WerewolfFaction.WEREWOLF,
        description = "认狼：所有狼人竖大拇指让爪牙认狼",
        hasNightAction = true,
        nightOrder = 3
    ),
    DOPPELGANGER(
        displayName = "化身幽灵",
        faction = WerewolfFaction.VILLAGER, // 初始阵营，行动后可能改变
        description = "复制1名玩家身份，同步获得其阵营与夜间行动",
        hasNightAction = true,
        nightOrder = 1
    ),
    SEER(
        displayName = "预言家",
        faction = WerewolfFaction.VILLAGER,
        description = "验1人或查看2张中央底牌",
        hasNightAction = true,
        nightOrder = 4
    ),
    ROBBER(
        displayName = "强盗",
        faction = WerewolfFaction.VILLAGER,
        description = "交换自己与1名玩家身份，查看新身份",
        hasNightAction = true,
        nightOrder = 5
    ),
    TROUBLEMAKER(
        displayName = "捣蛋鬼",
        faction = WerewolfFaction.VILLAGER,
        description = "交换任意两名其他玩家的身份牌",
        hasNightAction = true,
        nightOrder = 6
    ),
    DRUNK(
        displayName = "酒鬼",
        faction = WerewolfFaction.VILLAGER,
        description = "将自己的牌与1张中央底牌交换，不能看新牌",
        hasNightAction = true,
        nightOrder = 7
    ),
    INSOMNIAC(
        displayName = "失眠者",
        faction = WerewolfFaction.VILLAGER,
        description = "最后查看自己当前的最终身份",
        hasNightAction = true,
        nightOrder = 8
    ),
    HUNTER(
        displayName = "猎人",
        faction = WerewolfFaction.VILLAGER,
        description = "出局时必须带走1名玩家",
        hasNightAction = false,
        nightOrder = 0
    ),
    VILLAGER(
        displayName = "村民",
        faction = WerewolfFaction.VILLAGER,
        description = "无技能，纯推理",
        hasNightAction = false,
        nightOrder = 0
    ),
    MASON_A(
        displayName = "守夜人",
        faction = WerewolfFaction.VILLAGER,
        description = "夜间与另一守夜人互认",
        hasNightAction = true,
        nightOrder = 3
    ),
    MASON_B(
        displayName = "守夜人",
        faction = WerewolfFaction.VILLAGER,
        description = "夜间与另一守夜人互认",
        hasNightAction = true,
        nightOrder = 3
    ),
    TANNER(
        displayName = "皮匠",
        faction = WerewolfFaction.INDEPENDENT,
        description = "唯一胜利条件：自己被投票出局",
        hasNightAction = false,
        nightOrder = 0
    )
}

enum class WerewolfFaction(val displayName: String) {
    VILLAGER("村民阵营"),
    WEREWOLF("狼人阵营"),
    INDEPENDENT("独立阵营")
}

/**
 * 游戏阶段
 */
enum class WerewolfGamePhase {
    DEAL_CARDS,      // 发牌/查看身份（每人传递设备查看自己的牌）
    NIGHT_START,     // 夜晚开始（闭眼提示）
    NIGHT_ACTION,    // 夜间行动（各角色依次行动，隐私传递模式）
    DAY_DISCUSSION,  // 白天讨论
    DAY_VOTING,      // 白天投票（隐私传递模式）
    VOTE_RESULT,     // 投票结果
    HUNTER_ACTION,   // 猎人发动技能
    GAME_OVER        // 游戏结束
}

/**
 * 夜间行动子步骤
 * 每个角色的夜间行动分为：传递→行动→结果
 */
enum class NightActionSubStep {
    HAND_OFF,  // 传递设备屏幕："请将设备递给 XX"
    ACTION,    // 行动屏幕：玩家执行操作
    RESULT     // 结果屏幕：查看行动结果
}

/**
 * 玩家数据
 */
@Serializable
data class WerewolfPlayer(
    val id: Int,
    val nickname: String = "",
    val initialRole: WerewolfRole,     // 初始分配的角色
    val currentRole: WerewolfRole,      // 当前角色（可能被交换）
    val isAlive: Boolean = true,
    val voteTarget: Int? = null,        // 投票目标玩家ID
    val isRevealed: Boolean = false     // 身份是否已公开
)

/**
 * 中央底牌
 */
@Serializable
data class CenterCard(
    val index: Int,
    val role: WerewolfRole
)

/**
 * 夜间行动记录（用于结算时展示完整行动历史）
 */
@Serializable
data class NightActionRecord(
    val role: WerewolfRole,
    val playerIndex: Int,
    val actionType: String,      // "view", "swap", "rob", "exchange", "copy"
    val targetPlayerIndex: Int? = null,
    val targetCenterIndex: Int? = null,
    val targetCenterIndex2: Int? = null,
    val resultInfo: String = ""  // 行动结果描述
)

/**
 * 夜间交换记录（用于按 nightOrder 回溯计算身份）
 * 记录每次交换操作的详细信息，在夜间结束时按 nightOrder 统一结算
 */
@Serializable
data class NightSwapAction(
    val actorPlayerId: Int,       // 执行交换的玩家ID
    val actorNightOrder: Int,     // 执行者角色的 nightOrder（决定交换逻辑顺序）
    val type: String,             // "robber", "troublemaker", "drunk"
    val targetPlayerId: Int? = null,    // 强盗目标 / 捣蛋鬼目标1
    val targetPlayerId2: Int? = null,   // 捣蛋鬼目标2
    val targetCenterIndex: Int? = null  // 酒鬼目标底牌
)

/**
 * 一夜终极狼人 - 游戏状态
 */
@Serializable
data class WerewolfGameState(
    val phase: WerewolfGamePhase = WerewolfGamePhase.DEAL_CARDS,
    val playerCount: Int = 5,
    val players: List<WerewolfPlayer> = emptyList(),
    val centerCards: List<CenterCard> = emptyList(),

    // === 夜间行动 ===
    val nightActions: List<NightActionRecord> = emptyList(),
    val nightActionOrder: List<Int> = emptyList(), // 玩家ID列表，随机打乱，游戏开始时固定
    val nightSwapActions: List<NightSwapAction> = emptyList(), // 夜间交换记录，按nightOrder结算
    val currentNightStep: Int = 0,       // 当前夜间行动步骤索引
    val nightSubStep: NightActionSubStep = NightActionSubStep.HAND_OFF, // 夜间行动子步骤
    val nightActionResultText: String = "", // 当前夜间行动的结果文本

    // === 发牌阶段 ===
    val dealCardPlayerIndex: Int = 0,    // 当前正在查看身份的玩家索引
    val dealCardRevealed: Boolean = false, // 当前玩家是否已翻开牌

    // === 投票 ===
    val voteResults: Map<Int, Int> = emptyMap(), // playerId -> 被投票数
    val currentVoterIndex: Int = 0,      // 当前投票的玩家索引
    val eliminatedPlayerIds: List<Int> = emptyList(),
    val winner: WerewolfFaction? = null,

    // === 猎人 ===
    val hunterPending: Boolean = false,
    val hunterPlayerId: Int? = null,
    val hunterTargetId: Int? = null,

    // === 夜间行动中间状态 ===
    val doppelgangerTargetId: Int? = null,
    val doppelgangerCopiedRole: WerewolfRole? = null,   // 化身幽灵复制的角色类型
    val doppelgangerPendingAction: Boolean = false,      // 化身幽灵是否需要执行被复制角色的行动
    val robberTargetId: Int? = null,
    val troublemakerTarget1Id: Int? = null,
    val troublemakerTarget2Id: Int? = null,
    val drunkCenterIndex: Int? = null,
    val seerActionType: Int = 0,                // 0=未选, 1=查看1人, 2=查看2张底牌
    val seerTargetPlayerId: Int? = null,
    val seerTargetCenter1: Int? = null,
    val seerTargetCenter2: Int? = null,
)

/**
 * 标准角色配置
 */
data class WerewolfPreset(
    val name: String,
    val playerCount: Int,
    val roles: List<WerewolfRole>,
    val description: String
) {
    fun isValid(): Boolean {
        return roles.size == playerCount + 3
    }
}

/**
 * 预设配置
 */
object WerewolfPresets {

    val presets = listOf(
        // 3人局（最少）
        WerewolfPreset(
            name = "3人入门",
            playerCount = 3,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.SEER, WerewolfRole.ROBBER,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "最简单配置，1狼人+预言家+强盗"
        ),
        // 4人局
        WerewolfPreset(
            name = "4人标准",
            playerCount = 4,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.SEER, WerewolfRole.ROBBER,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "2狼人+预言家+强盗，经典入门"
        ),
        // 5人局
        WerewolfPreset(
            name = "5人标准",
            playerCount = 5,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.SEER, WerewolfRole.ROBBER, WerewolfRole.TROUBLEMAKER,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "2狼人+预言家+强盗+捣蛋鬼"
        ),
        // 6人局
        WerewolfPreset(
            name = "6人标准",
            playerCount = 6,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.SEER, WerewolfRole.ROBBER, WerewolfRole.TROUBLEMAKER,
                WerewolfRole.DRUNK,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "加入酒鬼，更多身份不确定性"
        ),
        // 7人局
        WerewolfPreset(
            name = "7人进阶",
            playerCount = 7,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.SEER, WerewolfRole.ROBBER, WerewolfRole.TROUBLEMAKER,
                WerewolfRole.DRUNK, WerewolfRole.INSOMNIAC,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "加入失眠者，可确认最终身份"
        ),
        // 8人局
        WerewolfPreset(
            name = "8人进阶",
            playerCount = 8,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.MINION,
                WerewolfRole.SEER, WerewolfRole.ROBBER, WerewolfRole.TROUBLEMAKER,
                WerewolfRole.DRUNK, WerewolfRole.INSOMNIAC,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "加入爪牙，狼人阵营更强"
        ),
        // 9人局
        WerewolfPreset(
            name = "9人完整",
            playerCount = 9,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.MINION,
                WerewolfRole.SEER, WerewolfRole.ROBBER, WerewolfRole.TROUBLEMAKER,
                WerewolfRole.DRUNK, WerewolfRole.INSOMNIAC, WerewolfRole.HUNTER,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "加入猎人，出局时带走一人"
        ),
        // 10人局
        WerewolfPreset(
            name = "10人完整",
            playerCount = 10,
            roles = listOf(
                WerewolfRole.WEREWOLF, WerewolfRole.WEREWOLF,
                WerewolfRole.MINION, WerewolfRole.TANNER,
                WerewolfRole.SEER, WerewolfRole.ROBBER, WerewolfRole.TROUBLEMAKER,
                WerewolfRole.DRUNK, WerewolfRole.INSOMNIAC, WerewolfRole.HUNTER,
                WerewolfRole.VILLAGER, WerewolfRole.VILLAGER, WerewolfRole.VILLAGER
            ),
            description = "加入皮匠，三方博弈"
        )
    )

    fun getPresetForPlayerCount(count: Int): WerewolfPreset {
        return presets.find { it.playerCount == count } ?: presets.first()
    }
}
