package org.walks.gamecopilot.ui.page.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.theme.LocalAppDesign

/**
 * 信息收集与统计页面
 * 展示游戏数据统计、使用记录等信息
 */
@Composable
fun StatsPage(viewmodel: MainViewmodel) {
    val design = LocalAppDesign.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = design.spacing.xl)
            .verticalScroll(scrollState)
            .padding(top = design.spacing.xl, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(design.spacing.lg)
    ) {
        // 页面标题
        Box(
            modifier = Modifier.padding(
                horizontal = design.spacing.sm,
                vertical = design.spacing.md
            )
        ) {
            Text(
                text = "信息中心",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 游戏数据概览
        StatsCard(title = "游戏概览", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Rounded.VideogameAsset,
                        label = "已集成游戏",
                        value = "5",
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        icon = Icons.Rounded.Groups,
                        label = "支持人数",
                        value = "3-16",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatItem(
                        icon = Icons.Rounded.Timer,
                        label = "单局时长",
                        value = "5-30min",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // 游戏列表
        StatsCard(title = "游戏列表", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(design.spacing.md)
            ) {
                GameInfoRow("谁是卧底", "4-16人", "发言推理")
                GameInfoRow("阿瓦隆", "5-10人", "阵营博弈")
                GameInfoRow("你画我猜", "3-10人", "创意表达")
                GameInfoRow("猎巫镇", "4-12人", "身份对抗")
                GameInfoRow("一夜终极狼人", "3-10人", "快速推理")
            }
        }

        // 随机工具概览
        StatsCard(title = "随机工具", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(design.spacing.md)
            ) {
                ToolInfoRow("骰子", "支持自定义面数")
                ToolInfoRow("硬币", "正反面随机")
                ToolInfoRow("转盘", "自定义选项与权重")
                ToolInfoRow("指转盘", "多人随机选择")
                ToolInfoRow("卡牌", "正反面翻转")
            }
        }

        // 使用提示
        StatsCard(title = "使用提示", design = design) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(design.spacing.xl)
            ) {
                Text(
                    text = "• 所有游戏均支持单机传机模式，无需联网即可使用\n" +
                            "• 选择\"同网络游玩\"可在局域网内与好友同步游戏\n" +
                            "• 身份卡支持左右滑动翻看，注意传递时避免旁人窥视\n" +
                            "• 随机工具支持自定义配置，长按可编辑或删除",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    design: org.walks.gamecopilot.theme.AppDesignSystem,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.cornerRadius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = design.elevation.card)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = design.spacing.xl,
                    end = design.spacing.xl,
                    top = design.spacing.lg,
                    bottom = design.spacing.sm
                )
            )
            content()
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GameInfoRow(name: String, players: String, category: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = players,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolInfoRow(name: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
