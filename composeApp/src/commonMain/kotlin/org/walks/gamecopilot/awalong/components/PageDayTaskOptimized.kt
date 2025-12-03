package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AwalongConfig
import org.walks.gamecopilot.awalong.AwalongIntent
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.awalong.data.AwalongGameState

/**
 * 优化后的任务页面组件
 * 处理单日任务的完整流程：组队 -> 执行 -> 结果
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageDayTaskOptimized(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskNum: Int,
    dayEntity: AwalongGameDayEntity?,
    taskIndex: Int,
    gameConfig: AwalongConfig,
    gameState: AwalongGameState,
    viewmodel: MainViewmodel,
    onCheck: (Map<Int, Int>, Int, Int) -> Unit,
    pageState: PagerState? = null,
    scope: CoroutineScope? = null
) {
    // 从 gameState 获取当前天的状态，确保状态持久化
    val currentDayState = gameState.dayList.getOrNull(taskIndex)
    
    // 使用 remember 来管理本地状态，但通过 ViewModel 同步
    var gamePhase by remember { 
        mutableStateOf(when (currentDayState?.gamePhase) {
            "TEAM_FORMATION" -> TaskPhase.TEAM_FORMATION
            "TASK_EXECUTION" -> TaskPhase.TASK_EXECUTION
            "TASK_RESULT" -> TaskPhase.TASK_RESULT
            else -> TaskPhase.TEAM_FORMATION
        })
    }
    
    // 监听 currentDayState 的变化，同步 gamePhase
    LaunchedEffect(currentDayState?.gamePhase) {
        currentDayState?.let { state ->
            gamePhase = when (state.gamePhase) {
                "TEAM_FORMATION" -> TaskPhase.TEAM_FORMATION
                "TASK_EXECUTION" -> TaskPhase.TASK_EXECUTION
                "TASK_RESULT" -> TaskPhase.TASK_RESULT
                else -> TaskPhase.TEAM_FORMATION
            }
        }
    }
    
    var result by remember { mutableStateOf(currentDayState?.taskResult ?: 0) }
    var taskPlayer by remember { mutableStateOf(currentDayState?.selectedTeam?.toMutableList() ?: mutableListOf<Int>()) }
    var teamVotes by remember { mutableStateOf(currentDayState?.teamVotes?.toMutableMap() ?: mutableMapOf<Int, Boolean>()) }
    var taskVotes by remember { mutableStateOf(currentDayState?.taskVotes?.toMutableMap() ?: mutableMapOf<Int, Boolean>()) }
    
    val currentCaptain = remember(taskIndex) {
        if (currentDayState?.currentCaptain != -1) {
            currentDayState?.currentCaptain ?: -1
        } else if (taskIndex == 0) {
            roleList.indices.random()
        } else {
            ((currentDayState?.captain ?: 0 - 1 + roleList.size) % roleList.size)
        }
    }

    remember {
        mutableMapOf<Int, Int>()
    }

    // 保存状态的函数
    fun saveState() {
        val updatedDayEntity = AwalongGameDayEntity(
            day = taskIndex,
            mainTask = dayEntity?.mainTask ?: emptyMap(),
            taskResult = result,
            murderTask = dayEntity?.murderTask ?: -1,
            captain = dayEntity?.captain ?: -1,
            requiresTwoFailures = dayEntity?.requiresTwoFailures ?: false,
            morguseUsed = dayEntity?.morguseUsed ?: false,
            sirGalahadUsed = dayEntity?.sirGalahadUsed ?: false,
            plotCard = dayEntity?.plotCard,
            
            // 保存当前状态
            gamePhase = when (gamePhase) {
                TaskPhase.TEAM_FORMATION -> "TEAM_FORMATION"
                TaskPhase.TASK_EXECUTION -> "TASK_EXECUTION"
                TaskPhase.TASK_RESULT -> "TASK_RESULT"
            },
            teamVotes = teamVotes.toMap(),
            taskVotes = taskVotes.toMap(),
            selectedTeam = taskPlayer.toList(),
            currentCaptain = currentCaptain
        )
        
        // 通过 ViewModel 更新状态
        viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(updatedDayEntity))
    }
    
    // 监听状态变化并保存
    LaunchedEffect(gamePhase, taskPlayer, teamVotes, taskVotes, currentCaptain) {
        saveState()
    }
    
    Column {
        // 当前阶段指示器
        PhaseIndicator(currentPhase = gamePhase, taskResult = if (gamePhase == TaskPhase.TASK_RESULT) result else null)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (gamePhase) {
            TaskPhase.TEAM_FORMATION -> {
                TeamFormationPhase(
                    roleList = roleList,
                    nicknameList = nicknameList,
                    taskNum = taskNum,
                    taskPlayer = taskPlayer,
                    currentCaptain = currentCaptain,
                    onTeamComplete = { team ->
                        // 通过 ViewModel 更新状态
                        val updatedDayEntity = currentDayState?.copy(
                            selectedTeam = team,
                            gamePhase = "TASK_EXECUTION"
                        ) ?: AwalongGameDayEntity(
                            day = taskIndex,
                            mainTask = emptyMap(),
                            taskResult = 0,
                            murderTask = -1,
                            captain = currentCaptain,
                            selectedTeam = team,
                            gamePhase = "TASK_EXECUTION"
                        )
                        
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.UpdateDayState(updatedDayEntity)
                        )
                        
                        // 直接更新本地状态以确保UI立即响应
                        gamePhase = TaskPhase.TASK_EXECUTION
                    },
                    onTaskPlayerUpdate = { newTaskPlayer ->
                        taskPlayer = newTaskPlayer
                    }
                )
            }
            
            TaskPhase.TASK_EXECUTION -> {
                TaskExecutionPhaseOptimized(
                    roleList = roleList,
                    nicknameList = nicknameList,
                    taskPlayer = taskPlayer,
                    taskVotes = taskVotes,
                    requiresTwoFailures = currentDayState?.requiresTwoFailures ?: false,
                    onExecutionComplete = { votes, success ->
                        // 立即更新本地状态
                        taskVotes = votes.toMutableMap()
                        result = if (success) 1 else -1
                        gamePhase = TaskPhase.TASK_RESULT
                        
                        // 保存任务结果到dayEntity
                        val finalTaskMap = mutableMapOf<Int, Int>()
                        taskPlayer.forEach { playerIndex ->
                            finalTaskMap[playerIndex] = if (success) 1 else -1
                        }
                        
                        val updatedDayEntity = currentDayState?.copy(
                            taskVotes = votes.toMap(),
                            taskResult = if (success) 1 else -1,
                            gamePhase = "TASK_RESULT",
                            mainTask = finalTaskMap
                        ) ?: AwalongGameDayEntity(
                            day = taskIndex,
                            mainTask = finalTaskMap,
                            taskResult = if (success) 1 else -1,
                            murderTask = -1,
                            captain = currentCaptain,
                            taskVotes = votes.toMap(),
                            gamePhase = "TASK_RESULT"
                        )

                        // 立即保存到ViewModel
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.UpdateDayState(updatedDayEntity)
                        )

                        // 立即调用onCheck回调，通知主页面任务完成
                        onCheck(finalTaskMap, result, currentCaptain)
                    },
                    onBackToTeamFormation = {
                        // 撤回到组队阶段
                        gamePhase = TaskPhase.TEAM_FORMATION
                        val updatedDayEntity = currentDayState?.copy(
                            gamePhase = "TEAM_FORMATION"
                        )
                        updatedDayEntity?.let {
                            viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(it))
                        }
                    }
                )
            }
            
            TaskPhase.TASK_RESULT -> {
                TaskResultPhase(
                    result = result,
                    gameState = gameState,
                    viewmodel = viewmodel,
                    taskIndex = taskIndex,
                    taskNum = taskNum,
                    taskPlayer = taskPlayer, // 传递参与任务的玩家列表
                    onNextRound = {
                        // 实现进入下一轮的逻辑
                        proceedToNextRound(viewmodel, taskIndex, pageState, scope)
                    }
                )
            }
        }
    }
}