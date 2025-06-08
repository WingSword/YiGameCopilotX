package org.walks.gamecopilot.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 新增WebSocket消息实体
@Serializable
data class WsMessage(
    val type:  String,
    val roomId: String,

    val passWord: String? = null,
    val userId: String? = null,
    val payload: String? = null // 附加数据
)
