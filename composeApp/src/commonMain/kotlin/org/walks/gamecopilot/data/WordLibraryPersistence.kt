package org.walks.gamecopilot.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.data.entity.WordGroup
import org.walks.gamecopilot.data.entity.WordGroupManager
import org.walks.gamecopilot.data.entity.WordLibrary
import org.walks.gamecopilot.data.entity.WordLibraryManager
import org.walks.gamecopilot.mmkv.MMKVUtils

/**
 * 词库持久化管理工具
 * 支持词库的本地存储和读取
 */
object WordLibraryPersistence {
    private const val CUSTOM_WORD_GROUPS_KEY = "custom_word_groups"
    private const val CUSTOM_WORD_LIBRARIES_KEY = "custom_word_libraries"
    private const val SELECTED_WORD_GROUPS_KEY = "selected_word_groups"
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
    
    /**
     * 保存自定义词组到本地
     */
    fun saveCustomGroups(groups: Map<String, WordGroup>) {
        try {
            val jsonString = json.encodeToString(groups)
            MMKVUtils.put(CUSTOM_WORD_GROUPS_KEY, jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 从本地加载自定义词组
     */
    fun loadCustomGroups(): Map<String, WordGroup> {
        return try {
            val jsonString: String = MMKVUtils.getString(CUSTOM_WORD_GROUPS_KEY, "")
            if (jsonString.isNotEmpty()) {
                json.decodeFromString<Map<String, WordGroup>>(jsonString)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
    
    /**
     * 保存自定义词库到本地
     */
    fun saveCustomLibraries(libraries: List<WordLibrary>) {
        try {
            val jsonString = json.encodeToString(libraries)
            MMKVUtils.put(CUSTOM_WORD_LIBRARIES_KEY, jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 从本地加载自定义词库
     */
    fun loadCustomLibraries(): List<WordLibrary> {
        return try {
            val jsonString = MMKVUtils.getString(CUSTOM_WORD_LIBRARIES_KEY, "")
            if (jsonString.isNotEmpty()) {
                json.decodeFromString<List<WordLibrary>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 保存用户选择的词组
     */
    fun saveSelectedGroups(groupIds: Set<String>) {
        try {
            val jsonString = json.encodeToString(groupIds)
            MMKVUtils.put(SELECTED_WORD_GROUPS_KEY, jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 加载用户选择的词组
     */
    fun loadSelectedGroups(): Set<String> {
        return try {
            val jsonString = MMKVUtils.getString(SELECTED_WORD_GROUPS_KEY, "") ?: ""
            if (jsonString.isNotEmpty()) {
                json.decodeFromString<Set<String>>(jsonString)
            } else {
                WordGroupManager.getDefaultSelectedGroups()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            WordGroupManager.getDefaultSelectedGroups()
        }
    }
    
    /**
     * 初始化词库数据
     * 从本地加载自定义词组和词库
     */
    fun initializeWordLibraries() {
        // 加载自定义词组
        val customGroups = loadCustomGroups()
        customGroups.forEach { (id, group) ->
            WordGroupManager.addGroup(group)
        }
        
        // 加载自定义词库
        val customLibraries = loadCustomLibraries()
        customLibraries.forEach { library ->
            WordLibraryManager.addLibrary(library)
        }
    }
    
    /**
     * 导出词库为JSON字符串
     */
    fun exportLibrary(libraryId: String): String? {
        return try {
            val library = WordLibraryManager.getLibrary(libraryId)
            library?.let { json.encodeToString(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从JSON字符串导入词库
     */
    fun importLibrary(jsonString: String): Boolean {
        return try {
            val library = json.decodeFromString<WordLibrary>(jsonString)
            // 检查ID是否已存在
            if (WordLibraryManager.getLibrary(library.id) == null) {
                WordLibraryManager.addLibrary(library)
                saveCustomLibraries(WordLibraryManager.getAllLibraries().filter { !it.isBuiltIn })
                true
            } else {
                false // ID已存在
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}