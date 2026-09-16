package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.WheelItem
import kotlin.math.*
import kotlin.random.Random

/** Only the canvas rotates; the centre control and layout remain stationary. */
@Composable
fun WheelRandomComponent(items: List<WheelItem>, onItemsChange: (List<WheelItem>) -> Unit,
                         onTriggerRandom: () -> Unit) {
    val options = remember(items) { items.map { it.copy(weight = if (it.weight.isFinite() && it.weight > 0) it.weight else 1f) } }
    val angle = remember(items) { Animatable(0f) }
    var spinning by remember(items) { mutableStateOf(false) }
    var result by remember(items) { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val measurer = rememberTextMeasurer()
    val colors = listOf(Color(0xFFBCEAD6), Color(0xFFD4E1BC), Color(0xFFF6DCDC), Color(0xFFFDEBD0), Color(0xFFDAD0DD), Color(0xFFD8EAF7))
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val diameter = minOf(340.dp, maxWidth - 32.dp, maxHeight - 110.dp).coerceAtLeast(120.dp)
        Surface(Modifier.widthIn(max = 560.dp).fillMaxWidth(), shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (spinning) "转动中…" else result.ifEmpty { "准备转动" },
                    Modifier.fillMaxWidth().heightIn(min = 28.dp), fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Box(Modifier.size(diameter), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.matchParentSize()) {
                        val radius = size.minDimension / 2 - 8.dp.toPx()
                        val total = options.sumOf { it.weight.toDouble() }
                        rotate(angle.value) {
                            var start = -90f
                            options.forEachIndexed { index, option ->
                                val sweep = (360 * option.weight / total).toFloat()
                                drawArc(colors[index % colors.size], start, sweep, true,
                                    Offset(center.x - radius, center.y - radius), androidx.compose.ui.geometry.Size(radius * 2, radius * 2))
                                val radians = (start + sweep / 2) * PI / 180
                                val anchor = Offset(center.x + (cos(radians) * radius * 0.68).toFloat(), center.y + (sin(radians) * radius * 0.68).toFloat())
                                val labelWidth = (radius * min(0.8f, sweep * PI.toFloat() / 180 * 0.6f)).toInt().coerceAtLeast(24.dp.roundToPx())
                                val layout = measurer.measure(option.text, TextStyle(color = Color(0xFF24382E),
                                    fontSize = if (sweep < 20) 13.sp else 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    maxLines = 2, overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = labelWidth))
                                drawText(layout, topLeft = anchor - Offset(layout.size.width / 2f, layout.size.height / 2f))
                                start += sweep
                            }
                        }
                        drawPath(Path().apply {
                            moveTo(center.x - 9.dp.toPx(), 0f); lineTo(center.x + 9.dp.toPx(), 0f)
                            lineTo(center.x, 20.dp.toPx()); close()
                        }, Color(0xFFBD6652))
                    }
                    Button(onClick = {
                        if (!spinning && options.isNotEmpty()) {
                            spinning = true
                            result = ""
                            val total = options.sumOf { it.weight.toDouble() }
                            var cursor = Random.nextDouble() * total
                            val winner = options.indexOfFirst { cursor -= it.weight; cursor < 0 }.coerceAtLeast(0)
                            val midpoint = (options.take(winner).sumOf { it.weight.toDouble() } + options[winner].weight / 2) / total * 360
                            scope.launch {
                                try {
                                    PlatformHelper.getInstance().vibrateMethod()
                                    val target = floor(angle.value / 360) * 360 + 2160 - midpoint.toFloat()
                                    angle.animateTo(target, tween(4200, easing = FastOutSlowInEasing))
                                    result = options[winner].text
                                    PlatformHelper.getInstance().vibrateMethod()
                                    onTriggerRandom()
                                } finally { spinning = false }
                            }
                        }
                    }, enabled = !spinning && options.isNotEmpty(), modifier = Modifier.size(64.dp),
                        shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
                        Text(if (spinning) "转动中" else "开始", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(if (options.isEmpty()) "请添加转盘选项" else "轻点中心，转出一个答案", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
