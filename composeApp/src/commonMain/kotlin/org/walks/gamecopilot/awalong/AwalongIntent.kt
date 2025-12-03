package org.walks.gamecopilot.awalong

import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity

/**
 *  Created by Wing at 17:17 on 2025/5/26
 *
 */

sealed class AwalongIntent {
    data class StartGame(val gameConfig:  AwalongConfig) : AwalongIntent()
    data class StartCustomGame(val customConfig: AwalongCustomConfig) : AwalongIntent()
    data object RestartGame : AwalongIntent()
    data class ChangeNickName(val nickName: String, val sn: Int) : AwalongIntent()
    data class CheckTask(val task:AwalongGameDayEntity) : AwalongIntent()
    
    // 扩展包新增Intent
    data class ProphetCheck(val player1Index: Int, val player2Index: Int) : AwalongIntent()
    data class LadyOfLakeCheck(val playerIndex: Int) : AwalongIntent()
    data object SirGalahadUseDoubleVote : AwalongIntent()
    data class MorguseConvertSuccessToFailure(val taskIndex: Int) : AwalongIntent()
    data class ShapeshifterCopy(val targetRole: AwalongRole) : AwalongIntent()
    data object LancelotConvert : AwalongIntent()
    data object DrawPlotCard : AwalongIntent()
    
    // 新增游戏操作Intent
    data class SelectCaptain(val captainIndex: Int) : AwalongIntent()
    data class FormTeam(val taskIndex: Int, val teamMembers: List<Int>) : AwalongIntent()
    data class VoteTeam(val taskIndex: Int, val vote: Boolean) : AwalongIntent()
    data class ExecuteTask(val taskIndex: Int, val success: Boolean) : AwalongIntent()
    data class UpdateDayState(val dayState: AwalongGameDayEntity) : AwalongIntent()
    data class UpdateCurrentPage(val pageIndex: Int) : AwalongIntent()
    data class Assassinate(val targetIndex: Int) : AwalongIntent()
    data object CheckGameEnd : AwalongIntent()
}