package org.walks.gamecopilot.ui.page.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.yi.yigamecopilot.android.theme.MorandiColorList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.PlatformHelper
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

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.background(
                shape = RoundedCornerShape(32.dp, 32.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.surface
            ).weight(1f).fillMaxWidth().padding(top = 20.dp, start = 10.dp, end = 10.dp)
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
 * 模式选择水平分页列表组件
 * @param list 要显示的数据列表，每个元素对应一个分页项
 * @param selectedPos 初始选中项的位置，默认值为0（第一项）
 * @param onItemClick 当选中项发生变化时的回调函数，返回选中项的索引位置
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModeSelectList(
    list: List<GameMode> = GameMode.entries,
    selectedPos: Int = 0,
    onItemClick: (Int) -> Unit
) {
    // 创建分页状态管理，初始页设置为选中项位置，根据列表大小自动计算页数
    val pagerState = rememberPagerState(
        initialPage = selectedPos,
        initialPageOffsetFraction = 0f,
        pageCount = { list.size }
    )
    // 创建协程作用域用于处理滚动动画
    val coroutineScope = rememberCoroutineScope()

    var isInitialized by remember { mutableStateOf(false) } // 新增初始化标记

    LaunchedEffect(key1 = pagerState.currentPage) {
        if (isInitialized)
            PlatformHelper.getInstance().vibrateMethod()
        isInitialized = true
    }

    Column(modifier = Modifier.height(180.dp).fillMaxWidth()) {
        // 水平分页容器：设置间距和边距实现卡片堆叠视觉效果
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 10.dp,     // 卡片间视觉间距
            contentPadding = PaddingValues(horizontal = 50.dp)  // 左右留白保证边缘卡片可见
        ) { page ->
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth()) {
                // 当前选中页的渲染逻辑：显示完整卡片并触发回调
                if (pagerState.currentPage == page) {
                    ModeCard(
                        title = list[page].title,
                        res = list[page].icon,
                        background = MorandiColorList[page % MorandiColorList.size] // 循环使用颜色列表
                    )
                    onItemClick(pagerState.currentPage)
                }
                // 非选中页的渲染逻辑：显示次级卡片并绑定点击滚动事件
                else {
                    Box(modifier = Modifier.clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page) // 平滑滚动到目标页
                        }
                    }) {
                        ModeCardNext(
                            title = list[page].title,
                            background = MorandiColorList[page % MorandiColorList.size]
                        )
                    }
                }
            }
        }
    }
}


