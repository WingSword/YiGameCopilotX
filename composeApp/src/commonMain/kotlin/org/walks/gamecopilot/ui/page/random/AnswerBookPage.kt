package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.AnswerBookPhase
import org.walks.gamecopilot.intent.AnswerBookIntent
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
fun AnswerBookPage(viewmodel: MainViewmodel, modifier: Modifier = Modifier) {
    val design = LocalAppDesign.current
    val state by viewmodel.answerBookState.collectAsState()
    val progress = remember {
        Animatable(if (state.phase == AnswerBookPhase.OPEN || state.phase == AnswerBookPhase.CLOSING) 1f else 0f)
    }
    // Use the display frame clock; leaving the page cancels playback and returning resumes its phase.
    LaunchedEffect(state.phase) {
        val phase = state.phase
        when (phase) {
            AnswerBookPhase.CLOSED -> progress.snapTo(0f)
            AnswerBookPhase.OPEN -> progress.snapTo(1f)
            AnswerBookPhase.CLOSING, AnswerBookPhase.OPENING -> {
                progress.animateTo(
                    targetValue = if (phase == AnswerBookPhase.OPENING) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = if (phase == AnswerBookPhase.OPENING) 520 else 300,
                        easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                    )
                )
                viewmodel.handleAnswerBookIntent(AnswerBookIntent.AnimationFinished(phase))
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // Match Harmony's available-content layout, including a scrollable book on short screens.
        val bookWidth = (maxWidth - 32.dp).coerceIn(1.dp, 460.dp)
        val bookHeight = (maxHeight - 170.dp).coerceIn(220.dp, 320.dp)
        val showTwoPages = maxWidth >= 420.dp
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = design.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(design.spacing.md)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = design.spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(design.spacing.sm)
            ) {
                Text(
                    "答案之书", fontSize = design.fontSize.headline.value.sp,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (state.phase == AnswerBookPhase.OPEN || state.phase == AnswerBookPhase.CLOSING) {
                        "需要换个角度？可以再问一次。"
                    } else {
                        "心中默念问题，轻触书本翻开。"
                    },
                    fontSize = design.fontSize.body.value.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(bookHeight + 20.dp)
                        .semantics {
                            contentDescription = if (state.isFlipping) "翻页中" else "点击答案之书翻页"
                        }
                        .clickable(
                            enabled = !state.isFlipping,
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewmodel.handleAnswerBookIntent(AnswerBookIntent.FlipBook) },
                    contentAlignment = Alignment.Center
                ) {
                    AnswerBookSpread(
                        answer = state.currentAnswer,
                        phase = state.phase,
                        progress = progress.value,
                        bookWidth = bookWidth,
                        bookHeight = bookHeight,
                        showTwoPages = showTwoPages
                    )
                }
            }
        }
    }
}
