package org.walks.gamecopilot.data

import androidx.compose.ui.graphics.Color
import com.yi.yigamecopilot.android.theme.MorandiBlue
import com.yi.yigamecopilot.android.theme.MorandiBrown
import com.yi.yigamecopilot.android.theme.MorandiGreen
import com.yi.yigamecopilot.android.theme.MorandiOrange
import com.yi.yigamecopilot.android.theme.MorandiPink
import com.yi.yigamecopilot.android.theme.MorandiPurple
import com.yi.yigamecopilot.android.theme.MorandiRed
import com.yi.yigamecopilot.android.theme.MorandiYellow

/**
 * Created by AI at 2025/6/6
 * 转盘选项数据类
 */
data class WheelItem(
    val id: String,
    val text: String,
    val color: Color
) {
    companion object {
        // 预设颜色
        val DEFAULT_COLORS = mutableListOf<Color>(
            MorandiBlue,
            MorandiGreen,
            MorandiPink,
            MorandiYellow,
            MorandiPurple,
            MorandiBrown,
            MorandiOrange,
            MorandiRed,
            Color(0xFF03A9F4), // 浅蓝色
            Color(0xFF673AB7), // 深紫色
            Color(0xFF3F51B5), // 靛蓝色
            Color(0xFF009688), // 蓝绿色
            Color(0xFF795548), // 棕色
            Color(0xFF607D8B), // 蓝灰色
            Color(0xFF9E9E9E),  // 灰色
            Color(0xFF687170),
            Color(0xFFAA8C8E),
            Color(0xFFBE8480),
            Color(0xFFBE8663),
            Color(0xFFC2B299),
        )
    }
}