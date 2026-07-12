package org.walks.gamecopilot.ui.page.game.localspy.game.components

import androidx.compose.runtime.Composable
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.ui.components.common.LocalSpySwipeableIdentityCard

@Composable
fun LocalSpyIdentityCard(
    gameState: LocalSpyEntity,
    currentSelectPlayer: Int = 1,
    onClose: () -> Unit = {}
) {
    val identity = gameState.optIdentity(currentSelectPlayer)
    LocalSpySwipeableIdentityCard(
        resetKey = "local-spy-component-$currentSelectPlayer-$identity",
        playerNumber = currentSelectPlayer,
        nickname = gameState.nicknames.getOrNull(currentSelectPlayer - 1)
            ?.takeIf { it.isNotBlank() }
            ?: "玩家$currentSelectPlayer",
        identity = identity,
        isSpy = gameState.isSpy(currentSelectPlayer),
        onClose = onClose
    )
}
