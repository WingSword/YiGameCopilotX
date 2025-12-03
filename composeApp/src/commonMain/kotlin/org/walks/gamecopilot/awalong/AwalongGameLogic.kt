package org.walks.gamecopilot.awalong

import org.walks.gamecopilot.awalong.AwalongRole.AOBOLUN
import org.walks.gamecopilot.awalong.AwalongRole.CISHA
import org.walks.gamecopilot.awalong.AwalongRole.LADY_OF_LAKE
import org.walks.gamecopilot.awalong.AwalongRole.LANCELOT
import org.walks.gamecopilot.awalong.AwalongRole.MEILING
import org.walks.gamecopilot.awalong.AwalongRole.MODELEDE
import org.walks.gamecopilot.awalong.AwalongRole.MOGANNA
import org.walks.gamecopilot.awalong.AwalongRole.MORGUSE
import org.walks.gamecopilot.awalong.AwalongRole.PAIXIWEIWEIER
import org.walks.gamecopilot.awalong.AwalongRole.PROPHET
import org.walks.gamecopilot.awalong.AwalongRole.SHAPESHIFTER
import org.walks.gamecopilot.awalong.AwalongRole.SIR_GALAHAD
import org.walks.gamecopilot.awalong.data.AwalongGameState

/**
 * 阿瓦隆扩展包游戏逻辑辅助类
 * 处理复杂的游戏规则和角色能力
 */
object AwalongGameLogic {

    /**
     * 检查指定玩家能看到哪些角色
     */
    fun getVisibleRoles(playerRole: AwalongRole, allRoles: List<AwalongRole>, gameState: AwalongGameState): Map<Int, AwalongRole> {
        return when (playerRole) {
            MEILING -> {
                // 梅林能看到除莫德雷德外的所有坏人
                allRoles.mapIndexedNotNull { index, role ->
                    if (role.roleType == BAD_PERSON && role != MODELEDE) {
                        index to role
                    } else null
                }.toMap()
            }
            
            PAIXIWEIWEIER -> {
                // 派西维尔能看到梅林和莫甘娜
                allRoles.mapIndexedNotNull { index, role ->
                    if (role == MEILING || role == MOGANNA) {
                        index to role
                    } else null
                }.toMap()
            }
            
            PROPHET -> {
                // 预言者检查过的玩家
                val checked = gameState.prophetChecked
                if (checked != null) {
                    mapOf(
                        checked.first to allRoles[checked.first],
                        checked.second to allRoles[checked.second]
                    )
                } else emptyMap()
            }
            
            LADY_OF_LAKE -> {
                // 湖中仙女检查过的玩家
                val checked = gameState.ladyOfLakeChecked
                if (checked != null) {
                    mapOf(checked to allRoles[checked])
                } else emptyMap()
            }
            
            AOBOLUN -> {
                // 奥伯伦看不到任何人
                emptyMap()
            }
            
            SHAPESHIFTER -> {
                // 变形者复制了谁的能力就有什么样的视野
                val target = gameState.shapeshifterTarget
                if (target != null) {
                    getVisibleRoles(target, allRoles, gameState)
                } else {
                    // 默认坏人视野（看不到奥伯伦）
                    allRoles.mapIndexedNotNull { index, role ->
                        if (role.roleType == BAD_PERSON && role != AOBOLUN && role != playerRole) {
                            index to role
                        } else null
                    }.toMap()
                }
            }
            
            LANCELOT -> {
                // 兰斯洛特根据阵营转换状态决定视野
                if (gameState.lancolotConverted) {
                    // 转换后是红方，可以看到其他坏人
                    allRoles.mapIndexedNotNull { index, role ->
                        if (role.roleType == BAD_PERSON && role != MODELEDE && role != AOBOLUN && role != playerRole) {
                            index to role
                        } else null
                    }.toMap()
                } else {
                    // 转换前是蓝方，没有特殊视野
                    emptyMap()
                }
            }
            
            else -> {
                // 其他角色使用默认逻辑
                playerRole.checkSkills(allRoles)
            }
        }
    }

