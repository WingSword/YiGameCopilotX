package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.awalong.AwalongConfig
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
fun TaskProgressBar(
    currentDay: Int,
    dayList: List<AwalongGameDayEntity>,
    gameConfig: AwalongConfig,
    actualProcess: List<Int>
) {
    val design = LocalAppDesign.current
    val completedCount = dayList.count { it.taskResult != 0 }
    val successCount = dayList.count { it.taskResult == 1 }
    val failCount = dayList.count { it.taskResult == -1 }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = design.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "任务进度",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(design.spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "成功 $successCount",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "失败 $failCount",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(design.spacing.sm))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = design.elevation.xs
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = design.spacing.sm, vertical = design.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(design.spacing.sm)
            ) {
                dayList.forEachIndexed { index, day ->
                    TaskProgressItem(
                        modifier = Modifier.weight(1f),
                        index = index,
                        day = day,
                        dayList = dayList,
                        taskNum = if (index < actualProcess.size) actualProcess[index] else 0,
                        isCurrent = index == currentDay,
                        totalItems = dayList.size,
                        requiresTwoFailures = day.requiresTwoFailures
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskProgressItem(
    modifier: Modifier = Modifier,
    index: Int,
    day: AwalongGameDayEntity,
    dayList: List<AwalongGameDayEntity>,
    taskNum: Int,
    isCurrent: Boolean,
    totalItems: Int,
    requiresTwoFailures: Boolean
) {
    val design = LocalAppDesign.current
    val isCompleted = day.gamePhase == "TASK_RESULT"
    val isSuccess = day.taskResult == 1

    val shape = RectangleShape

    val backgroundModifier = when {
        isCompleted -> {
            val gradientColors = if (isSuccess) {
                listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primaryContainer
                )
            } else {
                listOf(
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.errorContainer
                )
            }
            modifier
                .fillMaxHeight()
                .shadow(
                    elevation = design.elevation.sm,
                    shape = shape
                )
                .clip(shape)
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = shape
                )
        }

        isCurrent -> {
            modifier
                .fillMaxHeight()
                .clip(shape)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = shape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    ),
                    shape = shape
                )
        }

        else -> {
            modifier
                .fillMaxHeight()
                .clip(shape)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = shape
                )
        }
    }

    Box(
        modifier = backgroundModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RectangleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Rounded.Check else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = "${taskNum}人",
                color = when {
                    isCompleted -> Color.White
                    isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontSize = 11.sp,
                fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Medium
            )

            if (requiresTwoFailures) {
                Text(
                    text = "需2票",
                    color = when {
                        isCompleted -> Color.White.copy(alpha = 0.8f)
                        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}