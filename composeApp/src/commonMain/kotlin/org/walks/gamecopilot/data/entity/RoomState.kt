package org.walks.gamecopilot.data.entity

import org.walks.gamecopilot.data.UserInfoEntity

data class RoomState(
    val startedGameMode: Int = 0,
    val roomFinished: Boolean = false,
    val roomId: String = "",
    val roomKey: String = "",
    val playerNo: Int = 1,
    val roomPlayerNum: Int = 1,
    val users: String = "",
    var memberList: List<UserInfoEntity> = listOf()
) {

}