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
    val currentPage: Int = 0, // 当前页面索引
    // 扩展包新增字段
    val ladyOfLakeUsed: Boolean = false, // 湖中仙女是否已使用
    val sirGalahadUsed: Boolean = false, // 圆桌骑士是否已使用
    val morguseUsed: Boolean = false, // 莫高斯是否已使用
    val prophetChecked: Pair<Int, Int>? = null, // 预言者检查的玩家索引 (player1, player2)
    val ladyOfLakeChecked: Int? = null, // 湖中仙女检查的玩家索引
    val lancolotConverted: Boolean = false, // 兰斯洛特是否已转换阵营
    val shapeshifterTarget: AwalongRole? = null, // 变形者复制的目标角色
    val assassinationResult: Boolean? = null // 刺客刺杀结果（true=成功，false=失败，null=未刺杀）
)

data class AwalongGameDayEntity(
    val day: Int = 1,
    val mainTask: Map<Int, Int> = mutableMapOf(),
    val taskResult: Int=0,
    val murderTask: Int = -1,
    var captain: Int = -1,
    // 扩展包新增字段
    val requiresTwoFailures: Boolean = false, // 该任务是否需要2张失败卡才判定失败
    val morguseUsed: Boolean = false, // 莫高斯是否在该任务使用了能力
    val sirGalahadUsed: Boolean = false, // 圆桌骑士是否在该轮使用了双倍投票
    val plotCard: String? = null, // 该轮触发的情节卡
    
    // 新增状态保存字段
    val gamePhase: String = "TEAM_FORMATION", // 当前游戏阶段
    val teamVotes: Map<Int, Boolean> = emptyMap(), // 组队投票结果
    val taskVotes: Map<Int, Boolean> = emptyMap(), // 任务投票结果
    val selectedTeam: List<Int> = emptyList(), // 当前选中的队伍
    val currentCaptain: Int = -1, // 当前队长索引
    
    // 新增详细记录字段
    val skillUsageRecords: List<SkillUsageRecord> = emptyList(), // 技能使用记录
    val taskExecutionRecords: List<TaskExecutionRecord> = emptyList(), // 任务执行记录
    val lockedPlayers: Set<Int> = emptySet() // 被锁定的玩家
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
