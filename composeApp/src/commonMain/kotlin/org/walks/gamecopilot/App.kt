package org.walks.gamecopilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yi.yigamecopilot.android.theme.MorandiColorList
import kotlinx.coroutines.flow.collectLatest
import org.walks.gamecopilot.theme.WeUITheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.intent.GameIntent
import org.walks.gamecopilot.ui.badge.WeBadge
import org.walks.gamecopilot.ui.button.ButtonType
import org.walks.gamecopilot.ui.button.CircleButton
import org.walks.gamecopilot.ui.button.CommonButton
import org.walks.gamecopilot.ui.button.WeButton
import org.walks.gamecopilot.ui.page.home.ModeSelectList
import org.walks.gamecopilot.ui.page.home.StartPage
import org.walks.gamecopilot.ui.input.CommonTextField
import org.walks.gamecopilot.ui.input.HalfRadioTextField
import org.walks.gamecopilot.ui.page.room.RoomPage
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

@Composable
@Preview
fun App() {
    val viewModelFactory = viewModelFactory { initializer { MainViewmodel() } }
    val extras: CreationExtras = MutableCreationExtras()
    val viewModel = viewModelFactory.create(MainViewmodel::class, extras)

    WeUITheme {
        Surface(
            modifier = Modifier.fillMaxSize().background(Color.White),
            color = Color.White
        ) {
            AppView(viewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppView(viewmodel: MainViewmodel) {
    val snackState = remember { mutableStateOf(SnackbarHostState()) }
    val playerNum = viewmodel.roomEntityState.collectAsState().value.playerNum
    val navi = rememberNavController()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "卧底游戏",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        AnimatedVisibility(playerNum > 0) {
                            Text(
                                "当前房间人数: ${playerNum}人",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        AnimatedVisibility(viewmodel.topTipState.value.isNotBlank()) {
                            Text(
                                viewmodel.topTipState.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape
                            ),
                        onClick = {
                            if(navi.currentBackStack.value.isEmpty()){

                            }else{
                                navi.popBackStack()
                                if(navi.currentDestination?.route == "start"){
                                    viewmodel.handleIntent(GameIntent.LeaveGameRoom)
                                }
                            }

                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    ) {
                        Icon(
                            imageVector = if(navi.currentBackStack.value.isEmpty()) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back button"
                        )
                    }

                },
                modifier = Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackState.value)
        },
    ) { inp ->
        Column(
            modifier = Modifier
                .padding(inp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NavigationHost(viewmodel, navi)
        }
    }
}

@Composable
fun NavigationHost(viewmodel: MainViewmodel, navi: NavHostController) {
    LaunchedEffect(viewmodel.roomEntityState) {
        viewmodel.roomEntityState.collectLatest { roomState ->
            if (navi.currentDestination?.route == "start" && roomState.roomFinished) {
                navi.navigate("room")
            }
        }
    }


    NavHost(navi, startDestination = "start") {
        composable("start") {
            StartPage(viewmodel)
        }
        composable("room") {
            RoomPage(viewmodel)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GreetingView(spyNum: Int, gameWord: String) {
    var greetingTotalPlayer by remember { mutableIntStateOf(0) }
    var buttonText by remember { mutableStateOf("选择游玩人数") }
    var greetingPlayerNumber by remember { mutableIntStateOf(1) }
    var buttonClickTime = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    var currentSelect = remember { mutableIntStateOf(0) }
    var currentPlayTime by remember { mutableIntStateOf(0) }
    Spacer(Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("选择查看编号", modifier = Modifier.padding(16.dp), color = Color.Black)
        LazyVerticalGrid(
            GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(greetingTotalPlayer) { pos ->
                Box(modifier = Modifier.padding(vertical = 16.dp)) {
                    WeButton(
                        text = (pos + 1).toString(),
                        type = if (buttonClickTime[pos] == 0) ButtonType.PRIMARY else ButtonType.PLAIN
                    ) {
                        currentSelect.value = 0
                        greetingPlayerNumber = pos + 1
                        buttonText = greetingPlayerNumber.toString()
                    }
                    if (buttonClickTime[pos] > 1) {
                        WeBadge("查看" + buttonClickTime[pos] + "次", size = 14.dp)
                    }

                }

            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(160.dp)
                .width(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 4.dp,
                    color = MorandiColorList[currentPlayTime % 8],
                    shape = RoundedCornerShape(20.dp)
                )
                .background(if (currentSelect.value == 0) Color.LightGray else Color.White)
                .shadow(1.dp)
                .combinedClickable(onLongClick = {
                    buttonClickTime[greetingPlayerNumber - 1] =
                        buttonClickTime[greetingPlayerNumber - 1] + 1
                    currentSelect.value = 1
                }) {
                    currentSelect.value = 0
                },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    if (currentSelect.value == 0) "$greetingPlayerNumber 号\n长按查看身份词"
                    else if (spyNum == greetingPlayerNumber) "你是卧底"
                    else gameWord,
                    textAlign = TextAlign.Center,
                    color = if (currentSelect.value == 0) MaterialTheme.colorScheme.secondary
                    else if (spyNum == greetingPlayerNumber) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "再次点击关闭身份牌",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    WeUITheme {
        GreetingView(1, "")
    }
}
