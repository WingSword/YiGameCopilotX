package org.walks.gamecopilot.intent

sealed class GameIntent {
    data class CreateAGameRoom(val roomId: String, val roomKey: String) : GameIntent()
    data class JoinToAGameRoom(
        val roomId: String,
        val roomKey: String,
        val asPrimary: Boolean = false
    ) : GameIntent()

    data class RefreshPlayerNumber(val num: Int = 0) : GameIntent()
    data class RefreshSpyNumber(val num: Int = 0) : GameIntent()
    data object RefreshRoomInfo : GameIntent()
    data class SwitchGameMode(val mode: Int = 0) : GameIntent()
    data object StartGame : GameIntent()
    data object LeaveGameRoom : GameIntent()
}