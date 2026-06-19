package org.walks.gamecopilot.data.entity

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import yigamecopilotx.composeapp.generated.resources.Res
import yigamecopilotx.composeapp.generated.resources.icon_edit
import yigamecopilotx.composeapp.generated.resources.icon_moon
import yigamecopilotx.composeapp.generated.resources.icon_spy_awalong
import yigamecopilotx.composeapp.generated.resources.icon_spy_together

/**
 * 游戏模式主题色渐变对
 */
data class GradientColors(val start: Color, val end: Color)

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
    ;

    /**
     * 获取该游戏模式的主题色渐变
     */
    val gradientColors: GradientColors
        get() = when (this) {
            SPY_MAIN -> GradientColors(
                start = Color(0xFF9B7FED),
                end = Color(0xFF7C4DFF)
            )

            SPY_AWALONG -> GradientColors(
                start = Color(0xFF64B5F6),
                end = Color(0xFF2979FF)
            )

            DRAW_GUESS -> GradientColors(
                start = Color(0xFFFFB74D),
                end = Color(0xFFFF9100)
            )

            HUNT_TOWN -> GradientColors(
                start = Color(0xFFE57373),
                end = Color(0xFFC62828)
            )

            ONE_NIGHT_WEREWOLF -> GradientColors(
                start = Color(0xFF81C784),
                end = Color(0xFF2E7D32)
            )
        }
}
