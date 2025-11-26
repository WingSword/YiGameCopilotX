package org.walks.gamecopilot.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.TextStyle

import com.yi.yigamecopilot.android.theme.BackgroundColorDark
import com.yi.yigamecopilot.android.theme.BackgroundColorLight
import com.yi.yigamecopilot.android.theme.BorderColorDark
import com.yi.yigamecopilot.android.theme.BorderColorLight
import com.yi.yigamecopilot.android.theme.CardColorDark
import com.yi.yigamecopilot.android.theme.CardColorLight
import com.yi.yigamecopilot.android.theme.DangerColorDark
import com.yi.yigamecopilot.android.theme.DangerColorLight
import com.yi.yigamecopilot.android.theme.FontColorDark
import com.yi.yigamecopilot.android.theme.FontColorLight
import com.yi.yigamecopilot.android.theme.FontSecondaryColorDark
import com.yi.yigamecopilot.android.theme.FontSecondaryColorLight
import com.yi.yigamecopilot.android.theme.MorandiBrown
import com.yi.yigamecopilot.android.theme.OnBackgroundColorDark
import com.yi.yigamecopilot.android.theme.OnBackgroundColorLight
import com.yi.yigamecopilot.android.theme.PrimaryColor

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.Black, // 确保主色上的文字清晰可见
    secondary = MorandiBrown,
    onSecondary = Color.Black, // 次要颜色上的文字使用黑色确保对比度
    background = BackgroundColorDark,
    onBackground = FontColorDark, // 背景上的文字使用深色主题文字色
    surface = Color(0xFF1E1E1E), // 使用稍微亮一点的表面色而不是纯黑
    onSurface = FontColorDark, // 表面上的文字使用深色主题文字色
    error = DangerColorDark,
    errorContainer = Color(0xFF3D1A1A), // 错误容器使用深红色调
    onError = Color.White, // 错误色上的文字使用白色
    onErrorContainer = Color(0xFFFFB4B4), // 错误容器上的文字使用浅红色
    outline = BorderColorDark,
    primaryContainer = CardColorDark,
    onPrimaryContainer = Color.Black, // 主容器上的文字使用黑色
    secondaryContainer = MorandiBrown,
    onSecondaryContainer = Color.Black, // 次容器上的文字使用黑色
    surfaceVariant = Color(0xFF2A2A2A), // 表面变体色
    onSurfaceVariant = FontSecondaryColorDark, // 表面变体上的文字
    scrim = Color.Black.copy(alpha = 0.5f) // 遮罩色
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White, // 主色上的文字使用白色确保对比度
    secondary = Color(0xFF6B7280), // 使用更合适的灰色作为次要色
    onSecondary = Color.White, // 次要颜色上的文字使用白色
    background = BackgroundColorLight,
    onBackground = Color(0xFF1C1C1C), // 背景上的文字使用深色确保可读性
    surface = Color.White,
    onSurface = Color(0xFF1C1C1C), // 表面上的文字使用深色
    error = DangerColorLight,
    errorContainer = Color(0xFFFFF3F3), // 错误容器使用浅红色调
    onError = Color.White, // 错误色上的文字使用白色
    onErrorContainer = Color(0xFF7F1D1D), // 错误容器上的文字使用深红色
    outline = BorderColorLight,
    primaryContainer = CardColorLight,
    onPrimaryContainer = Color.White, // 主容器上的文字使用白色
    secondaryContainer = Color(0xFFF6B550),
    onSecondaryContainer = Color(0xFF1C1C1C), // 次容器上的文字使用深色
    surfaceVariant = Color(0xFFF5F5F5), // 表面变体色
    onSurfaceVariant = Color(0xFF6B7280), // 表面变体上的文字
    scrim = Color.Black.copy(alpha = 0.5f) // 遮罩色
)


@Composable
fun WeUITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    darkStatusBar: Boolean = !darkTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = Color.Transparent.toArgb()
//            WindowCompat.setDecorFitsSystemWindows(window, false)
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
//                darkStatusBar
//        }
//    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,

    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(

            )
        ) {
            Box(modifier = Modifier.navigationBarsPadding()) {
                content()
            }
        }
    }
}


/**
 * 各种颜色及其含义：
 *
 * - primary: 主色，在应用的各个屏幕和组件中最常出现的颜色。
 * - onPrimary: 主色的文本和图标颜色。
 * - primaryContainer: 容器的首选色调。
 * - onPrimaryContainer: 在 primaryContainer 上显示的内容的颜色。
 * - inversePrimary: 主色的反色，在需要反色方案的地方使用，例如 SnackBar 上的按钮。
 * - secondary: 次要颜色，用于强调和区分产品的不同部分。
 * - onSecondary: 次要颜色上显示的文本和图标的颜色。
 * - secondaryContainer: 容器的次要色调。
 * - onSecondaryContainer: 在 secondaryContainer 上显示的内容的颜色。
 * - tertiary: 第三颜色，用于平衡主要和次要颜色，或者突出显示元素，例如输入字段。
 * - onTertiary: 第三颜色上显示的文本和图标的颜色。
 * - tertiaryContainer: 容器的第三色调。
 * - onTertiaryContainer: 在 tertiaryContainer 上显示的内容的颜色。
 * - background: 可滚动内容背后的背景颜色。
 * - onBackground: 在背景颜色上显示的文本和图标的颜色。
 * - surface: 影响组件表面的颜色，例如卡片、表单和菜单。
 * - onSurface: 在 surface 上显示的文本和图标的颜色。
 * - surfaceVariant: 用于装饰元素边界的轻微颜色，当不需要强烈对比度时使用。
 * - inverseSurface: 与 surface 形成鲜明对比的颜色，用于位于其他 surface 颜色上的表面。
 * - inverseOnSurface: 与 inverseSurface 形成良好对比的颜色，用于位于 inverseSurface 上的容器上的内容。
 * - error: 错误颜色，用于指示组件中的错误，例如文本字段中的无效文本。
 * - onError: 在错误颜色上显示的文本和图标的颜色。
 * - errorContainer: 错误容器的首选色调。
 * - onErrorContainer: 在 errorContainer 上显示的内容的颜色。
 * - outline: 用于边界的微妙颜色。轮廓颜色角色为增加对比度，以提高可访问性。
 * - outlineVariant: 用于装饰元素边界的实用颜色，当不需要强烈对比度时使用。
 * - scrim: 用于遮挡内容的薄纱的颜色。
 * - surfaceBright: 始终比 surface 亮的 surface 变体，无论是在亮色模式还是暗色模式下。
 * - surfaceDim: 始终比 surface 暗的 surface 变体，无论是在亮色模式还是暗色模式下。
 * - surfaceContainer: 影响组件容器的 surface 变体，例如卡片、表单和菜单。
 * - surfaceContainerHigh: 比 surfaceContainer 更强调的容器的 surface 变体。用于需要比 surfaceContainer 更强调的内容。
 * - surfaceContainerHighest: 比 surfaceContainerHigh 更强调的容器的 surface 变体。用于需要比 surfaceContainerHigh 更强调的内容。
 * - surfaceContainerLow: 比 surfaceContainer 更低调的容器的 surface 变体。用于需要比 surfaceContainer 更低调的内容。
 * - surfaceContainerLowest: 比 surfaceContainerLow 更低调的容器的 surface 变体。用于需要比 surfaceContainerLow 更低调的内容。
 */