package org.walks.gamecopilot.awalong.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.awalong.AwalongConfig
import org.walks.gamecopilot.awalong.data.AwalongGameDayEntity

/**
 * 任务进度条组件
 * 显示当前游戏任务的完成状态和进度
 */
@Composable
fun TaskProgressBar(
    currentDay: Int,
    dayList: List<AwalongGameDayEntity>,
    gameConfig: AwalongConfig,
    actualProcess: List<Int>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {

        // 进度条背景
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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

/**
 * 单个任务进度项组件
 */
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
    val isCompleted = day.gamePhase == "TASK_RESULT"
    val isSuccess = day.taskResult == 1
    
    // 计算圆角：连续完成的任务之间没有圆角
    val shape = if (isCompleted) {
        val prevCompleted = index > 0 && dayList.getOrNull(index - 1)?.taskResult != 0
        val nextCompleted = index < totalItems - 1 && dayList.getOrNull(index + 1)?.taskResult != 0
        when {
            prevCompleted && nextCompleted -> RoundedCornerShape(0.dp) // 两边都完成，无圆角
            prevCompleted -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp) // 只有右边
            nextCompleted -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp) // 只有左边
            else -> RoundedCornerShape(16.dp) // 都不连续，全圆角
        }
    } else {
        RoundedCornerShape(16.dp)
    }
    
    val boxModifier = if (isCompleted) {
        modifier
            .fillMaxHeight()
            .background(
                color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                shape = shape
            )
    } else if (isCurrent) {
        modifier
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
    } else {
        modifier
            .fillMaxHeight() // 未执行的任务无背景
    }
    
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = if (isSuccess) Icons.Sharp.Check else Icons.Sharp.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = "${taskNum}人${if (requiresTwoFailures) "*" else ""}",
                color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}