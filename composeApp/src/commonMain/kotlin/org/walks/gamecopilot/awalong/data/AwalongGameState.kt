package org.walks.gamecopilot.awalong.data

import org.walks.gamecopilot.awalong.AwalongRole

/**
 *  Created by Wing at 10:39 on 2025/5/26
 *
 */
data class AwalongGameState(
    val playTime: Long = 0,
    val roleList: MutableList<AwalongRole> = mutableListOf(),
    val dayList: MutableList<AwalongGameDayEntity> = mutableListOf(),
    val isPublic: Boolean = false,
    val nickNameList:MutableList<String> = mutableListOf()
)

data class AwalongGameDayEntity(
    val day: Int = 1,
    val mainTask: Map<Int, Boolean>,
    val taskResult:Boolean,
    val murderTask: Int? = null,
)
