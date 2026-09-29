package org.walks.gamecopilot

// JVM harness adapter: keep the production host independent of Android logging.
object GameLogger {
    fun info(message: String) = println(message)
    fun error(message: String, cause: Throwable) = println("$message: ${cause.message}")
}
