package org.walks.gamecopilot.data.entity

/**
 * 词汇难度分组数据结构
 * 使用 Map 结构支持动态词库管理
 */
object WordGroupManager {
    // 内置词组定义
    val BUILTIN_GROUPS = mapOf(
        "easy" to WordGroup("easy", "初级", true),
        "middle" to WordGroup("middle", "中级", true),
        "hard" to WordGroup("hard", "高级", true)
    )
    
    // 所有可用词组（内置 + 自定义）
    private val _availableGroups = mutableMapOf<String, WordGroup>()
    
    init {
        // 初始化内置词组
        _availableGroups.putAll(BUILTIN_GROUPS)
    }
    
    /**
     * 获取所有可用词组
     */
    fun getAllGroups(): Map<String, WordGroup> {
        return _availableGroups.toMap()
    }
    
    /**
     * 添加自定义词组
     */
    fun addGroup(group: WordGroup) {
        _availableGroups[group.id] = group
    }
    
    /**
     * 移除词组（仅限自定义词组）
     */
    fun removeGroup(groupId: String): Boolean {
        val group = _availableGroups[groupId]
        return if (group != null && !group.isBuiltIn) {
            _availableGroups.remove(groupId)
            true
        } else {
            false
        }
    }
    
    /**
     * 获取默认选中的词组ID集合
     */
    fun getDefaultSelectedGroups(): Set<String> {
        return BUILTIN_GROUPS.keys
    }

    /**
     * 获取所有自定义词组（非内置）
     */
    fun getCustomGroups(): Map<String, WordGroup> {
        return _availableGroups.filter { !it.value.isBuiltIn }
    }
}

/**
 * 词组数据类
 */
data class WordGroup(
    val id: String,
    val displayName: String,
    val isBuiltIn: Boolean = false,
    val description: String = "",
    val isEnabled: Boolean = true
)