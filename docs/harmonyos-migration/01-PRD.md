# YiGameCopilotX 鸿蒙 HarmonyOS 迁移 PRD

> 产品经理：许清楚（Xu） | 日期：2026-07-12  
> 原项目：Kotlin Multiplatform + Compose Multiplatform 桌游合集  
> 目标平台：HarmonyOS（ArkTS + ArkUI）  
> 原项目根目录：`d:\Develop\Android\Projects\YiGameCopilotX`

---

## 1. 产品目标

将现有的成熟跨平台桌游合集应用 **YiGameCopilotX** 完整迁移到鸿蒙 HarmonyOS 平台，使用 ArkTS + ArkUI 技术栈 1:1 复刻全部核心功能与用户体验，交付一款可在 HarmonyOS 设备上独立运行、功能对等的桌游助手应用。

**交付物定义**：
- 单一 HarmonyOS 应用（HAP 包），覆盖手机/折叠屏/平板形态
- 包含原项目全部 5 款游戏（谁是卧底、阿瓦隆、你画我猜、猎巫镇、一夜终极狼人）
- 包含全部随机工具（骰子、硬币、转盘、卡牌、答案之书、指转盘）
- 包含 LAN 局域网联机、AI 主持人、主题系统、统计数据等全部基础能力
- 不包含原项目的 Web(Wasm) 专有能力和 iOS 专有能力

---

## 2. 功能对等清单

### 2.1 导航与框架层

| 原项目功能模块 | 原实现位置 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| **App 根入口** | `App.kt` + `WeUITheme` | `EntryAbility` (UIAbility) + `AppStorage` 全局主题状态 | P0 |
| **全局 MVI ViewModel** | `MainViewmodel.kt`（唯一 ViewModel，62KB） | 鸿蒙侧用全局 `AppStorage` + `@Provide/@Consume` 或单例 Store 替代；按业务域拆分为多个 Store（GameStore/AiStore/LanStore/RandomStore/ThemeStore） | P0 |
| **底部悬浮导航栏** | `App.kt` FloatingNavBar（首页/联机/信息/设置） | ArkUI `Tabs` 或自定义 `Stack` + `TabBar` | P0 |
| **页面导航框架** | `navigation-compose` `NavHost` + `NaviRoute` 枚举（16 条路由） | `Navigation` 组件 + `NavPathStack`，路由表对等迁移 | P0 |
| **返回栈管理** | `NavHostController.popBackStack()` | `NavPathStack.pop()` | P0 |

**原项目路由表（NaviRoute 枚举，须全部对等迁移）**：

| 路由 key | 标签 | 类型 | 说明 |
|---|---|---|---|
| HOME | 首页 | Tab(0) | 游戏大厅 |
| RANDOM | 随机工具 | Tab(0) | 随机工具合集 |
| MULTIPLAYER | 联机 | Tab(0) | 在线联机入口（规划中） |
| STATS | 信息 | Tab(0) | 信息中心/统计 |
| SETTING | 设置 | Tab(0) | 设置中心 |
| LOCAL_SPY | 本地卧底 | 子页(1) | 谁是卧底单机 |
| ROOM | 房间 | 子页(1) | 在线房间准备页 |
| AWALONG | 阿瓦隆配置 | 子页(1) | 阿瓦隆入口配置 |
| AWALONG_GAME | 阿瓦隆 | 子页(1) | 阿瓦隆对局页 |
| HUNT_TOWN | 猎巫镇 | 子页(1) | 猎巫镇对局页 |
| DRAW_GUESS | 你画我猜 | 子页(1) | 画猜入口配置 |
| DRAW_BOARD | 画板 | 子页(1) | 画猜画板页 |
| ONE_NIGHT_WEREWOLF | 一夜狼人配置 | 子页(1) | 狼人入口配置 |
| ONE_NIGHT_WEREWOLF_GAME | 一夜狼人 | 子页(1) | 狼人对局页 |
| LAN_DISCOVERY | 局域网发现 | 子页(1) | LAN 房间发现 |
| LAN_CREATE_ROOM | 创建房间 | 子页(1) | LAN 创建房间 |
| LAN_LOBBY | 房间大厅 | 子页(1) | LAN 房间大厅 |

### 2.2 首页（游戏大厅）

**原实现**：`ui/page/home/HomePage.kt`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 5 款游戏卡片列表 | `GameCardMeta` 列表，每卡含 GameMode/描述/人数/渐变色 | `List` + 自定义 `GameCard` 组件，渐变色用 `linearGradient` | P0 |
| 卡片展开/收起 | `animateContentSize` + `expandedGameMode` 状态 | `animateTo` + `@State isExpanded` | P0 |
| 游戏模式徽章绘制 | `Canvas` 内 `drawCircle/drawPath/drawRoundRect`（SPY/AWALONG/DRAW/HUNT/WEREWOLF 5 种图形） | `Canvas` + `CanvasRenderingContext2D` 同等绘制 | P0 |
| 操作模式选择（单机/LAN/在线） | `OperationMode` 枚举，猎巫镇仅支持 LAN/ONLINE | 同等逻辑，UI 用 `SegmentButton` 或自定义按钮组 | P0 |
| 进入游戏导航 | `navigateByMode()` 按 gameMode×operationMode 分发到对应路由 | 同等路由分发逻辑 | P0 |
| 单机对局统计上报 | `GameStatsManager.recordGameStart()` | 鸿蒙侧统计 Store 调用 | P0 |
| 顶部菜单 Popup | `Popup` + `HomeStatsMenu`（总局数/总人次/最近游玩/各游戏局数/主题切换） | `bindMenu` 或自定义 `Stack` 浮层 | P1 |
| 主题快速切换 | Popup 内浅色/深色/跟随系统三选一 | 调用 `ThemeStore.setThemeMode()` | P0 |

### 2.3 谁是卧底（LocalSpy）

