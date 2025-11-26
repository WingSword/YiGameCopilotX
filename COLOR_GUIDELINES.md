# 颜色使用指南

## 概述
本文档说明了应用中颜色管理的最佳实践，确保在不同主题下都有良好的可读性和一致性。

## 主题颜色系统

### 主要颜色角色
- **primary**: 应用主色调，用于主要按钮、选中状态等
- **onPrimary**: 主色上的文字和图标颜色（确保对比度）
- **background**: 应用背景色
- **onBackground**: 背景上的文字颜色
- **surface**: 卡片、表单等表面颜色
- **onSurface**: 表面上的文字颜色
- **error**: 错误状态颜色
- **onError**: 错误色上的文字颜色

### 容器颜色
- **primaryContainer**: 主容器背景色
- **onPrimaryContainer**: 主容器上的文字颜色
- **secondaryContainer**: 次要容器背景色
- **onSecondaryContainer**: 次要容器上的文字颜色

## 使用原则

### 1. 始终使用 MaterialTheme.colorScheme
```kotlin
// ✅ 正确
Text(
    text = "标题",
    color = MaterialTheme.colorScheme.primary
)

// ❌ 错误 - 硬编码颜色
Text(
    text = "标题",
    color = Color(0xFF53AA99)
)
```

### 2. 根据语义选择颜色角色
```kotlin
// ✅ 正确 - 主要按钮使用 primary
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("开始游戏", color = MaterialTheme.colorScheme.onPrimary)
}

// ✅ 正确 - 卡片使用 surface
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    Text("内容", color = MaterialTheme.colorScheme.onSurface)
}

// ✅ 正确 - 错误信息使用 error
Text(
    text = "错误信息",
    color = MaterialTheme.colorScheme.error
)
```

### 3. 避免直接使用 Morandi 颜色
Morandi 颜色已映射到主题颜色中，应通过主题系统使用：
```kotlin
// ✅ 正确 - 使用主题中的容器颜色
Box(
    modifier = Modifier.background(
        MaterialTheme.colorScheme.primaryContainer
    )
)

// ❌ 错误 - 直接使用 Morandi 颜色
Box(
    modifier = Modifier.background(MorandiGreen)
)
```

## 颜色映射

### Morandi 颜色映射
- `MorandiGreen` → `primaryContainer`
- `MorandiBrown` → `secondaryContainer`
- 其他 Morandi 颜色保留在 `MorandiColorList` 中用于特殊场景

### 文字颜色映射
- 主要文字：`onBackground`, `onSurface`
- 次要文字：`onSurfaceVariant`
- 强调文字：`primary`
- 错误文字：`error`

## 测试要点

1. **对比度检查**: 确保文字在背景上清晰可读
2. **主题切换**: 测试浅色/深色主题切换效果
3. **状态一致性**: 确保相同语义的元素使用相同颜色

## 常见问题

### Q: 什么时候使用 surface vs background？
A: `background` 用于整个应用背景，`surface` 用于卡片、对话框等浮动元素。

### Q: 如何处理自定义颜色？
A: 如果必须使用自定义颜色，确保提供浅色/深色两种变体，并通过主题系统管理。

### Q: 如何处理品牌色？
A: 品牌色应映射到 `primary` 和相关颜色角色，避免直接硬编码。