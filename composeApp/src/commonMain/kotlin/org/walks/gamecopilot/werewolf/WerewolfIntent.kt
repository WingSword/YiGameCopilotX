package org.walks.gamecopilot.werewolf

import org.walks.gamecopilot.werewolf.data.WerewolfPreset
import org.walks.gamecopilot.werewolf.data.WerewolfRole

/**
 * 一夜终极狼人 - 用户意图
 */
sealed class WerewolfIntent {
    // ===== 配置阶段 =====
    data class SelectPreset(val preset: WerewolfPreset) : WerewolfIntent()
    data class SetPlayerCount(val count: Int) : WerewolfIntent()
    data class UpdateNickname(val playerIndex: Int, val nickname: String) : WerewolfIntent()

    // ===== 游戏流程控制 =====
    data object StartGame : WerewolfIntent()
    data object StartNight : WerewolfIntent()
    data class ExecuteNightAction(val actionData: NightActionData) : WerewolfIntent()
    data object NextNightStep : WerewolfIntent()
    data object EndNight : WerewolfIntent()
    data object StartDiscussion : WerewolfIntent()
    data object StartVoting : WerewolfIntent()
    data class CastVote(val voterId: Int, val targetId: Int) : WerewolfIntent()
    data object ResolveVotes : WerewolfIntent()
    data class HunterShoot(val targetId: Int) : WerewolfIntent()
    data object RestartGame : WerewolfIntent()

    // ===== 身份查看 =====
    data class RevealIdentity(val playerId: Int) : WerewolfIntent()
    data object RevealAllIdentities : WerewolfIntent()
}

/**
 * 夜间行动数据
 */
sealed class NightActionData {
    // 化身幽灵：选择复制目标
    data class DoppelgangerAction(val targetPlayerId: Int) : NightActionData()

    // 狼人：无其他狼人时查看底牌
    data class WerewolfPeekCenter(val centerIndex: Int) : NightActionData()

    // 预言家：查看1名玩家
    data class SeerViewPlayer(val targetPlayerId: Int) : NightActionData()

    // 预言家：查看2张中央底牌
    data class SeerViewCenter(val centerIndex1: Int, val centerIndex2: Int) : NightActionData()

    // 强盗：交换身份
    data class RobberSwap(val targetPlayerId: Int) : NightActionData()

    // 捣蛋鬼：交换两个玩家
    data class TroublemakerSwap(val target1Id: Int, val target2Id: Int) : NightActionData()

    // 酒鬼：交换底牌
    data class DrunkSwap(val centerIndex: Int) : NightActionData()

    // 失眠者：查看自己（自动执行，无选择）
    data object InsomniacCheck : NightActionData()

    // 守夜人：确认（自动执行）
    data object MasonConfirm : NightActionData()

    // 爪牙：确认（查看信息）
    data object MinionConfirm : NightActionData()
}
