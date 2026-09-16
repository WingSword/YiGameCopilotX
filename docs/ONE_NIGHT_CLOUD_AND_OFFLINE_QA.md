# 一夜狼人接入与同机流程检查（2026-09-11）

## 实现位置

- 云端：`server/` 为独立 Java 17 项目，`server/dist/yigame-room-server.jar` 可直接运行；部署步骤见 `server/README.md`。
- Android 共享规则：`shared/src/commonMain/kotlin/org/walks/gamecopilot/werewolf/`，网络数据模型：同目录下 `online/CloudRoomModels.kt`。
- Android 页面与网络适配器保留在 `composeApp`；Harmony 使用相邻工程的 ArkUI 页面和 Store，同一云端协议已接入两端。
- 未自动部署到现有云服务器；管理员先替换 JAR，再让两端使用新客户端。

## 本次修复

| 游戏 | 检查链路及修复 |
| --- | --- |
| 谁是卧底 | 人数和空白牌边界、词语分发、实际揭牌计数、遮挡与换人、公布身份、再开局。修复减少卧底后空白牌越界、打开卡片即误计已查看、旧状态被闭包保留。两端提供点击揭牌与传机操作，鸿蒙复用公共卡片并隔离过期动画回调。 |
| 阿瓦隆 | 配置、逐人身份/视野、组队、任务提交、连续轮次、刺杀、结算、重开。两端五人配置补齐第五轮；Android 修复结果不刷新、结算自动消失、刺杀后锁定、结果说明错误，保留完整任务投票记录。 |
| 一夜终极狼人 | 配置、发牌、角色夜间行动、换牌回放、白天、秘密投票、猎人、阵营胜负、再开局。修复乱序查验、复制行动重复/时点、物理复制牌、白天泄露最终身份、猎人另选目标以及共同胜负。页面支持滚动，必须行动的角色不能被直接跳过。 |
| 猎巫镇 | 配置、逐人发牌、闭眼、女巫、凌晨提示、警长、白天公布、放逐、胜负、重开。补齐两端此前缺失的身份下发，限制每日一次放逐，已出局警长不能守护，夜间灭队后直接结算。 |
| 你画我猜 | 词卡、换词、画板、画笔/橡皮/背景、撤销重做清空、结束取消/确认、下一局。Android 实测发现 `SnapshotStateList.removeLast()` 在 API 34 上抛出 `NoSuchMethodError`，改用 `removeAt(lastIndex)`；画布和图标按钮补齐语义标签以便访问与回归。 |

## 可重跑检查

```text
powershell -ExecutionPolicy Bypass -File server/build.ps1
python scripts/test-cloud-clients.py
python scripts/test-offline-games.py
python scripts/check-cross-platform.py
```

规则测试编译运行实际 Kotlin 源码、转译运行实际 ArkTS Store；只替换操作系统适配器。Java 640 局、Kotlin 640 局、ArkTS 640 局，涵盖 3–10 人预设。另测特殊角色、身份牌守恒、猎人、共同胜利、无胜方、卧底 4–16 人全部身份/空白组合、猎巫镇 4–12 人和阿瓦隆 2:2 后第五轮。

跨端联机测试使用 Java 服务、生产 Kotlin 客户端、生产 ArkTS Store，包含发牌到结算、重复请求、服务和客户端重启、下一局清空、旧局请求拒绝和离局中止。它验证 HTTP 与业务链路；设备上的界面操作单独记录在 `artifacts/one-night-20260910/`。

## 本轮验证结果

Android `assembleDebug`、Harmony `assembleHap` 均通过；Java 128,630 项狼人断言及已有 HTTP 服务测试通过；两端客户端各 30 项契约检查、跨端真实服务整局通过；Kotlin 和 ArkTS 离线规则检查通过。跨平台检查的共享设计、随机工具 6,010 个用例、圆桌边界及 11 组源代码复核记录通过。

| 实际界面操作 | Android 模拟器 | Harmony 模拟器 |
| --- | --- | --- |
| 卧底 | 4 人逐人揭牌、公布、重开 | 6 人逐人揭牌、公布、重开 |
| 阿瓦隆 | 5 人揭牌、2/3/2 人连续任务、刺杀、结算、重开 | 5 人揭牌、2/3/2 人连续任务、刺杀、结算、重开 |
| 狼人 | 3 人揭牌、独狼查底牌、无技能玩家传机、投票、结算、重开 | 3 人揭牌、强盗换牌、无技能玩家传机、投票、结算、重开 |
| 猎巫镇 | 8 人揭牌、两轮夜晚/放逐、胜负、重开 | 8 人揭牌、两轮夜晚/放逐、胜负、重开 |
| 画板 | 词卡/换词、绘画、撤销/重做、橡皮、清空、结束取消/确认 | 词卡/换词、绘画、撤销/重做、橡皮、颜色/背景面板、清空、结束取消/确认 |

`ui/` 为 Android 截图与当时的界面树，`harmony-ui/` 为 Harmony 对应证据。Android 撤销曾实测闪退，修复后的截图为 `drawing-undo.png`、`drawing-redo.png`、`drawing-clear.png`；当前应用进程的崩溃日志为空，原始故障保留在 `artifacts/one-night-drawing-crash-before.log`。两端猎巫镇完整流程还可通过 `scripts/qa-hunt-round.py` 重跑。

本轮使用模拟器和本地 Java 服务；跨端网络整局使用真实客户端代码与真实 HTTP，未将其表述为三台真机同步点击验证。人数、特殊角色及异常请求的广覆盖来自规则和契约测试，界面操作覆盖上表代表配置。

## 后续 Compose 适配准备

本轮将无平台依赖的狼人规则/模型及云房间 DTO 放入现有 KMP `shared/commonMain`，包名和调用接口保持兼容。Android Compose 页面只负责展示与输入，云端 `options` 决定可执行动作。以后替换 Harmony 页面渲染层时可复用这部分 Kotlin 规则与协议模型。

当前 Harmony 仍为 ArkTS 原生实现；`ohosArm64` 目标并不等于已完成 Compose 鸿蒙适配。本轮不引入新框架、不改签名体系。两端原生规则通过相同边界场景检查，后续修改继续更新 `cross-platform/features.json` 和复核记录。

网络模式提供 3–10 人预设；自定义网络牌组目前只在协议层开放。同手机狼人按规则顺序传机，界面不公开行动角色，但玩家仍应遮挡屏幕；单机局面仍遵循现有内存生命周期，应用进程被杀后不会像网络房间那样恢复云端局面。
