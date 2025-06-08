package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable


@Serializable
data class RoomDataModel<T>(
    val code: Int = 0,
    val msg: String? = null,
    val data: T? = null
) {
    fun isSuccess(): Boolean {
        return code == 1000
    }
}
