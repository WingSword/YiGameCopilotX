package org.walks.gamecopilot.ui.page.monopoly

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class TableSeat(val playerIndex: Int, val x: Double, val y: Double, val width: Double, val height: Double)
data class TablePlan(val seats: List<TableSeat>, val capacity: Int, val page: Int, val pages: Int)

/** Logical dp/vp coordinates shared with the native Harmony implementation. */
object RoundTableLayout {
    val colors = listOf(0xFF218D76, 0xFF497BD0, 0xFFD27337, 0xFF9764C1, 0xFFCE597F,
        0xFF358EAA, 0xFF839633, 0xFFAD6550, 0xFF5A72AC, 0xFFC18D25,
        0xFF5C995F, 0xFFBF5260, 0xFF7863AA, 0xFF369D98, 0xFFAC758E,
        0xFF8C7F44, 0xFF567D8D, 0xFFB165A2, 0xFF907363, 0xFF617C37)

    fun plan(width: Double, height: Double, players: Int, requestedPage: Int): TablePlan {
        val w = max(280.0, width)
        val h = max(if (w >= 600) 188.0 else 280.0, height)
        val gap = 12.0
        val columns = max(2, floor((w - gap) / 156.0).toInt())
        val cardWidth = min(176.0, (w - (columns + 1) * gap) / columns)
        val cardHeight = if (h < 400) 82.0 else 100.0
        val sideRows = if (w >= 600) max(0, floor((h - 2 * cardHeight - 4 * gap) / (cardHeight + gap)).toInt()) else 0
        val positions = mutableListOf<Pair<Double, Double>>()
        for (i in 0 until columns) positions += Pair(gap + i * (w - 2 * gap - cardWidth) / (columns - 1), gap)
        for (i in 0 until sideRows) positions += Pair(w - gap - cardWidth, cardHeight + 2 * gap + i * (cardHeight + gap))
        for (i in columns - 1 downTo 0) positions += Pair(gap + i * (w - 2 * gap - cardWidth) / (columns - 1), h - gap - cardHeight)
        for (i in sideRows - 1 downTo 0) positions += Pair(gap, cardHeight + 2 * gap + i * (cardHeight + gap))
        // On short landscape screens, reserve the middle for the controls and use the corners.
        positions.removeAll { (x, y) ->
            x < (w + 220) / 2 && x + cardWidth > (w - 220) / 2 &&
                y < (h + 84) / 2 && y + cardHeight > (h - 84) / 2
        }
        val capacity = positions.size
        val pages = max(1, (max(0, players) + capacity - 1) / capacity)
        val page = requestedPage.coerceIn(0, pages - 1)
        val count = min(capacity, max(0, players - page * capacity))
        val seats = (0 until count).map { i ->
            val position = positions[i * capacity / count]
            TableSeat(page * capacity + i, position.first, position.second, cardWidth, cardHeight)
        }
        return TablePlan(seats, capacity, page, pages)
    }

    /** Retain valid unique assignments, migrating legacy or duplicate colors deterministically. */
    fun assignColors(existing: List<Int>): List<Int> {
        val reserved = existing.filter { it in colors.indices }.toMutableSet()
        val seen = mutableSetOf<Int>()
        return existing.map { value ->
            if (value in colors.indices && seen.add(value)) value
            else {
                val next = colors.indices.firstOrNull { it !in reserved } ?: 0
                reserved.add(next); seen.add(next); next
            }
        }
    }
}
