package org.walks.gamecopilot.data.entity

import kotlinx.serialization.Serializable

/**
 * 词库数据结构
 * 用于支持后续添加自定义词库功能
 */
@Serializable
data class WordLibrary(
    val id: String,
    val name: String,
    val description: String = "",
    val words: Map<String, String> = emptyMap(),
    val isBuiltIn: Boolean = false,
    val isEnabled: Boolean = true,
    val groupId: String = "custom" // 关联到词组ID
)

/**
 * 词库管理器
 * 用于管理所有词库（内置+自定义）
 */
object WordLibraryManager {
    private val _libraries = mutableMapOf<String, WordLibrary>()
    
    init {
        // 初始化内置词库
        initializeBuiltInLibraries()
    }
    
    private fun initializeBuiltInLibraries() {
        // 初始化内置词库
        _libraries["easy_builtin"] = WordLibrary(
            id = "easy_builtin",
            name = "初级词库",
            description = "日常生活中常见的词汇对",
            isBuiltIn = true,
            groupId = "easy"
        )
        
        _libraries["middle_builtin"] = WordLibrary(
            id = "middle_builtin", 
            name = "中级词库",
            description = "需要一定联想的词汇对",
            isBuiltIn = true,
            groupId = "middle"
        )
        
        _libraries["hard_builtin"] = WordLibrary(
            id = "hard_builtin",
            name = "高级词库", 
            description = "网络流行语、专业术语等较难的词汇对",
            isBuiltIn = true,
            groupId = "hard"
        )
    }
    
    fun addLibrary(library: WordLibrary) {
        _libraries[library.id] = library
    }
    
    fun removeLibrary(libraryId: String) {
        val library = _libraries[libraryId]
        if (library != null && !library.isBuiltIn) {
            _libraries.remove(libraryId)
        }
    }
    
    fun getLibrary(libraryId: String): WordLibrary? {
        return _libraries[libraryId]
    }
    
    fun getAllLibraries(): List<WordLibrary> {
        return _libraries.values.toList()
    }
    
    fun getEnabledLibraries(): List<WordLibrary> {
        return _libraries.values.filter { it.isEnabled }
    }
    
    fun getLibrariesByGroup(groupId: String): List<WordLibrary> {
        return _libraries.values.filter { it.isEnabled && it.groupId == groupId }
    }
    
    /**
     * 从JSON字符串加载词库
     */
    fun loadLibraryFromJson(jsonString: String): Boolean {
        return try {
            // TODO: 实现JSON解析逻辑
            // val library = Json.decodeFromString<WordLibrary>(jsonString)
            // addLibrary(library)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 将词库导出为JSON字符串
     */
    fun exportLibraryToJson(libraryId: String): String? {
        return try {
            // TODO: 实现JSON导出逻辑
            // val library = getLibrary(libraryId)
            // library?.let { Json.encodeToString(it) }
            null
        } catch (e: Exception) {
            null
        }
    }
}