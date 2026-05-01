package org.walks.gamecopilot.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 *  Created by Wing at 16:42 on 2025/3/28
 *
 */

@Composable
fun DiceFace(value: Int, background: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .aspectRatio(1f)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF4F4F4),
                        Color(0xFFEAEAEA)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(2.dp, Color(0xFF2A2A2A), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            1 -> Dot(offset = Offset(0f, 0f))
            2 -> {
                Dot(offset = Offset(-0.3f, -0.3f))
                Dot(offset = Offset(0.3f, 0.3f))
            }

            3 -> {
                Dot(offset = Offset(-0.3f, -0.3f))
                Dot(offset = Offset(0f, 0f))
                Dot(offset = Offset(0.3f, 0.3f))
            }

            4 -> {
                Dot(offset = Offset(-0.3f, -0.3f))
                Dot(offset = Offset(0.3f, -0.3f))
                Dot(offset = Offset(-0.3f, 0.3f))
                Dot(offset = Offset(0.3f, 0.3f))
            }

            5 -> {
                Dot(offset = Offset(-0.3f, -0.3f))
                Dot(offset = Offset(0.3f, -0.3f))
                Dot(offset = Offset(0f, 0f))
                Dot(offset = Offset(-0.3f, 0.3f))
                Dot(offset = Offset(0.3f, 0.3f))
            }

            6 -> {
                Dot(offset = Offset(-0.3f, -0.3f))
                Dot(offset = Offset(0.3f, -0.3f))
                Dot(offset = Offset(-0.3f, 0f))
                Dot(offset = Offset(0.3f, 0f))
                Dot(offset = Offset(-0.3f, 0.3f))
                Dot(offset = Offset(0.3f, 0.3f))
            }

            else -> {
                Text(
                    value.toString(),
                    fontWeight = FontWeight.W900,
                    color = MaterialTheme.colorScheme.primary.copy(0.5f),
                    textAlign = TextAlign.End,
                    fontSize = 50.sp
                )
            }
        }
    }
}

@Composable
private fun Dot(color: Color = MaterialTheme.colorScheme.primary, offset: Offset, size: Dp = 8.dp) {
    Box(
        modifier = Modifier
            .offset(
                x = (offset.x * 45).dp,
                y = (offset.y * 45).dp
            )
            .size(size)
            .background(color, CircleShape)
    )
}


