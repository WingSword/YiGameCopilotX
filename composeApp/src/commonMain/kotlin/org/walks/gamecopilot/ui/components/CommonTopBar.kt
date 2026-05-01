package org.walks.gamecopilot.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.theme.LocalAppDesign

/**
 * 顶部栏操作按钮数据类
 * @param icon 图标Composable
 * @param contentDescription 内容描述
 * @param onClick 点击回调
 */
data class TopBarAction(
    val icon: @Composable (Color) -> Unit,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * 通用顶部导航栏组件
 * 
 * 主要特性：
 * - 支持返回按钮
 * - 支持标题和副标题
 * - 支持右侧操作按钮
 * - 使用设计系统统一样式
 * 
 * @param title 标题文字
 * @param subtitle 副标题文字（可选）
 * @param onBack 返回按钮回调（为null时不显示返回按钮）
 * @param actions 右侧操作按钮列表
 * @param modifier Modifier
 * @param customAction 自定义操作区域
 */
@Composable
fun CommonTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: List<TopBarAction> = emptyList(),
    modifier: Modifier = Modifier,
    customAction: (@Composable () -> Unit)? = null
) {
    val design = LocalAppDesign.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = design.spacing.xl,
                vertical = design.spacing.sm
            ),
        shape = RoundedCornerShape(design.cornerRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = design.elevation.none,
        shadowElevation = design.elevation.xs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = design.spacing.xl,
                    vertical = design.spacing.lg
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                BackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(design.spacing.lg))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            actions.forEach { action ->
                ActionButton(action = action)
            }

            customAction?.invoke()
        }
    }
}

/**
 * 返回按钮组件
 */
@Composable
private fun BackButton(onClick: () -> Unit) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "backButtonScale"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .shadow(
                elevation = design.elevation.none,
                shape = RoundedCornerShape(design.cornerRadius.md)
            )
            .clip(RoundedCornerShape(design.cornerRadius.md))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(design.cornerRadius.md)
            )
            .clickable(
                interactionSource = interactionSource,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        BackIcon(
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 操作按钮组件
 */
@Composable
private fun ActionButton(action: TopBarAction) {
    val design = LocalAppDesign.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "actionButtonScale"
    )

    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(RoundedCornerShape(design.cornerRadius.md))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                onClick = action.onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        action.icon(MaterialTheme.colorScheme.primary)
    }
}

/**
 * 返回图标
 */
@Composable
fun BackIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.1f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.15f)
                lineTo(size.width * 0.25f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.85f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/**
 * 页面容器组件
 * 包含顶部栏和内容区域
 * 
 * @param title 标题
 * @param subtitle 副标题（可选）
 * @param onBack 返回回调（可选）
 * @param actions 操作按钮列表
 * @param showTopBar 是否显示顶部栏
 * @param content 内容Composable
 */
@Composable
fun PageContainer(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: List<TopBarAction> = emptyList(),
    showTopBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val design = LocalAppDesign.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showTopBar) {
            CommonTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                actions = actions
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/**
 * 游戏模式头部组件
 * 用于显示游戏模式的标题和副标题
 * 
 * @param title 标题
 * @param subtitle 副标题（可选）
 * @param modifier Modifier
 */
@Composable
fun GameModeHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = design.spacing.xl,
                vertical = design.spacing.sm
            )
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = design.spacing.xs)
            )
        }
    }
}
