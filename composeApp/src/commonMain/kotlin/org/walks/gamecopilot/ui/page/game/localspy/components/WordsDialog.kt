package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.data.entity.LocalSpyEntity
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppEmptyState

/**
 * 词汇查看弹窗组件
 * 显示当前词库中除已使用词汇外的所有可用词汇
 * 
 * @param show 是否显示弹窗
 * @param onDismiss 关闭弹窗回调
 * @param currentWords 当前词汇映射
 * @param currentGame 当前游戏实体
 */
@Composable
fun WordsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    currentWords: Map<String, String>,
    currentGame: LocalSpyEntity
) {
    if (show) {
        AppDialog(
            title = "当前可用词汇",
            subtitle = "${currentWords.size} 个词组，已自动隐藏本局正在使用的词。",
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxHeight(0.72f)
        ) {
                    // 词汇列表 - 过滤掉当前游戏使用的词汇
                    val availableWords =
                        remember(currentWords, currentGame.gameWord, currentGame.spyWord) {
                            currentWords.entries.filterNot { (spyWord, normalWord) ->
                                // 过滤掉当前游戏使用的词汇对
                                (spyWord == currentGame.spyWord && normalWord == currentGame.gameWord) ||
                                        (spyWord == currentGame.gameWord && normalWord == currentGame.spyWord)
                            }
                        }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 如果没有可用词汇，显示提示
                        if (availableWords.isEmpty()) {
                            item {
                                AppEmptyState(
                                    title = "暂无其他可用词汇",
                                    description = "当前词库只剩本局正在使用的词组。",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            items(availableWords) { (spyWord, normalWord) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = spyWord,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = normalWord,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
        }
    }
}
