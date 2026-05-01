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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.MainViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialAbilityDialog(
    onDismiss: () -> Unit,
    abilityType: SpecialAbilityType,
    currentPlayerIndex: Int,
    viewmodel: MainViewmodel,
    taskIndex: Int? = null
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
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (abilityType) {
                            SpecialAbilityType.LADY_OF_LAKE -> "湖中仙女技能"
                            SpecialAbilityType.ASSASSINATE -> "刺客刺杀"
                            SpecialAbilityType.MORGUSE -> "莫高斯技能"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
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
                
                AbilityDescription(abilityType = abilityType)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                when (abilityType) {
                    SpecialAbilityType.LADY_OF_LAKE -> {
                        LadyOfLakeContent(
                            gameState = gameState,
                            roleList = roleList,
                            nicknameList = nicknameList,
                            taskIndex = taskIndex ?: 0,
                            onPlayerSelected = { targetIndex ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.LadyOfLakeCheck(targetIndex, taskIndex ?: 0)
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
                    
                    SpecialAbilityType.MORGUSE -> {
                        MorguseContent(
                            currentPlayerIndex = currentPlayerIndex,
                            onAbilityUsed = { taskIdx ->
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.MorguseConvertSuccessToFailure(taskIdx)
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
    LADY_OF_LAKE,
    ASSASSINATE,
    MORGUSE
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
                        "湖中仙女为头衔：持有者选择一名玩家秘密查看其阵营（亚瑟的仆人/莫德雷德的手下），无法查看莫德雷德。使用后头衔传给被查验的玩家；曾持有过头衔的玩家不可再被查验。"
                    
                    SpecialAbilityType.ASSASSINATE -> 
                        "当好人完成3个任务后，刺客可以选择一名玩家进行刺杀。如果刺杀的是梅林，则坏人阵营获胜。"
                    
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
    gameState: org.walks.gamecopilot.awalong.data.AwalongGameState,
    roleList: List<AwalongRole>,
    nicknameList: List<String>,
    taskIndex: Int,
    onPlayerSelected: (Int) -> Unit
) {
    var selectedPlayer by remember { mutableStateOf(-1) }
    val holder = gameState.ladyOfLakeHolder
    val history = gameState.ladyOfLakeHoldersHistory
    val checkableIndices = roleList.indices.filter { idx ->
        idx != holder && idx !in history
    }
    
    Text(
        text = "选择要查验的玩家（不可选曾持有湖中仙女的玩家）：",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(checkableIndices) { playerIndex ->
            PlayerSelectionCard(
                playerIndex = playerIndex,
                nickname = nicknameList.getOrElse(playerIndex) { "" },
                role = roleList.getOrNull(playerIndex) ?: AwalongRole.ZHONGCHEN,
                isSelected = selectedPlayer == playerIndex,
                onClick = { selectedPlayer = playerIndex }
            )
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
        shape = RectangleShape,
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
            if (roleList[playerIndex].roleType == GOOD_PERSON) {
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
        shape = RectangleShape,
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
        items(5) { taskIdx ->
            TaskSelectionCard(
                taskIndex = taskIdx + 1,
                isSelected = selectedTask == taskIdx,
                onClick = { selectedTask = taskIdx }
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
        shape = RectangleShape,
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
        shape = RectangleShape,
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
        shape = RectangleShape,
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
