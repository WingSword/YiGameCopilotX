# Android / Harmony 同步开发

现有 Android 使用 Compose，Harmony 使用 ArkUI。两端可以同步开发；仅复制主题色无法保证页面或功能一致。本次采用“共享定义生成 + 原图复用 + 原生实现配对检查”，先落地随机工具，再逐项扩展规则共享范围。

## 已经共用的内容

| 内容 | 唯一来源 | 两端如何使用 |
| --- | --- | --- |
| 主题、间距、圆角、字体 | Android `theme/DesignSystem.kt`、`theme/Theme.kt` | 生成 Harmony 对应类，109 个参数及 24 个主题角色 |
| 工具图标与分类映射 | Android `RandomCate` 和 `composeResources/drawable` | PNG 直接复制，VectorDrawable 转换为 SVG；手指转盘与答案之书使用新的共享矢量图；自动生成 ArkUI 映射 |
| 骰子/硬币的卡片、点阵、颜色、旋转时长 | `cross-platform/random-tools.json` | 生成并实际引用 Kotlin / ArkTS 的 `RandomToolDesign` |
| 骰子范围、结果映射、硬币正反概率 | 同一 JSON 规则参数和生成器 | 生成 `RandomToolRules`；Android `MainViewmodel`、Harmony `RandomStore` 实际调用 |
| 行为边界和固定随机输入 | JSON 的 `cases` + `scripts/test-random-parity.py` | 编译运行生产 Kotlin 规则，转译运行生产 ArkTS 规则，对照 6,010 次输入 |
| 狼人纯规则与云房间数据模型 | `shared/commonMain` 的 `werewolf/` 与 `online/` | Android 直接引用，云端通过统一协议下发操作；Harmony 暂保留原生 Store，以整局测试和边界用例核对，为后续 Compose 适配提供模块边界 |

随机源仍由各平台提供。相同输入和相同随机样本得到相同结果；两个设备分别投掷时并不要求得到同一点数。测试还检查倒序范围、空值、小数、越界范围、正反概率边界、非法随机样本和点数分布。

## 每次开发的操作

在 Android 工程根目录运行：

```powershell
# 修改共享定义或原图后，生成两端文件并检查
python scripts/check-cross-platform.py --sync

# 日常开发 / 提交前，检查已经生成的内容是否漂移
python scripts/check-cross-platform.py
```

默认寻找相邻的 `YiGameCopilotX-Harmony`，也可传 `--harmony <路径>`。需要 Python、JDK、Node、已经构建过该项目的 Kotlin 编译器缓存和 DevEco SDK；测试不安装新依赖。可通过 `JAVA_HOME`、`GRADLE_USER_HOME`、`DEVECO_HOME`、`ARKTS_TYPESCRIPT` 调整本机位置。

修改页面或业务逻辑时，从 `cross-platform/features.json` 找到对应功能，在相关平台同一轮完成实现与验证。脚本覆盖原有 11 组功能，并新增 Web，共 12 组路径；新增功能必须加入映射。Web 组检查 Wasm 平台实现、入口、构建、主题、字体和许可证；二进制字体按原字节计算哈希。

云房间协议与部署说明位于 `server/README.md`。两端调用同一 Java 服务，创建与加入持久化并复用请求标识以支持超时重试，房间视图只携带当前成员的私有身份。协议修改需要同时更新两个客户端并运行 Java HTTP 集成测试及 `python scripts/test-cloud-clients.py`。根据 2026-09-09 的接口文档，两端默认连接 `http://8.133.216.39:8080`，保留自定义服务器入口和已有会话地址。

2026-09-11 已增加云端一夜终极狼人，发牌、行动、投票和结算均由 Java 服务决定。游戏规则变更还需要运行 `python scripts/test-offline-games.py`；此次同机流程修复、模拟器证据和覆盖范围见 `docs/ONE_NIGHT_CLOUD_AND_OFFLINE_QA.md`。

2026-09-14 两端增加云端桌游记账与猎巫镇单身份简化玩法。通用游戏视图为 `CloudGameView`，记账模型独立为 `CloudLedgerModels.kt`；Harmony 使用对应 ArkTS 模型。涉及金额、权限、幂等请求、批量撤销的改动必须同时验证 Java、两端生产客户端及本机记账规则，不能只做页面编译。当前实现、原版规则差异、测试证据和账本保留范围见 `docs/CLOUD_LEDGER_HUNT_QA.md`。

原生文件有变化时，检查会返回非零退出码并列出变更文件。完成两端核对后，记录具体证据，例如：

```powershell
python scripts/check-cross-platform.py --record-review random-tools --note "两端编译通过；折叠、横屏、大竖屏截图复核；连续投掷与结果一致"
```

若改动只适用于某平台，在说明中写明原因和验证，例如鸿蒙光感导航或 Android 权限入口。`review-state.json` 记录内容哈希及说明；它用于发现未核对的修改，不能证明任意页面像素一致、业务完全等价。未复核功能的初始状态是 `tracking-started`，不是“全部功能已通过”。

脚本目前需要主动运行，没有修改本机全局 Git hook，也没有虚构已接入远程 CI。后续 CI 同时检出两个工程后，可以运行同一条检查命令，非零退出码阻止合入。这个本地 Harmony 目录目前没有 `.git`，如需要两个工程独立发布和 CI，应先纳入版本管理，或统一放在一个仓库管理。

## 每个功能的验收

共享定义先改一处并重新生成；原生页面改完后分别编译。视觉核对使用相同配置和主题，检查工具选择、初始状态、结果状态及折叠/展开/旋转。功能检查包含新增/编辑/删除、重复点击、切页中断、空数据及持久化；涉及联机时增加双设备验证。

本次已经重做鸿蒙工具选择栏、骰子和硬币：原图、选中底色、左对齐结果与说明、白色渐变骰面、金银硬币、统一主题主按钮，以及大屏 560vp 卡片上限。小高度窗口允许卡片和模型缩小；鸿蒙继续保留左右工具布局与光感导航。动画由各自原生渲染器实现，曲线、字体光栅和阴影不会因此自动达到逐像素一致。

## 如果要求整套 UI 和功能只改一次

那需要进一步统一页面渲染和业务引擎，是架构迁移。当前两种原生 UI 代码不会因为增加脚本就自动互相转换。项目中的 `shared` 目录及 `ohosArm64` 目标也不能直接视作已经共用 Compose 页面；本轮检查的鸿蒙页面仍是 `.ets`，随机逻辑仍有独立 Store。

现阶段可以沿用已实现的生成和检查机制，把纯规则、默认配置、协议字段和测试用例逐项移到共享定义；页面保持配对修改。这样先防止遗漏与规则漂移，再决定是否承担统一渲染框架的迁移成本。