**原实现**：`ui/page/game/localspy/`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 入口配置页 | `LocalSpyGamePage.kt`，含玩家数选择（4-16人） | ArkUI 页面 + 滑选器/步进器 | P0 |
| 词库选择与展开 | `GameConfigurationSection` + `WordLibrarySection`，支持 easy/middle/hard + 自定义词组 | `List` 分组 + `toggle` 展开动画 | P0 |
| 词组查看弹窗 | `WordsDialog` | `CustomDialog` | P0 |
| 词汇映射 | `WordMap.kt` + `wordsEasy/wordsMiddle/wordsHard` + `customSpyWordMaps` | 纯数据层直接移植，存入 `preferences` 或 rawjson | P0 |
| 词库导入引擎 | `WordImportEngine`（文本格式批量导入，校验长度/特殊字符/数量限制） | 同等正则校验逻辑移植 | P1 |
| 游戏开始问候 | `GameGreetingView` | ArkUI 页面 | P0 |
| 离线传递引导 | `OfflinePassingGuideDialog` + `OfflinePassingGuideCard` | `CustomDialog` | P0 |
| 身份卡片（可滑动） | `LocalSpySwipeableIdentityCard` + `LocalSpyIdentitySelector` | `Swiper` 或手势 `pan` + 翻转动画 | P0 |
| 玩家选择区域 | `LocalPlayerSelectArea` | `List` / `Grid` | P0 |
| 公布所有身份 | `showAllIdentities` 长按触发 | 长按手势 `LongPress` | P0 |
| AI 主持人消息 | `AiMessageBubble`（在卧底页内集成） | 同等气泡组件 | P1 |
| 震动反馈 | `PlatformHelper.vibrateLongMethod()`（游戏开始时） | `@ohos.vibrator` | P0 |

### 2.4 阿瓦隆（Awalong）

**原实现**：`awalong/`（20 个文件，最复杂的游戏模块）

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 入口配置页 | `AwalongEntrance` + `AwalongCustomConfigScreen` | ArkUI 配置页面 | P0 |
| 规则弹窗 | `GameRulesDialog` | `CustomDialog` | P0 |
| 自定义配置 | `AwalongCustomConfig` + `DefaultCustomConfig`，支持角色选择/人数/扩展包 | 配置 Store + 表单 | P0 |
| 角色体系（完整） | `AwalongRole` 枚举：梅林/派希维尔/忠臣/莫甘娜/莫德雷德/刺客/爪牙/湖中仙女/莫高斯/变形者/奥伯伦/兰斯洛特 等 12+ 角色 | 完整枚举移植 | P0 |
| 角色技能视野逻辑 | `AwalongGameLogic.getVisibleRoles()`（梅林看坏人、派西维尔看梅林+莫甘娜、奥伯伦看红方等） | 纯逻辑直接移植 | P0 |
| 游戏状态机 | `AwalongGameState`（角色列表/天列表/当前页/湖中仙女持有者/莫高斯使用/变形者目标/刺杀结果/兰斯洛特转化） | 完整数据模型移植 | P0 |
| 每天实体 | `AwalongGameDayEntity`（主任务/任务结果/刺杀任务/队长/投票/队伍/技能记录/锁定玩家） | 完整移植 | P0 |
| 对局页（优化版） | `AwalongGamePageOptimized` | ArkUI 对局页 | P0 |
| 第0天页面 | `AwalongDayZeroPage`（夜晚角色睁眼顺序引导） | ArkUI 页面 | P0 |
| 任务执行阶段 | `TaskExecutionPhaseOptimized` + `PageDayTaskOptimized` | ArkUI 分步组件 | P0 |
| 身份卡片 | `AwalongIdentityCard` | 翻转卡片组件 | P0 |
| 玩家卡片 | `PlayerCard` | `ListItem` | P0 |
| 任务进度条 | `TaskProgressBar` | `Progress` 自定义 | P0 |
| 全部结果弹窗 | `AllResultsDialog` | `CustomDialog` | P0 |
| 特殊技能弹窗 | `SpecialAbilityDialog` | `CustomDialog` | P0 |
| 角色能力展示 | `RoleAbilityDisplay` + `RoleConfigurationDisplay` | ArkUI 展示组件 | P0 |
| 游戏阶段组件 | `GamePhaseComponents` | 按阶段渲染 | P0 |
| 持续提醒（凌晨阶段） | `PlatformHelper.startPersistentAlert()` / `stopPersistentAlert()` | `@ohos.multimedia.audioSystem` 或系统铃声 | P1 |

### 2.5 一夜终极狼人（Werewolf）

**原实现**：`werewolf/`（7 个文件）

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 入口配置页 | `WerewolfEntrance`（玩家数 3-10，昵称编辑） | ArkUI 配置页面 | P0 |
| 预设配置 | `WerewolfPresets.getPresetForPlayerCount()`（按人数自动配置角色配比） | 纯逻辑移植 | P0 |
| 角色体系（完整） | `WerewolfRole` 枚举：狼人/爪牙/化身幽灵/预言家/强盗/捣蛋鬼/酒鬼/失眠者/猎人/村民 等，每个含阵营/技能描述/夜间行动/行动顺序 | 完整枚举移植 | P0 |
| 阵营定义 | `WerewolfFaction`（狼人阵营/村民阵营） | 枚举移植 | P0 |
| 夜间技能逻辑 | `WerewolfGameLogic`（化身幽灵复制/预言家查验/强盗交换/捣蛋鬼交换/酒鬼换中央牌/失眠者终局查看） | 纯逻辑移植 | P0 |
| 对局页 | `WerewolfGamePage` | ArkUI 对局页 | P0 |
| 身份卡片 | `WerewolfIdentityCard` | 翻转卡片组件 | P0 |
| 规则弹窗 | 内置 `AppDialog`（`icon_info`） | `CustomDialog` | P0 |
| 离线传递引导 | `OfflinePassingGuideDialog` | `CustomDialog` | P0 |
| 主题色 | `WerewolfColors`（狼人专属配色） | 色值直接移植 | P1 |
| Intent 体系 | `WerewolfIntent`（MVI 意图） | Store action 移植 | P0 |

