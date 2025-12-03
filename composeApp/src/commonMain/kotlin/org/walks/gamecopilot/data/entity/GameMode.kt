package org.walks.gamecopilot.data.entity

import org.jetbrains.compose.resources.DrawableResource
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_spy_awalong
import yigamecopilotx.composeapp.generated.resources.icon_spy_together

/**
 *  Created by Wing at 15:30 on 2025/5/19
 *
 */
enum class GameMode(val title: String, val icon: DrawableResource) {
    SPY_MAIN(
        title = "谁是卧底",
        icon = Res.drawable.icon_spy_together
    ),
    SPY_AWALONG(
        title = "阿瓦隆",
        icon = Res.drawable.icon_spy_awalong
    ),
}