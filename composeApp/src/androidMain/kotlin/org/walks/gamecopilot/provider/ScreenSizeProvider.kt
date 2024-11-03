package org.walks.gamecopilot.provider// androidMain 模块
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.Composable

actual object ScreenSizeProvider {
    actual val screenWidthDp: Dp
        @Composable
        get() = with(LocalConfiguration.current) {
            screenWidthDp.dp
        }
}
