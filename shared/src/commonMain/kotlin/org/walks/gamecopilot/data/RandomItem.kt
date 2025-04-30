package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable

/**
 *  Created by Wing at 22:22 on 2025/4/25
 *
 */
@Serializable
data class RandomItem(
    var id: Int = 0,
    var second: String = "",
    var first: String = "",
    var cate: String = ""
)

@Serializable
data class RandomListEntity(
    val list: List<RandomItem> = listOf(),
    val name: String = "",
    var refreshTime: Long = 0
)