    /**
     * 检查任务是否需要2张失败卡才判定失败
     * 根据标准阿瓦隆规则：
     * - 7人游戏：第4个任务需要2张失败卡
     * - 8-10人游戏：第4个及以后的任务需要2张失败卡
     */
    fun requiresTwoFailures(taskIndex: Int, playerNum: Int): Boolean {
        return when (playerNum) {
            7 -> taskIndex == 3 // 第4个任务（索引3）
            8, 9, 10 -> taskIndex >= 3 // 第4个任务及以后
            else -> false
        }
    }

    /**
     * 检查游戏是否结束
     */
    fun checkGameEnd(gameState: AwalongGameState): GameEndResult? {
        val successTasks = gameState.dayList.count { it.taskResult == 1 }
        val failedTasks = gameState.dayList.count { it.taskResult == -1 }

        // 调试信息：打印当前任务状态
        println("游戏结束检查：成功任务=$successTasks, 失败任务=$failedTasks")

        // 检查红方胜利条件（优先检查，因为红方胜利条件更直接）
        if (failedTasks >= 3) {
            println("红方胜利：破坏3个任务")
            return GameEndResult(
                winner = "红方",
                reason = "破坏3个任务"
            )
        }
        
        // 检查蓝方胜利条件
        if (successTasks >= 3) {
            println("蓝方完成3个任务，检查刺客角色")
            // 蓝方完成3个任务，但如果有刺客，需要进入刺杀阶段
            if (gameState.roleList.contains(CISHA)) {
                println("有刺客，进入刺杀阶段")
                return GameEndResult(
                    winner = "蓝方",
                    reason = "完成3个成功任务，进入刺杀阶段"
                )
            } else {
                println("无刺客，蓝方直接胜利")
                return GameEndResult(
                    winner = "蓝方",
                    reason = "完成3个成功任务"
                )
            }
        }
        
        // 检查奥伯伦单独胜利条件
        val oberonIndex = gameState.roleList.indexOf(AOBOLUN)
        if (oberonIndex != -1 && failedTasks >= 2 && successTasks >= 3) {
            // 奥伯伦需要单独胜利：至少破坏2个任务 + 蓝方达成3胜后刺杀梅林 + 未被揭露
            if (gameState.prophetChecked?.let { it.first == oberonIndex || it.second == oberonIndex } != true &&
                gameState.ladyOfLakeChecked != oberonIndex) {
                println("奥伯伦独立胜利")
                return GameEndResult(
                    winner = "奥伯伦",
                    reason = "独立胜利：破坏2个任务且未被揭露身份"
                )
            }
        }

        println("游戏未结束")
        return null
    }

    /**
     * 检查刺杀是否成功（简化逻辑）
     */
    fun checkAssassinationSuccess(assassinationTarget: Int, gameState: AwalongGameState): Boolean {
        // 检查刺杀目标是否是梅林
        return gameState.roleList[assassinationTarget] == MEILING
    }

    /**
     * 获取可用的特殊能力
     */
    fun getAvailableAbilities(playerRole: AwalongRole, gameState: AwalongGameState): List<String> {
        val abilities = mutableListOf<String>()
        
        when (playerRole) {
            PROPHET -> {
                if (gameState.prophetChecked == null) {
                    abilities.add("查看2名玩家阵营")
                }
            }
            
            LADY_OF_LAKE -> {
                if (!gameState.ladyOfLakeUsed && gameState.dayList.count { it.taskResult != 0 } >= 2) {
                    abilities.add("查看1名玩家阵营")
                }
            }
            
            SIR_GALAHAD -> {
                if (!gameState.sirGalahadUsed) {
                    abilities.add("双倍投票权")
                }
            }
            
            MORGUSE -> {
                if (!gameState.morguseUsed) {
                    abilities.add("转换成功卡为失败卡")
                }
            }
            
            SHAPESHIFTER -> {
                if (gameState.shapeshifterTarget == null) {
                    abilities.add("复制角色能力")
                }
            }
            
            LANCELOT -> {
                if (!gameState.lancolotConverted) {
                    abilities.add("阵营转换（需抽卡）")
                }
            }
            
            else -> {}
        }
        
        return abilities
    }
}

/**
 * 游戏结束结果
 */
data class GameEndResult(
    val winner: String,
    val reason: String
)