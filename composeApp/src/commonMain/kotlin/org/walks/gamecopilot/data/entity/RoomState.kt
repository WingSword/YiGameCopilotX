package org.walks.gamecopilot.data.entity

data class RoomState(
    val startedGameMode: Int = 0,
    val roomFinished: Boolean = false,
    val roomNumber: String = "",
    val playerNo: Int = 1,
    val playerNum: Int = 1
)