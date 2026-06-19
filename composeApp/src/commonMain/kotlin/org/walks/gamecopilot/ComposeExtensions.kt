package org.walks.gamecopilot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

// ==================== Modifier 扩展方法 ====================

/**
 * 无波纹效果的点击扩展方法
 */
fun Modifier.clickableWithoutRipple(enabled: Boolean = true, onClick: () -> Unit) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled
    ) {
        onClick()
    }
}

/**
 * 带圆角的点击效果
 */
fun Modifier.clickableWithRoundedCorners(
    enabled: Boolean = true,
    cornerRadius: Dp = 8.dp,
    onClick: () -> Unit
) = composed {
    this.clip(RoundedCornerShape(cornerRadius))
        .clickableWithoutRipple(enabled, onClick)
}

/**
 * 带阴影的点击效果
 */
fun Modifier.clickableWithShadow(
    enabled: Boolean = true,
    elevation: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: () -> Unit
) = composed {
    this.shadow(elevation, shape)
        .clickableWithRoundedCorners(enabled, cornerRadius = 8.dp, onClick)
}

/**
 * 条件性应用 Modifier
 */
fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

/**
 * 仅在满足条件时应用 Modifier
 */
fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        this.then(block(Modifier))
    } else {
        this
    }
}

/**
 * 设置最小宽度
 */
fun Modifier.minWidth(minWidth: Dp) = composed {
    this.widthIn(min = minWidth)
}

/**
 * 设置最大宽度
 */
fun Modifier.maxWidth(maxWidth: Dp) = composed {
    this.widthIn(max = maxWidth)
}

/**
 * 设置正方形尺寸
 */
fun Modifier.squareSize(size: Dp) = composed {
    this.size(size)
}

/**
 * 设置圆形尺寸
 */
fun Modifier.circleSize(size: Dp) = composed {
    this.size(size).clip(RoundedCornerShape(50))
}

/**
 * 设置透明度（条件性）
 */
fun Modifier.alphaIf(condition: Boolean, alpha: Float) = composed {
    this.alpha(if (condition) alpha else 1f)
}

/**
 * 设置所有方向相同的 padding
 */
fun Modifier.paddingAll(value: Dp) = composed {
    this.padding(value)
}

/**
 * 设置水平方向 padding
 */
fun Modifier.paddingHorizontal(horizontal: Dp) = composed {
    this.padding(horizontal = horizontal)
}

/**
 * 设置垂直方向 padding
 */
fun Modifier.paddingVertical(vertical: Dp) = composed {
    this.padding(vertical = vertical)
}

/**
 * 转换为像素值
 */
val Dp.px: Float
    @Composable
    get() = with(LocalDensity.current) { this@px.toPx() }

/**
 * 设置点击区域的最小尺寸
 */
fun Modifier.minimumTouchSize(minSize: Dp = 48.dp) = composed {
    this.size(minSize)
}

/**
 * 设置边框圆角
 */
fun Modifier.roundedCorners(radius: Dp) = composed {
    this.clip(RoundedCornerShape(radius))
}

/**
 * 设置圆形边框
 */
fun Modifier.circularBorder() = composed {
    this.clip(RoundedCornerShape(50))
}

// ==================== 通用扩展方法 ====================

/**
 * 布尔值格式化
 */
fun Boolean.format(trueLabel: String = "是", falseLabel: String = "否") =
    if (this) trueLabel else falseLabel

/**
 * 安全地判断布尔值是否为 true
 */
fun Boolean?.isTrue(): Boolean = this == true

/**
 * 安全地判断布尔值是否为 false
 */
fun Boolean?.isFalse(): Boolean = this == false

/**
 * 如果为 null 则返回默认值
 */
fun <T> T?.orDefault(defaultValue: T): T = this ?: defaultValue

/**
 * 如果为 null 则返回空字符串
 */
fun String?.orEmpty(): String = this ?: ""

/**
 * 安全的字符串转换为整数
 */
fun String.toIntOrDefault(default: Int = 0): Int {
    return try {
        trim().toInt()
    } catch (_: NumberFormatException) {
        default
    }
}

