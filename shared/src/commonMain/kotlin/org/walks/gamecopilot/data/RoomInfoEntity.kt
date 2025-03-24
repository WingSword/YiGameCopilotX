package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable


@Serializable
data class RoomInfoEntity(
    /**
     * 房主id
     */
    val ownerId: String,

    /**
     * 房间密码
     */
    val passWord: String,

    /**
     * 房间id
     */
    val roomId: String,

    /**
     * 房间状态
     */
    val status: String,

    /**
     * 房间所有用户信息
     */
    val users: String,

    /**
     * 游戏词
     */
    val wordPair: WordPair
) {
    /**
     * 游戏词
     */

    @Serializable
    data class WordPair(
        val civilianWord: String,
        val spyWord: String
    )
}