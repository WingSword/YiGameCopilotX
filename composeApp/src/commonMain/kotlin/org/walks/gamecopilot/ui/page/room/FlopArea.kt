package org.walks.gamecopilot.ui.page.room


import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.yi.yigamecopilot.android.theme.SpyColorList
import org.walks.gamecopilot.utils.DateTimeUtils

@Composable
fun FlopArea(word:String,time:Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("更新于  ${DateTimeUtils.formatTimestamp(time)}")
        StandingCard(word, emptySet())
    }


}

@Composable
fun GoSign(modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val startX = size.width
        val startY = size.height / 2
        val lineLength = size.width / 3
        val halfLineLength = lineLength / 2

        drawLine(
            color = Color.Black,
            start = Offset(startX, startY),
            end = Offset(startX / 2, startY),
            strokeWidth = size.width / 8,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.Black,
            start = Offset(startX, startY),
            end = Offset(startX, size.height),
            strokeWidth = size.width / 8,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun StandingCard(standingWord: String, skills: Set<String>) {
    var hideStandingWord by remember { mutableStateOf(true) }

    // 添加颜色动画
    val animatedTextColor by animateColorAsState(
        targetValue = if (hideStandingWord)
            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.onTertiaryContainer,
        animationSpec = tween(durationMillis = 300),
        label = "textColorAnimation"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (hideStandingWord)
            SpyColorList[standingWord.hashCode() % SpyColorList.size].copy(alpha = 0.3f)
        else
            SpyColorList[standingWord.hashCode() % SpyColorList.size].copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 500),
        label = "bgColorAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth().height(60.dp)
            .shadow(10.dp, shape = CircleShape)
            .clip(CircleShape),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor) // 使用动画颜色
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentSize().padding(horizontal = 12.dp)
        ) {
            Text(
                if (hideStandingWord) "查看身份" else "隐藏身份",
                style = MaterialTheme.typography.bodyMedium,
                color = animatedTextColor, // 使用动画颜色
                modifier = Modifier.clickable {
                    hideStandingWord = !hideStandingWord
                }
            )

            Spacer(
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxHeight(0.8f).width(0.5.dp)
                    .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))
            )
            Column(
                modifier = Modifier.fillMaxHeight().weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 添加文本内容的动画过渡
                Crossfade(
                    targetState = hideStandingWord,
                    animationSpec = tween(durationMillis = 400)
                ) { hidden ->
                    Text(
                        if (hidden) "*****" else standingWord,
                        style = MaterialTheme.typography.bodyLarge,
                        color = animatedTextColor // 使用动画颜色
                    )
                }
            }
        }
    }
}
