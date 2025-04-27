package org.walks.gamecopilot.ui.page.random

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.sharp.AddCircle
import androidx.compose.material.icons.sharp.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yi.yigamecopilot.android.theme.MorandiBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
import org.walks.gamecopilot.data.entity.RandomCardItem
import kotlin.math.ceil

/**
 *  Created by Wing at 09:47 on 2025/4/25
 *  随机组件页
 */
// 洗牌动画配置
private const val SHUFFLE_CYCLE_DURATION = 800  // 单次循环时长
private const val MAX_SHUFFLE_CYCLES = 5       // 循环次数
private val CARD_OFFSET = 64.dp               // 最大位移量
private const val MAX_ROTATION = 30f          // 最大旋转角度
private const val SCALE_FACTOR = 1.2f         // 缩放比例

// 新增动画配置参数
private const val SHUFFLE_DURATION = 1200
private val CONTRACT_OFFSET = 96.dp
private const val MIN_SCALE = 0.8f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RandomPage(viewmodel: MainViewmodel) {
    var addRandomDialogShow by remember { mutableStateOf(false) }
    val cards = remember {
        listOf(
            RandomCardItem(1, "卡片1正面", "卡片1背面"),
            RandomCardItem(2, "卡片2正面", "卡片2背面"),
            RandomCardItem(3, "卡片3正面", "卡片3背面"),
            RandomCardItem(1, "卡片1正面", "卡片1背面"),
            RandomCardItem(2, "卡片2正面", "卡片2背面"),
            RandomCardItem(3, "卡片3正面", "卡片3背面"),
            RandomCardItem(1, "卡片1正面", "卡片1背面"),
            RandomCardItem(2, "卡片2正面", "卡片2背面"),
            RandomCardItem(3, "卡片3正面", "卡片3背面")
        )
    }
    var isShuffling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxHeight()) {
        item(span = { GridItemSpan(3) }) {
            // 顶部操作栏
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChipRandom(icon = Icons.Sharp.AddCircle, {
                        PlatformHelper.getInstance().vibrateMethod()
                        addRandomDialogShow = true
                    }, "新增配置")
                }
                item {
                    AssistChipRandom(icon = Icons.Sharp.Refresh, {
                        PlatformHelper.getInstance().vibrateLongMethod()
                        isShuffling = !isShuffling
                        scope.launch {
                            delay(500L)
                            isShuffling = !isShuffling
                            delay(500L)
                            isShuffling = !isShuffling
                            delay(500L)
                            isShuffling = !isShuffling
                        }
                    }, "洗牌")
                }
                items(6) {
                    RandomModFilterChip(
                        label = "配置" + it, leadingIcon = {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Localized description",
                                Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }, onclick = {

                        }
                    )
                }
            }
        }
        items(cards) { card ->
            AnimatedShuffleCard(
                card = card,
                isShuffling = isShuffling,
                index = cards.indexOf(card),
                total = cards.size
            )
        }
    }


    AddNewRandomDialog(addRandomDialogShow, onDismiss = { addRandomDialogShow = false })
}

@Composable
fun FlippableCard(
    modifier: Modifier = Modifier,
    back: @Composable () -> Unit,
    front: @Composable () -> Unit,
    cardColor: Color = Color.White,
    cornerRadius: Dp = 8.dp,
    animationDuration: Int = 500
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation = animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(animationDuration), label = "cardRotation"
    )

    val cameraDistance = 12f * LocalDensity.current.density

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = rotation.value  // 改回X轴旋转
                this.cameraDistance = cameraDistance
            }
            .fillMaxWidth(0.33f)
            .aspectRatio(1.25f)
            .shadow(4.dp, shape = RoundedCornerShape(cornerRadius,cornerRadius,0.dp,cornerRadius ))
            .background(cardColor, shape = RoundedCornerShape(cornerRadius,cornerRadius,0.dp,cornerRadius))
            .clickable { flipped = !flipped }
    ) {
        // 正面内容（前90度可见）
        if (rotation.value <= 90f) {
            CardContent(
                rotation = rotation.value,
                isFront = true,
                content = back
            )
        }
        // 背面内容（后90度可见）
        else {
            CardContent(
                rotation = rotation.value,
                isFront = false,
                content = front
            )
        }
    }
}