### 2.6 你画我猜（DrawGuess）

**原实现**：`ui/page/drawguess/DrawBoardPage.kt` + `ui/page/home/DrawGuessEntrance.kt`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 画板核心 | `DrawBoardPage`（Canvas + `detectDragGestures` + `PathState` 路径记录） | `Canvas` + `CanvasRenderingContext2D` + `PanGesture` + 路径数组 | P0 |
| 画笔颜色 | `presetColors`（10 色：黑/白/红/橙/黄/绿/蓝/深蓝/紫） | 色板组件 | P0 |
| 画笔粗细 | `brushSizes`（4f/8f/16f/24f/32f 五档） | 滑块或档位选择 | P0 |
| 橡皮擦 | `PathState.isEraser` | `globalCompositeOperation = 'destination-out'` | P0 |
| 绘制路径状态 | `mutableStateListOf<PathState>`（支持撤销/清空） | `@State paths: PathState[]` | P0 |
| 词库 | `DrawGuessWordLibrary`（动物/水果/物品/食物/运动/交通工具/建筑 7 类，每类 20 词） | 纯数据移植 | P0 |
| 自定义词库 | `customDrawWordLists`（支持注册/移除自定义词组） | preferences 持久化 | P1 |
| 顶部通用栏 | `CommonTopBar` | ArkUI 通用标题栏 | P0 |
| AI 主持人 | `AiMessageBubble` 集成 | 气泡组件 | P1 |
| 离线传递引导 | `OfflinePassingGuideDialog` | `CustomDialog` | P0 |
| 震动反馈 | `PlatformHelper.vibrateMethod()` | `@ohos.vibrator` | P0 |

### 2.7 猎巫镇（HuntTown）

**原实现**：`ui/page/hunttown/HuntTownPage.kt`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 配置阶段 | `HuntPhase.SETUP`（玩家数 4-12，女巫数配置） | ArkUI 配置表单 | P0 |
| 身份体系 | `HuntRole`（女巫/警长/村民） + `HuntPlayer`（存活/已翻身份） | 数据模型移植 | P0 |
| 游戏阶段机（完整） | `HuntPhase`：配置→夜晚闭眼→女巫睁眼→凌晨持续提示→警长守护→白天公布结果→白天讨论放逐→游戏结束 | 状态机移植 | P0 |
| 夜晚女巫选择目标 | `selectedMurderTarget` | 选择交互 | P0 |
| 警长守护选择 | `selectedProtectTarget` | 选择交互 | P0 |
| 昨夜死亡结果 | `lastNightDeath` | 结果展示 | P0 |
| 胜负判定 | `winnerText` | 逻辑移植 | P0 |
| 翻身份 | `HuntPlayer.revealed` | 卡片翻转动画 | P0 |
| 持续提醒（凌晨阶段） | `PlatformHelper.startPersistentAlert()` / `stopPersistentAlert()`（`DisposableEffect` 生命周期绑定） | `@ohos.multimedia.audioSystem`，页面 aboutToDisappear 时停止 | P1 |
| 玩家列表 | `LazyColumn` + `items` | `List` 渲染 | P0 |
| 离线传递引导 | `OfflinePassingGuideDialog` | `CustomDialog` | P0 |
| 注意：猎巫镇不支持单机模式 | 首页限制仅 LAN/ONLINE | 同等限制 | P0 |

### 2.8 随机工具（RandomPage）

**原实现**：`ui/page/random/`（5 个文件，功能最杂的模块）

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 配置列表（横向滚动） | `RandomConfigList` + `LazyRow` + `RandomConfigCircleItem`（圆形图标项） | `List` horizontal + 自定义圆形项 | P0 |
| 默认选中首个工具 | 进入时自动选中答案之书 | `aboutToAppear` 设置默认 | P0 |
| 工具排序规则 | 答案之书第一 → 指转盘第二 → 其余按序 | 同等排序逻辑 | P0 |
| 长按编辑模式 | `combinedClickable.onLongClick` 切换编辑态 | `LongPress` 手势 | P0 |
| 删除配置项 | 编辑模式下右上角删除按钮 | 图标按钮 | P0 |
| 编辑配置项 | 编辑模式下居中编辑按钮 | 图标按钮 + `CustomDialog` | P0 |
| 系统默认保护 | 答案之书/指转盘不可删除编辑 | `isSystemDefault` 判断 | P0 |
| 新增配置弹窗 | `AddNewRandomDialog` + `AddNewRandom.kt` | `CustomDialog` | P0 |
| 编辑配置弹窗 | `EditRandomDialog`（名称/类型/内容编辑） | `CustomDialog` | P0 |

**工具类型（RandomCate 枚举，6 类）**：

| 类型 | 原实现 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| **骰子（DICE）** | `DiceAnimation` + `RollDiceAnimation.kt`（范围骰子，显示起止数值） | `Canvas` 3D 骰子 + `animateTo` 旋转 | P0 |
| **硬币（COIN）** | `RollCoinAnimation.kt`（正反面文本，翻转动画） | `Canvas` + `rotate` 翻转 | P0 |
| **转盘（WHEEL）** | `WheelRandomComponent`（`Canvas` 圆形转盘，加权随机，旋转动画） | `Canvas` + `CanvasRenderingContext2D` 绘制扇形 + `animateTo` 旋转 | P0 |
| **卡牌（CARD）** | `FlippableCard`（3D 翻转，正面/背面内容，一键翻转） | `Canvas` + `rotateX` + `@State flipped` | P0 |
| **答案之书（ANSWER_BOOK）** | `AnswerBookPage` + `AnswerBookData`（105 条答案，正面/中性/负面分类） | 纯数据移植 + 展示页 | P0 |
| **指转盘（FINGER）** | `FingerSpinnerComponent`（系统默认，不可编辑删除） | `Canvas` 旋转 + 指针 | P0 |
| **洗牌动画** | `AnimatedShuffleContent`（卡牌聚拢/旋转/3D 效果） | `animateTo` + `transform` | P1 |
| **文本骰子** | `TextDiceAnimation`（用户输入文本而非数字的骰子） | 同等实现 | P1 |

