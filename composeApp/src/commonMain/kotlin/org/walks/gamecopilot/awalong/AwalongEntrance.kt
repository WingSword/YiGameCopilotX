package org.walks.gamecopilot.awalong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.navigation.NaviRoute
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info


/**
 *  Created by Wing at 16:03 on 2025/5/19
 *  阿瓦隆游戏入口
 */

@Composable
fun AwalongEntrance(viewmodel: MainViewmodel,navi: NavHostController) {
    var currentSelect by remember { mutableStateOf(0) }
    var showRulesDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 顶部标题和规则按钮区域
        TopHeaderSection(
            onShowRules = { showRulesDialog = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 游戏配置列表
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(AwalongConfig.entries) {
                val description = if (it.description.isNotEmpty()) {
                    it.description
                } else {
                    "可以进行${it.playerNum}人游戏"
                }
                AwalongGame(
                    config = it,
                    description = description,
                    isSelected = currentSelect == it.ordinal,
                    onClick = {
                        currentSelect = it.ordinal
                    }, 
                    goToPage = {
                        viewmodel.handleAwalongGameIntent(AwalongIntent.StartGame(it))
                        navi.navigate(NaviRoute.AWALONG.route) 
                    }
                )
            }
        }
    }
    
    // 规则弹窗
    if (showRulesDialog) {
        GameRulesDialog(
            onDismiss = { showRulesDialog = false }
        )
    }
}

@Composable
fun TopHeaderSection(onShowRules: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "阿瓦隆",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "选择游戏配置开始游戏",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        OutlinedButton(
            onClick = onShowRules,
            modifier = Modifier.padding(start = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.icon_info),
                contentDescription = "游戏规则",
                tint =Color.Unspecified ,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "游戏规则",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRulesDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                        text = "阿瓦隆游戏规则",
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
                
                // 规则内容
                SelectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RuleSection(title = "游戏目标", content = """
                            蓝方(好人)：完成3次任务成功且梅林未被刺杀
                            红方(坏人)：完成2次任务失败或成功刺杀梅林
                        """.trimIndent())
                        
                        RuleSection(title = "角色特性", content = """
                            • 梅林：能看到除莫德雷德外的所有坏人
                            • 派西维尔：能看到梅林和莫甘娜，但无法区分
                            • 忠臣：无特殊能力，通过推理帮助团队
                            • 刺客：任务完成后可刺杀梅林
                            • 莫甘娜：可冒充梅林身份
                            • 莫德雷德：梅林无法看到的隐藏坏人
                        """.trimIndent())
                        
                        RuleSection(title = "游戏流程", content = """
                            1. 队长组队：每轮由队长选择执行任务的人员
                            2. 组队投票：全员投票是否通过组队，超过半数同意则执行
                            3. 任务执行：队伍成员秘密投票决定任务成功或失败
                            4. 轮换队长：组队失败则轮换下一位队长
                            5. 刺杀阶段：好人完成3个任务后，刺客可刺杀梅林
                        """.trimIndent())
                        
                        RuleSection(title = "任务失败条件", content = """
                            • 5-6人场：1张失败卡即任务失败
                            • 7-9人场：部分任务需要2张失败卡才失败
                            • 10人场：第4轮需要2张失败卡才失败
                        """.trimIndent())
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
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
                        text = "知道了",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RuleSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = content,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AwalongGame(
    config: AwalongConfig,
    description: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    goToPage: () -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 150),
        label = "elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 游戏图标
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isSelected) {
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 游戏信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = config.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 玩家数量标签
                        PlayerCountBadge(
                            count = config.playerNum,
                            isSelected = isSelected
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
                
                // 操作按钮区域
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 展开/收起按钮
                    IconButton(
                        onClick = { isExpanded.value = !isExpanded.value },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded.value) "收起" else "展开",
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // 进入游戏按钮
                    AnimatedVisibility(
                        visible = isSelected,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Button(
                            onClick = goToPage,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "开始游戏",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
            
            // 角色配置展开区域
            AnimatedVisibility(
                visible = isExpanded.value,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                RoleConfigurationDisplay(config)
            }
        }
    }
}

@Composable
fun PlayerCountBadge(count: Int, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                }
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${count}人",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}