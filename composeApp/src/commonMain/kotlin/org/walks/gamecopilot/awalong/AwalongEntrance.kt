package org.walks.gamecopilot.awalong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.yi.yigamecopilot.android.theme.MorandiBlue
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.navigation.NaviRoute

/**
 *  Created by Wing at 16:03 on 2025/5/19
 *  阿瓦隆游戏入口
 */

@Composable
fun AwalongEntrance(viewmodel: MainViewmodel,navi: NavHostController) {

    var currentSelect by remember { mutableStateOf(0) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        items(AwalongConfig.entries) {
            AwalongGame(
                it.title,
                "可以进行${it.playerNum}人游戏",
                isSelected = currentSelect == it.ordinal,
                onClick = {
                    currentSelect = it.ordinal
                }, goToPage = {
                    viewmodel.handleAwalongGameIntent(AwalongIntent.StartGame(it))
                    navi.navigate(NaviRoute.AWALONG.route) })
        }
    }
}

@Composable
fun AwalongGame(
    text: String,
    desc: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    goToPage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(
            brush = Brush.linearGradient(
                if (isSelected) {
                    listOf(
                        Color(0xFF9360FC),
                        Color(0xFF4104BF)
                    )
                } else {
                    listOf(
                        Color.DarkGray,
                        Color.Gray
                    )
                }

            )
        ).padding(15.dp).clickable {
            onClick()
        }
    ) {
        Box(
            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text[0].toString(),
                fontWeight = FontWeight.W900,
                fontSize = 50.sp,
                color = MorandiBlue,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(desc, color = Color.LightGray, fontSize = 12.sp)
        }
        AnimatedVisibility(isSelected, modifier = Modifier.align(Alignment.Bottom)) {
            TextButton(
                onClick = {
                    goToPage()
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9360FC),
                    contentColor = Color.White
                )
            ) {
                Text("ENTER", fontWeight = FontWeight.W500)
            }
        }


    }
}