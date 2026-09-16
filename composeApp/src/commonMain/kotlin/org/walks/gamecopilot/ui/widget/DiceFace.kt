package org.walks.gamecopilot.ui.widget

import org.walks.gamecopilot.theme.RandomToolDesign as D

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
fun DiceFace(
    value: Int,
    background: Color = Color.White,
    dotColor: Color = Color(D.diceInk)
) {
    val shape = RoundedCornerShape(D.diceRadius.dp)
    Box(
        modifier = Modifier
            .height(D.diceSize.dp)
            .aspectRatio(1f)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        background,
                        Color(D.diceFaceMiddle),
                        Color(D.diceFaceEnd)
                    )
                ),
                shape = shape
            )
            .border(1.dp, dotColor.copy(alpha = 0.16f), shape),
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            1 -> Dot(color = dotColor, offset = Offset(0f, 0f))
            2 -> {
                Dot(color = dotColor, offset = Offset(-0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, 0.3f))
            }

            3 -> {
                Dot(color = dotColor, offset = Offset(-0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(0f, 0f))
                Dot(color = dotColor, offset = Offset(0.3f, 0.3f))
            }

            4 -> {
                Dot(color = dotColor, offset = Offset(-0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(-0.3f, 0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, 0.3f))
            }

            5 -> {
                Dot(color = dotColor, offset = Offset(-0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(0f, 0f))
                Dot(color = dotColor, offset = Offset(-0.3f, 0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, 0.3f))
            }

            6 -> {
                Dot(color = dotColor, offset = Offset(-0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, -0.3f))
                Dot(color = dotColor, offset = Offset(-0.3f, 0f))
                Dot(color = dotColor, offset = Offset(0.3f, 0f))
                Dot(color = dotColor, offset = Offset(-0.3f, 0.3f))
                Dot(color = dotColor, offset = Offset(0.3f, 0.3f))
            }

            else -> {
                Text(
                    value.toString(),
                    fontWeight = FontWeight.W900,
                    color = dotColor,
                    textAlign = TextAlign.End,
                    fontSize = 50.sp
                )
            }
        }
    }
}

@Composable
private fun Dot(color: Color, offset: Offset, size: Dp = D.diceDot.dp) {
    Box(
        modifier = Modifier
            .offset(
                x = (offset.x * (D.diceDotOffset / 0.3)).dp,
                y = (offset.y * (D.diceDotOffset / 0.3)).dp
            )
            .size(size)
            .background(color, CircleShape)
    )
}

