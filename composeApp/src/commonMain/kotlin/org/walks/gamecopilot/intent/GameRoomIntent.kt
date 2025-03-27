package org.walks.gamecopilot.intent

/**
 *  Created by Wing at 16:38 on 2025/3/27
 *
 */
sealed class GameRoomIntent {
    data class CreateAGameRoom(val roomId: String, val roomKey: String) : GameRoomIntent()
    data class JoinToAGameRoom(
        val roomId: String,
        val roomKey: String,
    ) : GameRoomIntent()
    data object LeaveGameRoom : GameRoomIntent()
    data object RefreshRoomInfo : GameRoomIntent()
    data object StartGame : GameRoomIntent()
    data object DeleteGameRoom : GameRoomIntent()
}