/**
 * 安全的字符串转换为浮点数
 */
fun String.toFloatOrDefault(default: Float = 0f): Float {
    return try {
        trim().toFloat()
    } catch (_: NumberFormatException) {
        default
    }
}

/**
 * 兼容旧调用：返回非空默认值，建议新代码使用 toIntOrDefault。
 */
fun String.toIntOrNull(default: Int = 0): Int = toIntOrDefault(default)

/**
 * 兼容旧调用：返回非空默认值，建议新代码使用 toFloatOrDefault。
 */
fun String.toFloatOrNull(default: Float = 0f): Float = toFloatOrDefault(default)

/**
 * 限制字符串长度
 */
fun String.limitLength(maxLength: Int, suffix: String = "..."): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + suffix
    } else {
        this
    }
}

/**
 * 检查字符串是否为数字
 */
fun String.isNumeric(): Boolean = this.matches("-?\\d+(\\.\\d+)?".toRegex())

/**
 * 检查字符串是否为空或仅包含空白字符
 */
fun String.isBlankOrEmpty(): Boolean = this.isBlank() || this.isEmpty()

// ==================== 集合扩展方法 ====================

/**
 * 使用 Fisher-Yates 洗牌算法
 */
fun <T> List<T>.optimizedShuffle(): List<T> {
    val list = this.toMutableList()
    for (i in list.size - 1 downTo 1) {
        val j = Random.nextInt(i + 1)
        val temp = list[i]
        list[i] = list[j]
        list[j] = temp
    }
    return list
}

/**
 * 获取列表的第一个元素，如果为空则返回 null
 */
fun <T> List<T>.firstOrNull(): T? = if (this.isNotEmpty()) this[0] else null

/**
 * 获取列表的最后一个元素，如果为空则返回 null
 */
fun <T> List<T>.lastOrNull(): T? = if (this.isNotEmpty()) this[this.size - 1] else null

/**
 * 安全的获取列表元素
 */
fun <T> List<T>.getOrNull(index: Int): T? = if (index in 0 until size) this[index] else null

/**
 * 检查索引是否在列表范围内
 */
fun <T> List<T>.isIndexValid(index: Int): Boolean = index in 0 until size

/**
 * 将列表转换为带索引的 Pair 列表
 */
fun <T> List<T>.withIndex(): List<Pair<Int, T>> = this.mapIndexed { index, value -> index to value }

/**
 * 过滤掉 null 值
 */
fun <T> List<T?>.filterNotNull(): List<T> = this.filterNotNull()

// ==================== 颜色扩展方法 ====================

/**
 * 带透明度的颜色
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

/**
 * 检查颜色是否为亮色（简化版）
 */
fun Color.isLight(): Boolean {
    val luminance = 0.299 * this.red + 0.587 * this.green + 0.114 * this.blue
    return luminance > 0.5
}

/**
 * 获取相反的颜色（黑白反转）
 */
fun Color.inverted(): Color = if (this.isLight()) Color.Black else Color.White

// ==================== 几何扩展方法 ====================

/**
 * Offset 转换为 IntOffset
 */
fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())

/**
 * 计算两点之间的距离
 */
fun Offset.distanceTo(other: Offset): Float {
    val dx = this.x - other.x
    val dy = this.y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

/**
 * 检查点是否在矩形区域内
 */
fun Offset.isInRect(x: Float, y: Float, width: Float, height: Float): Boolean {
    return this.x >= x && this.x <= x + width && this.y >= y && this.y <= y + height
}

// ==================== 数字扩展方法 ====================

/**
 * 限制数字在指定范围内
 */
fun Int.clamp(min: Int, max: Int): Int = when {
    this < min -> min
    this > max -> max
    else -> this
}

/**
 * 限制浮点数在指定范围内
 */
fun Float.clamp(min: Float, max: Float): Float = when {
    this < min -> min
    this > max -> max
    else -> this
}

/**
 * 将数字转换为带单位的字符串（如：1K, 1M）
 */
fun Long.toHumanReadable(): String = when {
    this >= 1_000_000_000 -> "${this / 1_000_000_000}B"
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> this.toString()
}

