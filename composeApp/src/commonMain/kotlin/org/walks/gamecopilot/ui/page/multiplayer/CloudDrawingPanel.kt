package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import org.walks.gamecopilot.online.CloudRoom
import org.walks.gamecopilot.online.CloudStroke
import org.walks.gamecopilot.online.CloudRoomClient as Cloud
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppPrimaryAction
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.theme.LocalAppDesign
import kotlin.math.roundToInt

/** Normalized vector strokes use the same 4:3 board on phones and browsers. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudDrawingPanel(room: CloudRoom, busy: Boolean, manage: Boolean = room.isHost, playerMode: Boolean = true) {
    val design = LocalAppDesign.current
    val drawing = room.drawing ?: return
    val pending by Cloud.pendingDraw.collectAsState()
    val scope = rememberCoroutineScope()
    var color by remember { mutableStateOf("#111111") }
    var width by remember { mutableIntStateOf(8) }
    var guess by remember(drawing.stepId) { mutableStateOf("") }
    var path by remember(drawing.stepId) { mutableStateOf(emptyList<List<Int>>()) }
    var confirmEnd by remember(drawing.stepId) { mutableStateOf(false) }
    val enabled = !busy && pending.isEmpty()
    fun send(operation: String, extra: JsonObject = buildJsonObject {}) {
        val body = buildJsonObject {
            put("roundId", room.roundId); put("stepId", drawing.stepId); put("operation", operation)
            put("revision", drawing.revision); extra.forEach { (key, value) -> put(key, value) }
        }
        scope.launch { if(Cloud.drawAction(body) && operation == "guess") guess = "" }
    }
    val finishStroke by rememberUpdatedState<(List<List<Int>>) -> Unit> { points ->
        if(drawing.canDraw && enabled && points.isNotEmpty()) {
            // Keep endpoints while bounding payload size for long gestures.
            val sampled = if(points.size <= 96) points else (0..95).map { points[it * (points.size-1) / 95] }
            send("stroke", buildJsonObject {
                put("color", color); put("width", width)
                put("points", JsonArray(sampled.map { JsonArray(it.map(::JsonPrimitive)) }))
            })
        }
        path = emptyList()
    }
    val canDraw by rememberUpdatedState(drawing.canDraw && enabled)
    AppCard {
        AppSectionHeader("第 ${drawing.turn}/${drawing.totalTurns} 轮", subtitle = "${room.players.firstOrNull { it.id == drawing.painterId }?.nickname.orEmpty()} 作画",
            action = { if(drawing.phase == "DRAWING") CloudStatusLabel("剩余 ${drawing.remainingSeconds} 秒", true) })
        if(drawing.word.isNotEmpty()) Text("${if(drawing.phase in listOf("TURN_RESULT", "RESULT")) "答案" else "请画"}：${drawing.word}", style = MaterialTheme.typography.headlineSmall)
        else Text("${drawing.wordLength} 个字 · 看图猜词")
        if(drawing.phase == "DRAW_READY") {
            if(playerMode && room.selfId == drawing.painterId) AppPrimaryAction("准备好了，开始画", onClick = { send("start") }, enabled = enabled)
            else Text("等待画者开始")
        }
        if(pending.isNotEmpty()) {
            Text("上一项操作尚未确认，请重试；不会重复记分或落笔。")
            Button(onClick = { scope.launch { Cloud.drawAction() } }, enabled = !busy) { Text("重试画板操作") }
        }
        if(drawing.canDraw) {
            Text("画笔颜色", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(design.spacing.md)) {
                listOf("#111111" to "黑", "#E53935" to "红", "#1E88E5" to "蓝", "#43A047" to "绿", "#FDD835" to "黄", "#8E24AA" to "紫", "#FFFFFF" to "橡皮").forEach { (value, label) ->
                    FilterChip(color == value, { color = value }, enabled = enabled, label = { Text(label) }, leadingIcon = {
                        Canvas(Modifier.size(12.dp)) { drawCircle(Color(0xFF000000 or value.removePrefix("#").toLong(16))) }
                    })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(design.spacing.md), verticalArrangement = Arrangement.Center) {
                listOf(4 to "细", 8 to "中", 18 to "粗").forEach { (value, label) -> FilterChip(width == value, { width = value }, enabled = enabled, label = { Text(label) }) }
                TextButton(onClick = { send("undo") }, enabled = enabled && drawing.strokes.isNotEmpty()) { Text("撤销") }
                TextButton(onClick = { send("clear") }, enabled = enabled && drawing.strokes.isNotEmpty()) { Text("清空") }
            }
        }
        Canvas(Modifier.fillMaxWidth().aspectRatio(4f/3f).background(Color.White).border(1.dp, MaterialTheme.colorScheme.outline)
            .semantics { contentDescription = if(drawing.canDraw) "同步画板，在此作画" else "同步画板" }
            .pointerInput(drawing.stepId, drawing.canDraw) {
                // The detector consumes drags internally, even if onDrag does nothing.
                // Observers must leave these gestures to the enclosing scroll container.
                if (!drawing.canDraw) return@pointerInput
                fun point(offset: Offset) = listOf((offset.x / size.width * 1000).roundToInt().coerceIn(0,1000), (offset.y / size.height * 1000).roundToInt().coerceIn(0,1000))
                detectDragGestures(onDragStart = { if(canDraw) path = listOf(point(it)) }, onDragEnd = { finishStroke(path) }, onDragCancel = { path = emptyList() }) { change, _ ->
                    if(canDraw) {
                        change.consume()
                        val next = point(change.position)
                        if(path.lastOrNull() != next) path = (if(path.size >= 1500) path.filterIndexed { index, _ -> index % 2 == 0 } else path) + listOf(next)
                    }
                }
            }.pointerInput(drawing.stepId, drawing.canDraw) {
                if (!drawing.canDraw) return@pointerInput
                detectTapGestures { if(canDraw) finishStroke(listOf(listOf((it.x / size.width * 1000).roundToInt().coerceIn(0,1000), (it.y / size.height * 1000).roundToInt().coerceIn(0,1000)))) }
            }) {
            (drawing.strokes + if(path.isNotEmpty()) listOf(CloudStroke(color = color, width = width, points = path)) else emptyList()).forEach { stroke ->
                val ink = Color(0xFF000000 or stroke.color.removePrefix("#").toLong(16))
                val pixels = stroke.width * size.width / 1000f
                fun offset(p: List<Int>) = Offset(p[0] * size.width / 1000f, p[1] * size.height / 1000f)
                if(stroke.points.size == 1) drawCircle(ink, pixels/2, offset(stroke.points[0]))
                stroke.points.zipWithNext().forEach { (a,b) -> drawLine(ink, offset(a), offset(b), pixels, StrokeCap.Round) }
            }
        }
        if(drawing.canGuess) {
            OutlinedTextField(guess, { guess = it.take(40) }, singleLine = true, enabled = enabled, label = { Text("我的答案") }, modifier = Modifier.fillMaxWidth())
            AppPrimaryAction("提交答案", onClick = { send("guess", buildJsonObject { put("guess", guess.trim()) }) }, enabled = enabled && guess.isNotBlank())
        }
        if(drawing.feedback.isNotEmpty()) Text(drawing.feedback, color = MaterialTheme.colorScheme.primary)
        if(manage && drawing.phase in listOf("DRAW_READY", "DRAWING")) TextButton(onClick = { confirmEnd = true }, enabled = enabled) { Text("提前结束本轮") }
        if(manage && drawing.phase == "TURN_RESULT") AppPrimaryAction(if(drawing.turn == drawing.totalTurns) "查看总成绩" else "下一位画者", onClick = { send("next") }, enabled = enabled)
    }
    AppCard {
        AppSectionHeader("积分榜", subtitle = "猜中 +10，画者 +5")
        drawing.scores.sortedByDescending { it.score }.forEachIndexed { index, score ->
            if(index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Row(Modifier.fillMaxWidth().padding(vertical = design.spacing.sm), horizontalArrangement = Arrangement.spacedBy(design.spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Text(score.nickname, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                if(score.guessed) CloudStatusLabel("已猜中", true)
                Text("${score.score} 分", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    if(drawing.publicLog.isNotEmpty()) AppCard {
        AppSectionHeader("本局动态")
        drawing.publicLog.takeLast(8).forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if(confirmEnd) AlertDialog(onDismissRequest = { confirmEnd = false }, title = { Text("提前结束本轮") }, text = { Text("将公布答案，并保留本轮已获得的积分。") },
        confirmButton = { TextButton(onClick = { confirmEnd = false; send("end") }) { Text("结束本轮") } }, dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("取消") } })
}