### 2.9 LAN 局域网联机

**原实现**：`ui/page/lan/`（5 个页面） + `shared/.../lan/`（跨平台 LAN 核心）

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| LAN 入口页 | `LANEntrancePage`（5 种游戏类型入口卡片） | ArkUI 入口页 | P0 |
| 房间发现页 | `LANRoomDiscoveryPage`（扫描局域网房间列表） | `@ohos.net.socket` UDP 广播 + `ServiceDiscovery` 等价实现 | P0 |
| 加入房间弹窗 | `JoinRoomDialog`（密码校验） | `CustomDialog` | P0 |
| 创建房间页 | `LANCreateRoomPage`（房间名/游戏类型/人数/密码） | ArkUI 表单页 | P0 |
| 房间大厅页 | `LANRoomLobbyPage`（玩家列表/准备/开始） | ArkUI 大厅页 | P0 |
| 服务发现（mDNS/NSD） | `ServiceDiscovery` 接口 + Android `NsdManager` 实现 | `@ohos.net.mdns`（鸿蒙 mDNS API） | P0 |
| 主机服务器 | `LANHostServer` 接口 + Android Socket Server | `@ohos.net.socket` TCP Server | P0 |
| 客户端 | `LANClient` 接口 + Android Socket Client | `@ohos.net.socket` TCP Client | P0 |
| 房间管理器 | `LANRoomManager`（单例，管理发现/连接/玩家/错误/游戏状态更新） | 鸿蒙单例 Store | P0 |
| 消息协议 | `LANMessageType` 枚举（16 种：发现/加入/离开/状态同步/游戏动作/聊天/心跳/开始/结束等） + `LANMessage` 序列化 | 完整协议移植 | P0 |
| 数据模型 | `LANRoomInfo`/`LANPlayer`/`LANRoomState`/`LANConnectionState`/`LANGameState`/`LANError` | 完整数据类移植 | P0 |
| 连接状态机 | `ConnectionStatus`（断开/发现中/连接中/已连接/重连中/错误） | 状态机移植 | P0 |
| 错误码 | `LANErrorCodes`（9 种：房间不存在/已满/密码错误/被踢/主机断开/超时等） | 完整移植 | P0 |
| 心跳机制 | `HEARTBEAT_INTERVAL = 5000ms` | 定时器 | P0 |
| 发现广播间隔 | `DISCOVERY_BROADCAST_INTERVAL = 3000ms` | 定时器 | P0 |
| 默认端口 | `DEFAULT_DISCOVERY_PORT = 37668` | 同等端口 | P0 |

### 2.10 AI 主持人

**原实现**：`service/ai/`（7 个文件）

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| AI 开关 | `AiConfig.isEnabled`（设置页 Switch） | `Toggle` | P1 |
| 服务商选择 | `AiProvider` 枚举：DeepSeek / 本地预设(FALLBACK) | 同等枚举 | P1 |
| API Key 输入 | DeepSeek 时显示，密码遮罩 + 显隐切换 | `TextInput` password 类型 | P1 |
| 回复风格 | `AiStyle` 枚举：幽默风趣 / 严肃专业 / 毒舌犀利 | 同等枚举 | P1 |
| AI 服务接口 | `AiService`（chat/isAvailable） | 鸿蒙接口 + 实现 | P1 |
| DeepSeek 实现 | `DeepSeekProvider`（调用 `https://api.deepseek.com`） | `@ohos.net.http` 请求 | P1 |
| 本地预设实现 | `FallbackAiService`（离线预设回复） | 纯逻辑移植 | P1 |
| 服务工厂 | `AiServiceFactory`（按 provider 创建实例） | 工厂模式移植 | P1 |
| 提示词模板 | `GamePromptTemplates`（各游戏场景的系统提示词） | 纯文本数据移植 | P1 |
| AI 消息气泡 | `AiMessageBubble` 组件 | ArkUI 气泡组件 | P1 |
| 加载状态 | `isLoadingAi` StateFlow | `@State isLoading` | P1 |
| 状态提示 | 设置页显示"已就绪/未设置 Key/本地预设模式" | 同等 UI | P1 |

### 2.11 统计/信息中心

**原实现**：`ui/page/stats/StatsPage.kt` + `data/GameStatsManager.kt`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 页面标题 | "信息中心" | ArkUI Text | P0 |
| 游戏概览卡 | 总局数/总人次/最近游玩时间 | StatsCard 组件 | P0 |
| 各游戏局数统计 | `GameStatsManager.countByGameMode()` | 统计 Store | P0 |
| 总局数 | `GameStatsManager.totalGames()` | 统计 Store | P0 |
| 总参与人次 | `GameStatsManager.totalPlayerParticipations()` | 统计 Store | P0 |
| 最近游玩时间 | `GameStatsManager.lastPlayedTime()`（刚刚/X分钟前/X小时前/X天前/更早） | 时间格式化移植 | P0 |
| 数据持久化 | `GameStatsManager`（基于 MMKV 存储对局记录） | `@ohos.data.preferences` 或 `relationalStore` | P0 |

### 2.12 主题与设计系统

