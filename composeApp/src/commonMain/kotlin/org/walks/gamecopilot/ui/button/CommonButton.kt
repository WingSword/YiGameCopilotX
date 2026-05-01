package org.walks.gamecopilot.ui.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.theme.LocalAppDesign

/**
 * 通用按钮组件
 * 
 * 主要特性：
 * - 支持自定义背景颜色
 * - 按压时有缩放动画效果
 * - 使用设计系统统一样式
 * 
 * @param text 按钮文字
 * @param backColor 背景颜色
 * @param onClick 点击回调
 */
@Composable
fun CommonButton(
    text: String,
    backColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "buttonScale"
    )

    val shape = RoundedCornerShape(design.cornerRadius.button)

    Box(
        modifier = Modifier
            .wrapContentSize()
            .scale(scale)
            .shadow(
                elevation = if (isPressed) design.elevation.xs else design.elevation.md,
                shape = shape
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backColor,
                        backColor.copy(alpha = 0.86f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f),
                shape = shape
            )
            .padding(
                horizontal = design.spacing.xl,
                vertical = design.spacing.lg
            )
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                onClick = { onClick?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

/**
 * 圆形按钮组件
 * 
 * 主要特性：
 * - 1:1宽高比的圆形按钮
 * - 支持禁用状态
 * - 按压时有缩放动画
 * 
 * @param text 按钮文字
 * @param backColor 背景颜色
 * @param textColor 文字颜色
 * @param canClickable 是否可点击
 * @param onClick 点击回调
 */
@Composable
fun CircleButton(
    text: String,
    backColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    canClickable: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && canClickable) 0.96f else 1f,
        animationSpec = tween(150),
        label = "circleButtonScale"
    )

    val alpha = if (canClickable) 1f else 0.5f

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .aspectRatio(1f / 1f)
            .scale(scale)
            .shadow(
                elevation = if (isPressed || !canClickable) design.elevation.none else design.elevation.md,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(
                color = backColor.copy(alpha = alpha)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = canClickable,
                onClick = { onClick?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = textColor.copy(alpha = alpha)
        )
    }
}

/**
 * 主要操作按钮
 * 用于页面主要操作，如"开始游戏"、"确认"等
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(150),
        label = "primaryButtonScale"
    )

    val shape = RoundedCornerShape(design.cornerRadius.button)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed || !enabled) design.elevation.none else design.elevation.md,
                shape = shape
            )
            .clip(shape)
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
            .padding(
                horizontal = design.spacing.xxl,
                vertical = design.spacing.lg
            )
            .heightIn(min = 50.dp)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 次要操作按钮
 * 用于页面次要操作，如"取消"、"返回"等
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(150),
        label = "secondaryButtonScale"
    )

    val shape = RoundedCornerShape(design.cornerRadius.button)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .border(
                width = 1.5.dp,
                color = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .padding(
                horizontal = design.spacing.xxl,
                vertical = design.spacing.lg
            )
            .heightIn(min = 50.dp)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 危险操作按钮
 * 用于危险操作，如"删除"、"退出"等
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(150),
        label = "dangerButtonScale"
    )

    val shape = RoundedCornerShape(design.cornerRadius.button)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed || !enabled) design.elevation.none else design.elevation.sm,
                shape = shape
            )
            .clip(shape)
            .background(
                color = if (enabled)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(
                horizontal = design.spacing.xxl,
                vertical = design.spacing.lg
            )
            .heightIn(min = 50.dp)
            .clickable(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}