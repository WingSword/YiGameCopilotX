package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable

/**
 *  Created by Wing at 17:08 on 2025/3/25
 *
 */
@Serializable
data class UserInfoEntity(
    val assignedWord: String? = null,
    val userId: String? = null,
    val role: String? = null,
    val index: String? = "1",
    val userSession:String?=null,
    val userStatus:Boolean=false,
    var isMine:Boolean=false,

    ) {

}