package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun ModeCard(desc: String, isSelected: Boolean = false, background: Color = Color(0xFFF6B550)) {
    Column(
        modifier = Modifier.clip(shape = RoundedCornerShape(32.dp)).size(136.dp, 208.dp)
            .background(color = background)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else background,
                shape = RoundedCornerShape(32.dp)
            ).padding(16.dp)
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = desc,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ModeSelectList(list: List<String>, onItemClick: (Int) -> Unit) {
    val colorList= listOf(
        Color(0xFFF6B550),
        Color(0xFF53AA99),
        Color(0xFFE46B49),
        Color(0xFF2E476E),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(list.size) {
            Box(modifier = Modifier.clickable { onItemClick.invoke(it) }) {
                ModeCard(list[it], background = colorList[it%4])
            }
        }
    }
}

@Preview()
@Composable
fun ModeselectPreview() {
    ModeCard("Mode 1")
}