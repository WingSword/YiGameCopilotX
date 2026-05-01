package org.walks.gamecopilot

/**
 *  Created by Wing at 17:11 on 2025/6/8
 *
 */
// Logger.kt
object GameLogger {
    private const val TAG = "GameCopilot"

    fun debug(msg: String) {
        println("DEBUG/$TAG: $msg")
    }

    fun info(msg: String) {
        println("INFO/$TAG: $msg")
    }

    fun warn(msg: String) {
        println("WARN/$TAG: $msg")
    }

    fun warning(msg: String) {
        warn(msg)
    }

    fun error(msg: String, e: Throwable? = null) {
        println("ERROR/$TAG: $msg ${e?.stackTraceToString() ?: ""}")
    }
}


