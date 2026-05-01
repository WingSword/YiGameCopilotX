package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.awalong.AwalongConfig

/**
 * 游戏规则弹窗组件
 * 显示阿瓦隆游戏的详细规则说明
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRulesDialog(
    onDismiss: () -> Unit,
    gameConfig: AwalongConfig
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 标题栏
                RulesHeader(onDismiss = onDismiss)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 规则内容
                RulesContent(gameConfig = gameConfig)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 关闭按钮
                RulesCloseButton(onDismiss = onDismiss)
            }
        }
    }
}

/**
 * 规则弹窗标题栏
 */
@Composable
private fun RulesHeader(onDismiss: () -> Unit) {
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
}

/**
 * 规则内容区域
 */
@Composable
private fun RulesContent(gameConfig: AwalongConfig) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RuleSection(
                title = "游戏目标",
                icon = Icons.Default.Star,
                content = """
                    蓝方(好人)：完成3次任务成功且梅林未被刺杀
                    红方(坏人)：完成2次任务失败或成功刺杀梅林
                """.trimIndent()
            )
        }
        
        item {
            RuleSection(
                title = "角色特性",
                icon = Icons.Default.Person,
                content = """
                    • 梅林：能看到除莫德雷德外的所有坏人
                    • 派西维尔：能看到梅林和莫甘娜，但无法区分
                    • 忠臣：无特殊能力，通过推理帮助团队
                    • 刺客：任务完成后可刺杀梅林
                    • 莫甘娜：可冒充梅林身份
                    • 莫德雷德：梅林无法看到的隐藏坏人
                """.trimIndent()
            )
        }
        
        item {
            RuleSection(
                title = "游戏流程",
                icon = Icons.Default.LocationOn,
                content = """
                    1. 队长组队：每轮由队长选择执行任务的人员
                    2. 组队投票：全员投票是否通过组队，超过半数同意则执行
                    3. 任务执行：队伍成员秘密投票决定任务成功或失败
                    4. 轮换队长：组队失败则轮换下一位队长
                    5. 刺杀阶段：好人完成3个任务后，刺客可刺杀梅林
                """.trimIndent()
            )
        }
        
        item {
            RuleSection(
                title = "任务失败条件",
                icon = Icons.Default.Lock,
                content = """
                    • 5-6人场：每轮任务1张失败卡即失败
                    • 7-9人场：第4轮任务需要2张失败卡才失败
                    • 10人场：第4轮任务需要2张失败卡才失败
                """.trimIndent()
            )
        }
        
        item {
            RuleSection(
                title = "当前配置",
                icon = Icons.Default.Star,
                content = """
                    玩家数量：${gameConfig.playerNum}人
                    任务配置：${gameConfig.process.joinToString("-")}人
                    角色分配：${gameConfig.role.joinToString(", ") { it.title }}
                """.trimIndent()
            )
        }
    }
}

/**
 * 规则弹窗关闭按钮
 */
@Composable
private fun RulesCloseButton(onDismiss: () -> Unit) {
    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
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

/**
 * 单个规则条目组件
 */
@Composable
private fun RuleSection(
    title: String,
    icon: ImageVector,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
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
            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}