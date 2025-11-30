package org.walks.gamecopilot.awalong.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onNameChange: (String, Int) -> Unit
) {
    // 用于强制刷新身份选择器的key
    var refreshKey by remember { mutableIntStateOf(0) }

    // 监听昵称列表变化，强制刷新组件
    LaunchedEffect(nicknameList) {
        refreshKey++
    }

    // 确保角色列表和昵称列表长度一致
    val playerNum = roleList.size
    val identities = roleList.map { it.title }
    val safeNicknameList = if (nicknameList.size >= playerNum) {
        nicknameList.take(playerNum)
    } else {
        nicknameList + List(playerNum - nicknameList.size) { "" }
    }

    // 监听角色变化，刷新组件
    LaunchedEffect(roleList) {
        refreshKey++
    }

    IdentitySelector(
        key = refreshKey,
        playerNum = playerNum,
        identities = identities,
        nicknames = safeNicknameList,
        onNicknameChange = { playerIndex, newNickname ->
            onNameChange(newNickname, playerIndex)
        },
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