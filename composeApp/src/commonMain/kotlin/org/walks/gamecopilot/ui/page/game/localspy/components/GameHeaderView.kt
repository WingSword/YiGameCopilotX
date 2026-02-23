package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.ui.components.CommonTopBar
import org.walks.gamecopilot.ui.components.TopBarAction

@Composable
fun GameHeaderView(
    onBack: () -> Unit,
    isWordLibraryExpanded: Boolean,
    onToggleWordLibrary: (Boolean) -> Unit,
    onShowWordsDialog: () -> Unit
) {
    val actions = buildList {
        if (!isWordLibraryExpanded) {
            add(
                TopBarAction(
                    icon = { color -> InfoIcon(color) },
                    contentDescription = "词汇选择",
                    onClick = { onToggleWordLibrary(true) }
                )
            )
        }
        if (isWordLibraryExpanded) {
            add(
                TopBarAction(
                    icon = { color -> ChevronUpIcon(color, isWordLibraryExpanded) },
                    contentDescription = "折叠词库",
                    onClick = { onToggleWordLibrary(false) }
                )
            )
        }
    }

    CommonTopBar(
        title = "本地卧底",
        subtitle = "找出隐藏的卧底，保护平民身份",
        onBack = onBack,
        actions = actions
    )
}

@Composable
private fun InfoIcon(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = size.width * 0.1f
        drawCircle(
            color = color,
            radius = size.width * 0.35f,
            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2),
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = color,
            radius = size.width * 0.06f,
            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.32f)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.45f)
                lineTo(size.width * 0.5f, size.height * 0.72f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawCircle(
            color = color,
            radius = size.width * 0.06f,
            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.82f)
        )
    }
}

@Composable
private fun ChevronUpIcon(color: Color, isExpanded: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 180f,
        animationSpec = tween(durationMillis = 300),
        label = "chevron_rotation"
    )
    Canvas(modifier = Modifier.size(22.dp).rotate(rotation)) {
        val stroke = size.width * 0.1f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.6f)
                lineTo(size.width * 0.5f, size.height * 0.35f)
                lineTo(size.width * 0.75f, size.height * 0.6f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
