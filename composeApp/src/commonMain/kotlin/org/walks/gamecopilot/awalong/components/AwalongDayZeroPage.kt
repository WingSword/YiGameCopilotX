package org.walks.gamecopilot.awalong.components

import androidx.compose.runtime.Composable
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.ui.components.common.IdentitySelector

@Composable
fun AwalongDayZeroPage(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    playTime: Long,
    onNameChange: (String, Int) -> Unit,
    onRefreshRoles: (() -> Unit)? = null
) {
    val playerNum = roleList.size
    val identities = roleList.map { it.title }
    val safeNicknameList = if (nicknameList.size >= playerNum) {
        nicknameList.take(playerNum)
    } else {
        nicknameList + List(playerNum - nicknameList.size) { "" }
    }

    val key = playTime.toInt()
    println("AwalongDayZeroPage: playTime=$playTime, key=$key")
    
    IdentitySelector(
        refreshKey = key,
        playerNum = playerNum,
        identities = identities,
        nicknames = safeNicknameList,
        onNicknameChange = { index, nickname -> onNameChange(nickname, index) },
        onRefreshIdentities = onRefreshRoles,
        customIdentityCard = { playerNumber, identity, nickname, onClose ->
            val role = roleList.find { it.title == identity } ?: roleList[playerNumber - 1]
            AwalongIdentityCard(
                playerNumber = playerNumber,
                role = role,
                nickname = nickname,
                allRoles = roleList,
                allNicknames = safeNicknameList,
                onClose = onClose
            )
        }
    )
}
