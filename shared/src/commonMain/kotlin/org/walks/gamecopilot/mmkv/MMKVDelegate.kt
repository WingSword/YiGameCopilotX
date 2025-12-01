package org.walks.gamecopilot.mmkv

/**
 * 用于在非Android平台提供MMKV功能的委托类
 */
class MMKVDelegate {
    private val memoryStorage = mutableMapOf<String, Any?>()

    operator fun set(key: String, value: Any?) {
        memoryStorage[key] = value
    }

    fun takeString(key: String, default: String): String {
        return memoryStorage[key] as? String ?: default
    }

    fun takeInt(key: String, default: Int): Int {
        return memoryStorage[key] as? Int ?: default
    }

    fun takeFloat(key: String, default: Float): Float {
        return memoryStorage[key] as? Float ?: default
    }

    fun takeLong(key: String, default: Long): Long {
        return memoryStorage[key] as? Long ?: default
    }

    fun takeBoolean(key: String, default: Boolean): Boolean {
        return memoryStorage[key] as? Boolean ?: default
    }

    fun takeDouble(key: String, default: Double): Double {
        return memoryStorage[key] as? Double ?: default
    }

    fun takeStringSet(key: String, default: Set<String>?): Set<String>? {
        @Suppress("UNCHECKED_CAST")
        return memoryStorage[key] as? Set<String> ?: default
    }

    fun clearAll() {
        memoryStorage.clear()
    }

    fun removeValueForKey(key: String) {
        memoryStorage.remove(key)
    }

    fun removeValuesForKeys(keys: List<String>) {
        keys.forEach { key ->
            memoryStorage.remove(key)
        }
    }

    fun containsKey(key: String): Boolean {
        return memoryStorage.containsKey(key)
    }
}