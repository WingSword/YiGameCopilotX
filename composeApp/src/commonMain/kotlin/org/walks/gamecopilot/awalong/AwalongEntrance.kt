package org.walks.gamecopilot.awalong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info


/**
 *  Created by Wing at 16:03 on 2025/5/19
 *  阿瓦隆游戏入口
 */

@Composable
fun AwalongEntrance(viewmodel: MainViewmodel,navi: NavHostController) {
    var showRulesDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 顶部标题和规则按钮区域
        TopHeaderSection(
            onShowRules = { showRulesDialog = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // 自定义配置界面
        AwalongCustomConfigScreen(
            viewmodel = viewmodel,
            navi = navi
        )
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