**原实现**：`theme/`（5 个文件）

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 三种主题模式 | `ThemeMode` 枚举：SYSTEM / LIGHT / DARK | `AppStorage` 全局状态 | P0 |
| 深色配色方案 | `DarkColorScheme`（完整 20+ 色值定义） | 色值逐项移植到 ArkUI 暗色资源 | P0 |
| 浅色配色方案 | `LightColorScheme`（完整 20+ 色值定义） | 色值逐项移植到 ArkUI 亮色资源 | P0 |
| 设计系统 | `AppDesignSystem`：AppColors/AppSpacing/AppCornerRadius/AppElevation/AppFontSize/AppIconSize 六大子系统 | TypeScript design tokens 对象 | P0 |
| 间距系统 | `AppSpacing`（4dp 网格：xs2/sm4/md8/lg12/xl16/xxl24/xxxl32/huge48） | 常量定义 | P0 |
| 圆角系统 | `AppCornerRadius`（button12/card16/dialog20/input10/badge8/chip8 等） | 常量定义 | P0 |
| 阴影系统 | `AppElevation`（card6/button4/dialog16/dropdown10/appBar6） | 鸿蒙 shadow tokens | P0 |
| 字体大小系统 | `AppFontSize`（caption12/body14/bodyLarge16/subtitle18/title20/headline24/display28） | 鸿蒙 fontSize tokens | P0 |
| 图标大小系统 | `AppIconSize`（navigation24/button20/listItem24/avatar40/largeAvatar64） | 常量定义 | P0 |
| 角色配色工具 | `getRoleColor()`（梅林=蓝队/派西维尔=info/莫甘娜=morandiPurple 等） | 纯函数移植 | P1 |
| 游戏渐变色 | `GameMode.gradientColors`（5 种游戏各自的起止色） | 色值移植 | P0 |
| 本地注入 | `LocalAppDesign`（`staticCompositionLocalOf`） | `@Provide` 全局提供 | P0 |

### 2.13 大富翁记账（Monopoly）

**原实现**：`ui/page/monopoly/MonopolyMoneyPage.kt`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 记账页面 | 玩家列表 + 金额增减 + 记录列表 | ArkUI 表单 + List | P2 |
| 玩家管理 | 增删玩家 | List 操作 | P2 |
| 金额操作 | 收入/支出/转账 | 表单交互 | P2 |

> **注**：大富翁在 `GameType` 枚举中存在（MONOPOLY），但未在首页 `GameMode` 中直接暴露，属于附属功能，优先级 P2。

### 2.14 在线房间（Room/Multiplayer）

**原实现**：`ui/page/room/RoomPage.kt` + `ui/page/multiplayer/`

| 功能点 | 原实现细节 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| 房间准备页 | `PrepairPage`（玩家数/房主标识/开始游戏） | ArkUI 页面 | P2 |
| 翻牌区 | `FlopArea` | 卡片翻转 | P2 |
| 成员列表 | `MemberList` | List 组件 | P2 |
| 联机页 | `MultiplayerPage`（在线联机入口，规划中） | ArkUI 页面 | P2 |
| WebSocket 通信 | `HttpClient` Ktor WebSocket + 远程服务器 `116.198.196.244:6688` | `@ohos.net.websocket`（若服务器保留） | P2 |

> **注**：在线联机在原项目标记为"规划中"（`OperationMode.ONLINE` description = "跨网络联机（规划中）"），优先级 P2。

### 2.15 共享组件库

**原实现**：`ui/components/` + `ui/animation/` + `ui/widget/` + `ui/picker/` + `ui/button/` + `ui/badge/`

| 组件 | 原实现 | 鸿蒙实现方案 | 优先级 |
|---|---|---|---|
| `AppDialog` | 通用对话框（标题/副标题/内容/动作区） | `CustomDialog` 封装 | P0 |
| `CommonTopBar` | 通用顶部栏（返回/标题/动作） | ArkUI 自定义组件 | P0 |
| `AiMessageBubble` | AI 消息气泡 | 自定义 `Column` | P1 |
| `AppChrome` | 应用外壳组件 | ArkUI 容器 | P0 |
| `OfflinePassingGuideDialog` | 离线传递手机引导弹窗 | `CustomDialog` | P0 |
| `OfflinePassingGuideCard` | 离线传递引导卡片 | 自定义组件 | P0 |
| `GameConfigSection` | 通用游戏配置区块 | 自定义 `Column` | P0 |
| `IdentitySelector` | 身份选择器 | 选择组件 | P0 |
| `LocalSpyIdentitySelector` | 卧底专用身份选择器 | 选择组件 | P0 |
| `SwipeableIdentityCard` | 可滑动身份卡 | `Swiper` / 手势 | P0 |
| `LocalSpySwipeableIdentityCard` | 卧底专用滑动卡 | 手势 + 翻转 | P0 |
| `WordImportDialog` | 词库导入弹窗 | `CustomDialog` + `TextInput` | P1 |
| `DiceAnimation` | 骰子动画 | `Canvas` 3D 骰子 | P0 |
| `RollCoinAnimation` | 硬币翻转动画 | `Canvas` + `rotate` | P0 |
| `RollDiceAnimation` | 骰子滚动动画 | `Canvas` | P0 |
| `DiceFace` | 骰子面（`ui/widget`） | `Canvas` 点数绘制 | P0 |
| `FlipCard` | 翻转卡片（`ui/widget`） | `rotateY` + 状态 | P0 |
| `TreeNode`（梅花） | 梅花树节点（`ui/widget/plum`） | 自定义绘制 | P2 |
| `ExtendedPicker` | 扩展选择器（`ui/picker`） | 鸿蒙 `Picker` | P0 |
| `Picker` | 基础选择器 | 鸿蒙 `Picker` | P0 |
| `SingleColumnPicker` | 单列选择器 | 鸿蒙 `TextPicker` | P0 |
| `TripleColumnPicker` | 三列选择器 | 鸿蒙 `TextPicker` 多列 | P0 |
| `CommonButton` / `Button` | 通用按钮（`ui/button`） | 封装 `Button` | P0 |
| `Badge` | 徽标（`ui/badge`） | 自定义 `Stack` | P1 |

---

## 3. 鸿蒙平台适配要点

### 3.1 权限声明（module.json5）

原项目涉及的能力对应的鸿蒙权限：

