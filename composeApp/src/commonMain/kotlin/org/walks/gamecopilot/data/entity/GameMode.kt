package org.walks.gamecopilot.data.entity

import org.jetbrains.compose.resources.DrawableResource
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_edit
import yigamecopilotx.composeapp.generated.resources.icon_moon
import yigamecopilotx.composeapp.generated.resources.icon_spy_awalong
import yigamecopilotx.composeapp.generated.resources.icon_spy_together

enum class GameMode(val title: String, val icon: DrawableResource) {
    SPY_MAIN(
        title = "谁是卧底",
        icon = Res.drawable.icon_spy_together
    ),
    SPY_AWALONG(
        title = "阿瓦隆",
        icon = Res.drawable.icon_spy_awalong
    ),
    DRAW_GUESS(
        title = "你画我猜",
        icon = Res.drawable.icon_edit
    ),
    HUNT_TOWN(
        title = "猎巫镇",
        icon = Res.drawable.icon_moon
    ),
    ONE_NIGHT_WEREWOLF(
        title = "一夜终极狼人",
        icon = Res.drawable.icon_moon
    ),
}