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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * 主题模式：跟随系统 / 浅色 / 深色
 */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/** 全局主题模式提供者，由 App 层注入 */
val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF43E889),
    onPrimary = Color(0xFF06110A),
    primaryContainer = Color(0xFF153523),
    onPrimaryContainer = Color(0xFF91F2B8),
    secondary = Color(0xFFBE92FF),
    onSecondary = Color(0xFF160B22),
    secondaryContainer = Color(0xFF302341),
    onSecondaryContainer = Color(0xFFE3CEFF),
    tertiary = Color(0xFF4DA7FF),
    onTertiary = Color(0xFF061522),
    tertiaryContainer = Color(0xFF14303E),
    onTertiaryContainer = Color(0xFFC4E8FF),
    background = Color(0xFF050505),
    onBackground = Color(0xFFF6F6F7),
    surface = Color(0xFF1B1B1D),
    onSurface = Color(0xFFF6F6F7),
    surfaceVariant = Color(0xFF2B2B2E),
    onSurfaceVariant = Color(0xFFA5A5AA),
    outline = Color(0xFF3B3B3F),
    error = Color(0xFFFF6257),
    errorContainer = Color(0xFF4A211F),
    onError = Color(0xFF240300),
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color.Black.copy(alpha = 0.64f)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF111113),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0F8E9),
    onPrimaryContainer = Color(0xFF112D1B),
    secondary = Color(0xFF7653B5),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECE0FF),
    onSecondaryContainer = Color(0xFF2B173F),
    tertiary = Color(0xFF1769AA),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6EEFF),
    onTertiaryContainer = Color(0xFF0F3A54),
    background = Color(0xFFF3F3F7),
    onBackground = Color(0xFF0D0D10),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0D0D10),
    surfaceVariant = Color(0xFFE9E9EE),
    onSurfaceVariant = Color(0xFF66666E),
    outline = Color(0xFFD8D8DE),
    error = Color(0xFFD83A32),
    errorContainer = Color(0xFFFFE2DF),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF7A1A1A),
    scrim = Color.Black.copy(alpha = 0.42f)
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
        typography = AppTypography(),
        shapes = designSystem.cornerRadius.toMaterialShapes()
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
