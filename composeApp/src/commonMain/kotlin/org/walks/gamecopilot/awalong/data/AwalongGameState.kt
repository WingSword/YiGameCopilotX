package org.walks.gamecopilot.awalong.data

import org.walks.gamecopilot.awalong.AwalongRole
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 *  Created by Wing at 10:39 on 2025/5/26
 *
 */
data class AwalongGameState(
    val playTime: Long = 0,
    val roleList: MutableList<AwalongRole> = mutableListOf(),
    val dayList: MutableList<AwalongGameDayEntity> = mutableListOf(),
    val isPublic: Boolean = false,
    val nickNameList: MutableList<String> = mutableListOf(),
    val currentPage: Int = 0,
    val useLadyOfLake: Boolean = false,
    val morguseUsed: Boolean = false,
    val ladyOfLakeHolder: Int? = null,
    val ladyOfLakeHoldersHistory: Set<Int> = emptySet(),
    val ladyOfLakeUsedForTaskIndex: Int? = null,
    val ladyOfLakeChecked: Int? = null,
    val lancolotConverted: Boolean = false,
    val shapeshifterTarget: AwalongRole? = null,
    val assassinationResult: Boolean? = null
)

data class AwalongGameDayEntity(
    val day: Int = 1,
    val mainTask: Map<Int, Int> = mutableMapOf(),
    val taskResult: Int=0,
    val murderTask: Int = -1,
    var captain: Int = -1,
    // 扩展包新增字段
    val requiresTwoFailures: Boolean = false,
    val morguseUsed: Boolean = false,
    val plotCard: String? = null,
    
    // 新增状态保存字段
    val gamePhase: String = "TEAM_FORMATION",
    val teamVotes: Map<Int, Boolean> = emptyMap(),
    val taskVotes: Map<Int, Boolean> = emptyMap(),
    val selectedTeam: List<Int> = emptyList(),
    val currentCaptain: Int = -1,
    
    // 新增详细记录字段
    val skillUsageRecords: List<SkillUsageRecord> = emptyList(),
    val taskExecutionRecords: List<TaskExecutionRecord> = emptyList(),
    val lockedPlayers: Set<Int> = emptySet()
)

/**
 * 技能使用记录
 */
data class SkillUsageRecord @OptIn(ExperimentalTime::class) constructor(
    val skillType: String, // 技能类型
    val userIndex: Int, // 使用者索引
    val targetIndex: Int? = null, // 目标索引（如果有的话）
    val description: String, // 描述
    val timestamp: Long = Clock.System.now().toEpochMilliseconds() // 时间戳
)

/**
 * 任务执行记录
 */
data class TaskExecutionRecord(
    val playerIndex: Int, // 玩家索引
    val taskResult: Boolean, // 任务结果（true=成功，false=失败）
    val day: Int, // 第几天
    val taskIndex: Int, // 任务索引
    val isLocked: Boolean = false // 是否被锁定
)
