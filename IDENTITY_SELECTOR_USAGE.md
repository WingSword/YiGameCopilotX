# 通用身份选择器组件使用指南

## 概述

`IdentitySelector` 是一个通用的身份选择器组件，支持多种游戏的身份查看、卡片翻转和昵称设置功能。该组件已经成功应用于阿瓦隆游戏和本地卧底游戏。

## 文件结构

```
composeApp/src/commonMain/kotlin/org/walks/gamecopilot/
├── ui/components/common/
│   ├── IdentitySelector.kt              # 核心通用组件
│   └── LocalSpyIdentitySelector.kt      # 本地卧底游戏专用封装
└── awalong/components/
    ├── AwalongDayZeroPage.kt            # 阿瓦隆第0天页面
    └── AwalongIdentityCard.kt            # 阿瓦隆专用身份卡片
```

## 核心功能

### 1. 基础功能

- **玩家网格显示**：4列网格布局，显示玩家号码牌
- **身份查看**：点击号码牌翻转卡片查看身份
- **昵称设置**：长按号码牌弹出昵称编辑弹窗
- **查看记录**：记录每个玩家的查看次数

### 2. 自定义扩展

- **自定义身份卡片**：支持传入自定义的身份卡片组件
- **状态管理**：自动处理组件状态重置和刷新
- **响应式设计**：适配不同屏幕尺寸

## 使用方法

### 基础用法（适用于简单游戏）

```kotlin
IdentitySelector(
    refreshKey = refreshKey,
    playerNum = playerCount,
    identities = listOf("角色1", "角色2", "角色3", "角色4"),
    nicknames = listOf("昵称1", "昵称2", "昵称3", "昵称4"),
    onNicknameChange = { playerIndex, newNickname ->
        // 处理昵称变更
    }
)
```

### 高级用法（自定义身份卡片）

```kotlin
IdentitySelector(
    refreshKey = refreshKey,
    playerNum = playerCount,
    identities = roleList.map { it.title },
    nicknames = nicknameList,
    onNicknameChange = { playerIndex, newNickname ->
        onNameChange(newNickname, playerIndex)
    },
    customIdentityCard = { playerNumber, identity, nickname ->
        CustomIdentityCard(
            playerNumber = playerNumber,
            identity = identity,
            nickname = nickname,
            // 其他自定义参数
        )
    }
)
```

## 实际案例

### 1. 阿瓦隆游戏

阿瓦隆游戏需要显示复杂的角色信息和技能，因此使用了自定义身份卡片：

```kotlin
// AwalongDayZeroPage.kt
IdentitySelector(
    refreshKey = refreshKey,
    playerNum = playerNum,
    identities = identities,
    nicknames = safeNicknameList,
    onNicknameChange = { playerIndex, newNickname ->
        onNameChange(newNickname, playerIndex)
    },
    customIdentityCard = { playerNumber, identity, nickname ->
        val role = roleList.find { it.title == identity } ?: roleList[playerNumber - 1]
        AwalongIdentityCard(
            playerNumber = playerNumber,
            role = role,
            nickname = nickname,
            allRoles = roleList,
            allNicknames = safeNicknameList
        )
    }
)
```

### 2. 本地卧底游戏

本地卧底游戏相对简单，使用默认身份卡片即可：

```kotlin
// LocalSpyIdentitySelector.kt
@Composable
fun LocalSpyIdentitySelector(
    key: Int,
    gameState: LocalSpyEntity,
    onNicknameChange: (Int, String) -> Unit
) {
    val playerNum = gameState.totalPlayerNumber
    val identities = List(playerNum) { index ->
        if (gameState.isSpy(index)) "卧底" else "平民"
    }
    val nicknames = List(playerNum) { "" }

    IdentitySelector(
        refreshKey = key,
        playerNum = playerNum,
        identities = identities,
        nicknames = nicknames,
        onNicknameChange = onNicknameChange
    )
}
```

## 组件参数说明

### IdentitySelector

| 参数                 | 类型                                           | 说明             |
|--------------------|----------------------------------------------|----------------|
| refreshKey         | Int                                          | 重组标识键，用于控制状态重置 |
| playerNum          | Int                                          | 玩家总数           |
| identities         | List<String>                                 | 玩家身份列表         |
| nicknames          | List<String>                                 | 玩家昵称列表         |
| onNicknameChange   | (Int, String) -> Unit                        | 昵称修改回调         |
| customIdentityCard | @Composable ((Int, String, String) -> Unit)? | 自定义身份卡片        |

## 扩展新游戏

要为新游戏添加身份选择功能，只需：

1. **创建游戏专用页面**：

```kotlin
@Composable
fun NewGameDayZeroPage(
    roleList: List<NewGameRole>,
    nicknameList: List<String>,
    onNameChange: (String, Int) -> Unit
) {
    IdentitySelector(
        refreshKey = refreshKey,
        playerNum = roleList.size,
        identities = roleList.map { it.title },
        nicknames = nicknameList,
        onNicknameChange = { playerIndex, newNickname ->
            onNameChange(newNickname, playerIndex)
        },
        customIdentityCard = { playerNumber, identity, nickname ->
            NewGameIdentityCard(
                playerNumber = playerNumber,
                role = roleList[playerNumber - 1],
                nickname = nickname
            )
        }
    )
}
```

2. **创建专用身份卡片**（如果需要）：

```kotlin
@Composable
fun NewGameIdentityCard(
    playerNumber: Int,
    role: NewGameRole,
    nickname: String
) {
    FlipCard(
        // 自定义卡片内容
    )
}
```

## 优势

1. **代码复用**：避免为每个游戏重复实现相同的身份选择逻辑
2. **统一体验**：所有游戏都有一致的交互体验
3. **易于扩展**：新游戏只需提供角色数据和自定义卡片
4. **状态管理**：自动处理复杂的状态重置和刷新逻辑
5. **响应式设计**：自动适配不同屏幕尺寸和玩家数量

## 注意事项

1. **Key管理**：确保在游戏状态变化时正确更新key以触发状态重置
2. **数据一致性**：确保identities和nicknames列表长度与playerNum一致
3. **性能优化**：避免在重组过程中创建大量临时对象
4. **自定义卡片**：自定义卡片应遵循FlipCard的接口规范