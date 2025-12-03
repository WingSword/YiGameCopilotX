package org.walks.gamecopilot.awalong


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.awalong.data.AwalongGameState
import org.walks.gamecopilot.awalong.data.SkillUsageRecord

/**
 * 显示所有游戏结果的对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllResultsDialog(
    gameState: AwalongGameState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "游戏结果总览",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 结果内容
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 游戏总体结果
                    item {
                        GameSummarySection(gameState = gameState)
                    }
                    
                    // 每轮任务详细情况
                    item {
                        ResultSection(
                            title = "每轮任务详细情况",
                            icon = Icons.Default.Star
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                gameState.dayList.forEachIndexed { dayIndex, dayEntity ->
                                    // 只显示有人参与投票的任务
                                    if (dayEntity.taskVotes.isNotEmpty()) {
                                        TaskDetailItem(
                                            dayIndex = dayIndex,
                                            dayEntity = dayEntity,
                                            nicknameList = gameState.nickNameList
                                        )
                                    }
                                }
                                // 如果没有有投票记录的任务
                                if (gameState.dayList.all { it.taskVotes.isEmpty() }) {
                                    Text(
                                        text = "暂无任务投票记录",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 玩家任务执行情况
                    item {
                        ResultSection(
                            title = "玩家任务执行情况",
                            icon = Icons.Default.Person
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                gameState.roleList.forEachIndexed { index, role ->
                                    PlayerResultItem(
                                        playerIndex = index,
                                        nickname = gameState.nickNameList.getOrNull(index) ?: "玩家${index + 1}",
                                        role = role,
                                        dayList = gameState.dayList
                                    )
                                }
                            }
                        }
                    }
                    
                    // 技能使用情况
                    item {
                        ResultSection(
                            title = "技能使用记录",
                            icon = Icons.Default.Star
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 显示每轮技能使用记录
                                gameState.dayList.forEachIndexed { dayIndex, dayEntity ->
                                    if (dayEntity.skillUsageRecords.isNotEmpty()) {
                                        Text(
                                            text = "第${dayIndex + 1}天：",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        
                                        dayEntity.skillUsageRecords.forEach { record ->
                                            SkillUsageItem(
                                                record = record,
                                                nicknameList = gameState.nickNameList
                                            )
                                        }
                                    }
                                }

                                // 添加刺客刺杀技能记录
                                val hasAssassin = gameState.roleList.contains(AwalongRole.CISHA)
                                val assassinationResult = gameState.assassinationResult

                                if (hasAssassin && assassinationResult != null) {
                                    Text(
                                        text = "刺客刺杀：",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )

                                    // 查找刺客玩家索引
                                    val assassinIndex =
                                        gameState.roleList.indexOf(AwalongRole.CISHA)
                                    if (assassinIndex >= 0) {
                                        gameState.nickNameList.getOrNull(assassinIndex) ?: "刺客"
                                    } else {
                                        "刺客"
                                    }

                                    SkillUsageItem(
                                        record = SkillUsageRecord(
                                            skillType = "刺客刺杀",
                                            userIndex = assassinIndex,
                                            description = if (assassinationResult) "刺杀成功，坏人获得胜利" else "刺杀失败，好人获得胜利"
                                        ),
                                        nicknameList = gameState.nickNameList
                                    )
                                }

                                // 如果没有技能使用记录
                                val allSkillRecordsEmpty =
                                    gameState.dayList.all { it.skillUsageRecords.isEmpty() }
                                val noAssassinationRecord =
                                    !hasAssassin || assassinationResult == null

                                if (allSkillRecordsEmpty && noAssassinationRecord) {
                                    Text(
                                        text = "暂无技能使用记录",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 锁定玩家情况
                    item {
                        ResultSection(
                            title = "锁定玩家情况",
                            icon = Icons.Default.Lock
                        ) {
                            val allLockedPlayers = gameState.dayList.flatMap { it.lockedPlayers }.toSet()
                            
                            if (allLockedPlayers.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    allLockedPlayers.forEach { playerIndex ->
                                        val nickname = gameState.nickNameList.getOrNull(playerIndex) ?: "玩家${playerIndex + 1}"
                                        Text(
                                            text = "${playerIndex + 1}号 - $nickname",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "暂无锁定玩家",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "关闭",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun GameSummarySection(gameState: AwalongGameState) {
    val successCount = gameState.dayList.count { it.taskResult == 1 }
    val failureCount = gameState.dayList.count { it.taskResult == -1 }
    val totalTasks = gameState.dayList.size

    // 检查是否有刺客刺杀成功的情况
    val hasAssassin = gameState.roleList.contains(AwalongRole.CISHA)
    val assassinationResult = gameState.assassinationResult

    // 判断游戏结果（考虑刺客刺杀情况）
    val gameResult = when {
        // 刺客刺杀成功，红方胜利
        hasAssassin && assassinationResult == true -> "坏人胜利"
        // 刺客刺杀失败，蓝方胜利
        hasAssassin && assassinationResult == false -> "好人胜利"
        // 正常任务胜利条件
        failureCount >= 3 -> "坏人胜利"
        successCount >= 3 -> "好人胜利"
        totalTasks >= 5 && successCount >= 3 -> "好人胜利"
        totalTasks >= 5 && failureCount >= 2 -> "坏人胜利"
        else -> "游戏进行中"
    }
    
    val (resultColor, resultIcon) = when (gameResult) {
        "好人胜利" -> Pair(Color.Green, Icons.Sharp.Check)
        "坏人胜利" -> Pair(Color.Red, Icons.Sharp.Close)
        else -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.Star)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (gameResult) {
                "好人胜利" -> Color.Green.copy(alpha = 0.1f)
                "坏人胜利" -> Color.Red.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 游戏结果标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = resultIcon,
                    contentDescription = null,
                    tint = resultColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = gameResult,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = resultColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 任务统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$successCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                    Text(
                        text = "任务成功",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$failureCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text(
                        text = "任务失败",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalTasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "总轮数",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 胜利条件说明
            Text(
                text = when (gameResult) {
                    "好人胜利" -> "好人完成了3次任务成功，获得胜利！"
                    "坏人胜利" -> "坏人完成了3次任务失败，获得胜利！"
                    else -> "游戏仍在进行中..."
                },
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResultSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun PlayerResultItem(
    playerIndex: Int,
    nickname: String,
    role: AwalongRole,
    dayList: List<org.walks.gamecopilot.awalong.data.AwalongGameDayEntity>
) {
    // 统计玩家参与情况
    val participatedTasks = mutableListOf<Int>()
    val taskResults = mutableListOf<Boolean>()
    
    dayList.forEachIndexed { dayIndex, dayEntity ->
        if (dayEntity.mainTask.containsKey(playerIndex) || dayEntity.taskVotes.containsKey(playerIndex)) {
            participatedTasks.add(dayIndex + 1) // 轮次从1开始
            val result = dayEntity.taskVotes[playerIndex] 
                ?: dayEntity.taskExecutionRecords
                    .find { it.playerIndex == playerIndex && it.day == dayIndex }
                    ?.taskResult
            if (result != null) {
                taskResults.add(result)
            }
        }
    }
    
    val successCount = taskResults.count { it }
    val failureCount = taskResults.count { !it }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 玩家基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${playerIndex + 1}号 - $nickname",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "角色：${role.title}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 阵营标识
                Box(
                    modifier = Modifier
                        .background(
                            color = if (role.roleType==GOOD_PERSON) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (role.roleType==GOOD_PERSON) "好人" else "坏人",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (role.roleType==GOOD_PERSON) Color.Green else Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 参与统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${participatedTasks.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "参与任务",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$successCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                    Text(
                        text = "成功票",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$failureCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text(
                        text = "失败票",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 参与的具体轮次
            if (participatedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "参与轮次：${participatedTasks.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TaskDetailItem(
    dayIndex: Int,
    dayEntity: org.walks.gamecopilot.awalong.data.AwalongGameDayEntity,
    nicknameList: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 任务标题和结果
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第${dayIndex + 1}轮任务",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (dayEntity.taskResult == 1) Icons.Sharp.Check else Icons.Sharp.Close,
                        contentDescription = null,
                        tint = if (dayEntity.taskResult == 1) Color.Green else Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (dayEntity.taskResult == 1) "成功" else "失败",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayEntity.taskResult == 1) Color.Green else Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 队长信息
            if (dayEntity.captain >= 0) {
                Text(
                    text = "队长：${dayEntity.captain + 1}号 - ${nicknameList.getOrNull(dayEntity.captain) ?: "未知"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 参与玩家信息
            val selectedTeam = dayEntity.selectedTeam.ifEmpty { dayEntity.mainTask.keys.toList() }
            if (selectedTeam.isNotEmpty()) {
                Text(
                    text = "参与玩家：${selectedTeam.joinToString(", ") { "${it + 1}号-${nicknameList.getOrNull(it) ?: "未知"}" }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 投票详情
            if (dayEntity.taskVotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "任务投票详情：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                dayEntity.taskVotes.forEach { (playerIndex, voteResult) ->
                    val playerName = "${playerIndex + 1}号-${nicknameList.getOrNull(playerIndex) ?: "未知"}"
                    Text(
                        text = "  • $playerName: ${if (voteResult) "成功" else "失败"}",
                        fontSize = 11.sp,
                        color = if (voteResult) Color.Green else Color.Red
                    )
                }
            }
            
            // 组队投票详情
            if (dayEntity.teamVotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "组队投票详情：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val approveCount = dayEntity.teamVotes.values.count { it }
                val totalCount = dayEntity.teamVotes.size
                
                Text(
                    text = "  结果：$approveCount/$totalCount 票同意",
                    fontSize = 11.sp,
                    color = if (approveCount > totalCount / 2) Color.Green else Color.Red
                )
                
                dayEntity.teamVotes.forEach { (playerIndex, approved) ->
                    val playerName = "${playerIndex + 1}号-${nicknameList.getOrNull(playerIndex) ?: "未知"}"
                    Text(
                        text = "  • $playerName: ${if (approved) "同意" else "反对"}",
                        fontSize = 11.sp,
                        color = if (approved) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillUsageItem(
    record: SkillUsageRecord,
    nicknameList: List<String>
) {
    val userNickname = nicknameList.getOrNull(record.userIndex) ?: "玩家${record.userIndex + 1}"
    val targetNickname = record.targetIndex?.let { 
        nicknameList.getOrNull(it) ?: "玩家${it + 1}" 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "• ${record.skillType}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "使用者：$userNickname",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (targetNickname != null) {
                Text(
                    text = "目标：$targetNickname",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "描述：${record.description}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}