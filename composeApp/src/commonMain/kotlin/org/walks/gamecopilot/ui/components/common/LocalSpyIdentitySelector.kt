package org.walks.gamecopilot.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.walks.gamecopilot.data.entity.LocalSpyEntity

/**
 * 本地卧底游戏的身份选择器
 * 基于通用身份选择器组件，专门为本地卧底游戏定制
 */
@Composable
fun LocalSpyIdentitySelector(
    key: Int,
    gameState: LocalSpyEntity,
    onNicknameChange: (Int, String) -> Unit,
    onRefreshIdentities: (() -> Unit)? = null
) {
    val playerNum = gameState.totalPlayerNumber
    val identities = List(playerNum) { index ->
        if (gameState.isSpy(index)) "卧底" else "平民"
    }
    // 确保昵称列表长度正确，如果没有昵称则使用默认的玩家编号
    // 使用remember确保昵称列表变化时组件会重新渲染
    val nicknames = remember(gameState.nicknames, playerNum) {
        if (gameState.nicknames.size == playerNum) {
            gameState.nicknames
        } else {
            List(playerNum) { (it + 1).toString() } // 默认使用玩家编号作为昵称
        }
    }

    IdentitySelector(
        key = key,
        playerNum = playerNum,
        identities = identities,
        nicknames = nicknames,
        onNicknameChange = onNicknameChange,
        onRefreshIdentities = onRefreshIdentities
        // 使用默认身份卡片，不需要自定义
    )
}