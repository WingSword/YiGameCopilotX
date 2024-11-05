package org.walks.gamecopilot.ui.page.room


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.RoomState
import org.walks.gamecopilot.intent.GameIntent

@Composable
fun FlopArea(viewmodel: MainViewmodel, roomState: State<RoomState>) {

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
        StandingCard("ababababa", emptySet())
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.clickable { viewmodel.handleIntent(GameIntent.StartGame) }) {

            GoSign()
            Spacer(Modifier.width(8.dp))
            Text(
                "开始新游戏",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )

        }

    }
}

@Composable
fun GoSign(modifier: Modifier = Modifier.size(20.dp)){
    Canvas(modifier = modifier) {
        val startX = size.width
        val startY = size.height/2
        val lineLength = size.width / 3
        val halfLineLength = lineLength / 2

        drawLine(
            color = Color.Black,
            start = Offset(startX, startY),
            end = Offset(startX/2, startY),
            strokeWidth = size.width / 8,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.Black,
            start = Offset(startX, startY ),
            end = Offset(startX, size.height),
            strokeWidth = size.width / 8,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun StandingCard(standingWord: String, skills: Set<String>) {
    var hideStandingWord by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp)
            .shadow(10.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row {
            Box(
                Modifier.width(88.dp).fillMaxHeight().background(
                    color = Color(0xFF8EC3CB),
                    shape = RoundedCornerShape(32, 0, 0, 32)
                ).clickable {
                    hideStandingWord = false
                },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(!hideStandingWord) {
                    Row {
                        Text(
                            "隐藏",
                            modifier = Modifier.background(
                                shape = RoundedCornerShape(32, 0, 0, 32),
                                color = Color(0xFF8EC3CB)
                            ).clickable {
                                hideStandingWord = true
                            }.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        if (skills.isNotEmpty()) {
                            Text(
                                "技能",
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.secondary
                                ).clickable {
                                    hideStandingWord = true
                                }.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(hideStandingWord) {
                    Text(
                        "查看身份",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxHeight().padding(16.dp).weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (hideStandingWord) "*****" else standingWord,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
