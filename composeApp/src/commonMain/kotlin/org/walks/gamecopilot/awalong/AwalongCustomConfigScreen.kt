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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
fun AwalongCustomConfigScreen(
    viewmodel: MainViewmodel,
    navi: NavHostController,
    modifier: Modifier = Modifier
) {
    var customConfig by remember { mutableStateOf(DefaultCustomConfig) }
    var showPredefinedConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "角色配置",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.width(32.dp))
                    OutlinedButton(
                        onClick = { showPredefinedConfigDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RectangleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "预定义配置",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "选择预定义配置",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                                if (role != AwalongRole.MEILING) {
                                    val newBlueRoles = customConfig.blueRoles.toMutableList()
                                    newBlueRoles.remove(role)
                                    customConfig = customConfig.copy(
                                        blueRoles = newBlueRoles,
                                        blueCount = newBlueRoles.size
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

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
                                if (role != AwalongRole.MOGANNA) {
                                    val newRedRoles = customConfig.redRoles.toMutableList()
                                    newRedRoles.remove(role)
                                    customConfig = customConfig.copy(
                                        redRoles = newRedRoles,
                                        redCount = newRedRoles.size
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

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

                        Spacer(modifier = Modifier.height(18.dp))
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "扩展包",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "湖中仙女（头衔，不占角色位；第2任务结束后加入）",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = customConfig.useLadyOfLake,
                                onCheckedChange = {
                                    customConfig = customConfig.copy(useLadyOfLake = it)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (customConfig.isValid()) {
                    Button(
                        onClick = {
                            viewmodel.handleAwalongGameIntent(
                                AwalongIntent.StartCustomGame(customConfig)
                            )
                            navi.navigate(NaviRoute.AWALONG_GAME.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RectangleShape,
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
                        shape = RectangleShape,
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor
                )
                Spacer(modifier = Modifier.width(8.dp))
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

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableRoles.chunked(4).forEach { rowRoles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowRoles.forEach { role ->
                        Box(modifier = Modifier.weight(1f)) {
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
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(3f / 4f)
                            )
                        }
                    }
                    repeat(4 - rowRoles.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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
    canRemove: Boolean,
    modifier: Modifier = Modifier
) {
    var showRoleDetailDialog by remember { mutableStateOf(false) }

    // 判断是否为梅林或莫甘娜
    val isSpecialRole = role == AwalongRole.MEILING || role == AwalongRole.MOGANNA

    Card(
        modifier = modifier,
        shape = RectangleShape,
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
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        text = role.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2
                    )
                    if (selectedCount > 1) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    roleColor,
                                    shape = RectangleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$selectedCount",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(
                    visible = !isSpecialRole,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = selectedCount > 0 && canRemove,
                        ) {
                            IconButton(
                                onClick = onRemove,
                                modifier = Modifier.size(16.dp)
                                    .border(2.dp, Color.White, shape = RectangleShape),
                            ) {
                                Icon(
                                    imageVector = Icons.Sharp.Clear,
                                    contentDescription = "移除",
                                    tint = roleColor,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = false,
                        ) {
                            IconButton(
                                onClick = onAdd,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "添加",
                                    tint = roleColor,
                                    modifier = Modifier.size(10.dp)
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
        AwalongRole.ZHONGCHEN
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
        AwalongRole.ZHAOYA,
        AwalongRole.MORGUSE,
        AwalongRole.SHAPESHIFTER,
        AwalongRole.AOBOLUN
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
            shape = RectangleShape,
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
                                    shape = RectangleShape
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
                        shape = RectangleShape,
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
                                shape = RectangleShape,
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
                                shape = RectangleShape,
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
        AwalongRole.LADY_OF_LAKE -> "蓝方"

        AwalongRole.MOGANNA, AwalongRole.MODELEDE, AwalongRole.CISHA,
        AwalongRole.MORGUSE, AwalongRole.SHAPESHIFTER, AwalongRole.ZHAOYA,
        AwalongRole.AOBOLUN -> "红方"

        AwalongRole.LANCELOT -> "中立方"
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
        AwalongRole.LADY_OF_LAKE -> "头衔（不占角色位）：第2任务完成后加入，持有者每轮结束可查验一人并传给被查验者，曾持有者不可再被查验。"
        AwalongRole.MODELEDE -> "红方领袖，梅林无法看到他的身份。"
        AwalongRole.CISHA -> "好人完成3次任务后可选择刺杀一名玩家，若选中梅林则坏人获胜。"
        AwalongRole.MORGUSE -> "在任意一个任务中可将1张成功卡变为失败卡，整场仅一次，且无法在需要2张失败卡的任务中单独使用。"
        AwalongRole.SHAPESHIFTER -> "游戏开始时复制一名随机玩家的角色（能力相同、阵营不变），无法复制莫德雷德，复制后不可改变。"
        AwalongRole.ZHAOYA -> "无特殊视野的红方角色，夜晚与红方队友互认。"
        AwalongRole.AOBOLUN -> "红方角色，看不到其他红方成员，其他红方成员也看不到他，可以正常参与任务破坏。"
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
            shape = RectangleShape,
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
                            shape = RectangleShape,
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
                    shape = RectangleShape,
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