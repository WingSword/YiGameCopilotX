package org.walks.gamecopilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.theme.LocalAppDesign

/**
 * 顶部栏操作按钮数据类
 * @param icon 图标
 * @param contentDescription 内容描述
 * @param onClick 点击回调
 */
data class TopBarAction(
    val icon: ImageVector,
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
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = design.elevation.none,
        shadowElevation = design.elevation.appBar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = design.spacing.xl,
                    vertical = design.spacing.md
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                AppBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(design.spacing.lg))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                actions.forEach { action ->
                    AppIconButton(
                        icon = action.icon,
                        contentDescription = action.contentDescription,
                        onClick = action.onClick
                    )
                }
            }

            customAction?.invoke()
        }
    }
}

@Composable
fun BackIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = null,
        tint = color,
        modifier = modifier
    )
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
