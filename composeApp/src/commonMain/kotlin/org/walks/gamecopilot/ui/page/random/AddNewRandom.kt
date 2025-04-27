package org.walks.gamecopilot.ui.page.random

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.walks.gamecopilot.data.entity.RandomCardItem

/**
 *  Created by Wing at 09:45 on 2025/4/27
 *
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddNewRandomDialog(isShow: Boolean, onDismiss: () -> Unit) {
    val cardListState = remember {
        mutableStateListOf(RandomCardItem())
    }
    if (isShow) {
        Dialog(
            onDismissRequest = {
                onDismiss()
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.heightIn(max = 600.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 4.dp, end = 4.dp, top = 12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        stickyHeader {
                            Row(horizontalArrangement = Arrangement.Center) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = "反面（第一面）",
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "正面（第二面）",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        items(cardListState) { cardState ->
                            ItemInput(cardState) {
                                cardListState.remove(cardState)
                            }
                        }

                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = "新增一项",
                            modifier = Modifier.weight(1f).clickable {
                                cardListState.add(RandomCardItem())
                            }.clip(CircleShape)
                                .padding(vertical = 4.dp, horizontal = 10.dp),
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            textAlign = TextAlign.Center,
                            text = "保存",
                            modifier = Modifier.weight(1f).clickable {
                                cardListState.add(RandomCardItem())
                            }.clip(CircleShape)
                                .padding(vertical = 4.dp, horizontal = 10.dp),
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun ItemInput(cardState: RandomCardItem, onDelete: () -> Unit) {
    var cardTextBack by remember {
        mutableStateOf(cardState.back)
    }
    var cardTextFront by remember {
        mutableStateOf(cardState.front)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            onValueChange = {
                cardState.back = it
                cardTextBack = it
            },
            value = cardTextBack,
            modifier = Modifier.weight(1f).padding(vertical = 0.dp),
            shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(
                    0.55f
                ),
                unfocusedTextColor = MaterialTheme.colorScheme.secondary.copy(
                    0.55f
                ),
                focusedBorderColor = MaterialTheme.colorScheme.secondary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.secondary,
            )
        )
        Spacer(Modifier.width(4.dp))
        OutlinedTextField(
            onValueChange = {
                cardState.front = it
                cardTextFront = it
            },
            value = cardTextFront,
            modifier = Modifier.weight(1f).padding(vertical = 0.dp),
            shape = RoundedCornerShape(8.dp, 0.dp, 8.dp, 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                    0.55f
                ),
                unfocusedTextColor = MaterialTheme.colorScheme.primary.copy(
                    0.55f
                ),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.88f),
                focusedTextColor = MaterialTheme.colorScheme.primary,
            ),
        )

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "删除",
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.clickable {
                onDelete()
            }
        )

    }
}


