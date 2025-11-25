# 词库选择功能说明

## 功能概述

为本地卧底游戏添加了词库选择功能，允许玩家选择不同难度的词汇库，并支持动态扩展和持久化存储。

## 主要特性

### 1. 词库分组
- **初级词库 (easy)**: 包含日常生活中常见的词汇对
- **中级词库 (middle)**: 包含一些需要一定联想的词汇对  
- **高级词库 (hard)**: 包含网络流行语、专业术语等较难的词汇对

### 2. 动态扩展
- **Map结构**: 使用Map而非枚举，支持动态添加词组
- **自定义词组**: 允许用户创建和管理自己的词组
- **持久化存储**: 词组和词库数据本地持久化保存

### 3. 默认设置
- 默认选中所有内置词库
- 玩家可以自由组合选择词库
- 支持单选、多选和全选

### 4. 扩展性
- **JSON序列化**: 支持词库的导入导出
- **MMKV存储**: 使用MMKV进行高性能本地存储
- **词库管理**: 完整的词库生命周期管理

## 技术实现

### 数据结构
```kotlin
// 词组管理器 - 支持动态词组
object WordGroupManager {
    val BUILTIN_GROUPS = mapOf(
        "easy" to WordGroup("easy", "初级", true),
        "middle" to WordGroup("middle", "中级", true),
        "hard" to WordGroup("hard", "高级", true)
    )
}

// 词组数据类
data class WordGroup(
    val id: String,
    val displayName: String,
    val isBuiltIn: Boolean = false,
    val description: String = "",
    val isEnabled: Boolean = true
)

// 游戏实体 - 使用词组ID集合
data class LocalSpyEntity(
    // ... 其他字段
    var selectedWordGroups: Set<String> = WordGroupManager.getDefaultSelectedGroups()
)
```

### 核心功能
1. **词库过滤**: 根据用户选择的词组ID动态生成可用词汇
2. **状态管理**: 通过 ViewModel 管理词组选择状态
3. **持久化**: 使用MMKV和JSON序列化进行本地存储
4. **动态UI**: 根据可用词组数量动态调整布局

### 持久化架构
```kotlin
object WordLibraryPersistence {
    // 保存/加载自定义词组
    fun saveCustomGroups(groups: Map<String, WordGroup>)
    fun loadCustomGroups(): Map<String, WordGroup>
    
    // 保存/加载自定义词库
    fun saveCustomLibraries(libraries: List<WordLibrary>)
    fun loadCustomLibraries(): List<WordLibrary>
    
    // 导入/导出功能
    fun exportLibrary(libraryId: String): String?
    fun importLibrary(jsonString: String): Boolean
}
```

## 使用方法

1. 进入本地卧底游戏页面
2. 在人数设置下方找到"词库选择"区域
3. 点击想要的词库按钮进行选择/取消选择
4. 系统会自动根据选择的词库生成游戏词汇

## 扩展开发指南

### 添加自定义词组
```kotlin
val customGroup = WordGroup(
    id = "programming",
    displayName = "编程词汇",
    isBuiltIn = false,
    description = "程序员专用词汇"
)
WordGroupManager.addGroup(customGroup)
```

### 添加自定义词库
```kotlin
val customLibrary = WordLibrary(
    id = "my_custom_lib",
    name = "我的词库",
    description = "自定义词汇集合",
    words = mapOf("变量" to "常量", "函数" to "方法"),
    groupId = "custom"
)
WordLibraryManager.addLibrary(customLibrary)
```

### 持久化自定义数据
```kotlin
// 保存到本地
WordLibraryPersistence.saveCustomGroups(customGroups)
WordLibraryPersistence.saveCustomLibraries(customLibraries)

// 从本地加载
WordLibraryPersistence.initializeWordLibraries()
```

## 后续扩展计划

1. **词库编辑器**: 提供可视化的词库编辑界面
2. **词库分享**: 支持词库的云端分享和下载
3. **智能推荐**: 基于使用频率推荐合适的词库
4. **主题词库**: 根据节日、事件等推出特殊主题词库
5. **词库统计**: 显示各词库的使用频率和难度分布

## 文件结构

```
composeApp/src/commonMain/kotlin/org/walks/gamecopilot/
├── data/
│   ├── WordLibraryPersistence.kt    # 持久化管理
│   └── entity/
│       ├── WordGroup.kt            # 词组管理器
│       ├── WordLibrary.kt          # 词库数据结构
│       └── GameEntity.kt           # 游戏实体（已修改）
├── intent/
│   └── GameIntent.kt              # 游戏意图（已修改）
├── ui/page/game/
│   └── LocalSpyGamePage.kt        # 游戏页面（已修改）
├── MainViewmodel.kt               # 视图模型（已修改）
└── WordMap.kt                     # 词汇映射（已修改）
```

## 技术优势

1. **灵活性**: Map结构比枚举更灵活，支持运行时动态扩展
2. **持久化**: 完整的本地存储方案，数据不丢失
3. **序列化**: JSON格式便于数据交换和备份
4. **性能**: MMKV提供高性能的键值存储
5. **扩展性**: 预留了完整的扩展接口，便于后续功能开发