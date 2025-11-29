package org.walks.gamecopilot.awalong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.MainViewmodel

/**
 * 特殊技能对话框组件
 * 用于处理湖中仙女、刺客等技能的使用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAbilityDialog(
    onDismiss: () -> Unit,
    abilityType: SpecialAbilityType,
    currentPlayerIndex: Int,
    viewmodel: MainViewmodel
) {
    val gameState = viewmodel.awalongGameState.value
    val roleList = gameState.roleList
    val nicknameList = gameState.nickNameList
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
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
                        text = when (abilityType) {
                            SpecialAbilityType.LADY_OF_LAKE -> "湖中仙女技能"
                            SpecialAbilityType.ASSASSINATE -> "刺客刺杀"
                            SpecialAbilityType.PROPHET_CHECK -> "预言者查验"
                            SpecialAbilityType.SIR_GALAHAD -> "圆桌骑士技能"
                            SpecialAbilityType.MORGUSE -> "莫高斯技能"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 只有在非强制技能时才显示关闭按钮
                    if (abilityType != SpecialAbilityType.ASSASSINATE) {
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
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 技能说明
                AbilityDescription(abilityType = abilityType)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 玩家选择区域
                when (abilityType) {
                    SpecialAbilityType.LADY_OF_LAKE -> {
                        LadyOfLakeContent(
                            roleList = roleList,
                            nicknameList = nicknameList,
                            currentPlayerIndex = currentPlayerIndex,
                            onPlayerSelected = { targetIndex ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.LadyOfLakeCheck(targetIndex)
                                )
                                onDismiss()
                            }
                        )
                    }
                    
                    SpecialAbilityType.ASSASSINATE -> {
                        AssassinContent(
                            roleList = roleList,
                            nicknameList = nicknameList,
                            currentPlayerIndex = currentPlayerIndex,
                            onTargetSelected = { targetIndex ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.Assassinate(targetIndex)
                                )
                                onDismiss()
                            }
                        )
                    }
                    
                    SpecialAbilityType.PROPHET_CHECK -> {
                        ProphetContent(
                            roleList = roleList,
                            nicknameList = nicknameList,
                            currentPlayerIndex = currentPlayerIndex,
                            onPlayersSelected = { player1, player2 ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.ProphetCheck(player1, player2)
                                )
                                onDismiss()
                            }
                        )
                    }
                    
                    SpecialAbilityType.SIR_GALAHAD -> {
                        SirGalahadContent(
                            currentPlayerIndex = currentPlayerIndex,
                            onAbilityUsed = {
                                viewmodel.handleAwalongGameIntent(AwalongIntent.SirGalahadUseDoubleVote)
                                onDismiss()
                            }
                        )
                    }
                    
                    SpecialAbilityType.MORGUSE -> {
                        MorguseContent(
                            currentPlayerIndex = currentPlayerIndex,
                            onAbilityUsed = { taskIndex ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.MorguseConvertSuccessToFailure(taskIndex)
                                )
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class SpecialAbilityType {
    LADY_OF_LAKE,    // 湖中仙女
    ASSASSINATE,     // 刺客刺杀
    PROPHET_CHECK,   // 预言者查验
    SIR_GALAHAD,     // 圆桌骑士
    MORGUSE          // 莫高斯
}

@Composable
private fun AbilityDescription(abilityType: SpecialAbilityType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "技能说明",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (abilityType) {
                    SpecialAbilityType.LADY_OF_LAKE -> 
                        "湖中仙女可以查看任意一名玩家的真实阵营（好人/坏人），但无法查看莫德雷德。每个游戏只能使用一次。"
                    
                    SpecialAbilityType.ASSASSINATE -> 
                        "当好人完成3个任务后，刺客可以选择一名玩家进行刺杀。如果刺杀的是梅林，则坏人阵营获胜。"
                    
                    SpecialAbilityType.PROPHET_CHECK -> 
                        "预言者可以在游戏开始时查看任意2名玩家的阵营（好人/坏人），但无法查看莫德雷德的具体身份。"
                    
                    SpecialAbilityType.SIR_GALAHAD -> 
                        "圆桌骑士可以在任意一轮投票中使用双倍投票权，相当于两张投票。整场游戏只能使用一次。"
                    
                    SpecialAbilityType.MORGUSE -> 
                        "莫高斯可以在任意一个任务中将1张成功卡变为失败卡，无法在需要2张失败卡的任务中单独使用。整场游戏只能使用一次。"
                },
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LadyOfLakeContent(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    currentPlayerIndex: Int,
    onPlayerSelected: (Int) -> Unit
) {
    var selectedPlayer by remember { mutableStateOf(-1) }
    
    Text(
        text = "选择要查验的玩家：",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(roleList.indices.toList()) { playerIndex ->
            if (playerIndex != currentPlayerIndex) { // 不能查验自己
                PlayerSelectionCard(
                    playerIndex = playerIndex,
                    nickname = nicknameList[playerIndex],
                    role = roleList[playerIndex],
                    isSelected = selectedPlayer == playerIndex,
                    onClick = { selectedPlayer = playerIndex }
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = { 
            if (selectedPlayer != -1) {
                onPlayerSelected(selectedPlayer)
            }
        },
        enabled = selectedPlayer != -1,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "查验玩家阵营",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AssassinContent(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    currentPlayerIndex: Int,
    onTargetSelected: (Int) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(-1) }
    
    Text(
        text = "选择要刺杀的目标（你认为的梅林）：",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(roleList.indices.toList()) { playerIndex ->
            if (roleList[playerIndex].roleType == GOOD_PERSON) { // 只能刺杀好人
                PlayerSelectionCard(
                    playerIndex = playerIndex,
                    nickname = nicknameList[playerIndex],
                    role = roleList[playerIndex],
                    isSelected = selectedTarget == playerIndex,
                    onClick = { selectedTarget = playerIndex }
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = { 
            if (selectedTarget != -1) {
                onTargetSelected(selectedTarget)
            }
        },
        enabled = selectedTarget != -1,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = "确认刺杀",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProphetContent(
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    currentPlayerIndex: Int,
    onPlayersSelected: (Int, Int) -> Unit
) {
    var selectedPlayers = remember { mutableSetOf<Int>() }
    
    Text(
        text = "选择2名玩家进行查验：",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(roleList.indices.toList()) { playerIndex ->
            if (playerIndex != currentPlayerIndex) { // 不能查验自己
                PlayerSelectionCard(
                    playerIndex = playerIndex,
                    nickname = nicknameList[playerIndex],
                    role = roleList[playerIndex],
                    isSelected = selectedPlayers.contains(playerIndex),
                    onClick = { 
                        if (selectedPlayers.contains(playerIndex)) {
                            selectedPlayers.remove(playerIndex)
                        } else if (selectedPlayers.size < 2) {
                            selectedPlayers.add(playerIndex)
                        }
                    }
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = { 
            if (selectedPlayers.size == 2) {
                val players = selectedPlayers.toList()
                onPlayersSelected(players[0], players[1])
            }
        },
        enabled = selectedPlayers.size == 2,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = "查验2名玩家",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SirGalahadContent(
    currentPlayerIndex: Int,
    onAbilityUsed: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "圆桌骑士技能",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "使用双倍投票权",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAbilityUsed,
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "使用技能",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MorguseContent(
    currentPlayerIndex: Int,
    onAbilityUsed: (Int) -> Unit
) {
    var selectedTask by remember { mutableStateOf(-1) }
    
    Text(
        text = "选择要破坏的任务：",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(5) { taskIndex -> // 假设最多5个任务
            TaskSelectionCard(
                taskIndex = taskIndex + 1,
                isSelected = selectedTask == taskIndex,
                onClick = { selectedTask = taskIndex }
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Button(
        onClick = { 
            if (selectedTask != -1) {
                onAbilityUsed(selectedTask)
            }
        },
        enabled = selectedTask != -1,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = "破坏任务",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlayerSelectionCard(
    playerIndex: Int,
    nickname: String,
    role: AwalongRole,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${playerIndex + 1}号玩家",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskSelectionCard(
    taskIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.error
            )
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "第${taskIndex}个任务",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}