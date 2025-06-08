package org.walks.gamecopilot.awalong

/**
 *  Created by Wing at 17:17 on 2025/5/26
 *
 */

sealed class AwalongIntent {
    data class StartGame(val gameConfig:  AwalongConfig) : AwalongIntent()
    data object RestartGame : AwalongIntent()
    data class ChangeNickName(val nickName: String, val sn: Int) : AwalongIntent()
}