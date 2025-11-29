package org.walks.gamecopilot.awalong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AwalongRole.AOBOLUN
import org.walks.gamecopilot.awalong.AwalongRole.MODELEDE
import org.walks.gamecopilot.awalong.AwalongRole.SHAPESHIFTER
import org.walks.gamecopilot.awalong.data.AwalongGameState

/**
 * 角色能力显示组件
 */
@Composable
fun RoleAbilityDisplay(
    playerIndex: Int,
    playerRole: AwalongRole,
    gameState: AwalongGameState,
    viewmodel: MainViewmodel
) {
    val visibleRoles = AwalongGameLogic.getVisibleRoles(playerRole, gameState.roleList, gameState)
    val availableAbilities = AwalongGameLogic.getAvailableAbilities(playerRole, gameState)
    var showAbilities by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (playerRole.roleType) {
                GOOD_PERSON -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                BAD_PERSON -> Color(0xFFF44336).copy(alpha = 0.1f)
                NEUTRAL_PERSON -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 玩家信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "玩家 ${playerIndex + 1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = playerRole.title,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
                
                // 显示能力按钮
                if (availableAbilities.isNotEmpty()) {
                    Button(
                        onClick = { showAbilities = !showAbilities },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9360FC),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "能力",
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // 可见角色信息
            if (visibleRoles.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "可见角色：",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                visibleRoles.forEach { (index, role) ->
                    Text(
                        text = "玩家 ${index + 1} - ${role.title}",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // 可用能力
            AnimatedVisibility(showAbilities) {
                Column {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "可用能力：",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    availableAbilities.forEach { ability ->
                        AbilityButton(
                            ability = ability,
                            playerRole = playerRole,
                            playerIndex = playerIndex,
                            gameState = gameState,
                            viewmodel = viewmodel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AbilityButton(
    ability: String,
    playerRole: AwalongRole,
    playerIndex: Int,
    gameState: AwalongGameState,
    viewmodel: MainViewmodel
) {
    when (ability) {
        "查看2名玩家阵营" -> {
            // 预言者能力
            var selectedPlayers by remember { mutableStateOf(listOf<Int>()) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = ability,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                
                // 简化的玩家选择按钮
                Button(
                    onClick = {
                        // 简化处理：随机选择2名玩家
                        val availablePlayers = gameState.roleList.indices.filter { it != playerIndex }
                        if (availablePlayers.size >= 2) {
                            val shuffled = availablePlayers.shuffled()
                            viewmodel.handleAwalongGameIntent(
                                AwalongIntent.ProphetCheck(shuffled[0], shuffled[1])
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("使用", fontSize = 8.sp)
                }
            }
        }
        
        "查看1名玩家阵营" -> {
            // 湖中仙女能力
            Button(
                onClick = {
                    val availablePlayers = gameState.roleList.indices.filter { it != playerIndex }
                    if (availablePlayers.isNotEmpty()) {
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.LadyOfLakeCheck(availablePlayers.random())
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .height(24.dp)
            ) {
                Text(ability, fontSize = 10.sp)
            }
        }
        
        "双倍投票权" -> {
            Button(
                onClick = {
                    viewmodel.handleAwalongGameIntent(AwalongIntent.SirGalahadUseDoubleVote)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .height(24.dp)
            ) {
                Text(ability, fontSize = 10.sp)
            }
        }
        
        "转换成功卡为失败卡" -> {
            Button(
                onClick = {
                    // 选择当前任务
                    val currentTaskIndex = gameState.dayList.indexOfLast { it.taskResult == 0 }
                    if (currentTaskIndex >= 0) {
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.MorguseConvertSuccessToFailure(currentTaskIndex)
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .height(24.dp)
            ) {
                Text(ability, fontSize = 10.sp)
            }
        }
        
        "复制角色能力" -> {
            Button(
                onClick = {
                    // 随机选择一个目标角色（除了莫德雷德和奥伯伦）
                    val availableTargets = gameState.roleList.filter { 
                        it != MODELEDE && it != AOBOLUN && it != SHAPESHIFTER 
                    }
                    if (availableTargets.isNotEmpty()) {
                        viewmodel.handleAwalongGameIntent(
                            AwalongIntent.ShapeshifterCopy(availableTargets.random())
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .height(24.dp)
            ) {
                Text(ability, fontSize = 10.sp)
            }
        }
        
        "阵营转换（需抽卡）" -> {
            Button(
                onClick = {
                    viewmodel.handleAwalongGameIntent(AwalongIntent.LancelotConvert)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF795548),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .height(24.dp)
            ) {
                Text(ability, fontSize = 10.sp)
            }
        }
        
        else -> {
            Text(
                text = ability,
                fontSize = 10.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}