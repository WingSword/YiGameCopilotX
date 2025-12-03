package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.awalong.AwalongRole
import org.walks.gamecopilot.awalong.BAD_PERSON
import org.walks.gamecopilot.awalong.GOOD_PERSON

/**
 * 优化后的任务执行阶段组件
 * 处理任务执行投票逻辑，支持撤回功能
 */
@Composable
fun TaskExecutionPhaseOptimized(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskPlayer: List<Int>,
    taskVotes: MutableMap<Int, Boolean>,
    requiresTwoFailures: Boolean,
    onExecutionComplete: (Map<Int, Boolean>, Boolean) -> Unit,
    onBackToTeamFormation: () -> Unit
) {
    // 创建本地状态来确保UI立即响应
    val localTaskVotes = remember { mutableStateOf(taskVotes.toMutableMap()) }
    
    // 同步外部状态变化到本地状态
    LaunchedEffect(taskVotes) {
        localTaskVotes.value = taskVotes.toMutableMap()
    }
    val taskPlayerIndices = taskPlayer.filter { it < roleList.size }
    
    Column {
        Text(
            text = "任务执行阶段",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "请被选中的玩家点击自己的头像选择任务执行结果：",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Column {
            // 撤回按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onBackToTeamFormation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("撤回组队")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(taskPlayerIndices.size) { index ->
                    val playerIndex = taskPlayerIndices[index]
                    val hasVoted = localTaskVotes.value.containsKey(playerIndex)
                    
                    PlayerTaskCardOptimized(
                        playerIndex = playerIndex,
                        nickname = nicknameList[playerIndex],
                        role = roleList[playerIndex],
                        hasVoted = hasVoted,
                        voteResult = localTaskVotes.value[playerIndex],
                        onVote = { success ->
                            // 立即更新本地状态防止重复点击
                            val newLocalVotes = localTaskVotes.value.toMutableMap()
                            newLocalVotes[playerIndex] = success
                            localTaskVotes.value = newLocalVotes
                            
                            // 同步到外部状态
                            taskVotes[playerIndex] = success
                            
                            // 检查是否所有玩家都已投票
                            if (localTaskVotes.value.size == taskPlayerIndices.size) {
                                localTaskVotes.value.values.count { it }
                                val failureVotes = localTaskVotes.value.values.count { !it }
                                localTaskVotes.value.size

                                // 根据阿瓦隆规则判断任务结果
                                val isSuccess = if (requiresTwoFailures) {
                                    // 需要2张失败卡才失败的特殊任务
                                    failureVotes < 2
                                } else {
                                    // 普通任务：只要有1张失败卡就失败
                                    failureVotes == 0
                                }

                                onExecutionComplete(localTaskVotes.value.toMap(), isSuccess)
                            }
                        }
                    )
                }
                
                item(span = { GridItemSpan(3) }) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 显示当前投票进度
                    Text(
                        text = "投票进度：${localTaskVotes.value.size}/${taskPlayerIndices.size}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 优化后的玩家任务卡片组件
 * 显示任务执行投票状态
 */
@Composable
private fun PlayerTaskCardOptimized(
    playerIndex: Int,
    nickname: String,
    role: AwalongRole,
    hasVoted: Boolean,
    voteResult: Boolean?,
    onVote: (Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = when {
                hasVoted -> if (voteResult == true) MaterialTheme.colorScheme.primaryContainer 
                          else MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasVoted) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { if (!hasVoted) showDialog = true }
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 号码显示区域
            Box(
                modifier = Modifier
                    .size(36.dp) // 略小于48dp以适应正方形布局
                    .background(
                        color = if (hasVoted) {
                            if (voteResult == true) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error
                        } else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${playerIndex + 1}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nickname,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            if (hasVoted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已投票",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "点击投票",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // 投票对话框
    if (showDialog) {
        TaskVoteDialogOptimized(
            playerName = nickname,
            playerRole = role,
            onDismiss = { showDialog = false },
            onVote = { success ->
                onVote(success)
                showDialog = false
            }
        )
    }
}

/**
 * 优化后的任务投票对话框
 */
@Composable
private fun TaskVoteDialogOptimized(
    playerName: String,
    playerRole: AwalongRole,
    onDismiss: () -> Unit,
    onVote: (Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
        )
    ) {
        var hasVoted by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$playerName，请选择任务执行：",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (playerRole.roleType == BAD_PERSON) {
                                onVote(false)
                            } else {
                                //显示Toast 作为好人 必须选择任务成功
                                hasVoted = true
                            }

                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("阻止任务")
                    }
                    
                    Button(
                        onClick = { onVote(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("执行任务")
                    }
                }

                if (playerRole.roleType == GOOD_PERSON && hasVoted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "提示：作为好人阵营，你必须选择任务成功",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}