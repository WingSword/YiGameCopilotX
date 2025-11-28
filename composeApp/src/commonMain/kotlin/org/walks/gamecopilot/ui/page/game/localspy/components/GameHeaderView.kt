package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yi.yigamecopilot.android.theme.MorandiColorList
import org.jetbrains.compose.resources.painterResource
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_info

/**
 * 游戏页面顶部导航栏组件
 * 包含返回按钮、游戏标题和词库控制按钮
 * 
 * @param onBack 返回按钮点击回调
 * @param isWordLibraryExpanded 词库区域是否展开
 * @param onToggleWordLibrary 切换词库展开状态回调
 * @param onShowWordsDialog 显示词汇弹窗回调
 */
@Composable
fun GameHeaderView(
    onBack: () -> Unit,
    isWordLibraryExpanded: Boolean,
    onToggleWordLibrary: (Boolean) -> Unit,
    onShowWordsDialog: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "🕵️ 本地卧底",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "找出隐藏的卧底，保护平民身份",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 右侧按钮组
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 词汇选择按钮 - 只在词库区域隐藏时显示
            AnimatedVisibility(
                visible = !isWordLibraryExpanded,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onToggleWordLibrary(true) }
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.icon_info),
                        contentDescription = "词汇选择",
                        tint = Color.Unspecified, // 使用原色
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "词汇选择",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 折叠控制按钮 - 只在词库区域展开时显示
            AnimatedVisibility(
                visible = isWordLibraryExpanded,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                IconButton(
                    onClick = { onToggleWordLibrary(false) },
                    modifier = Modifier.size(32.dp)
                ) {
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isWordLibraryExpanded) 0f else 180f,
                        animationSpec = tween(durationMillis = 300),
                        label = "arrow_rotation"
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "折叠词库",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}