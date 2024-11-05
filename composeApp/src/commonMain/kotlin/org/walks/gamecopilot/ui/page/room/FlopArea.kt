package org.walks.gamecopilot.ui.page.room


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.ui.button.CommonButton

@Composable
fun FlopArea() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth()) {
        StandingCard("ba")
    }
}

@Composable
fun StandingCard(standingWord: String) {
    var hideStandingWord by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp)
            .shadow(10.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row {
            Box(
                Modifier.width(100.dp).fillMaxHeight().background(
                    color = Color(0xFF8EC3CB),
                    shape = RoundedCornerShape(32, 0, 0, 32)
                ).clickable {
                    hideStandingWord = false
                },
                contentAlignment = Alignment.Center
            ){
                androidx.compose.animation.AnimatedVisibility(!hideStandingWord){
                    Column {
                        CommonButton("隐藏"){
                            hideStandingWord = true
                        }
                        Spacer(Modifier.height(8.dp))
                        CommonButton("技能")
                    }
                }
                androidx.compose.animation.AnimatedVisibility(hideStandingWord){
                    Text("查看身份")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxHeight().padding(16.dp), verticalArrangement = Arrangement.Center ){
                Text(
                    if (hideStandingWord) "*****" else standingWord,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
