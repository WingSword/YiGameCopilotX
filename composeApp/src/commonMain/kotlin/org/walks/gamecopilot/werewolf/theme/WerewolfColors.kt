package org.walks.gamecopilot.werewolf.theme

import androidx.compose.ui.graphics.Color

/**
 * 一夜终极狼人 - 暗夜主题配色
 *
 * 狼人游戏使用独立暗色主题，营造夜间氛围。
 * 所有颜色集中管理，避免硬编码散落在 UI 代码中。
 */
object WerewolfColors {

    // ── 页面层级 ──
    /** 页面背景（最深） */
    val background = Color(0xFF0F1116)
    /** 卡片/面板背景 */
    val surface = Color(0xFF1A1E28)
    /** 次级表面（图标背景等） */
    val surfaceAlt = Color(0xFF1A1E2E)
    /** 列表项/容器背景 */
    val surfaceContainer = Color(0xFF252A34)
    /** 高亮容器背景 */
    val surfaceContainerHigh = Color(0xFF1D2027)
    /** 最高层级表面（圆形图标背景等） */
    val surfaceContainerHighest = Color(0xFF2C3039)

    // ── 文字层级 ──
    /** 主要文字 */
    val onSurface = Color(0xFFECEFF4)
    /** 次要/辅助文字 */
    val onSurfaceVariant = Color(0xFF9EA6B3)
    /** 中等对比文字 */
    val onSurfaceMedium = Color(0xFFC0C6D2)

    // ── 强调色 ──
    /** 主强调色（金色） */
    val primary = Color(0xFFF2C72B)
    /** 主强调色上的文字 */
    val onPrimary = Color(0xFF14161B)
    /** 次强调色（橙色） */
    val secondary = Color(0xFFE67E22)

    // ── 特殊用途 ──
    /** 信息/蓝色标记 */
    val info = Color(0xFF3498DB)
    /** 危险/红色标记 */
    val danger = Color(0xFFE74C3C)
}
