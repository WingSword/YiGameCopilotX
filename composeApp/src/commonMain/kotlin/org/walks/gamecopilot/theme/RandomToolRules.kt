package org.walks.gamecopilot.theme

// Generated; rule values and vectors live in cross-platform/random-tools.json.
object RandomToolRules {
    private fun integer(text: String, fallback: Int): Int = text.trim().toIntOrNull() ?: fallback
    fun low(first: String, second: String): Int = minOf(integer(first, 1), integer(second, 6)).coerceIn(1, 100)
    fun high(first: String, second: String): Int = maxOf(integer(first, 1), integer(second, 6)).coerceIn(1, 100)
    fun dice(first: String, second: String, sample: Double): Int {
        require(sample.isFinite() && sample >= 0 && sample < 1)
        val low = low(first, second)
        return low + kotlin.math.floor(sample * (high(first, second) - low + 1)).toInt()
    }
    fun heads(sample: Double): Boolean {
        require(sample.isFinite() && sample >= 0 && sample < 1)
        return sample < 0.5
    }
}
