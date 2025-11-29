package org.walks.gamecopilot.awalong.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.awalong.AwalongRole

/**
 * 玩家卡片组件
 * 用于显示玩家信息，支持选中、队长、锁定等状态
 */
@Composable
fun PlayerCard(
    playerIndex: Int,
    nickname: String,
    role: AwalongRole,
    isSelected: Boolean,
    isCaptain: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed || isSelected) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 150),
        label = "elevation"
    )
    
    Card(
        onClick = {
            if (!isLocked) onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isCaptain -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 号码显示区域
                PlayerNumberDisplay(
                    playerIndex = playerIndex,
                    isLocked = isLocked,
                    isCaptain = isCaptain
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 玩家信息区域
                PlayerInfoArea(
                    nickname = nickname,
                    isCaptain = isCaptain,
                    isLocked = isLocked,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 状态图标
            PlayerStatusIcon(
                isLocked = isLocked,
                isSelected = isSelected
            )
        }
    }
}

/**
 * 玩家号码显示组件
 */
@Composable
private fun PlayerNumberDisplay(
    playerIndex: Int,
    isLocked: Boolean,
    isCaptain: Boolean
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = when {
                    isLocked -> MaterialTheme.colorScheme.surfaceVariant
                    isCaptain -> MaterialTheme.colorScheme.secondary 
                    else -> MaterialTheme.colorScheme.primary
                },
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${playerIndex + 1}号",
            fontWeight = FontWeight.Bold,
            color = when {
                isLocked -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onPrimary
            },
            fontSize = 14.sp
        )
    }
}

/**
 * 玩家信息显示区域
 */
@Composable
private fun PlayerInfoArea(
    nickname: String,
    isCaptain: Boolean,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = nickname,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        if (isCaptain) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "队长",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (isLocked) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "已锁定",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 玩家状态图标组件
 */
@Composable
private fun PlayerStatusIcon(
    isLocked: Boolean,
    isSelected: Boolean
) {
    when {
        isLocked -> {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "已锁定",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(20.dp)
                    .padding(4.dp)
            )
        }
        isSelected -> {
            Icon(
                imageVector = Icons.Sharp.CheckCircle,
                contentDescription = "已选中",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(4.dp)
            )
        }
    }
}