@Composable
private fun CardContent(
    rotation: Float,
    isFront: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // 关键修正：保持内容正立的补偿逻辑
                rotationX = if (isFront) 0f else -(180f)
                // 优化透明度过渡
                alpha = when {
                    isFront && rotation < 90f -> 1f - (rotation / 90f)
                    !isFront && rotation > 90f -> (rotation - 90f) / 90f
                    else -> 0f
                }
            }
            .padding(10.dp)
    ) {
        content()
    }
}

@Composable
fun AssistChipRandom(icon: ImageVector, onclick: () -> Unit, text: String) {
    AssistChip(
        onClick = {
            onclick()
        },
        label = { Text(text, color = MaterialTheme.colorScheme.tertiary) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = "Localized description",
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
fun RandomModFilterChip(
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isEdit: Boolean = false,
    onclick: (Boolean) -> Unit
) {
    var selected by remember { mutableStateOf(false) }
    FilterChip(
        onClick = {
            selected = !selected
            onclick(selected)
        },
        label = {
            Text(label)
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            {
                leadingIcon?.invoke()
            }
        },
        trailingIcon = {
            if (isEdit) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Localized description",
                    Modifier.size(InputChipDefaults.AvatarSize)
                )
            }
        },
    )
}

@Composable
fun AddNewRandomDialog(isShow: Boolean, onDismiss: () -> Unit) {
    if (isShow) {
        Dialog(
            onDismissRequest = {
                onDismiss()
            }
        ) {
            Text("新增随机卡片")

        }
    }
}

@Composable
private fun AnimatedShuffleCard(
    card: RandomCardItem,
    isShuffling: Boolean,
    index: Int,
    total: Int
) {
    val density = LocalDensity.current.density

    // 生成随机动画参数（每个卡片不同）
    val (randomRotate, randomScale) = remember(index) {
        Pair(
            (-60..60).random().toFloat(), // 扩大旋转角度范围
            1 // 扩大缩放范围f
        )
    }

    // 水平位移动画（向中心聚拢）
    val offsetX by animateFloatAsState(
        targetValue = if (isShuffling) {
            // 根据网格位置计算偏移量
            val column = index % 3
            when (column) {
                2 -> -100f * density // 左侧卡片右移
                0 -> 100f * density  // 右侧卡片左移
                else -> 0f           // 中间卡片不动
            }
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "centerOffset"
    )

    // 垂直位移动画（向中间聚拢）
    val offsetY by animateFloatAsState(
        targetValue = if (isShuffling) {
            // 计算实际行数（每行3个）
            val row = index / 3
            // 计算总行数（精确算法）
            val totalRows = ceil(total.toDouble() / 3).toInt()
            // 计算精确中间点
            val middlePoint = (totalRows - 1) / 2f

            // 计算偏移量（正数向上，负数向下）
            (middlePoint - row) * 40f * density // 注意符号方向调整
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "verticalCenterOffset"
    )


    // 旋转动画
    val rotation by animateFloatAsState(
        targetValue = if (isShuffling) randomRotate else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shuffleRotation"
    )

    // 3D旋转
    val rotateX by animateFloatAsState(
        targetValue = if (isShuffling) 25f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "3dRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .graphicsLayer {
                cameraDistance = 12f * density
                translationX = offsetX
                translationY = offsetY
                rotationZ = rotation
                rotationX = rotateX
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
            }
    ) {
        FlippableCard(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(4.dp),
            back = {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
                ){
                    Text(
                        ""+card.id+"",
                        color = MaterialTheme.colorScheme.primary.copy(0.3f),
                        fontWeight = FontWeight.W900,
                        fontSize = 50.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth())
                    Text(
                        card.back,
                        color = MaterialTheme.colorScheme.onPrimary.copy(0.9F),
                                modifier = Modifier.fillMaxHeight()
                    )
                }


            },
            front = { Text(card.front, color = MorandiBlue) }
        )
    }
}