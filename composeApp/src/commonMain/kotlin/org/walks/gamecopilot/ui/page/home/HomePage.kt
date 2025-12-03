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
            // 模式0：显示谁是卧底入口（包含在线版和本地版）
            AnimatedVisibility(gameMode.value == 0) {
                RoomEntranceCard(viewmodel, navi)
            }

            // 模式1：显示阿瓦隆入口
            AnimatedVisibility(gameMode.value == 1) {
                AwalongEntrance(viewmodel=viewmodel,navi = navi)
            }
        }


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


