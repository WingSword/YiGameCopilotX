package org.walks.gamecopilot.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 *  Created by Wing at 09:56 on 2025/4/22
 *
 */

class WheelPickerState(
    initialSize: Int,
    initialSelected: Int
) {
    var currentOffset by mutableStateOf(initialSelected)
    var itemSize by mutableStateOf(initialSize)
    var requireScroll by mutableStateOf(false)

    fun updateSize(newSize: Int) {
        if (newSize != itemSize) {
            itemSize = newSize
            currentOffset = currentOffset.coerceIn(0 until newSize)
        }
    }

    fun snapTo(index: Int) {
        currentOffset = index.coerceIn(0 until itemSize)
        requireScroll = true
    }

    fun onScrollComplete() {
        requireScroll = false
    }

    val selectedIndex: Int
        get() = currentOffset % itemSize
}

@Composable
fun rememberPickerState(
    itemSize: Int,
    initialSelected: Int
): WheelPickerState {
    return remember {
        WheelPickerState(
            initialSize = itemSize,
            initialSelected = initialSelected.coerceIn(0 until itemSize)
        )
    }
}

