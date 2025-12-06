package org.walks.gamecopilot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.random.Random

fun Modifier.clickableWithoutRipple(enabled: Boolean = true, onClick: () -> Unit) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled
    ) {
        onClick()
    }
}

fun Boolean.format(trueLabel: String = "是", falseLabel: String = "否") =
    if (this) trueLabel else falseLabel

fun Boolean?.isTrue(): Boolean = this == true

fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())

// 使用 Fisher-Yates 洗牌算法
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
