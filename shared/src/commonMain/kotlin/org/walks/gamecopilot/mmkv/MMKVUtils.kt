package org.walks.gamecopilot.mmkv

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

object MMKVUtils {

    private val settings: Settings by lazy { Settings() }

    fun put(key: String, value: Any?) {
        when (value) {
            is String -> settings[key] = value
            is Int -> settings[key] = value
            is Float -> settings[key] = value
            is Long -> settings[key] = value
            is Boolean -> settings[key] = value
            is Double -> settings[key] = value
            is ByteArray -> settings[key] = value.toString()
        }
    }

    fun get(key: String, default: Any): Any {
        return when (default) {
            is String -> settings.getString(key, default)
            is Int -> settings.getInt(key, default)
            is Float -> settings.getFloat(key, default)
            is Long -> settings.getLong(key, default)
            is Boolean -> settings.getBoolean(key, default)
            is Double -> settings.getDouble(key, default)
            else -> default
        }
    }

    fun getString(key: String, default: String): String {
        return settings.getString(key, default)
    }

    fun getInt(key: String, default: Int): Int {
        return settings.getInt(key, default)
    }

    fun getFloat(key: String, default: Float): Float {
        return settings.getFloat(key, default)
    }

    fun getLong(key: String, default: Long): Long {
        return settings.getLong(key, default)
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return settings.getBoolean(key, default)
    }

    fun getDouble(key: String, default: Double): Double {
        return settings.getDouble(key, default)
    }

    fun putSet(key: String, value: Set<String>) {
        settings[key] = value.joinToString(",")
    }

    fun getSet(key: String, default: Set<String>? = null): Set<String>? {
        val stringValue = settings.getString(key, "")
        return if (stringValue.isNotEmpty()) {
            stringValue.split(",").toSet()
        } else {
            default
        }
    }

    fun clear() {
        val keys = settings.keys.toList()
        keys.forEach { settings.remove(it) }
    }

    fun remove(key: String) {
        settings.remove(key)
    }

    fun removeItems(keys: List<String>) {
        keys.forEach { settings.remove(it) }
    }

    fun contains(key: String): Boolean {
        return settings.hasKey(key)
    }
}
