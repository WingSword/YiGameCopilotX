package org.walks.gamecopilot.awalong

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 角色配置详情显示组件
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoleConfigurationDisplay(config: AwalongConfig) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RectangleShape
            )
            .padding(16.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "角色配置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "共${config.role.size}人",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(RectangleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 按阵营分组显示角色
        val goodRoles = config.role.filter { it.roleType == GOOD_PERSON }
        val badRoles = config.role.filter { it.roleType == BAD_PERSON }
        val neutralRoles = config.role.filter { it.roleType == NEUTRAL_PERSON }
        
        // 阵营分组
        if (goodRoles.isNotEmpty()) {
            RoleGroupDisplay("蓝方阵营", goodRoles, Color(0xFF2196F3))
        }
        
        if (badRoles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            RoleGroupDisplay("红方阵营", badRoles, Color(0xFFF44336))
        }
        
        if (neutralRoles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            RoleGroupDisplay("中立阵营", neutralRoles, Color(0xFFFF9800))
        }
        
        // 任务配置信息
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "任务流程",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    config.process.forEachIndexed { index, count ->
                        TaskRoundDisplay(
                            round = index + 1,
                            playerCount = count,
                            isSpecial = (config.playerNum >= 7 && index == 3) || 
                                       (config.playerNum >= 10 && index == 3)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "任务失败条件：${getFailureCondition(config.playerNum, config.process)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleGroupDisplay(title: String, roles: List<AwalongRole>, color: Color) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RectangleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$title (${roles.size}人)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            roles.forEach { role ->
                RoleChip(role = role, color = color)
            }
        }
    }
}

@Composable
private fun RoleChip(role: AwalongRole, color: Color) {
    Box(
        modifier = Modifier
            .clip(RectangleShape)
            .background(color.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RectangleShape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = role.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun TaskRoundDisplay(round: Int, playerCount: Int, isSpecial: Boolean) {
    Box(
        modifier = Modifier
            .clip(RectangleShape)
            .background(
                if (isSpecial) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                }
            )
            .border(
                width = if (isSpecial) 1.dp else 0.5.dp,
                color = if (isSpecial) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RectangleShape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "第${round}轮",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${playerCount}人",
                fontSize = 12.sp,
                color = if (isSpecial) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                fontWeight = FontWeight.Bold
            )
            if (isSpecial) {
                Text(
                    text = "特殊",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getFailureCondition(playerNum: Int, process: List<Int>): String {
    return when {
        playerNum <= 6 -> "每轮任务1张失败卡即失败"
        playerNum <= 9 -> "第4轮任务需要2张失败卡才失败，其他轮次1张失败卡即失败"
        playerNum == 10 -> "第4轮任务需要2张失败卡才失败，其他轮次1张失败卡即失败"
        else -> "默认1张失败卡即失败"
    }
}