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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.GameRecord
import org.walks.gamecopilot.data.GameStatsManager
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.theme.AppDesignSystem
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppBackButton
import org.walks.gamecopilot.utils.DateTimeUtils
import kotlin.time.Clock

/** 展示本机实际产生并持久化的对局数据。 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun StatsPage(viewmodel: MainViewmodel, onBack: () -> Unit = {}) {
    val design = LocalAppDesign.current
    val records by GameStatsManager.recordsFlow.collectAsState()
    val totalPlayers = remember(records) { records.sumOf { it.playerCount } }
    val lastPlayedTime = remember(records) { records.maxOfOrNull { it.startTime } ?: 0L }
    val modeCounts = remember(records) { records.groupingBy { it.gameModeOrdinal }.eachCount() }
    val recentRecords = remember(records) { records.asReversed().take(20) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = design.spacing.xl)
            .padding(top = design.spacing.xl, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(design.spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text("对局记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

            TextButton(
                onClick = { showClearConfirmation = true },
                enabled = records.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重置对局统计", color = MaterialTheme.colorScheme.error)
            }
        SummaryCard(
            totalGames = records.size,
            totalPlayers = totalPlayers,
            lastPlayedText = formatRelativeTime(lastPlayedTime),
            design = design
        )

        if (records.isEmpty()) {
            EmptyState()
        } else {
            DistributionCard(
                totalGames = records.size,
                modeCounts = modeCounts,
                design = design
            )
            RecentGamesCard(records = recentRecords, design = design)

        }
    }

    if (showClearConfirmation) {
        AppDialog(
            title = "重置对局统计",
            subtitle = "累计局数、玩法分布和最近对局都会被删除，此操作无法撤销。",
            onDismiss = { showClearConfirmation = false },
            showCloseButton = false,
            scrollable = false,
            actions = {
                OutlinedButton(
                    onClick = { showClearConfirmation = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        GameStatsManager.clearAll()
                        showClearConfirmation = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("重置")
                }
            }
        ) {
            Text(
                text = "清空后将从下一局重新统计。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(
    totalGames: Int,
    totalPlayers: Int,
    lastPlayedText: String,
    design: AppDesignSystem
) {
    StatsCard(design = design) {
        Column(modifier = Modifier.fillMaxWidth().padding(design.spacing.xl)) {
            Text(
                text = "游玩概览",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(design.spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(totalGames.toString(), "累计对局", Modifier.weight(1f))
                SummaryDivider()
                SummaryItem(totalPlayers.toString(), "参与人次", Modifier.weight(1f))
                SummaryDivider()
                SummaryItem(lastPlayedText, "最近游玩", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = if (value.length > 6) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(42.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "还没有对局记录",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "开始一局游戏后，这里会显示玩法分布、参与人数和最近记录。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DistributionCard(
    totalGames: Int,
    modeCounts: Map<Int, Int>,
    design: AppDesignSystem
) {
    StatsCard(design = design) {
        Column(modifier = Modifier.fillMaxWidth().padding(design.spacing.lg)) {
            SectionTitle("玩法分布")
            GameMode.entries.forEach { mode ->
                val count = modeCounts[mode.ordinal] ?: 0
                if (count > 0) {
                    DistributionRow(mode = mode, count = count, totalGames = totalGames)
                }
            }
        }
    }
}

@Composable
private fun DistributionRow(mode: GameMode, count: Int, totalGames: Int) {
    val color = mode.gradientColors.start
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeBadge(mode = mode, color = color)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$count 局",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val fraction = (count.toFloat() / totalGames.coerceAtLeast(1)).coerceIn(0.05f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(5.dp)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun RecentGamesCard(records: List<GameRecord>, design: AppDesignSystem) {
    StatsCard(design = design) {
        Column(modifier = Modifier.fillMaxWidth().padding(design.spacing.lg)) {
            SectionTitle("最近对局")
            records.forEachIndexed { index, record ->
                RecordRow(record)
                if (index < records.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 44.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordRow(record: GameRecord) {
    val mode = GameMode.entries.getOrNull(record.gameModeOrdinal)
    val color = mode?.gradientColors?.start ?: MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeBadge(mode = mode, color = color)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.gameModeName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${record.playerCount} 人 · ${formatDateTime(record.startTime)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = record.winner.ifBlank { "未结算" },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (record.winner.isNotBlank()) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (record.durationMillis > 0L) {
                Text(
                    text = formatDuration(record.durationMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeBadge(mode: GameMode?, color: Color) {
    Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = color.copy(alpha = 0.14f)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = mode?.title?.take(1) ?: "游",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun StatsCard(design: AppDesignSystem, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.cornerRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = design.elevation.card)
    ) {
        content()
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "暂无"
    val minutes = ((Clock.System.now().toEpochMilliseconds() - timestamp).coerceAtLeast(0L)) / 60_000L
    return when {
        minutes < 1L -> "刚刚"
        minutes < 60L -> "${minutes}分钟前"
        minutes < 1_440L -> "${minutes / 60L}小时前"
        minutes < 43_200L -> "${minutes / 1_440L}天前"
        else -> formatDateTime(timestamp).substringBefore(' ')
    }
}

private fun formatDateTime(timestamp: Long): String =
    DateTimeUtils.formatTimestamp(timestamp).dropLast(3)

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        hours > 0L -> "${hours}小时${minutes}分"
        minutes > 0L -> "${minutes}分${seconds}秒"
        else -> "${seconds}秒"
    }
}
