package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AllResultsDialog
import org.walks.gamecopilot.awalong.AwalongIntent
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.awalong.data.AwalongGameState

/**
 * 游戏阶段枚举
 */
enum class TaskPhase {
    TEAM_FORMATION,    // 组队阶段
    TASK_EXECUTION,    // 任务执行阶段
    TASK_RESULT        // 任务结果阶段
}

/**
 * 阶段指示器组件
 * 显示当前游戏阶段和状态
 */
@Composable
fun PhaseIndicator(currentPhase: TaskPhase, taskResult: Int? = null) {
    val (containerColor, contentColor, phaseText) = when (currentPhase) {
        TaskPhase.TEAM_FORMATION -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            "队长组队"
        )
        TaskPhase.TASK_EXECUTION -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary,
            "任务执行"
        )
        TaskPhase.TASK_RESULT -> {
            if (taskResult == 1) {
                Triple(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primary,
                    "任务成功"
                )
            } else if (taskResult == -1) {
                Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.error,
                    "任务失败"
                )
            } else {
                Triple(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    "任务结果"
                )
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "当前阶段：",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            
            Text(
                text = phaseText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * 组队阶段组件
 * 处理队长选择执行任务队员的逻辑
 */
@Composable
fun TeamFormationPhase(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskNum: Int,
    taskPlayer: MutableList<Int>,
    currentCaptain: Int,
    onTeamComplete: (List<Int>) -> Unit,
    onTaskPlayerUpdate: (MutableList<Int>) -> Unit
) {
    // 创建一个本地状态来触发重组
    var selectedTeam by remember { mutableStateOf(taskPlayer.toList()) }
    
    // 当外部 taskPlayer 变化时，同步到本地状态
    LaunchedEffect(taskPlayer) {
        selectedTeam = taskPlayer.toList()
    }
    
    Column {
        val captainName = if (currentCaptain >= 0 && currentCaptain < nicknameList.size) {
            nicknameList[currentCaptain]
        } else {
            "未确定"
        }
        
        Text(
            text = "队长（${captainName}）请选择 $taskNum 位玩家执行任务：",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(roleList.size) { playerIndex ->
                PlayerCard(
                    playerIndex = playerIndex,
                    nickname = nicknameList[playerIndex],
                    role = roleList[playerIndex],
                    isSelected = selectedTeam.contains(playerIndex),
                    isCaptain = playerIndex == currentCaptain,
                    onClick = {
                        val newTeam = if (selectedTeam.contains(playerIndex)) {
                            selectedTeam - playerIndex
                        } else if (selectedTeam.size < taskNum) {
                            selectedTeam + playerIndex
                        } else {
                            selectedTeam
                        }
                        selectedTeam = newTeam
                        
                        // 更新外部状态
                        onTaskPlayerUpdate(newTeam.toMutableList())
                    }
                )
            }
            
            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (selectedTeam.size == taskNum) {
                            onTeamComplete(selectedTeam.toList())
                        }
                    },
                    enabled = selectedTeam.size == taskNum,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "确认组队 (${selectedTeam.size}/$taskNum)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 任务结果阶段组件
 * 显示任务结果和下一步操作
 */
@Composable
fun TaskResultPhase(
    result: Int,
    gameState: AwalongGameState,
    viewmodel: MainViewmodel,
    taskIndex: Int,
    taskNum: Int,
    taskPlayer: List<Int> = emptyList(), // 添加参与任务的玩家列表
    onNextRound: () -> Unit
) {
    var showAllResults by remember { mutableStateOf(false) }

    // 判断游戏是否结束 - 实时计算，不使用remember缓存
    val successCount =
        gameState.dayList.count { it.gamePhase == "TASK_RESULT" && it.taskResult == 1 }
    val failureCount =
        gameState.dayList.count { it.gamePhase == "TASK_RESULT" && it.taskResult == -1 }
    val totalRounds = gameState.dayList.size

    // 好人完成3次任务成功，或坏人完成2次任务失败，或已完成所有轮次
    val isGameComplete =
        successCount >= 3 || failureCount >= 2 || (totalRounds >= 5 && (successCount >= 3 || failureCount >= 2))
    
    // 获取所有锁定的玩家
    val lockedPlayers = remember(taskIndex) {
        gameState.dayList.take(taskIndex + 1).flatMap { it.lockedPlayers }.toSet()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 显示任务结果和参与玩家信息
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (result) {
                    1 -> MaterialTheme.colorScheme.primaryContainer
                    -1 -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "任务结果：${if (result == 1) "成功" else "失败"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (result) {
                        1 -> MaterialTheme.colorScheme.primary
                        -1 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // 显示参与本轮任务的玩家
                if (taskPlayer.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "参与本轮任务的玩家：${taskPlayer.map { gameState.nickNameList[it] }.joinToString(", ")}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 操作按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onNextRound()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("下一轮")
            }
            
            // 只有在游戏结束时才显示查看结果按钮
            if (isGameComplete) {
                Button(
                    onClick = { showAllResults = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看结果")
                }
            }
        }
        
        // 角色选择区域（显示锁定状态）
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(gameState.roleList.size) { playerIndex ->
                val isLocked = lockedPlayers.contains(playerIndex)
                PlayerCard(
                    playerIndex = playerIndex,
                    nickname = gameState.nickNameList[playerIndex],
                    role = gameState.roleList[playerIndex],
                    isSelected = false, // 任务完成后不显示选中状态
                    isCaptain = false,
                    isLocked = isLocked, // 添加锁定状态
                    onClick = { /* 锁定的玩家不可点击 */ }
                )
            }
        }
        
        // 显示所有结果对话框
        if (showAllResults) {
            AllResultsDialog(
                gameState = gameState,
                onDismiss = { showAllResults = false }
            )
        }
    }
}

/**
 * 处理进入下一轮的逻辑
 */
@OptIn(ExperimentalFoundationApi::class)
fun proceedToNextRound(
    viewmodel: MainViewmodel,
    currentTaskIndex: Int,
    pageState: PagerState? = null,
    scope: CoroutineScope? = null
) {
    // 更新当前任务状态为已完成
    val currentDay = viewmodel.awalongGameState.value.dayList.getOrNull(currentTaskIndex)
    currentDay?.let { day ->
        val updatedDay = day.copy(gamePhase = "TASK_RESULT")
        viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(updatedDay))
    }
    
    // 如果存在下一个任务，初始化下一个任务的状态
    val nextTaskIndex = currentTaskIndex + 1
    val totalTasks = viewmodel.awalongConfigState.value.process.size
    
    if (nextTaskIndex < totalTasks) {
        val nextDay = viewmodel.awalongGameState.value.dayList.getOrNull(nextTaskIndex)
        if (nextDay == null) {
            // 创建新的任务日状态
            val newDay = AwalongGameDayEntity(
                day = nextTaskIndex,
                mainTask = emptyMap(),
                taskResult = 0,
                murderTask = -1,
                captain = -1,
                gamePhase = "TEAM_FORMATION",
                teamVotes = emptyMap(),
                taskVotes = emptyMap(),
                selectedTeam = emptyList(),
                currentCaptain = -1,
                lockedPlayers = emptySet()
            )
            viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(newDay))
        } else {
            // 更新现有任务状态为组队阶段
            val updatedDay = nextDay.copy(gamePhase = "TEAM_FORMATION")
            viewmodel.handleAwalongGameIntent(AwalongIntent.UpdateDayState(updatedDay))
        }
        
        // 切换到下一页
        if (pageState != null && scope != null) {
            scope.launch {
                pageState.scrollToPage(nextTaskIndex + 1) // +1 因为第0页是第零日
            }
        }
    }
}