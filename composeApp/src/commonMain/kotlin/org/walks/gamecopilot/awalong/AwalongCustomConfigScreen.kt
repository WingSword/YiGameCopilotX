package org.walks.gamecopilot.awalong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.navigation.NaviRoute

/**
 * 自定义阿瓦隆配置界面
 */
@Composable
fun AwalongCustomConfigScreen(viewmodel: MainViewmodel, navi: NavHostController) {
    var customConfig by remember { mutableStateOf(DefaultCustomConfig) }
    var showPredefinedConfigDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        item {
            // 预定义配置按钮
            OutlinedButton(
                onClick = { showPredefinedConfigDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "预定义配置",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择预定义配置",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            // 角色选择区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    Text(
                        text = "角色选择",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 蓝方阵营选择
                    RoleSelectionSection(
                        title = "蓝方阵营",
                        subtitle = "梅林必选",
                        roleColor = Color(0xFF2196F3),
                        selectedRoles = customConfig.blueRoles,
                        availableRoles = getAvailableBlueRoles(),
                        onRoleAdded = { role ->
                            val newBlueRoles = customConfig.blueRoles.toMutableList()
                            newBlueRoles.add(role)
                            customConfig = customConfig.copy(
                                blueRoles = newBlueRoles,
                                blueCount = newBlueRoles.size
                            )
                        },
                        onRoleRemoved = { role ->
                            if (role != AwalongRole.MEILING) { // 梅林不能移除
                                val newBlueRoles = customConfig.blueRoles.toMutableList()
                                newBlueRoles.remove(role)
                                customConfig = customConfig.copy(
                                    blueRoles = newBlueRoles,
                                    blueCount = newBlueRoles.size
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 红方阵营选择
                    RoleSelectionSection(
                        title = "红方阵营",
                        subtitle = "莫甘娜必选",
                        roleColor = Color(0xFFF44336),
                        selectedRoles = customConfig.redRoles,
                        availableRoles = getAvailableRedRoles(),
                        onRoleAdded = { role ->
                            val newRedRoles = customConfig.redRoles.toMutableList()
                            newRedRoles.add(role)
                            customConfig = customConfig.copy(
                                redRoles = newRedRoles,
                                redCount = newRedRoles.size
                            )
                        },
                        onRoleRemoved = { role ->
                            if (role != AwalongRole.MOGANNA) { // 莫甘娜不能移除
                                val newRedRoles = customConfig.redRoles.toMutableList()
                                newRedRoles.remove(role)
                                customConfig = customConfig.copy(
                                    redRoles = newRedRoles,
                                    redCount = newRedRoles.size
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 中立方阵营选择
                    RoleSelectionSection(
                        title = "中立方阵营",
                        subtitle = "可选角色",
                        roleColor = Color(0xFF9C27B0),
                        selectedRoles = customConfig.neutralRoles,
                        availableRoles = getAvailableNeutralRoles(),
                        onRoleAdded = { role ->
                            val newNeutralRoles = customConfig.neutralRoles.toMutableList()
                            newNeutralRoles.add(role)
                            customConfig = customConfig.copy(
                                neutralRoles = newNeutralRoles,
                                neutralCount = newNeutralRoles.size
                            )
                        },
                        onRoleRemoved = { role ->
                            val newNeutralRoles = customConfig.neutralRoles.toMutableList()
                            newNeutralRoles.remove(role)
                            customConfig = customConfig.copy(
                                neutralRoles = newNeutralRoles,
                                neutralCount = newNeutralRoles.size
                            )
                        }
                    )
                }
            }
        }

        item {
            // 配置摘要和开始按钮
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 配置摘要
                    Text(
                        text = "配置摘要",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = customConfig.getDescription(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 验证信息和开始按钮
                    if (customConfig.isValid()) {
                        Button(
                            onClick = {
                                viewmodel.handleAwalongGameIntent(
                                    AwalongIntent.StartCustomGame(
                                        customConfig
                                    )
                                )
                                navi.navigate(NaviRoute.AWALONG.route)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "开始游戏",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "开始游戏",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "配置无效：请确保包含梅林和莫甘娜，蓝方至少3人，红方至少2人，总人数5-10人",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            enabled = false
                        ) {
                            Text(
                                text = "配置不完整",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // 预定义配置弹窗
    if (showPredefinedConfigDialog) {
        PredefinedConfigDialog(
            onDismiss = { showPredefinedConfigDialog = false },
            onConfigSelected = { config ->
                customConfig = config
                showPredefinedConfigDialog = false
            }
        )
    }
}

/**
 * 角色选择区域
 */
@Composable
private fun RoleSelectionSection(
    title: String,
    subtitle: String,
    roleColor: Color,
    selectedRoles: List<AwalongRole>,
    availableRoles: List<AwalongRole>,
    onRoleAdded: (AwalongRole) -> Unit,
    onRoleRemoved: (AwalongRole) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${selectedRoles.size}人",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = roleColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 角色选择网格
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableRoles) { role ->
                RoleSelectionCard(
                    role = role,
                    roleColor = roleColor,
                    selectedCount = selectedRoles.count { it == role },
                    isSelected = selectedRoles.contains(role),
                    onAdd = { onRoleAdded(role) },
                    onRemove = { onRoleRemoved(role) },
                    canRemove = when (title) {
                        "蓝方阵营" -> role != AwalongRole.MEILING
                        "红方阵营" -> role != AwalongRole.MOGANNA
                        else -> true
                    }
                )
            }
        }
    }
}

/**
 * 角色选择卡片
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoleSelectionCard(
    role: AwalongRole,
    roleColor: Color,
    selectedCount: Int,
    isSelected: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    var showRoleDetailDialog by remember { mutableStateOf(false) }

    // 判断是否为梅林或莫甘娜
    val isSpecialRole = role == AwalongRole.MEILING || role == AwalongRole.MOGANNA

    Card(
        modifier = Modifier
            .width(88.dp)
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                roleColor.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, roleColor.copy(alpha = 0.5f))
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {
                        if (!isSpecialRole)
                            onAdd()
                    },
                    onLongClick = {
                        showRoleDetailDialog = true
                    }
                )

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 角色名称
                Row {
                    Text(
                        text = role.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2
                    )
                    // 选择计数 - 仅当计数大于1时显示
                    if (selectedCount > 1) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    roleColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$selectedCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 操作按钮区域（带动画）
                AnimatedVisibility(
                    visible = !isSpecialRole,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 移除按钮 - 只要已选择就可以移除
                        AnimatedVisibility(
                            visible = selectedCount > 0,
                        ) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.size(18.dp)
                                    .border(2.dp, Color.White, shape = CircleShape),
                                ) {
                                Icon(
                                    imageVector = Icons.Sharp.Clear,
                                    contentDescription = "移除",
                                    tint = roleColor
                                )
                            }
                        }

                        // 添加按钮 - 始终显示，可以继续添加更多
                        AnimatedVisibility(
                            visible = false,
                        ) {
                            IconButton(
                                onClick = onAdd,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加",
                                    tint = roleColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 角色详情弹窗
    if (showRoleDetailDialog) {
        RoleDetailDialog(
            role = role,
            roleColor = roleColor,
            selectedCount = selectedCount,
            isSelected = isSelected,
            onAdd = onAdd,
            onRemove = onRemove,
            canRemove = canRemove,
            onDismiss = { showRoleDetailDialog = false }
        )
    }
}


/**
 * 获取可用的蓝方角色
 */
private fun getAvailableBlueRoles(): List<AwalongRole> {
    return listOf(
        AwalongRole.MEILING,
        AwalongRole.PAIXIWEIWEIER,
        AwalongRole.ZHONGCHEN,
        AwalongRole.PROPHET,
        AwalongRole.LADY_OF_LAKE,
        AwalongRole.SIR_GALAHAD
    )
}

/**
 * 获取可用的红方角色
 */
private fun getAvailableRedRoles(): List<AwalongRole> {
    return listOf(
        AwalongRole.MOGANNA,
        AwalongRole.MODELEDE,
        AwalongRole.CISHA,
        AwalongRole.MORGUSE,
        AwalongRole.SHAPESHIFTER
    )
}

/**
 * 获取可用的中立方角色
 */
private fun getAvailableNeutralRoles(): List<AwalongRole> {
    return listOf(
        AwalongRole.AOBOLUN,
        AwalongRole.LANCELOT
    )
}

/**
 * 角色详情弹窗
 */
@Composable
private fun RoleDetailDialog(
    role: AwalongRole,
    roleColor: Color,
    selectedCount: Int,
    isSelected: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean,
    onDismiss: () -> Unit
) {
    // 判断是否为梅林或莫甘娜
    val isSpecialRole = role == AwalongRole.MEILING || role == AwalongRole.MOGANNA

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
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
                        text = role.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )

                    // 选择计数
                    if (selectedCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    roleColor,
                                    shape = RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$selectedCount",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 角色阵营
                Text(
                    text = "阵营：${getRoleCamp(role)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 角色描述
                Text(
                    text = getRoleDescription(role),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 操作按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 关闭按钮
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "关闭",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 添加和移除按钮（梅林/莫甘娜不显示）
                    if (!isSpecialRole) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSelected,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                        ) {
                            Button(
                                onClick = {
                                    onRemove()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "移除",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "移除",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isSelected,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                        ) {
                            Button(
                                onClick = {
                                    onAdd()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = roleColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "添加",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // 梅林/莫甘娜显示特殊提示
                        Text(
                            text = "此角色为必选角色",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取角色阵营
 */
private fun getRoleCamp(role: AwalongRole): String {
    return when (role) {
        AwalongRole.MEILING, AwalongRole.PAIXIWEIWEIER, AwalongRole.ZHONGCHEN,
        AwalongRole.PROPHET, AwalongRole.LADY_OF_LAKE, AwalongRole.SIR_GALAHAD -> "蓝方"

        AwalongRole.MOGANNA, AwalongRole.MODELEDE, AwalongRole.CISHA,
        AwalongRole.MORGUSE, AwalongRole.SHAPESHIFTER -> "红方"

        AwalongRole.AOBOLUN, AwalongRole.LANCELOT -> "中立方"
        AwalongRole.EMPTY_ROLE -> "未知"
    }
}

/**
 * 获取角色描述
 */
private fun getRoleDescription(role: AwalongRole): String {
    return when (role) {
        AwalongRole.MEILING -> "能看到除了莫德雷德之外的所有红方玩家。但必须隐藏自己的身份，否则会被刺杀。"
        AwalongRole.MOGANNA -> "能看到所有红方玩家，并伪装成梅林迷惑蓝方。"
        AwalongRole.PAIXIWEIWEIER -> "能知道梅林的身份，但需要保护梅林不被发现。"
        AwalongRole.ZHONGCHEN -> "普通的蓝方玩家，需要通过投票和任务来帮助蓝方获胜。"
        AwalongRole.PROPHET -> "在游戏开始时可以查看一名玩家的身份。"
        AwalongRole.LADY_OF_LAKE -> "在特定回合可以查看一名玩家的身份。"
        AwalongRole.SIR_GALAHAD -> "蓝方的特殊角色，有特殊能力。"
        AwalongRole.MODELEDE -> "红方领袖，梅林无法看到他的身份。"
        AwalongRole.CISHA -> "负责在游戏结束时刺杀梅林。"
        AwalongRole.MORGUSE -> "红方的特殊角色。"
        AwalongRole.SHAPESHIFTER -> "可以伪装成其他角色。"
        AwalongRole.AOBOLUN -> "中立方角色，有自己的胜利条件。"
        AwalongRole.LANCELOT -> "中立方角色，可以在游戏中改变阵营。"
        else -> ""
    }
}

/**
 * 预定义配置弹窗
 */
@Composable
private fun PredefinedConfigDialog(
    onDismiss: () -> Unit,
    onConfigSelected: (AwalongCustomConfig) -> Unit
) {
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
                        text = "选择预定义配置",
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

                // 配置列表
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PredefinedConfigs.forEach { config ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = { onConfigSelected(config) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${config.totalPlayers}人配置",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // 阵营标识
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "蓝方${config.blueCount}人",
                                            fontSize = 12.sp,
                                            color = Color(0xFF2196F3),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = " | ",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "红方${config.redCount}人",
                                            fontSize = 12.sp,
                                            color = Color(0xFFF44336),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = config.getDescription(),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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