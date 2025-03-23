package org.walks.gamecopilot

import kotlinx.serialization.Serializable


@Serializable
data class RoomDataModel(
    val code: Int=0,
    val msg: String?=null,
    val data: String?=null
)