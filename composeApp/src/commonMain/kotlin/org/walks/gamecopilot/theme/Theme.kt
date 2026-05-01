package org.walks.gamecopilot.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * 主题模式：跟随系统 / 浅色 / 深色
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/** 全局主题模式提供者，由 App 层注入 */
val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB347),
    onPrimary = Color(0xFF1A1206),
    secondary = Color(0xFFA9B6CC),
    onSecondary = Color(0xFF101722),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFEAF0FA),
    surface = Color(0xFF131C2C),
    onSurface = Color(0xFFEAF0FA),
    error = Color(0xFFFF6A6A),
    errorContainer = Color(0xFF5A2525),
    onError = Color(0xFF2D0000),
    onErrorContainer = Color(0xFFFFD8D8),
    outline = Color(0xFF33415A),
    primaryContainer = Color(0xFF3E2A0F),
    onPrimaryContainer = Color(0xFFFFE2B8),
    secondaryContainer = Color(0xFF1C273A),
    onSecondaryContainer = Color(0xFFD5DEED),
    tertiary = Color(0xFF79C4FF),
    onTertiary = Color(0xFF0B1A2B),
    tertiaryContainer = Color(0xFF123554),
    onTertiaryContainer = Color(0xFFD6EDFF),
    surfaceVariant = Color(0xFF1A2434),
    onSurfaceVariant = Color(0xFFB7C4D8),
    scrim = Color.Black.copy(alpha = 0.55f)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE89B2D),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5B6678),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1D26),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1D26),
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFECEC),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF7A1A1A),
    outline = Color(0xFFD0D5DD),
    primaryContainer = Color(0xFFFFE8C8),
    onPrimaryContainer = Color(0xFF3A2400),
    secondaryContainer = Color(0xFFE8ECF2),
    onSecondaryContainer = Color(0xFF1C273A),
    tertiary = Color(0xFF2196F3),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6EDFF),
    onTertiaryContainer = Color(0xFF0B1A2B),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF5A6275),
    scrim = Color.Black.copy(alpha = 0.32f)
)

private val NoRoundShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)


@Composable
fun WeUITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    darkStatusBar: Boolean = !darkTheme,
    content: @Composable () -> Unit
) {
    val themeMode = LocalThemeMode.current
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val designSystem = remember(useDarkTheme) {
        AppDesignSystem()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NoRoundShapes
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(),
            LocalAppDesign provides designSystem
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