package org.walks.gamecopilot.ui.page.random

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.data.AnswerBookEntry
import org.walks.gamecopilot.data.AnswerCategory
import org.walks.gamecopilot.theme.LocalAppDesign

/**
 * 书本封面组件
 * 深色背景 + 金色边框 + "答案之书"标题
 */
@Composable
fun BookCover(
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val coverColor = Color(0xFF1A1A3E)
    val goldColor = Color(0xFFD4AF37)
    val goldLightColor = Color(0xFFFFD700)

    Box(
        modifier = modifier
            .shadow(
                elevation = design.elevation.lg,
                shape = RoundedCornerShape(design.cornerRadius.card)
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A3E),
                        Color(0xFF0D0D2B)
                    )
                ),
                shape = RoundedCornerShape(design.cornerRadius.card)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(goldColor, goldLightColor, goldColor)
                ),
                shape = RoundedCornerShape(design.cornerRadius.card)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 封面装饰 - 内层边框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = goldColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(design.cornerRadius.sm)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "答 案 之 书",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = goldLightColor,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // 底部装饰文字
        Text(
            text = "心中默念问题，翻开寻找答案",
            fontSize = 12.sp,
            color = goldColor.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

/**
 * 书本内页组件
 * 显示答案文字，根据类别使用不同色调
 */
@Composable
fun BookContent(
    answer: AnswerBookEntry,
    question: String = "",
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val pageColor = Color(0xFFF5F0E8)

    // 根据答案类别选择文字颜色
    val answerTextColor = when (answer.category) {
        AnswerCategory.POSITIVE -> Color(0xFF2E7D32)
        AnswerCategory.NEUTRAL -> Color(0xFF5D4037)
        AnswerCategory.NEGATIVE -> Color(0xFFC62828)
    }

    // 根据答案类别选择装饰色
    val accentColor = when (answer.category) {
        AnswerCategory.POSITIVE -> Color(0xFF81C784)
        AnswerCategory.NEUTRAL -> Color(0xFFBCAAA4)
        AnswerCategory.NEGATIVE -> Color(0xFFE57373)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = design.elevation.lg,
                shape = RoundedCornerShape(design.cornerRadius.card)
            )
            .background(pageColor, RoundedCornerShape(design.cornerRadius.card))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(design.cornerRadius.card)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 内页装饰边框
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFD4AF37).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(design.cornerRadius.sm)
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 如果有问题，先显示问题
                if (question.isNotBlank()) {
                    Text(
                        text = "「$question」",
                        fontSize = 14.sp,
                        color = Color(0xFF8D6E63),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // 答案文字
                Text(
                    text = answer.text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = answerTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // 类别标签
                val categoryLabel = when (answer.category) {
                    AnswerCategory.POSITIVE -> "积极"
                    AnswerCategory.NEUTRAL -> "中性"
                    AnswerCategory.NEGATIVE -> "消极"
                }
                Text(
                    text = categoryLabel,
                    fontSize = 12.sp,
                    color = accentColor,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

/**
 * 翻书动画组件
 * Y轴翻转：前半段显示封面，后半段显示内页
 *
 * @param isFlipping 是否正在翻书
 * @param flipProgress 翻书进度（0.0 ~ 1.0）
 * @param answer 当前答案（翻开后显示）
 * @param question 当前问题（翻开后显示在答案上方）
 * @param modifier 修饰符
 */
@Composable
fun FlipBookAnimation(
    isFlipping: Boolean,
    flipProgress: Float,
    answer: AnswerBookEntry?,
    question: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val design = LocalAppDesign.current

    // 根据 flipProgress 计算 Y 轴旋转角度
    // 前半段 (0~0.5)：封面从 0° 翻到 90°（封面消失）
    // 后半段 (0.5~1.0)：内页从 90° 翻到 0°（内页出现）
    val rotationY = if (flipProgress <= 0.5f) {
        (flipProgress / 0.5f) * 90f
    } else {
        90f - ((flipProgress - 0.5f) / 0.5f) * 90f
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationY = rotationY
                this.cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (flipProgress <= 0.5f) {
            // 前半段：显示封面
            BookCover(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 后半段：显示内页
            if (answer != null) {
                BookContent(
                    answer = answer,
                    question = question,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
