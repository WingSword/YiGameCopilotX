package org.walks.gamecopilot.werewolf.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BackHand
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Cottage
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.ui.components.common.IdentityCardColors
import org.walks.gamecopilot.ui.components.common.rememberIdentityCardColors
import org.walks.gamecopilot.ui.components.common.SwipeableIdentityCardShell
import org.walks.gamecopilot.werewolf.data.WerewolfFaction
import org.walks.gamecopilot.werewolf.data.WerewolfRole

/**
 * 狼人杀专用身份卡片
 *
 * 基于 SwipeableIdentityCardShell 公共组件，遵循项目统一的身份卡规范：
 * - 左右滑动切换 隐藏/揭示 状态
 * - 统一使用 IdentityCardColors 配色
 * - 入场动画（缩放、偏移、旋转）
 * - 底部进度条 + 侧边线
 *
 * @param resetKey 重置标识（切换玩家时改变以重置动画状态）
 * @param playerNumber 玩家编号（从1开始）
 * @param nickname 玩家昵称
 * @param role 身份角色
 * @param showDescription 是否显示角色描述（发牌阶段显示，讨论阶段可省略）
 * @param onClose 关闭回调（滑动至少一次后显示关闭按钮）
 */
@Composable
fun WerewolfIdentityCard(
    resetKey: Any,
    playerNumber: Int,
    nickname: String,
    role: WerewolfRole,
    showDescription: Boolean = true,
    onClose: () -> Unit = {}
) {
    val roleColor = getRoleColor(role)
    val colors = rememberIdentityCardColors()

    SwipeableIdentityCardShell(
        resetKey = resetKey,
        cardWidth = 300.dp,
        cardHeight = 470.dp,
        onClose = onClose,
        showProgressBar = true,
        showSideBorders = true,
        hiddenContent = {
            // 未揭示面：玩家编号 + 昵称 + "身份已隐藏"
            HiddenFaceContent(
                playerNumber = playerNumber,
                nickname = nickname,
                colors = colors
            )
        },
        visibleContent = {
            // 已揭示面：角色图标 + 名称 + 阵营 + 描述
            RevealedFaceContent(
                role = role,
                roleColor = roleColor,
                playerNumber = playerNumber,
                showDescription = showDescription
            )
        }
    )
}

/**
 * 未揭示面内容
 */
@Composable
private fun HiddenFaceContent(
    playerNumber: Int,
    nickname: String,
    colors: IdentityCardColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PLAYER $playerNumber",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            letterSpacing = 0.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (nickname.isNotEmpty()) nickname else "玩家$playerNumber",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = Icons.Rounded.Visibility,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "身份已隐藏",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp
        )
    }
}

/**
 * 已揭示面内容
 */
@Composable
private fun RevealedFaceContent(
    role: WerewolfRole,
    roleColor: Color,
    playerNumber: Int,
    showDescription: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 角色图标
        Icon(
            imageVector = getRoleIcon(role),
            contentDescription = role.displayName,
            modifier = Modifier.size(48.dp),
            tint = roleColor
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 角色名称
        Text(
            text = role.displayName,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontSize = 36.sp,
            color = roleColor
        )

        // 阵营标签
        Surface(
            color = roleColor.copy(alpha = 0.15f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
        ) {
            Text(
                text = role.faction.displayName,
                fontSize = 13.sp,
                color = roleColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 金色分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 角色描述
        if (showDescription) {
            Text(
                text = role.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 底部提示
        Text(
            text = "左右滑动可隐藏身份",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// ===== 工具函数 =====

fun getRoleColor(role: WerewolfRole): Color {
    return when (role.faction) {
        WerewolfFaction.WEREWOLF -> Color(0xFFE74C3C)
        WerewolfFaction.VILLAGER -> Color(0xFF3498DB)
        WerewolfFaction.INDEPENDENT -> Color(0xFFE67E22)
    }
}

fun getRoleIcon(role: WerewolfRole): ImageVector {
    return when (role) {
        WerewolfRole.WEREWOLF -> Icons.Rounded.Pets
        WerewolfRole.MINION -> Icons.Rounded.BackHand
        WerewolfRole.DOPPELGANGER -> Icons.Rounded.ContentCopy
        WerewolfRole.SEER -> Icons.Rounded.Visibility
        WerewolfRole.ROBBER -> Icons.Rounded.Lock
        WerewolfRole.TROUBLEMAKER -> Icons.Rounded.SwapHoriz
        WerewolfRole.DRUNK -> Icons.Rounded.LocalBar
        WerewolfRole.INSOMNIAC -> Icons.Rounded.Bedtime
        WerewolfRole.HUNTER -> Icons.Rounded.MyLocation
        WerewolfRole.VILLAGER -> Icons.Rounded.Cottage
        WerewolfRole.MASON_A, WerewolfRole.MASON_B -> Icons.Rounded.Shield
        WerewolfRole.TANNER -> Icons.Rounded.LocalFireDepartment
    }
}
