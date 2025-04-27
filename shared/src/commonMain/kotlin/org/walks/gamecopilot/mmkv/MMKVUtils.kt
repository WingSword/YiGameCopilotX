package org.walks.gamecopilot.mmkv

import com.ctrip.flight.mmkv.defaultMMKV

/**
 *  Created by Wing at 15:27 on 2025/4/27
 *
 */
object MMKVUtils {

    private val kv by lazy {
         defaultMMKV()
    }

    fun put(key: String, value: Any?) {
        when (value) {
            is String -> kv[key] = value
            is Int ->  kv[key] = value
            is Float ->  kv[key] = value
            is Long ->  kv[key] = value
            is Boolean ->  kv[key] = value
            is Double ->  kv[key] = value
            is ByteArray ->  kv[key] = value
        }
    }

    fun get(key: String, default: Any): Any {
        return when (default) {
            is String -> kv.takeString(key,default)
            is Int ->  kv.takeInt(key,default)
            is Float ->  kv.takeFloat(key,default)
            is Long ->  kv.takeLong(key,default)
            is Boolean ->  kv.takeBoolean(key,default)
            is Double ->  kv.takeDouble(key,default)
            else -> default
        }
    }

    fun putSet(key: String, value: Set<String>){
        kv[key] = value
    }
    fun getSet(key: String, default: Set<String>?): Set<String>?{
        return kv.takeStringSet(key,default)
    }

    fun clear(){
        kv.clearAll()
    }

    fun remove(key: String){
        kv.removeValueForKey(key)
    }

    fun removeItems(keys: List<String>){
        kv.removeValuesForKeys(keys)
    }

    fun contains(key: String): Boolean{
        return kv.containsKey(key)
    }
}