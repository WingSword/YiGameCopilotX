package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable

/**
 *  Created by Wing at 17:15 on 2025/6/8
 *
 */
@Serializable
data class WsDataEntity(
    val usersNumber: Int=1,
    val role: String?="",
    val index: String?="",
    val wordPair: WordPairEntity?=null,
    val type: String?="",
    val roomId: String?="",

    val assignedWord: String?="",
    val civilianWord: String?="",
    val users:List<UserInfoEntity>?=null,

    )

@Serializable
data class WordPairEntity(
    val civilianWord:String?,
    val spyWord:String?
){

}