| 原项目能力 | 鸿蒙权限 | 说明 |
|---|---|---|
| 网络通信（Ktor/AI/LAN） | `ohos.permission.INTERNET` | HTTP/WebSocket/TCP 通信 |
| 震动反馈 | `ohos.permission.VIBRATE` | `vibrateMethod()` / `vibrateLongMethod()` |
| 持续提醒（铃声） | 无需特殊权限 | 使用 `@ohos.multimedia.audioSystem` |
| 局域网通信 | `ohos.permission.INTERNET`（UDP/TCP 已包含） | mDNS 发现 + Socket |
| 文件/词库存储 | 沙箱内无需权限 | `getApplicationContext().filesDir` |
| 屏幕保持常亮 | `ohos.permission.KEEP_SCREEN_ON` | 原 `KeepScreenOn` 调用 |
| 获取应用版本 | 无需权限 | `@ohos.bundle.bundleManager` |

### 3.2 生命周期映射

| 原 Android/Compose | 鸿蒙对应 |
|---|---|
| `Activity.onCreate` | `UIAbility.onCreate` |
| `Activity.onStart` | `UIAbility.onWindowStageCreate` |
| `Activity.onResume` | `UIAbility.onForeground` |
| `Activity.onPause` | `UIAbility.onBackground` |
| `Activity.onStop` | `UIAbility.onWindowStageDestroy` |
| `Activity.onDestroy` | `UIAbility.onDestroy` |
| Compose `DisposableEffect` | `aboutToAppear` / `aboutToDisappear`（组件级） |
| ViewModel `onCleared()` | UIAbility `onDestroy()` 中清理 Store |

### 3.3 状态管理映射

| 原 Compose | 鸿蒙 ArkUI |
|---|---|
| `MutableStateFlow` + `collectAsState()` | `@State` + `@Link` + `@Provide/@Consume` |
| `AppStorage`（全局） | `AppStorage` / `LocalStorage`（ArkUI 原生支持） |
| `CompositionLocalProvider` | `@Provide/@Consume` 跨层级传递 |
| `remember { mutableStateOf() }` | `@State`（组件内） |
| `LaunchedEffect` | `aboutToAppear` 中 `async` 操作 |
| `derivedStateOf` | `@Computed` 或 getter |
| 单一 `MainViewmodel`（62KB 巨石） | **按域拆分**：ThemeStore / GameStore / AiStore / LanStore / RandomStore / StatsStore |

> **重要架构调整建议**：原项目使用全局唯一 `MainViewmodel` 承载所有状态，在鸿蒙侧建议按业务域拆分为多个 Store，降低耦合，配合 `@Provide/@Consume` 或 `AppStorage` 分发。

### 3.4 数据存储映射

| 原 KMP 实现 | 鸿蒙对应 |
|---|---|
| MMKV（`shared/.../mmkv/MMKVUtils.kt`） | `@ohos.data.preferences`（KV 轻量存储） |
| multiplatform-settings（`Settings()` 封装） | `preferences.getPreferences(context, "name")` |
| 内存态 `mutableMapOf`（词库等） | TypeScript `Map` / `Record` |
| 对局统计持久化 | `preferences` 或 `@ohos.data.relationalStore`（若需复杂查询） |

**需持久化的数据清单**：
- AI 配置（enabled/provider/apiKey/style/baseUrl/timeout）
- 随机工具配置（`MMKV_RANDOM_CARDS_SETTING_KEY` + 配置名 → JSON）
- 随机工具标签列表（`MMKV_RANDOM_LABEL_NAME_KEY`）
- 主题模式（SYSTEM/LIGHT/DARK）
- 游戏配置记忆（玩家数/词组选择等）
- 对局统计数据
- 自定义词库（卧底词对 / 画猜词汇）

### 3.5 网络通信映射

| 原 KMP 实现 | 鸿蒙对应 |
|---|---|
| Ktor `HttpClient`（HTTP 请求） | `@ohos.net.http` `http.createHttp()` |
| Ktor `WebSockets` 插件 | `@ohos.net.websocket` `websocket.createWebSocket()` |
| `HttpClientEngine` 平台差异（Android/iOS/Wasm） | 鸿蒙统一用 `@ohos.net.http`，不再需要 expect/actual |
| AI 请求（DeepSeek API） | `@ohos.net.http` POST 请求 |
| 远程服务器地址 `116.198.196.244:8080/6688` | 保留或按需配置 |

### 3.6 导航映射

| 原 navigation-compose | 鸿蒙 Navigation |
|---|---|
| `NavHost(navController, startDestination)` | `Navigation(navPathStack)` |
| `NavHostController` | `NavPathStack` |
| `composable("route") { Screen() }` | `@Builder` + `navDestination()` |
| `navi.navigate("route")` | `navPathStack.pushPath({ name: "route" })` |
| `navi.popBackStack()` | `navPathStack.pop()` |
| 路由参数传递 | `pushPath` 的 `param` 字段 |

### 3.7 绘制与动画映射

| 原 Compose Canvas | 鸿蒙 Canvas |
|---|---|
| `Canvas { drawCircle/drawPath/drawLine/drawRoundRect }` | `Canvas(Context2D).getContext("2d")` + `fillRect/stroke/arc` |
| `DrawScope.rotate {}` | `ctx.rotate(angle)` |
| `graphicsLayer { rotationX/rotationY/translationX }` | `transform: rotateX/Y/translate` 或 `offset` + `rotate` 属性 |
| `animateFloatAsState` | `animateTo()` + `Curve` |
| `Animatable.animateTo()` | `animateTo({ duration, curve })` |
| `spring(dampingRatio, stiffness)` | `curves.springMotion()` / `curves.responsiveSpringMotion()` |
| `detectDragGestures`（画板） | `PanGesture` + `gestureEvent` 坐标采集 |

### 3.8 响应式布局

