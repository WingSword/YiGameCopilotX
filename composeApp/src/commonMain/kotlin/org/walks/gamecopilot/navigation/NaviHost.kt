package org.walks.gamecopilot.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.awalong.AwalongEntrance
import org.walks.gamecopilot.awalong.AwalongGamePageOptimized
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.ui.page.drawguess.DrawBoardPage
import org.walks.gamecopilot.ui.page.game.localspy.LocalSpyGamePage
import org.walks.gamecopilot.ui.page.home.HomePage
import org.walks.gamecopilot.ui.page.hunttown.HuntTownPage
import org.walks.gamecopilot.ui.page.lan.JoinRoomDialog
import org.walks.gamecopilot.ui.page.lan.LANCreateRoomPage
import org.walks.gamecopilot.ui.page.lan.LANRoomDiscoveryPage
import org.walks.gamecopilot.ui.page.lan.LANRoomLobbyPage
import org.walks.gamecopilot.ui.page.multiplayer.MultiplayerPage
import org.walks.gamecopilot.ui.page.random.RandomPage
import org.walks.gamecopilot.ui.page.room.RoomPage
import org.walks.gamecopilot.ui.page.setting.SettingPage
import org.walks.gamecopilot.ui.page.stats.StatsPage
import org.walks.gamecopilot.werewolf.WerewolfEntrance
import org.walks.gamecopilot.werewolf.WerewolfGamePage

@Composable
fun NavigationHost(viewmodel: MainViewmodel, navi: NavHostController) {
    val navEntries = remember { NaviRoute.entries }

    NavHost(navi, startDestination = NaviRoute.HOME.route) {

        navEntries.forEach { naviEntry ->
            composable(naviEntry.route) {
                when (naviEntry) {
                    NaviRoute.HOME -> HomePage(viewmodel, navi)
                    NaviRoute.MULTIPLAYER -> MultiplayerPage(viewmodel, navi)
                    NaviRoute.LOCAL_SPY -> LocalSpyGamePage(viewmodel) { navi.popBackStack() }
                    NaviRoute.ROOM -> RoomPage(viewmodel)
                    NaviRoute.RANDOM -> RandomPage(viewmodel)
                    NaviRoute.AWALONG -> AwalongEntrance(viewmodel, navi)
                    NaviRoute.AWALONG_GAME -> AwalongGamePageOptimized(navi, viewmodel)
                    NaviRoute.HUNT_TOWN -> HuntTownPage { navi.popBackStack() }
                    NaviRoute.STATS -> StatsPage(viewmodel)
                    NaviRoute.SETTING -> SettingPage(viewmodel)
                    NaviRoute.DRAW_GUESS -> org.walks.gamecopilot.ui.page.home.DrawGuessEntrance(
                        navi
                    )

                    NaviRoute.DRAW_BOARD -> DrawBoardPage(viewmodel) { navi.popBackStack() }
                    NaviRoute.ONE_NIGHT_WEREWOLF -> WerewolfEntrance(viewmodel, navi)
                    NaviRoute.ONE_NIGHT_WEREWOLF_GAME -> WerewolfGamePage(viewmodel) { navi.popBackStack() }
                    NaviRoute.LAN_DISCOVERY -> {
                        var selectedRoom by remember {
                            mutableStateOf<org.walks.gamecopilot.lan.data.LANRoomInfo?>(
                                null
                            )
                        }
                        LANRoomDiscoveryPage(
                            onRoomSelected = { room -> selectedRoom = room },
                            onCreateRoom = { navi.navigate(NaviRoute.LAN_CREATE_ROOM.route) },
                            viewModel = viewmodel
                        )
                        selectedRoom?.let { room ->
                            JoinRoomDialog(
                                roomInfo = room,
                                onDismiss = { selectedRoom = null },
                                onJoinSuccess = {
                                    selectedRoom = null
                                    navi.navigate(NaviRoute.LAN_LOBBY.route)
                                },
                                viewModel = viewmodel
                            )
                        }
                    }

                    NaviRoute.LAN_CREATE_ROOM -> LANCreateRoomPage(
                        onRoomCreated = { navi.navigate(NaviRoute.LAN_LOBBY.route) },
                        onCancel = { navi.popBackStack() },
                        viewModel = viewmodel
                    )

                    NaviRoute.LAN_LOBBY -> LANRoomLobbyPage(
                        onStartGame = {
                            when (viewmodel.lanState.value.currentRoom?.roomInfo?.gameType) {
                                GameType.LOCAL_SPY -> navi.navigate(NaviRoute.LOCAL_SPY.route)
                                GameType.AWALONG -> navi.navigate(NaviRoute.AWALONG_GAME.route)
                                GameType.DRAW_GUESS -> navi.navigate(NaviRoute.DRAW_GUESS.route)
                                GameType.RANDOM_TOOLS -> navi.navigate(NaviRoute.RANDOM.route)
                                GameType.ONE_NIGHT_WEREWOLF -> navi.navigate(NaviRoute.ONE_NIGHT_WEREWOLF_GAME.route)
                                GameType.HUNT_TOWN,
                                GameType.MONOPOLY,
                                GameType.ALL,
                                null -> Unit
                            }
                        },
                        onLeaveRoom = { navi.popBackStack() },
                        viewModel = viewmodel
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewmodel.navigationEvents.collect { event ->

            when (event) {
                is NavigationEvent.NavigateTo -> {
                    navi.navigate(event.route) {
                        event.popUpToRoute?.let { route ->
                            popUpTo(route) { inclusive = event.inclusive }
                        }
                    }
                }

                NavigationEvent.PopBackStack -> navi.popBackStack()
                is NavigationEvent.PopUpTo -> navi.popBackStack(event.route, event.inclusive)
            }

        }
    }

}
