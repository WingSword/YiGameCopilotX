package org.walks.gamecopilot.intent

sealed class GameIntent {
    data class SwitchGameMode(val mode: Int = 0) : GameIntent()
    data class RefreshPlayerNumber(val num: Int = 0) : GameIntent()
    data class RefreshSpyNumber(val spyNum: Int = 0, val blackNum: Int = 0) : GameIntent()
    data class RefreshWordGroups(val selectedGroups: Set<String>) : GameIntent()
    data object StartGame : GameIntent()
    data object RefreshIdentities : GameIntent()
    data class UpdateNickname(val playerIndex: Int, val newNickname: String) : GameIntent()
}