| 原 Compose | 鸿蒙 ArkUI |
|---|---|
| `fillMaxSize/fillMaxWidth/weight` | `width('100%')/height('100%')/layoutWeight(1)` |
| `Row/Column` | `Row/Column`（同名） |
| `Spacer(modifier = Modifier.height(x))` | `Blank().height(x)` 或 `margin` |
| `LazyColumn/LazyRow` | `List(direction: ...)` |
| `FlowRow`（自动换行） | `Flex(wrap: FlexWrap.Wrap)` |
| `Box/Stack` | `Stack` |
| `Padding(padding)` | `.padding({ top, bottom, left, right })` |
| 栅格断点（手机/折叠屏/平板） | `GridRow` + `GridCol`（鸿蒙栅格系统）或 `breakpoints` |

### 3.9 振动反馈映射

| 原 PlatformHelper | 鸿蒙实现 |
|---|---|
| `vibrateMethod()`（短震） | `vibrator.startVibration({ type: 'light' })` |
| `vibrateLongMethod()`（长震） | `vibrator.startVibration({ type: 'medium' })` 或预设效果 |

### 3.10 资源管理映射

| 原 Compose Resources | 鸿蒙资源 |
|---|---|
| `composeResources/drawable/`（图标 PNG/SVG） | `resources/base/media/` |
| `composeResources/values/`（字符串） | `resources/base/element/string.json` |
| `painterResource(Res.drawable.xxx)` | `$r('app.media.xxx')` |
| `DrawableResource` 引用 | `Resource` 类型 |

---

## 4. 不迁移/需特别处理项

### 4.1 明确不迁移

| 项目 | 原位置 | 原因 |
|---|---|---|
| **Wasm 专有代码** | `composeApp/src/wasmJsMain/` + `shared/src/wasmJsMain/` | 鸿蒙不需要 Web 目标 |
| **Wasm 字体子集化** | wasmJs 专有字体处理 | 鸿蒙使用系统字体或自定义字体资源 |
| **iOS 专有代码** | `composeApp/src/iosMain/`（若有） + `shared/src/iosMain/` + `iosApp/` | 鸿蒙不需要 iOS 目标 |
| **native 专有代码** | `composeApp/src/nativeMain/` | 桌面 native 目标，鸿蒙不需要 |
| **Android 专有代码** | `composeApp/src/androidMain/`（仅 `PlatformHelper` 实现） | 鸿蒙用自己的 `PlatformHelper` 实现 |
| **Gradle/AGP 构建系统** | `build.gradle.kts` / `settings.gradle.kts` | 鸿蒙用 DevEco Studio + hvigor |
| **在线联机后端依赖** | 远程服务器 `116.198.196.244` 若已停用 | 若服务器不可用，在线模式标记为"规划中" |

### 4.2 expect/actual 平台差异处理

原项目通过 KMP 的 `expect/actual` 机制实现平台差异。鸿蒙迁移时需用鸿蒙原生 API 重新实现以下 `expect` 声明：

| expect 声明（commonMain） | 原 actual 实现 | 鸿蒙实现 |
|---|---|---|
| `expect class PlatformHelper` | `androidMain/PlatformHelper.android.kt`（Context/Vibrator/MediaPlayer/PackageManager） | 鸿蒙 `PlatformHelper` 用 `@ohos.vibrator` / `@ohos.multimedia.audioSystem` / `@ohos.bundle.bundleManager` |
| `expect fun currentTimeMillis()` | `androidMain` → `System.currentTimeMillis()` | `Date.now()` 或 `@ohos.systemDateTime.getTime()` |
| `expect class MultiplatformHelper` | 各平台 actual | 按需实现 |
| `shared: expect class HttpClientEngine` | Android/iOS/Wasm 各有 actual | 鸿蒙用 `@ohos.net.http` 统一实现，**消除** expect/actual |
| `shared: interface ServiceDiscovery/LANClient/LANHostServer` + `createXxx()` 工厂 | Android/iOS/Wasm 各有 actual | 鸿蒙用 `@ohos.net.mdns` + `@ohos.net.socket` 实现 |

> **关键简化**：原项目为 4 个平台（Android/iOS/Wasm/Native）维护 4 套 actual。鸿蒙只需 1 套实现，可完全消除 expect/actual 机制，改为直接实现。

### 4.3 KMP 双模块结构处理

原项目为 `composeApp`（UI+业务） + `shared`（跨平台能力）双模块。鸿蒙迁移方案：

| 原模块 | 鸿蒙对应 |
|---|---|
| `composeApp/src/commonMain` | 鸿蒙工程 `entry/src/main/ets/pages/` + `entry/src/main/ets/components/` |
| `shared/src/commonMain`（网络/LAN/MMKV/数据） | 鸿蒙工程 `entry/src/main/ets/utils/` 或独立 `har` 模块 |
| 两模块的 expect/actual | 合并为单一鸿蒙实现，无需分层 |

**建议**：鸿蒙侧可保持"业务层 + 能力层"的代码组织，但不需要 Gradle 模块化，用目录划分即可。

---

## 5. 待确认问题

### 5.1 技术决策待确认

| # | 问题 | 影响 | 建议 |
|---|---|---|---|
| 1 | **MainViewmodel 拆分策略**：原项目单一 62KB ViewModel 在鸿蒙侧如何组织？ | 架构设计 | 建议按域拆分为 6 个 Store（Theme/Game/Ai/Lan/Random/Stats），通过 `AppStorage` + `@Provide` 分发 |
| 2 | **在线联机（ONLINE 模式）是否迁移？** 原项目标记"规划中"，但已有 WebSocket 通信代码和远程服务器地址 | 范围边界 | 建议 P2，先迁移单机+LAN，在线模式保留入口但标记"即将开放" |
| 3 | **远程服务器 `116.198.196.244` 是否仍在运行？** 若已停用，在线房间功能无法验证 | 功能完整性 | 需与原项目维护者确认 |
| 4 | **大富翁记账是否在首页暴露入口？** 原项目 `GameType` 有 MONOPOLY 但首页 `GameMode` 未列出 | UI 范围 | 建议 P2，若有现成代码可一并迁移 |
| 5 | **指转盘（FingerSpinner）的系统默认数据来源**：它是系统内置不可编辑的，具体数据存在哪？ | 数据迁移 | 需确认 `RANDOM_PAGE_SYSTEM_FINGER_SPINNER_NAME` 对应的内置数据 |
| 6 | **AI 主持人是否仅迁移 DeepSeek？** 原项目只有 DeepSeek + FALLBACK 两个 provider | AI 范围 | 建议两个都迁移，FALLBACK 保证离线可用 |

