package org.walks.gamecopilot.data.entity

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_spy_awalong
import yigamecopilotx.composeapp.generated.resources.icon_spy_more
import yigamecopilotx.composeapp.generated.resources.icon_spy_one
import yigamecopilotx.composeapp.generated.resources.icon_spy_together

/**
 *  Created by Wing at 15:30 on 2025/5/19
 *
 */
enum class GameMode(val title: String, val icon: DrawableResource) {
    SPY_ONLINE(
        title = "卧底在线版",
        icon = Res.drawable.icon_spy_together
    ),
    SPY_LOCAL(
        title = "卧底本地版",
        icon = Res.drawable.icon_spy_one,

    ),
    SPY_QUICK(
        title = "敬请期待",
        icon = Res.drawable.icon_spy_more
    ),

    SPY_AWALONG(
        title = "阿瓦隆",
        icon = Res.drawable.icon_spy_awalong
    ),
}