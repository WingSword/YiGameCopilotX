package org.walks.gamecopilot.ui.page.random

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.AnswerBookIntent
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.ui.components.AppPrimaryAction

@Composable
fun AnswerBookPage(
    viewmodel: MainViewmodel,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    val answerBookState by viewmodel.answerBookState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = design.spacing.lg, vertical = design.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "答案之书",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "心中默念问题，点击书本翻开答案。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = design.spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            val maxCardHeight = maxHeight.coerceAtMost(420.dp)
            val maxCardWidth = maxWidth * 0.86f
            val cardHeight = maxCardHeight.coerceAtMost(maxCardWidth / 0.72f)
            val cardWidth = cardHeight * 0.72f

            Box(
                modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    // 非翻转中时，点击书本即可翻开/再翻一次
                    .clickable(
                        enabled = !answerBookState.isFlipping,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewmodel.handleAnswerBookIntent(
                            if (answerBookState.currentAnswer == null) {
                                AnswerBookIntent.FlipBook
                            } else {
                                AnswerBookIntent.ResetFlip
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    answerBookState.isFlipping -> {
                        FlipBookAnimation(
                            isFlipping = true,
                            flipProgress = answerBookState.flipProgress,
                            answer = answerBookState.currentAnswer,
                            question = "",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    answerBookState.currentAnswer != null -> {
                        FlipBookAnimation(
                            isFlipping = false,
                            flipProgress = 1f,
                            answer = answerBookState.currentAnswer,
                            question = "",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        BookCover(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(design.cornerRadius.card))
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(design.spacing.sm)
        ) {
            if (answerBookState.isFlipping) {
                Text(
                    text = "命运之书正在翻开...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(52.dp))
            } else {
                AppPrimaryAction(
                    text = if (answerBookState.currentAnswer == null) {
                        "翻开答案之书"
                    } else {
                        "再翻一次"
                    },
                    onClick = {
                        viewmodel.handleAnswerBookIntent(
                            if (answerBookState.currentAnswer == null) {
                                AnswerBookIntent.FlipBook
                            } else {
                                AnswerBookIntent.ResetFlip
                            }
                        )
                    },
                    supportingText = if (answerBookState.currentAnswer == null) {
                        "心中默念问题，点击书本即可翻开。"
                    } else {
                        "点击书本可再翻一次，换个答案方向。"
                    }
                )
            }
        }
    }
}
