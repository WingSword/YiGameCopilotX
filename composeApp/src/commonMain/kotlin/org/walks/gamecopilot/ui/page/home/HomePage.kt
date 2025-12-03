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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AwalongEntrance
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.intent.GameIntent
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_spy_one

/**
 * 首页主界面组件，包含游戏模式选择和对应模式的内容展示
 * @param viewmodel 主视图模型，用于处理业务逻辑和状态管理
 *     - 提供游戏模式切换的状态管理
 *     - 处理本地游戏相关操作指令
 */
@Composable
fun HomePage(viewmodel: MainViewmodel,navi:NavHostController) {
    // 支持的三种游戏模式配置列表
    // 从ViewModel收集当前选择的游戏模式状态（转换为Compose可观察状态）
    val gameMode = viewmodel.startedGameMode.collectAsState()

    Column {
        /* 游戏模式选择列表组件
         * @param gameModeList 可用模式列表
         * @param selectedMode 当前选中模式索引
         * @param onSelect 模式切换回调，通过ViewModel处理游戏意图
         */
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
            /* 根据选中模式显示对应内容区域 */
            // 模式0：显示在线房间入口卡片
            AnimatedVisibility(gameMode.value == 0) {
                RoomEntranceCard(viewmodel)
            }

            // 模式1：显示本地卧底游戏入口
            AnimatedVisibility(gameMode.value == 1) {
                LocalSpyGameEntrance(onClick = {
                    // 导航到本地卧底游戏页面
                    navi.navigate("localSpy")
                })
            }

            // 快速游戏
            AnimatedVisibility(gameMode.value == 2) {
                QuickSetting()
            }

            AnimatedVisibility(gameMode.value == 3) {
                AwalongEntrance(viewmodel=viewmodel,navi = navi)
            }
        }


    }
}

/**
 * 本地卧底游戏入口组件
 * @param onClick 点击回调函数
 */
@Composable
fun LocalSpyGameEntrance(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "本地卧底游戏",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击开始游戏",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
fun ModeCard(
    title: String,
    res: DrawableResource = Res.drawable.icon_spy_one,
    label: String? = null,
    isSelected: Boolean = false,
    background: Color = Color(0xFFF6B550)
) {
    Box(
        modifier = Modifier.clip(shape = RoundedCornerShape(32.dp)).fillMaxSize()
            .background(color = background)
            .border(
                width = 8.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(res),
                contentDescription = "",
                alpha = 0.75f,
                contentScale = ContentScale.Fit
            )
        }
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                modifier = Modifier.fillMaxHeight(0.6f).fillMaxWidth(0.7f),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                fontSize = 40.sp,
                fontWeight = FontWeight.W900,
                maxLines = 2,
                textAlign = TextAlign.End
            )
        }

    }
}

/**
 * 模式选择卡片组件（双文字垂直布局版本）
 *
 * 实现带有圆角背景和动态边框的卡片布局，文字内容会垂直排列在左右两侧
 *
 * @param title 需要显示的文本内容，每个字符会被拆分成单独行（注意：非单词拆分）
 * @param isSelected 当前卡片是否被选中，控制边框颜色变化 [默认值：false]
 * @param background 卡片背景颜色 [默认值：#F6B550]
 */
@Composable
fun ModeCardNext(
    title: String,
    isSelected: Boolean = false,
    background: Color = Color(0xFFF6B550)
) {
    // 主容器布局：包含圆角裁剪、动态边框和内部间距
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(32.dp))
            .fillMaxSize()
            .background(color = background)
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else background,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧垂直文字：每个字符独立成行
        Text(
            text = title.map { "$it" }.joinToString("\n"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        // 中间弹性间隔：撑开左右文字布局
        Spacer(Modifier.weight(1f))

        // 右侧垂直文字：与左侧保持对称布局
        Text(
            text = title.map { "$it" }.joinToString("\n"),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}


/**
 * 游戏模式选择网格布局组件
 * 上面圆形图标，下面名称的列表布局，每个游戏有更大的配置空间
 * @param list 要显示的游戏模式列表
 * @param selectedPos 当前选中项的索引
 * @param onItemClick 点击回调函数，返回选中项的索引
 */
@Composable
fun ModeSelectList(
    list: List<GameMode> = GameMode.entries,
    selectedPos: Int = 0,
    onItemClick: (Int) -> Unit
) {
    // 创建响应式布局：根据屏幕宽度计算每行项目数
    val itemsPerRow = 4 // 每行显示4个项目

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 计算行数
        val rowCount = (list.size + itemsPerRow - 1) / itemsPerRow

        // 按行渲染游戏模式
        for (row in 0 until rowCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 渲染当前行的每个项目
                for (col in 0 until itemsPerRow) {
                    val index = row * itemsPerRow + col
                    if (index < list.size) {
                        ModeGridItem(
                            gameMode = list[index],
                            isSelected = index == selectedPos,
                            onClick = { onItemClick(index) }
                        )
                    } else {
                        // 空白占位符
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 游戏模式网格项组件
 * 上面圆形图标，下面名称的布局
 * @param gameMode 游戏模式数据
 * @param isSelected 是否选中
 * @param onClick 点击回调
 */
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
        // 圆形图标容器
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
            // 游戏图标
            Image(
                painter = painterResource(gameMode.icon),
                contentDescription = gameMode.title,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 游戏名称
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


