package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.provider.ScreenSizeProvider


@Composable
fun ModeCard(desc: String, isSelected: Boolean = false, background: Color = Color(0xFFF6B550)) {
    Column(
        modifier = Modifier.clip(shape = RoundedCornerShape(32.dp)).fillMaxSize()
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
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ModeCardNext(desc: String, isSelected: Boolean = false, background: Color = Color(0xFFF6B550)){
    Column(
        modifier = Modifier.clip(shape = RoundedCornerShape(32.dp)).fillMaxSize()
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
            maxLines = 4,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ModeSelectList(list: List<String>,selectedPos:Int=0, onItemClick: (Int) -> Unit) {
    val screenWidthDp = ScreenSizeProvider.screenWidthDp
    val currentItemWidth=(screenWidthDp/3)*2
    val nextItemWidth=(currentItemWidth/3)*2
    val lastItemWidth=currentItemWidth/3
    LaunchedEffect(Unit){


    }
    val colorList= listOf(
        Color(0xFFF6B550),
        Color(0xFF53AA99),
        Color(0xFFE46B49),
        Color(0xFF2E476E),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(list.size) {
            if(selectedPos==it){
                Box(modifier = Modifier.size(currentItemWidth,208.dp).clickable { onItemClick.invoke(it) }) {
                    ModeCard(list[it], background = colorList[it%4])
                }
            }else{
                Box(modifier = Modifier.size(nextItemWidth,208.dp).clickable { onItemClick.invoke(it) }) {
                    ModeCardNext(list[it], background = colorList[it%4])
                }

            }

        }
    }
}

@Preview()
@Composable
fun ModeselectPreview() {
    ModeCard("Mode 1")
}