package org.walks.gamecopilot.ui.page.monopoly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.data.entity.MonopolyPlayer

@Composable
internal fun RoundTableBoard(
    players: List<MonopolyPlayer>, recent: (MonopolyPlayer) -> String,
    onAdd: () -> Unit, onNew: () -> Unit, onHistory: () -> Unit, onEdit: (MonopolyPlayer) -> Unit,
    modifier: Modifier = Modifier
) {
    var requestedPage by remember { mutableStateOf(0) }
    BoxWithConstraints(modifier) {
        val iconColors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary)
        val boardHeight = maxOf(if (maxWidth >= 600.dp) 188.dp else 280.dp, maxHeight)
        val plan = RoundTableLayout.plan(maxWidth.value.toDouble(), boardHeight.value.toDouble(), players.size, requestedPage)
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(boardHeight)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f), RoundedCornerShape(32.dp))) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalIconButton(onClick = onAdd, modifier = Modifier.size(40.dp), enabled = players.size < 20, colors = iconColors) {
                            Icon(Icons.Outlined.PersonAdd, "添加玩家", Modifier.size(20.dp))
                        }
                        Button(onClick = onNew, enabled = players.isNotEmpty(),
                            modifier = Modifier.width(124.dp).height(52.dp), contentPadding = PaddingValues(8.dp),
                            shape = RoundedCornerShape(26.dp)) {
                            Text("新建收支", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalIconButton(onClick = onHistory, modifier = Modifier.size(40.dp), colors = iconColors) {
                            Icon(Icons.Outlined.History, "最近交易", Modifier.size(20.dp))
                        }
                    }
                    if (plan.pages > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { requestedPage = plan.page - 1 }, enabled = plan.page > 0, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上一组玩家")
                            }
                            Text("${plan.page + 1} / ${plan.pages} · ${players.size} 位玩家", fontSize = 12.sp)
                            IconButton(onClick = { requestedPage = plan.page + 1 }, enabled = plan.page < plan.pages - 1, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下一组玩家")
                            }
                        }
                    } else {
                        Text(if (players.isEmpty()) "点击左侧图标添加玩家" else "${players.size} 位玩家 · 点击卡片编辑",
                            modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                plan.seats.forEach { seat ->
                    val player = players[seat.playerIndex]
                    val accent = Color(RoundTableLayout.colors[player.colorIndex.coerceIn(0, 19)])
                    Surface(onClick = { onEdit(player) },
                        modifier = Modifier.offset(seat.x.dp, seat.y.dp).size(seat.width.dp, seat.height.dp),
                        color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, accent.copy(alpha = 0.65f))) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(8.dp).background(accent, CircleShape))
                                Text(player.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("${formatMoney(player.balance)}", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = if (player.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(recent(player), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
