package org.walks.gamecopilot.ui.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommonButton(
    text: String,
    backColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier.wrapContentSize()
            .background(color = backColor, shape = shape)
            .border(width = 1.dp, color = Color.Black, shape = shape)
            .padding(16.dp)
            .clickable(onClick = {
                onClick?.invoke()
            })
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}