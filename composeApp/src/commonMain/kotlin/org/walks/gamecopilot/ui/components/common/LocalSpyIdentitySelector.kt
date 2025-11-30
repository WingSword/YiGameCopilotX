package org.walks.gamecopilot.ui.components.common

import androidx.compose.runtime.Composable
import org.walks.gamecopilot.data.entity.LocalSpyEntity

/**
 * 本地卧底游戏的身份选择器
 * 基于通用身份选择器组件，专门为本地卧底游戏定制
 */
@Composable
fun LocalSpyIdentitySelector(
    key: Int,
    gameState: LocalSpyEntity,
    onNicknameChange: (Int, String) -> Unit
) {
    val playerNum = gameState.totalPlayerNumber
    val identities = List(playerNum) { index ->
        if (gameState.isSpy(index)) "卧底" else "平民"
    }
    val nicknames = List(playerNum) { "" } // 本地卧底游戏不使用昵称

    IdentitySelector(
        key = key,
        playerNum = playerNum,
        identities = identities,
        nicknames = nicknames,
        onNicknameChange = onNicknameChange
        // 使用默认身份卡片，不需要自定义
    )
}