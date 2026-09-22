package org.walks.gamecopilot.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.walks.gamecopilot.online.CloudInvitations
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.distribution.AppDistribution
import org.walks.gamecopilot.awalong.AwalongEntrance
import org.walks.gamecopilot.awalong.AwalongGamePageOptimized
import org.walks.gamecopilot.event.NavigationEvent
import org.walks.gamecopilot.ui.page.drawguess.DrawBoardPage
import org.walks.gamecopilot.ui.page.game.localspy.LocalSpyGamePage
import org.walks.gamecopilot.ui.page.home.HomePage
import org.walks.gamecopilot.ui.page.hunttown.HuntTownPage
import org.walks.gamecopilot.ui.page.lan.JoinRoomDialog
import org.walks.gamecopilot.ui.page.lan.LANCreateRoomPage
import org.walks.gamecopilot.ui.page.lan.LANRoomDiscoveryPage
import org.walks.gamecopilot.ui.page.lan.LANRoomLobbyPage
import org.walks.gamecopilot.ui.page.multiplayer.MultiplayerPage
import org.walks.gamecopilot.ui.page.monopoly.MonopolyMoneyPage
import org.walks.gamecopilot.ui.page.random.RandomPage
import org.walks.gamecopilot.ui.page.room.RoomPage
import org.walks.gamecopilot.ui.page.setting.SettingPage
import org.walks.gamecopilot.ui.page.stats.StatsPage
import org.walks.gamecopilot.werewolf.WerewolfEntrance
import org.walks.gamecopilot.werewolf.WerewolfGamePage

@Composable
fun NavigationHost(viewmodel: MainViewmodel, navi: NavHostController) {
    val navEntries = remember { NaviRoute.entries }
    val invitation by CloudInvitations.pending.collectAsState()
    val invitationError by CloudInvitations.error.collectAsState()
    LaunchedEffect(invitation, invitationError) {
        if (AppDistribution.roomsEnabled && (invitation != null || invitationError.isNotEmpty())) {
            navi.navigate(NaviRoute.MULTIPLAYER.route) { launchSingleTop = true }
        }
    }

    NavHost(navi, startDestination = NaviRoute.HOME.route) {

        navEntries.forEach { naviEntry ->
            composable(naviEntry.route) {
                // Keep destinations registered so restored back stacks can be safely redirected.
                if (!naviEntry.available) {
                    LaunchedEffect(Unit) {
                        navi.navigate(NaviRoute.HOME.route) {
                            popUpTo(NaviRoute.HOME.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    return@composable
                }
                when (naviEntry) {
                    NaviRoute.HOME -> HomePage(viewmodel, navi)
                    NaviRoute.MULTIPLAYER -> MultiplayerPage(viewmodel, navi)
                    NaviRoute.LOCAL_SPY -> LocalSpyGamePage(viewmodel) { navi.popBackStack() }
                    NaviRoute.ROOM -> org.walks.gamecopilot.ui.page.multiplayer.CloudRoomPage { navi.popBackStack() }
                    NaviRoute.RANDOM -> RandomPage(viewmodel)
                    NaviRoute.AWALONG -> AwalongEntrance(viewmodel, navi)
                    NaviRoute.AWALONG_GAME -> AwalongGamePageOptimized(navi, viewmodel)
                    NaviRoute.HUNT_TOWN -> HuntTownPage { navi.popBackStack() }
                    NaviRoute.STATS -> StatsPage(viewmodel) { navi.popBackStack() }
                    NaviRoute.SETTING -> SettingPage(
                        viewmodel = viewmodel,
                        onOpenMonopolyLedger = { navi.navigate(NaviRoute.MONOPOLY.route) },
                        onOpenStats = { navi.navigate(NaviRoute.STATS.route) }
                    )
                    NaviRoute.MONOPOLY -> MonopolyMoneyPage(viewmodel, onCloud = { navi.navigate(NaviRoute.CLOUD_LEDGER.route) }) { navi.popBackStack() }
                    NaviRoute.CLOUD_LEDGER -> org.walks.gamecopilot.ui.page.multiplayer.LedgerCloudEntryPage(onBack = { navi.popBackStack() }) { navi.navigate(NaviRoute.ROOM.route) }
                    NaviRoute.DRAW_GUESS -> org.walks.gamecopilot.ui.page.home.DrawGuessEntrance(
                        viewmodel, navi
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
                            onBack = { navi.popBackStack() },
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
                    if (NaviRoute.entries.any { it.route == event.route && !it.available }) return@collect
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
