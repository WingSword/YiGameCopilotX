package org.walks.gamecopilot.awalong.components

import androidx.compose.runtime.Composable
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.ui.components.common.IdentitySelector

/**
 * 阿瓦隆游戏第0天页面
 * 使用通用身份选择器组件重构，支持阿瓦隆特有的身份卡片显示
 */
@Composable
fun AwalongDayZeroPage(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    playTime: Long,
    onNameChange: (String, Int) -> Unit,
    onRefreshRoles: (() -> Unit)? = null
) {
    // 确保角色列表和昵称列表长度一致
    val playerNum = roleList.size
    val identities = roleList.map { it.title }
    val safeNicknameList = if (nicknameList.size >= playerNum) {
        nicknameList.take(playerNum)
    } else {
        nicknameList + List(playerNum - nicknameList.size) { "" }
    }

    // 直接使用playTime作为key，确保每次重启key都不同
    val key = playTime.toInt()
    println("AwalongDayZeroPage: playTime=$playTime, key=$key")
    
    IdentitySelector(
        refreshKey = key, // 组合key确保触发重组
        playerNum = playerNum,
        identities = identities,
        nicknames = safeNicknameList,
        onNicknameChange = { index, nickname -> onNameChange(nickname, index) },
        onRefreshIdentities = onRefreshRoles,
        customIdentityCard = { playerNumber, identity, nickname ->
            // 找到对应的角色对象
            val role = roleList.find { it.title == identity } ?: roleList[playerNumber - 1]
            AwalongIdentityCard(
                playerNumber = playerNumber,
                role = role,
                nickname = nickname,
                allRoles = roleList,
                allNicknames = safeNicknameList
            )
        }
    )
}