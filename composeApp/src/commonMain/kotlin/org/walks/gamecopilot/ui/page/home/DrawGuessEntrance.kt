package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

sealed class DrawGameMode(
    val title: String,
    val description: String
) {
    object Solo : DrawGameMode(
        title = "单人画",
        description = "独自练习，随机获取词汇"
    )

    object Together : DrawGameMode(
        title = "一起画",
        description = "多人同屏，共同作画"
    )

    object Relay : DrawGameMode(
        title = "接力画",
        description = "轮流接笔，完成画作"
    )
}

@Composable
fun DrawGuessEntrance(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DrawModeCard(
                mode = DrawGameMode.Solo,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconContent = { color -> PersonIcon(color) },
                onClick = {
                    navController.navigate("drawBoard")
                }
            )

            DrawModeCard(
                mode = DrawGameMode.Together,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconContent = { color -> PeopleIcon(color) },
                onClick = {
                }
            )

            DrawModeCard(
                mode = DrawGameMode.Relay,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                iconContent = { color -> RelayIcon(color) },
                onClick = {
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "提示：点击卡片进入对应模式",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PersonIcon(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val headRadius = size.width * 0.2f
        drawCircle(
            color = color,
            radius = headRadius,
            center = Offset(size.width / 2, size.height * 0.28f)
        )
        val bodyPath = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.9f)
            cubicTo(
                size.width * 0.2f, size.height * 0.55f,
                size.width * 0.8f, size.height * 0.55f,
                size.width * 0.8f, size.height * 0.9f
            )
        }
        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = size.width * 0.12f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun PeopleIcon(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val scale = 0.7f
        drawCircle(
            color = color,
            radius = size.width * 0.12f * scale,
            center = Offset(size.width * 0.35f, size.height * 0.25f)
        )
        drawCircle(
            color = color,
            radius = size.width * 0.12f * scale,
            center = Offset(size.width * 0.65f, size.height * 0.25f)
        )
        val bodyPath1 = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.85f)
            cubicTo(
                size.width * 0.2f, size.height * 0.5f,
                size.width * 0.5f, size.height * 0.5f,
                size.width * 0.5f, size.height * 0.85f
            )
        }
        val bodyPath2 = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.85f)
            cubicTo(
                size.width * 0.5f, size.height * 0.5f,
                size.width * 0.8f, size.height * 0.5f,
                size.width * 0.8f, size.height * 0.85f
            )
        }
        drawPath(
            bodyPath1,
            color = color,
            style = Stroke(width = size.width * 0.08f, cap = StrokeCap.Round)
        )
        drawPath(
            bodyPath2,
            color = color,
            style = Stroke(width = size.width * 0.08f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun RelayIcon(color: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val arrowSize = size.width * 0.15f
        val strokeWidth = size.width * 0.08f

        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.35f)
                lineTo(size.width * 0.5f, size.height * 0.35f)
                lineTo(size.width * 0.5f - arrowSize, size.height * 0.35f - arrowSize)
            },
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.35f)
                lineTo(size.width * 0.5f - arrowSize, size.height * 0.35f + arrowSize)
            },
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.65f)
                lineTo(size.width * 0.8f, size.height * 0.65f)
                lineTo(size.width * 0.8f - arrowSize, size.height * 0.65f - arrowSize)
            },
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.8f, size.height * 0.65f)
                lineTo(size.width * 0.8f - arrowSize, size.height * 0.65f + arrowSize)
            },
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun DrawModeCard(
    mode: DrawGameMode,
    containerColor: Color,
    contentColor: Color,
    iconContent: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    iconContent(contentColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = mode.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mode.description,
                    fontSize = 13.sp,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            Text(
                text = "开始",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
