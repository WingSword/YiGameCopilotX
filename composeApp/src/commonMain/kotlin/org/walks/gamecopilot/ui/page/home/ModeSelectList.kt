package org.walks.gamecopilot.ui.page.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
fun ModeCardNext(desc: String, isSelected: Boolean = false, background: Color = Color(0xFFF6B550)) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModeSelectList(list: List<String>, selectedPos: Int = 0, onItemClick: (Int) -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = selectedPos,
        initialPageOffsetFraction = 0f,
        pageCount = { list.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.height(180.dp).fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 10.dp, // 设置卡片之间的间距
            contentPadding = PaddingValues(horizontal = 50.dp) // 设置内容的水平内边距
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth() // 设置卡片的宽度
            ) {
                if (pagerState.currentPage == page) {
                    ModeCard(list[page], background = Color(0xFFF6B550))
                    onItemClick(pagerState.currentPage)
                } else {
                    Box(modifier = Modifier.clickable {
                       coroutineScope.launch {
                           pagerState.animateScrollToPage(page)
                       }
                    }) {
                        ModeCardNext(list[page], background = Color.Gray)
                    }

                }
            }
        }
    }
}
