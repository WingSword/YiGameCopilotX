package org.walks.gamecopilot.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 应用统一设计系统
 * 
 * 该文件定义了整个应用的设计规范，包括：
 * - 颜色系统：主色、功能色、语义色等
 * - 间距系统：统一的边距和间距规范
 * - 圆角系统：统一的圆角尺寸
 * - 阴影系统：统一的阴影层级
 * - 字体系统：统一的字体大小和行高
 * 
 * 使用方式：
 * - 在Composable中使用 LocalAppDesign.current 获取设计规范
 * - 所有UI组件应遵循此设计系统
 */

/**
 * 应用颜色系统
 * 定义应用中使用的所有颜色，支持亮色和暗色模式
 */
data class AppColors(
    val primary: Color = Color(0xFF1FCB8A),
    val primaryVariant: Color = Color(0xFF12A36D),
    val primaryLight: Color = Color(0xFFE9FBF4),
    
    val secondary: Color = Color(0xFF5B6678),
    val secondaryVariant: Color = Color(0xFF3B4554),
    
    val accent: Color = Color(0xFF2F74FF),
    val accentVariant: Color = Color(0xFF245FDE),
    
    val success: Color = Color(0xFF10B981),
    val successLight: Color = Color(0xFFD1FAE5),
    
    val warning: Color = Color(0xFFFFB02E),
    val warningLight: Color = Color(0xFFFEF3C7),
    
    val error: Color = Color(0xFFEF4444),
    val errorLight: Color = Color(0xFFFEE2E2),
    
    val info: Color = Color(0xFF4A8DFF),
    val infoLight: Color = Color(0xFFE7F0FF),
    
    val blueTeam: Color = Color(0xFF3B82F6),
    val blueTeamLight: Color = Color(0xFFDBEAFE),
    
    val redTeam: Color = Color(0xFFEF4444),
    val redTeamLight: Color = Color(0xFFFEE2E2),
    
    val neutralTeam: Color = Color(0xFF6B7280),
    val neutralTeamLight: Color = Color(0xFFF3F4F6),
    
    val morandiBlue: Color = Color(0xFFBCEAD6),
    val morandiGreen: Color = Color(0xFFD4E1BC),
    val morandiPink: Color = Color(0xFFF6DCDC),
    val morandiYellow: Color = Color(0xFFFDEBD0),
    val morandiPurple: Color = Color(0xFFDAD0DD),
    val morandiBrown: Color = Color(0xFFF2E9D4),
    val morandiOrange: Color = Color(0xFFF8D5B9),
    val morandiRed: Color = Color(0xFFF6D7D7),
    
    val cangSe: Color = Color(0xFF75878A),
    val yinBai: Color = Color(0xFFE9E7EF),
    val yueBai: Color = Color(0xFFD6ECF0),
    val qiHei: Color = Color(0xFF161823),
    val moSe: Color = Color(0xFF50616D),
    val anSe: Color = Color(0xFF41555D),
    val zhuQing: Color = Color(0xFF789262),
    val aiLv: Color = Color(0xFFA4E2C6),
    val chi: Color = Color(0xFFC3272B),
    val kuHuang: Color = Color(0xFFD3B17D),
    val wuJin: Color = Color(0xFFA78E44),
)

/**
 * 应用间距系统
 * 基于4dp网格的间距规范
 */
data class AppSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 12.dp,
    val xl: Dp = 16.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 48.dp,
    
    val cardPadding: Dp = 16.dp,
    val screenPadding: Dp = 16.dp,
    val listItemPadding: Dp = 12.dp,
    val buttonPadding: Dp = 16.dp,
    val inputPadding: Dp = 12.dp,
)

/**
 * 应用圆角系统
 * 统一的圆角尺寸规范
 */
data class AppCornerRadius(
    val none: Dp = 0.dp,
    val xs: Dp = 0.dp,
    val sm: Dp = 0.dp,
    val md: Dp = 0.dp,
    val lg: Dp = 0.dp,
    val xl: Dp = 0.dp,
    val xxl: Dp = 0.dp,
    val xxxl: Dp = 0.dp,
    val full: Dp = 9999.dp,
    
    val button: Dp = 0.dp,
    val card: Dp = 0.dp,
    val dialog: Dp = 0.dp,
    val input: Dp = 0.dp,
    val badge: Dp = 0.dp,
    val iconButton: Dp = 9999.dp,
)

