package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable

/**
 *  Created by Wing at 17:15 on 2025/6/8
 *
 * usersNumber：当前房间人数。（类型为int）
 *
 * role：玩家身份，未开始为“NOROLE”（无身份）
 *
 * index：玩家房间内编号。（类型为String）
 *
 * wordPair：平民、卧底词汇，未开始为空。
 *
 * type：返回请求的操作类型
 *
 * roomId：房间号
 *
 * type：返回请求的操作类型
 *
 * users：玩家列表。
 *
 * role，玩家身份
 *
 * userStatus，状态信息，true：该编号位置有玩家；false，该编号位置无玩家
 *
 * index，玩家房间内编号
 *
 * userSession，用户session标识
 *
 * assignedWord，分配的词汇
 *
 * assignedWord：玩家分配的词汇，未分配时为空。
 *
 *
 */
@Serializable
data class WsRoomDataEntity(
    val updateTime:Long=0L,
    val startedGameMode: Int = 0,
    val roomFinished: Int = 0,
    val isRoomOwner:Boolean  = false,
    val roomKey: String = "",
    val roomId: String = "",
    val usersNumber: Int = 1,
    val role: String? = "",
    val index: String? = "",
    val wordPair: WordPairEntity? = null,
    val type: String? = "",

    val assignedWord: String? = "",
    val civilianWord: String? = "",
    val users: List<UserInfoEntity>? = null,

    )

@Serializable
data class WordPairEntity(
    val civilianWord: String?,
    val spyWord: String?
) {

}
