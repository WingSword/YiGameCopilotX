package org.walks.gamecopilot.awalong

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppChoiceRow
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppDialogActions
import org.walks.gamecopilot.ui.components.AppPrimaryAction
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.ui.components.common.GameSetupScreen

/**
 * 自定义阿瓦隆配置界面
 */
@Composable
fun AwalongCustomConfigScreen(
    viewmodel: MainViewmodel,
    navi: NavHostController,
    onBack: () -> Unit,
    onShowRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customConfig by remember { mutableStateOf(DefaultCustomConfig) }
    var showPredefinedConfigDialog by remember { mutableStateOf(false) }

    GameSetupScreen(
        title = "阿瓦隆",
        subtitle = "5-10人 · 阵营推理 · 配置角色后传机发牌",
        onBack = onBack,
        onShowRules = onShowRules,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppSectionHeader(
                        title = "角色配置",
                        subtitle = "从推荐预设开始，或按阵营增减角色"
                    )
                    OutlinedButton(
                        onClick = { showPredefinedConfigDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("选择推荐配置")
                    }
                }
            }

            item {
                AppCard {
                    Column {
                        RoleSelectionSection(
                            title = "蓝方阵营",
                            subtitle = "梅林必选",
                            roleColor = MaterialTheme.colorScheme.tertiary,
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
                            roleColor = MaterialTheme.colorScheme.error,
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
                            roleColor = MaterialTheme.colorScheme.secondary,
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
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        AppSectionHeader(
                            title = "扩展规则",
                            subtitle = "按本局需要启用额外头衔"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "湖中仙女\n头衔，不占角色位；第 2 个任务结束后加入",
                                style = MaterialTheme.typography.bodyMedium,
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
        AppCard {
            Column {
                Text(
                    text = "配置摘要",
                    style = MaterialTheme.typography.titleMedium,
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
                    AppPrimaryAction(
                        text = "开始传机发牌",
                        onClick = {
                            viewmodel.handleAwalongGameIntent(
                                AwalongIntent.StartCustomGame(customConfig)
                            )
                            navi.navigate(NaviRoute.AWALONG_GAME.route)
                        },
                        supportingText = "玩家会按顺序查看身份，全部确认后进入组队阶段。"
                    )
                } else {
                    AppPrimaryAction(
                        text = "配置不完整",
                        onClick = {},
                        enabled = false,
                        supportingText = "需要梅林和莫甘娜；蓝方至少3人、红方至少2人，总人数5-10人。",
                        icon = null
                    )
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppSectionHeader(
            title = title,
            subtitle = subtitle,
            action = {
                Surface(
                    color = roleColor.copy(alpha = 0.14f),
                    contentColor = roleColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${selectedRoles.size} 人",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableRoles.chunked(2).forEach { rowRoles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowRoles.forEach { role ->
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
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(2 - rowRoles.size) {
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
    val isSpecialRole = role == AwalongRole.MEILING || role == AwalongRole.MOGANNA

    Surface(
        modifier = modifier
            .height(136.dp)
            .clickable { showRoleDetailDialog = true },
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            roleColor.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                roleColor.copy(alpha = 0.64f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.62f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = role.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (selectedCount > 0) {
                    Surface(
                        color = roleColor,
                        contentColor = Color.White,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = selectedCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Text(
                text = getRoleDescription(role),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isSpecialRole -> "必选角色"
                        selectedCount == 0 -> "未选择"
                        else -> "已选 $selectedCount"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) roleColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isSpecialRole) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = onRemove,
                            enabled = selectedCount > 0 && canRemove,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "减少${role.title}",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onAdd,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "增加${role.title}",
                                tint = roleColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = roleColor.copy(alpha = 0.14f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "已锁定",
                            style = MaterialTheme.typography.labelSmall,
                            color = roleColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

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

    AppDialog(
        title = role.title,
        subtitle = "阵营：${getRoleCamp(role)}",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(onClick = onDismiss) {
                Text("关闭")
            }
            if (!isSpecialRole) {
                Button(
                    onClick = {
                        if (isSelected) {
                            onRemove()
                        } else {
                            onAdd()
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.error
                        } else {
                            roleColor
                        }
                    )
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Clear else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSelected) "移除" else "添加")
                }
            }
        }
    ) {
        if (selectedCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            roleColor,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
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
                Text(
                    text = "当前配置中已选择该角色",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = getRoleDescription(role),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )

        if (isSpecialRole) {
            Text(
                text = "此角色为必选角色，不能从配置中移除。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    AppDialog(
        title = "选择预定义配置",
        subtitle = "按玩家人数快速套用常用角色组合",
        onDismiss = onDismiss,
        actions = {
            AppDialogActions(
                confirmText = "关闭",
                onConfirm = onDismiss
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PredefinedConfigs.forEach { config ->
                AppChoiceRow(
                    title = "${config.totalPlayers} 人推荐配置",
                    description = "蓝方 ${config.blueCount} 人 · 红方 ${config.redCount} 人 · ${config.getDescription()}",
                    selected = false,
                    onClick = { onConfigSelected(config) }
                )
            }
        }
    }
}
