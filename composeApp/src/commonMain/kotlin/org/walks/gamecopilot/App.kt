package org.walks.gamecopilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.walks.gamecopilot.intent.GameRoomIntent
import org.walks.gamecopilot.theme.WeUITheme
import org.walks.gamecopilot.ui.page.home.HomePage
import org.walks.gamecopilot.ui.page.room.RoomPage


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
    val roomTitle = viewmodel.roomEntityState.collectAsState().value.roomId
    val navi = rememberNavController()
    navi.addOnDestinationChangedListener { _, destination, _ ->
        val route = destination.route
        // 根据route进行相关操作，如记录日志或更新UI
        when (route) {

        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (navi.currentDestination?.route == "room") roomTitle else "卧底游戏",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        AnimatedVisibility(playerNum > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "当前房间人数: ${playerNum}人",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                Icon(
                                    Icons.Filled.Refresh,
                                    "刷新房间人数",
                                    modifier = Modifier.clickable {
                                        viewmodel.handleRoomIntent(GameRoomIntent.RefreshRoomInfo)
                                    },
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            }

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
                            if (!isStartRoute(navi)) {
                                try {
                                    navi.popBackStack()
                                } catch (e: Exception) {
                                    // 处理 popBackStack 异常，例如记录日志或提示用户
                                    println("Error popping back stack: ${e.message}")
                                }
                                if (navi.currentBackStackEntry?.destination?.route == "start") {
                                    viewmodel.handleRoomIntent(GameRoomIntent.LeaveGameRoom)
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    ) {
                        if (!isStartRoute(navi)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "back button"
                            )
                        }
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

// 提取公共逻辑到辅助函数
private fun isStartRoute(navi: NavHostController): Boolean {
    return navi.currentDestination?.route == "start" || navi.currentDestination?.route == null
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
            HomePage(viewmodel)
        }
        composable("room") {
            RoomPage(viewmodel)
        }
    }
}


@Preview
@Composable
fun DefaultPreview() {
    WeUITheme {
        //GreetingView(1, "",4)
    }
}
