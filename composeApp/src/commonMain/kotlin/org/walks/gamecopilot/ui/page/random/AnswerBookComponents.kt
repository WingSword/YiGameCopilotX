package org.walks.gamecopilot.ui.page.random

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.data.AnswerBookEntry
import org.walks.gamecopilot.data.AnswerCategory
import org.walks.gamecopilot.data.entity.AnswerBookPhase
import org.walks.gamecopilot.theme.LocalAppDesign
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.answer_book_cover_icon

@Composable
private fun BookCover(dark: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier.shadow(16.dp, shape).background(if (dark) Color(0xFF171C2A) else Color(0xFF263752), shape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxWidth(0.87f).fillMaxHeight(0.9f)
                .border(1.dp, if (dark) Color(0xFF806F4B) else Color(0xFFB79B61), RoundedCornerShape(12.dp))
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painterResource(Res.drawable.answer_book_cover_icon), contentDescription = null,
                tint = Color(0xFFD7BC7C), modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(22.dp))
            Text(
                "答案之书", fontSize = 27.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFFEAD49D), letterSpacing = 3.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text("想好问题，再翻开", fontSize = 10.sp, color = Color(0xFFAD9A70))
        }
        Box(
            Modifier.align(Alignment.TopStart).offset(x = 10.dp, y = 12.dp)
                .width(5.dp).fillMaxHeight(0.92f).background(Color(0xFF101722).copy(alpha = 0.7f))
        )
    }
}

@Composable
private fun AnswerPage(answer: AnswerBookEntry, dark: Boolean, bookHeight: Dp) {
    val design = LocalAppDesign.current
    val label = when (answer.category) {
        AnswerCategory.POSITIVE -> "积极倾向"
        AnswerCategory.NEUTRAL -> "中性提示"
        AnswerCategory.NEGATIVE -> "谨慎提醒"
    }
    val labelColor = when (answer.category) {
        AnswerCategory.POSITIVE -> design.colors.success
        AnswerCategory.NEUTRAL -> if (dark) Color(0xFFB9A98E) else Color(0xFF8A6F47)
        AnswerCategory.NEGATIVE -> design.colors.error
    }
    val labelSurface = when (answer.category) {
        AnswerCategory.POSITIVE -> if (dark) Color(0xFF253A31) else Color(0xFFE6F5ED)
        AnswerCategory.NEUTRAL -> if (dark) Color(0xFF3A352B) else Color(0xFFF3EDE1)
        AnswerCategory.NEGATIVE -> if (dark) Color(0xFF432A2B) else Color(0xFFFBEAEC)
    }
    Column(
        Modifier.fillMaxWidth().heightIn(min = bookHeight).padding(vertical = design.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            label, fontSize = design.fontSize.xs.value.sp, fontWeight = FontWeight.Medium,
            color = labelColor, letterSpacing = 1.sp,
            modifier = Modifier.background(labelSurface, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            answer.text, fontSize = design.fontSize.xl.value.sp, fontWeight = FontWeight.Bold,
            color = if (dark) Color(0xFFF2EEE5) else Color(0xFF30352F),
            textAlign = TextAlign.Center, lineHeight = 31.sp,
            modifier = Modifier.padding(horizontal = design.spacing.lg)
        )
    }
}

/** Only the cover moves. Answer text stays upright and legible through opening and closing. */
@Composable
internal fun AnswerBookSpread(
    answer: AnswerBookEntry?,
    phase: AnswerBookPhase,
    progress: Float,
    bookWidth: Dp,
    bookHeight: Dp,
    showTwoPages: Boolean
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.2f
    val design = LocalAppDesign.current
    val opening = progress.coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center) {
        if (answer != null) {
            val initialOpacity = if (phase == AnswerBookPhase.CLOSING) 0.32f else 0.28f
            Row(
                modifier = Modifier.width(bookWidth).height(bookHeight)
                    .graphicsLayer { alpha = initialOpacity + opening * (1f - initialOpacity) }
                    .shadow(12.dp, RoundedCornerShape(design.cornerRadius.card))
                    .clip(RoundedCornerShape(design.cornerRadius.card)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showTwoPages) {
                    val quoteColor = if (dark) Color(0xFFA9A397) else Color(0xFF817866)
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .background(if (dark) Color(0xFF282720) else Color(0xFFF5F0E4)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(design.spacing.lg, Alignment.CenterVertically)
                    ) {
                        Text("“", fontSize = 42.sp, color = quoteColor)
                        Text("读完这一页，\n留意你的第一反应。", fontSize = design.fontSize.body.value.sp,
                            color = quoteColor, textAlign = TextAlign.Center)
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight(0.9f)
                        .background(if (dark) Color(0xFF625D52) else Color(0xFFD4C9B3)))
                }
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .background(if (dark) Color(0xFF302F27) else Color(0xFFFFFDF6))
                        .verticalScroll(rememberScrollState())
                ) { AnswerPage(answer, dark, bookHeight) }
            }
        }
        if (phase != AnswerBookPhase.OPEN) {
            BookCover(
                dark,
                Modifier.width(bookWidth.coerceAtMost(224.dp)).height(bookHeight).graphicsLayer {
                    scaleX = 1f - opening * 0.92f
                    translationX = -76.dp.toPx() * opening
                    alpha = 1f - opening * 0.92f
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
            )
        }
    }
}