/**
 * 应用阴影系统
 * 统一的阴影层级规范
 */
data class AppElevation(
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 14.dp,
    val xl: Dp = 22.dp,
    
    val card: Dp = 6.dp,
    val button: Dp = 4.dp,
    val dialog: Dp = 16.dp,
    val dropdown: Dp = 10.dp,
    val appBar: Dp = 6.dp,
)

/**
 * 应用字体大小系统
 * 统一的字体大小规范
 */
data class AppFontSize(
    val xs: Dp = 10.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 14.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 18.dp,
    val xxl: Dp = 20.dp,
    val xxxl: Dp = 24.dp,
    val huge: Dp = 28.dp,
    val giant: Dp = 32.dp,
    
    val caption: Dp = 12.dp,
    val body: Dp = 14.dp,
    val bodyLarge: Dp = 16.dp,
    val subtitle: Dp = 18.dp,
    val title: Dp = 20.dp,
    val headline: Dp = 24.dp,
    val display: Dp = 28.dp,
)

/**
 * 应用图标大小系统
 * 统一的图标尺寸规范
 */
data class AppIconSize(
    val xs: Dp = 12.dp,
    val sm: Dp = 16.dp,
    val md: Dp = 20.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 40.dp,
    val xxxl: Dp = 48.dp,
    
    val navigation: Dp = 24.dp,
    val button: Dp = 20.dp,
    val listItem: Dp = 24.dp,
    val avatar: Dp = 40.dp,
    val largeAvatar: Dp = 64.dp,
)

/**
 * 完整的应用设计系统
 */
data class AppDesignSystem(
    val colors: AppColors = AppColors(),
    val spacing: AppSpacing = AppSpacing(),
    val cornerRadius: AppCornerRadius = AppCornerRadius(),
    val elevation: AppElevation = AppElevation(),
    val fontSize: AppFontSize = AppFontSize(),
    val iconSize: AppIconSize = AppIconSize(),
)

/**
 * Composable本地设计系统提供者
 * 用于在Composable树中传递设计规范
 */
val LocalAppDesign = staticCompositionLocalOf { AppDesignSystem() }

/**
 * 获取圆角Shape的扩展函数
 */
fun AppCornerRadius.shape() = RoundedCornerShape(this.button)

/**
 * 获取卡片圆角Shape的扩展函数
 */
fun AppCornerRadius.cardShape() = RoundedCornerShape(this.card)

/**
 * 获取对话框圆角Shape的扩展函数
 */
fun AppCornerRadius.dialogShape() = RoundedCornerShape(this.dialog)

/**
 * 获取输入框圆角Shape的扩展函数
 */
fun AppCornerRadius.inputShape() = RoundedCornerShape(this.input)

/**
 * 颜色工具函数
 */

/**
 * 获取角色对应的颜色
 * @param roleName 角色名称
 * @return 对应的颜色
 */
fun getRoleColor(roleName: String): Color {
    return when (roleName) {
        "梅林", "MERLIN" -> AppColors().blueTeam
        "派西维尔", "PERCIVAL" -> AppColors().info
        "忠臣", "LOYALIST" -> AppColors().morandiBlue
        "莫甘娜", "MORGANA" -> AppColors().morandiPurple
        "刺客", "ASSASSIN" -> AppColors().redTeam
        "莫德雷德", "MORDRED" -> AppColors().chi
        "奥伯伦", "OBERON" -> AppColors().morandiOrange
        else -> AppColors().neutralTeam
    }
}

/**
 * 获取任务状态对应的颜色
 * @param isSuccess 任务是否成功
 * @return 对应的颜色
 */
fun getTaskResultColor(isSuccess: Boolean): Color {
    return if (isSuccess) AppColors().success else AppColors().error
}

/**
 * 获取投票状态对应的颜色
 * @param isApproved 投票是否通过
 * @return 对应的颜色
 */
fun getVoteResultColor(isApproved: Boolean): Color {
    return if (isApproved) AppColors().success else AppColors().error
}
