package org.walks.gamecopilot.ui.page.drawguess

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.walks.gamecopilot.data.DrawGuessWordLibrary
import org.walks.gamecopilot.ui.components.CommonTopBar

data class PathState(
    val points: List<Offset>,
    val color: Color = Color.Black,
    val strokeWidth: Float = 8f,
    val isEraser: Boolean = false
)

val brushSizes = listOf(4f, 8f, 16f, 24f, 32f)

val presetColors = listOf(
    Color.Black,
    Color.White,
    Color.Red,
    Color(0xFFFF6B35),
    Color.Yellow,
    Color.Green,
    Color(0xFF2196F3),
    Color.Blue,
    Color(0xFF9C27B0),
    Color(0xFF795548),
    Color(0xFF607D8B),
    Color(0xFFE91E63)
)

val backgroundColors = listOf(
    Color.White,
    Color(0xFFF5F5DC),
    Color(0xFFFFE4C4),
    Color(0xFFE0FFFF),
    Color(0xFFF0FFF0),
    Color(0xFFFFF0F5),
    Color(0xFFF0F8FF),
    Color(0xFFFAFAD2)
)

@Composable
fun DrawBoardPage(
    onBack: () -> Unit
) {
    var showWordDialog by remember { mutableStateOf(false) }
    var currentWord by remember { mutableStateOf(DrawGuessWordLibrary.getRandomWord()) }
    val paths = remember { mutableStateListOf<PathState>() }
    val redoStack = remember { mutableStateListOf<PathState>() }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentStrokeWidth by remember { mutableStateOf(8f) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showBrushPicker by remember { mutableStateOf(false) }
    var showFillPicker by remember { mutableStateOf(false) }
    var isEraser by remember { mutableStateOf(false) }
    var canvasBackgroundColor by remember { mutableStateOf(Color.White) }
    var showMoreTools by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CommonTopBar(
            title = "画板",
            subtitle = "自由创作",
            onBack = onBack,
            actions = emptyList(),
            customAction = {
                TextButton(
                    onClick = { showWordDialog = true }
                ) {
                    Text(
                        text = "查看词汇",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(canvasBackgroundColor)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
        ) {
            DrawingCanvas(
                paths = paths.toList(),
                currentColor = currentColor,
                currentStrokeWidth = currentStrokeWidth,
                isEraser = isEraser,
                canvasBackgroundColor = canvasBackgroundColor,
                onPathComplete = { points ->
                    if (points.isNotEmpty()) {
                        paths.add(
                            PathState(
                                points,
                                if (isEraser) canvasBackgroundColor else currentColor,
                                currentStrokeWidth,
                                isEraser
                            )
                        )
                        redoStack.clear()
                    }
                }
            )
        }

        DrawingTools(
            currentColor = currentColor,
            currentStrokeWidth = currentStrokeWidth,
            showColorPicker = showColorPicker,
            showBrushPicker = showBrushPicker,
            showFillPicker = showFillPicker,
            showMoreTools = showMoreTools,
            isEraser = isEraser,
            canUndo = paths.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            onColorChange = { color ->
                currentColor = color
                showColorPicker = false
            },
            onStrokeWidthChange = { width ->
                currentStrokeWidth = width
                showBrushPicker = false
            },
            onBackgroundColorChange = { color ->
                canvasBackgroundColor = color
                showFillPicker = false
            },
            onColorPickerToggle = {
                showColorPicker = !showColorPicker
                showBrushPicker = false
                showFillPicker = false
            },
            onBrushPickerToggle = {
                showBrushPicker = !showBrushPicker
                showColorPicker = false
                showFillPicker = false
            },
            onFillPickerToggle = {
                showFillPicker = !showFillPicker
                showColorPicker = false
                showBrushPicker = false
            },
            onMoreToolsToggle = { showMoreTools = !showMoreTools },
            onEraserToggle = { isEraser = !isEraser },
            onUndo = {
                if (paths.isNotEmpty()) {
                    redoStack.add(paths.removeLast())
                }
            },
            onRedo = {
                if (redoStack.isNotEmpty()) {
                    paths.add(redoStack.removeLast())
                }
            },
            onClear = {
                redoStack.addAll(paths.toList())
                paths.clear()
            }
        )
    }

    if (showWordDialog) {
        WordCardDialog(
            word = currentWord,
            onDismiss = { showWordDialog = false },
            onNextWord = {
                currentWord = DrawGuessWordLibrary.getRandomWord()
            }
        )
    }
}

@Composable
fun DrawingCanvas(
    paths: List<PathState>,
    currentColor: Color,
    currentStrokeWidth: Float,
    isEraser: Boolean,
    canvasBackgroundColor: Color,
    onPathComplete: (List<Offset>) -> Unit
) {
    val currentPoints = remember { mutableStateListOf<Offset>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(currentColor, currentStrokeWidth, isEraser) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPoints.clear()
                        currentPoints.add(offset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPoints.add(change.position)
                    },
                    onDragEnd = {
                        onPathComplete(currentPoints.toList())
                        currentPoints.clear()
                    }
                )
            }
    ) {
        paths.forEach { pathState ->
            drawPathFromPoints(
                points = pathState.points,
                color = pathState.color,
                strokeWidth = pathState.strokeWidth
            )
        }

        if (currentPoints.isNotEmpty()) {
            drawPathFromPoints(
                points = currentPoints.toList(),
                color = if (isEraser) canvasBackgroundColor else currentColor,
                strokeWidth = currentStrokeWidth
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathFromPoints(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float
) {
    if (points.isEmpty()) return

    if (points.size == 1) {
        drawCircle(
            color = color,
            radius = strokeWidth / 2f,
            center = points[0]
        )
        return
    }

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            quadraticTo(prev.x, prev.y, midX, midY)
        }
        val last = points.last()
        lineTo(last.x, last.y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.cornerPathEffect(8f)
        )
    )
}

@Composable
fun DrawingTools(
    currentColor: Color,
    currentStrokeWidth: Float,
    showColorPicker: Boolean,
    showBrushPicker: Boolean,
    showFillPicker: Boolean,
    showMoreTools: Boolean,
    isEraser: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onColorChange: (Color) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    onColorPickerToggle: () -> Unit,
    onBrushPickerToggle: () -> Unit,
    onFillPickerToggle: () -> Unit,
    onMoreToolsToggle: () -> Unit,
    onEraserToggle: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        AnimatedVisibility(
            visible = showMoreTools,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                if (showFillPicker) {
                    FillColorPickerPanel(
                        colors = backgroundColors,
                        selectedColor = currentColor,
                        onColorSelected = onBackgroundColorChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToolButtonSmall(
                        isSelected = isEraser,
                        onClick = onEraserToggle,
                        modifier = Modifier.weight(1f)
                    ) { color ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            EraserIcon(color, Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEraser) "橡皮" else "橡皮擦",
                                fontSize = 12.sp,
                                color = color
                            )
                        }
                    }

                    ToolButtonSmall(
                        isSelected = showFillPicker,
                        onClick = onFillPickerToggle,
                        modifier = Modifier.weight(1f)
                    ) { color ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FillIcon(color, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "背景",
                                fontSize = 12.sp,
                                color = color
                            )
                        }
                    }

                    ToolButtonSmall(
                        isSelected = false,
                        onClick = onClear,
                        modifier = Modifier.weight(1f)
                    ) { color ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ClearIcon(color, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "清空",
                                fontSize = 12.sp,
                                color = color
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showColorPicker) {
            ColorPickerPanel(
                colors = presetColors,
                selectedColor = currentColor,
                onColorSelected = onColorChange
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showBrushPicker) {
            BrushSizePickerPanel(
                sizes = brushSizes,
                selectedSize = currentStrokeWidth,
                onSizeSelected = onStrokeWidthChange
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButtonSmall(
                isSelected = false,
                onClick = onMoreToolsToggle
            ) { color ->
                MoreIcon(color, showMoreTools)
            }

            ToolButtonSmall(
                isSelected = canUndo,
                onClick = onUndo,
                enabled = canUndo
            ) { color ->
                UndoIcon(color)
            }

            ToolButtonSmall(
                isSelected = canRedo,
                onClick = onRedo,
                enabled = canRedo
            ) { color ->
                RedoIcon(color)
            }

            ColorButton(
                color = currentColor,
                isSelected = showColorPicker,
                onClick = onColorPickerToggle
            )

            TextButtonSmall(
                text = "${(currentStrokeWidth / 4).toInt() + 1}级",
                isSelected = showBrushPicker,
                onClick = onBrushPickerToggle
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ToolButtonSmall(
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    iconContent: @Composable (Color) -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        iconContent(
            when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                isSelected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun ColorButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else Color.Gray,
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun TextButtonSmall(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColorPickerPanel(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val rows = colors.chunked(6)
            rows.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    rowColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (color == selectedColor) 2.dp else 1.dp,
                                    color = if (color == selectedColor) MaterialTheme.colorScheme.primary
                                    else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(color) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrushSizePickerPanel(
    sizes: List<Float>,
    selectedSize: Float,
    onSizeSelected: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            sizes.forEachIndexed { index, size ->
                val isSelected = size == selectedSize
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSizeSelected(size) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((size * 0.6).dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                    }
                    Text(
                        text = "${index + 1}",
                        fontSize = 10.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FillColorPickerPanel(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 2.dp else 1.dp,
                            color = if (color == selectedColor) MaterialTheme.colorScheme.primary
                            else Color.Gray,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun WordCardDialog(
    word: String,
    onDismiss: () -> Unit,
    onNextWord: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "请画出这个词语",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = word,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 32.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "关闭",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Button(
                        onClick = onNextWord,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "换一个",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreIcon(color: Color, isExpanded: Boolean, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        size.width * 0.08f
        val dotRadius = size.width * 0.06f
        drawCircle(color, dotRadius, Offset(size.width * 0.5f, size.height * 0.25f))
        drawCircle(color, dotRadius, Offset(size.width * 0.5f, size.height * 0.5f))
        drawCircle(color, dotRadius, Offset(size.width * 0.5f, size.height * 0.75f))
    }
}

@Composable
fun PaletteIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.08f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.1f)
                cubicTo(
                    size.width * 0.1f, size.height * 0.1f,
                    size.width * 0.1f, size.height * 0.7f,
                    size.width * 0.4f, size.height * 0.9f
                )
                cubicTo(
                    size.width * 0.9f, size.height * 0.9f,
                    size.width * 0.9f, size.height * 0.1f,
                    size.width * 0.5f, size.height * 0.1f
                )
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawCircle(color, size.width * 0.05f, Offset(size.width * 0.3f, size.height * 0.35f))
        drawCircle(color, size.width * 0.05f, Offset(size.width * 0.5f, size.height * 0.3f))
        drawCircle(color, size.width * 0.05f, Offset(size.width * 0.7f, size.height * 0.4f))
        drawCircle(color, size.width * 0.05f, Offset(size.width * 0.35f, size.height * 0.55f))
        drawCircle(color, size.width * 0.05f, Offset(size.width * 0.55f, size.height * 0.55f))
    }
}

@Composable
fun BrushIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.08f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.85f, size.height * 0.15f)
                lineTo(size.width * 0.4f, size.height * 0.6f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.4f, size.height * 0.6f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.7f,
                    size.width * 0.15f, size.height * 0.65f,
                    size.width * 0.15f, size.height * 0.8f
                )
                cubicTo(
                    size.width * 0.15f, size.height * 0.9f,
                    size.width * 0.35f, size.height * 0.9f,
                    size.width * 0.45f, size.height * 0.8f
                )
                lineTo(size.width * 0.5f, size.height * 0.7f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EraserIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.08f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.7f, size.height * 0.25f)
                lineTo(size.width * 0.25f, size.height * 0.7f)
                lineTo(size.width * 0.4f, size.height * 0.85f)
                lineTo(size.width * 0.85f, size.height * 0.4f)
                close()
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.85f)
                lineTo(size.width * 0.85f, size.height * 0.85f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FillIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.07f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.1f)
                lineTo(size.width * 0.85f, size.height * 0.35f)
                lineTo(size.width * 0.85f, size.height * 0.5f)
                lineTo(size.width * 0.5f, size.height * 0.75f)
                lineTo(size.width * 0.15f, size.height * 0.5f)
                lineTo(size.width * 0.15f, size.height * 0.35f)
                close()
            },
            color = color,
            style = Stroke(width = stroke)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.75f)
                lineTo(size.width * 0.5f, size.height * 0.95f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.3f, size.height * 0.85f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.75f,
                    size.width * 0.7f, size.height * 0.75f,
                    size.width * 0.7f, size.height * 0.85f
                )
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun UndoIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.08f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.3f)
                lineTo(size.width * 0.35f, size.height * 0.3f)
                lineTo(size.width * 0.45f, size.height * 0.2f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.35f, size.height * 0.3f)
                cubicTo(
                    size.width * 0.15f, size.height * 0.3f,
                    size.width * 0.15f, size.height * 0.6f,
                    size.width * 0.35f, size.height * 0.7f
                )
                lineTo(size.width * 0.65f, size.height * 0.7f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun RedoIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.08f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.35f, size.height * 0.3f)
                lineTo(size.width * 0.65f, size.height * 0.3f)
                lineTo(size.width * 0.55f, size.height * 0.2f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.3f)
                cubicTo(
                    size.width * 0.85f, size.height * 0.3f,
                    size.width * 0.85f, size.height * 0.6f,
                    size.width * 0.65f, size.height * 0.7f
                )
                lineTo(size.width * 0.35f, size.height * 0.7f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ClearIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.08f
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.2f)
                lineTo(size.width * 0.8f, size.height * 0.8f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.8f, size.height * 0.2f)
                lineTo(size.width * 0.2f, size.height * 0.8f)
            },
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
