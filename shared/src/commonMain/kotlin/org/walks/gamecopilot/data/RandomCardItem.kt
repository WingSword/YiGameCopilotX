package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable

/**
 *  Created by Wing at 22:22 on 2025/4/25
 *
 */
@Serializable
data class RandomCardItem(
    var id: Int = 0,
    var front:String="",
    var back:String=""
)
@Serializable
data class RandomListEntity(
    val list: List<RandomCardItem> = listOf(),
    val name:String="",
)
