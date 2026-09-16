package org.walks.gamecopilot.mmkv

import java.io.File
import java.util.Properties

/** Test-only disk storage, so restarting the client really drops all in-memory state. */
object MMKVUtils {
    private val file = File(System.getenv("CLOUD_TEST_STORE"))
    private val values = Properties().apply { if (file.exists()) file.inputStream().use { load(it) } }
    fun getString(key: String, default: String): String = values.getProperty(key, default)
    fun put(key: String, value: Any?) { values.setProperty(key, value.toString()); save() }
    fun remove(key: String) { values.remove(key); save() }
    private fun save() { file.outputStream().use { values.store(it, null) } }
}
