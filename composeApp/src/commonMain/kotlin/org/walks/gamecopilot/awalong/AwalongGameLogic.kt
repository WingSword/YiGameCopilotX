package org.walks.gamecopilot.awalong

import org.walks.gamecopilot.awalong.AwalongRole.AOBOLUN
import org.walks.gamecopilot.awalong.AwalongRole.CISHA
import org.walks.gamecopilot.awalong.AwalongRole.LANCELOT
import org.walks.gamecopilot.awalong.AwalongRole.MEILING
import org.walks.gamecopilot.awalong.AwalongRole.MODELEDE
import org.walks.gamecopilot.awalong.AwalongRole.MOGANNA
import org.walks.gamecopilot.awalong.AwalongRole.MORGUSE
import org.walks.gamecopilot.awalong.AwalongRole.PAIXIWEIWEIER
import org.walks.gamecopilot.awalong.AwalongRole.SHAPESHIFTER
import org.walks.gamecopilot.awalong.data.AwalongGameState

object AwalongGameLogic {

    fun getVisibleRoles(playerRole: AwalongRole, allRoles: List<AwalongRole>, gameState: AwalongGameState): Map<Int, AwalongRole> {
        return when (playerRole) {
            MEILING -> {
                allRoles.mapIndexedNotNull { index, role ->
                    if (role.roleType == BAD_PERSON && role != MODELEDE) {
                        index to role
                    } else null
                }.toMap()
            }
            
            PAIXIWEIWEIER -> {
                allRoles.mapIndexedNotNull { index, role ->
                    if (role == MEILING || role == MOGANNA) {
                        index to role
                    } else null
                }.toMap()
            }
            
            AOBOLUN -> {
                allRoles.mapIndexedNotNull { index, role ->
                    if (role.roleType == BAD_PERSON && role != playerRole) {
                        index to role
                    } else null
                }.toMap()
            }
            
            SHAPESHIFTER -> {
                val target = gameState.shapeshifterTarget
                if (target != null) {
                    getVisibleRoles(target, allRoles, gameState)
                } else {
                    allRoles.mapIndexedNotNull { index, role ->
                        if (role.roleType == BAD_PERSON && role != playerRole) {
                            index to role
                        } else null
                    }.toMap()
                }
            }
            
            LANCELOT -> {
                if (gameState.lancolotConverted) {
                    allRoles.mapIndexedNotNull { index, role ->
                        if (role.roleType == BAD_PERSON && role != MODELEDE && role != playerRole) {
                            index to role
                        } else null
                    }.toMap()
                } else {
                    emptyMap()
                }
            }
            
            else -> {
                playerRole.checkSkills(allRoles)
            }
        }
    }

    fun requiresTwoFailures(taskIndex: Int, playerNum: Int): Boolean {
        return when (playerNum) {
            7 -> taskIndex == 3
            8, 9, 10 -> taskIndex >= 3
            else -> false
        }
    }

    fun checkGameEnd(gameState: AwalongGameState): GameEndResult? {
        val successTasks = gameState.dayList.count { it.taskResult == 1 }
        val failedTasks = gameState.dayList.count { it.taskResult == -1 }

        println("游戏结束检查：成功任务=$successTasks, 失败任务=$failedTasks")

        if (failedTasks >= 3) {
            println("红方胜利：破坏3个任务")
            return GameEndResult(
                winner = "红方",
                reason = "破坏3个任务"
            )
        }
        
        if (successTasks >= 3) {
            println("蓝方完成3个任务，检查刺客角色")
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

        println("游戏未结束")
        return null
    }

    fun checkAssassinationSuccess(assassinationTarget: Int, gameState: AwalongGameState): Boolean {
        return gameState.roleList[assassinationTarget] == MEILING
    }

    fun getAvailableAbilities(playerRole: AwalongRole, gameState: AwalongGameState): List<String> {
        val abilities = mutableListOf<String>()
        
        when (playerRole) {
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

data class GameEndResult(
    val winner: String,
    val reason: String
)