### 5.2 资源迁移待确认

| # | 问题 | 影响 |
|---|---|---|
| 7 | 原项目 `composeResources/` 下的图标资源（`icon_spy_together`/`icon_spy_awalong`/`icon_moon`/`icon_edit` 等）清单需完整盘点 | 资源完整性 |
| 8 | 原项目是否有自定义字体？Wasm 端做了字体子集化，鸿蒙侧字体策略需确认 | 视觉一致性 |
| 9 | 游戏卡片 Canvas 绘制的徽章图形（5 种）是否保留代码绘制，还是改为图片资源？ | 实现方式 |

### 5.3 体验层待确认

| # | 问题 | 影响 |
|---|---|---|
| 10 | **持续提醒（凌晨提示音）**：原项目用 Android `RingtoneManager.TYPE_ALARM`，鸿蒙侧用什么系统铃声 API？ | 猎巫镇/阿瓦隆体验 |
| 11 | **屏幕常亮**：原项目有 `KeepScreenOn` 调用（Room 页面），鸿蒙用 `@ohos.window.setWindowKeepScreenOn()` | 对局体验 |
| 12 | **折叠屏/平板适配深度**：是否需要针对大屏做专门的分栏布局，还是仅做响应式拉伸？ | 适配范围 |

---

## 6. 优先级总览

### P0（必须，MVP 交付）
- 导航框架 + 路由表（16 条路由）
- 首页游戏大厅（5 款游戏卡片）
- 谁是卧底（单机完整流程）
- 阿瓦隆（单机完整流程，含全部角色/任务/刺杀）
- 一夜终极狼人（单机完整流程，含全部角色/夜间技能）
- 你画我猜（画板 + 词库）
- 猎巫镇（完整阶段机，仅 LAN/ONLINE 入口）
- 随机工具（6 类全部：骰子/硬币/转盘/卡牌/答案之书/指转盘）
- LAN 局域网联机（发现/创建/加入/大厅全流程）
- 主题系统（三模式 + 完整设计系统）
- 数据持久化（配置/词库/统计）
- 平台能力（震动/版本信息）

### P1（重要，体验增强）
- AI 主持人（DeepSeek + 本地预设）
- 首页统计 Popup
- 持续提醒（凌晨提示音）
- 词库导入引擎
- 自定义词库管理
- 屏幕常亮

### P2（可后续迭代）
- 在线联机（ONLINE 模式，依赖远程服务器）
- 大富翁记账
- 梅花树组件（TreeNode）
- 在线房间（Room/Multiplayer）

---

## 附录 A：原项目文件规模参考

| 目录 | 文件数 | 说明 |
|---|---|---|
| `awalong/` | 20 | 阿瓦隆（最复杂） |
| `werewolf/` | 7 | 一夜终极狼人 |
| `ui/page/game/localspy/` | 10 | 谁是卧底 |
| `ui/page/random/` | 5 | 随机工具 |
| `ui/page/lan/` | 5 | LAN 联机页面 |
| `ui/page/home/` | 5 | 首页 |
| `ui/page/setting/` | 1 | 设置 |
| `ui/page/stats/` | 1 | 统计 |
| `ui/page/drawguess/` | 1 | 画猜画板 |
| `ui/page/hunttown/` | 1 | 猎巫镇 |
| `ui/page/monopoly/` | 1 | 大富翁 |
| `ui/page/room/` | 3 | 在线房间 |
| `theme/` | 5 | 主题系统 |
| `data/` | 12 | 数据层 |
| `intent/` | 6 | MVI Intent |
| `service/ai/` | 7 | AI 服务 |
| `navigation/` | 2 | 导航 |
| `ui/components/` | 11 | 共享组件 |
| `ui/animation/` | 2 | 动画 |
| `ui/widget/` | 3 | 小部件 |
| `ui/picker/` | 4 | 选择器 |
| `shared/src/commonMain/` | ~15 | 跨平台能力核心 |
| **合计 commonMain** | **~120+** | — |

## 附录 B：关键数据模型清单（须完整移植）

- `GameMode` 枚举（5 种游戏 + 渐变色）
- `OperationMode` 枚举（LOCAL/LAN/ONLINE）
- `GameEntity` / `LocalSpyEntity`
- `AwalongGameState` / `AwalongGameDayEntity` / `SkillUsageRecord` / `TaskExecutionRecord`
- `AwalongRole` 枚举（12+ 角色）
- `WerewolfRole` 枚举（10+ 角色）/ `WerewolfFaction` / `WerewolfPreset`
- `HuntRole` / `HuntPlayer` / `HuntPhase`
- `LANRoomInfo` / `LANPlayer` / `LANRoomState` / `LANConnectionState` / `LANGameState` / `LANError`
- `LANMessageType` 枚举（16 种消息类型）/ `ConnectionStatus` / `GameType`
- `AiConfig` / `AiProvider` / `AiStyle` / `AiRequest` / `AiResponse`
- `RandomItem` / `RandomListEntity` / `WheelItem`
- `AnswerBookEntry` / `AnswerCategory` / `AnswerBookState`
- `DrawGuessEntity` / `PathState`
- `WordGroup` / `WordLibrary` / `MonopolyEntity` / `MemberEntry`
- `ThemeMode` / `AppDesignSystem`（6 大子系统）
- `NaviRoute` 枚举（16 条路由）

---

*本 PRD 基于对原项目 `d:\Develop\Android\Projects\YiGameCopilotX` 源代码的实际阅读和验证编写。所有功能模块、文件路径、数据模型均来自代码事实，非臆造。*
