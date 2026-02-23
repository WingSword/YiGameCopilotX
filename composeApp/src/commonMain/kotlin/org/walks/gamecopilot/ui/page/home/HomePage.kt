package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AwalongEntrance
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.intent.GameIntent

@Composable
fun HomePage(viewmodel: MainViewmodel,navi:NavHostController) {
    val gameMode = viewmodel.startedGameMode.collectAsState()

    Column {
        ModeSelectList(selectedPos = gameMode.value) { position ->
            viewmodel.handleGameIntent(GameIntent.SwitchGameMode(position))
        }

        Column(
            modifier = Modifier.background(
                shape = RoundedCornerShape(32.dp, 32.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.surface
            ).weight(1f).fillMaxWidth()
                .padding(top = 10.dp, start = 10.dp, end = 10.dp, bottom = 66.dp)
        ) {
            AnimatedVisibility(gameMode.value == 0) {
                RoomEntranceCard(viewmodel, navi)
            }

            AnimatedVisibility(gameMode.value == 1) {
                AwalongEntrance(viewmodel=viewmodel,navi = navi)
            }

            AnimatedVisibility(gameMode.value == 2) {
                DrawGuessEntrance(navController = navi)
            }
        }
    }
}


@Composable
fun ModeSelectList(
    list: List<GameMode> = GameMode.entries,
    selectedPos: Int = 0,
    onItemClick: (Int) -> Unit
) {
    val itemsPerRow = 4

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val rowCount = (list.size + itemsPerRow - 1) / itemsPerRow

        for (row in 0 until rowCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until itemsPerRow) {
                    val index = row * itemsPerRow + col
                    if (index < list.size) {
                        ModeGridItem(
                            gameMode = list[index],
                            isSelected = index == selectedPos,
                            onClick = { onItemClick(index) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ModeGridItem(
    gameMode: GameMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 3.dp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(gameMode.icon),
                contentDescription = gameMode.title,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = gameMode.title,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}


