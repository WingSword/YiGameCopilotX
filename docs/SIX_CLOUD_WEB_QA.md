# 六种网络游戏与 Web 验收（2026-09-14）

## 交付范围

Android、Harmony 及 KMP Wasm Web 接入同一 Java 房间服务：谁是卧底、一夜终极狼人、猎巫镇简化版、桌游记账、阿瓦隆、你画我猜。Java 工程位于 Android 仓库 `server/`，本次增加 `AvalonGame.java`、`DrawingGame.java`；鸿蒙仍使用相邻工程的 ArkUI 实现，未声称已迁移至 Compose。

Android/Harmony 的默认服务保持 `http://8.133.216.39:8080`；本机 QA 地址只保存在测试设备设置里。Web 的 HTTPS 部署默认请求同域 API，交付包附有 Nginx 示例。浏览器使用本地存储中的随机成员令牌恢复玩家，不依赖硬件标识。具体规则、来源、接口与部署步骤见 [六种网络游戏与 Web 部署](WEB_AND_SIX_CLOUD_GAMES.md)和 [Java 服务说明](../server/README.md)。

## 自动检查

检查直接编译、运行生产规则和客户端代码；ArkTS 测试只替换系统适配器，网络整局使用真实 Java HTTP 服务。

| 层级 | 结果及覆盖 |
| --- | --- |
| 原有 Java 房间 | 137 项 HTTP 检查：鉴权、角色隔离、并发、重启、落盘失败回滚、限流、JSON。 |
| 一夜狼人 Java | 128630 项断言、640 个完整预设局，含身份交换、特殊角色、共同胜利、持久化和重复请求。 |
| 猎巫与记账 Java | 378675 项断言、432 个猎巫整局；记账权限、收款确认、整批入账/撤销、整数边界、并发、重启。 |
| 阿瓦隆与绘画 Java | 98056 项断言、328 个完整游戏：288 个阿瓦隆、40 个绘画局。覆盖 5–10/3–10 人、角色视野、任务人数、多数票和五次否决、第四任务双失败、刺杀、答案隔离、计时、同步笔画、重复指令、HTTP、CORS 与重启。 |
| 生产客户端 | Kotlin、ArkTS 各 30 项既有契约检查；狼人整局回归。两端分别任房主完成记账、猎巫、阿瓦隆、绘画整局；包含客户端/服务重启、丢失响应后重试、结果一致及再来一局清空。绘画重试只产生一笔笔画，整局均为每人 30 分。 |
| 同机模式 | Kotlin、ArkTS 各 640 个狼人完整局，卧底 4–16 人身份组合、猎巫、阿瓦隆、画板回归；两端记账各 76 个场景/人数用例，覆盖原子批量、撤销、旧数据与 CSV。 |

Java 日志中的 `AccessDeniedException` 为刻意制造的落盘失败回滚场景，测试最终成功。广覆盖来自规则及协议测试，不等同于每个组合都在实体设备逐个点击。

## 实际界面联调

Android API 34 模拟器、Harmony 模拟器和应用内浏览器连接本机服务。Web 使用正式优化后的 Wasm 构建。截图、当时的原生界面树与日志保存在 `artifacts/cloud-six-web/`。

你画我猜使用 WebPlayer、AndroidNative、HarmonyNative 三个真实客户端界面完成三轮：分别在 Web、Android、Harmony 画板作画，其他端看到同一笔画；鸿蒙通过界面猜中「章鱼」，Web 猜中「火车」，安卓错误猜词返回失败提示且不加分。90 秒到期由服务端结束回合，房主推进到总成绩，三端均得到 HarmonyNative 15、WebPlayer 10、AndroidNative 5。浏览器刷新后从入口恢复原成员、房间和私密词卡，无重复玩家。

绘画证据包括 `android-web-stroke.png`、`harmony-web-stroke.png`、`android-painted.png`、`harmony-painted.png`、`web-harmony-guessed.png`、`guess-submitted-android.png`、`web-drawing-final.png`、`android-drawing-final.png`、`harmony-drawing-final.png`。本 GUI 局的轮次通过计时结束；全员猜中自动结束、房主提前结束、并发与断线重试由前述规则和真实客户端测试覆盖。

阿瓦隆由安卓界面创建五人房间，Web、鸿蒙界面加入，另有两名明确的 HTTP 测试参与者。三端准备、分别查看私密身份并确认，全部确认后进入队长组队；三端提交组队票，组队票齐后公布结果并进入秘密任务。第一任务由安卓和测试参与者出征，第二任务由 Web 队长选择三个实际客户端，第三任务由鸿蒙队长选择安卓和 Web。三次任务均成功，鸿蒙刺客通过界面完成指认，三端一致结算为「善良阵营胜利：梅林存活」，之后才公开全员身份。非出征客户端没有任务投票按钮。中途安装最新安卓包、刷新最新生产 Web 后，仍恢复原成员并继续投票和任务。

阿瓦隆证据为根目录下 `android-avalon-*`、`web-avalon-team-vote.png`、`web-avalon-team-selection.png`、`web-avalon-restored-quest.png`、`web-avalon-final.png`，以及 `harmony-ui/harmony-avalon-*`。两名 HTTP 参与者不会被表述为额外真机。

本轮发现并修复 Web 中文字体资源为空导致的缺字，随包加入 Noto Sans SC 与许可证，移除外部字体依赖。修正 Web 时间为实际时间戳，限制宽屏房间内容宽度。两套绘画页面都只在当前玩家可绘画时接收画笔手势，避免观看者滚动被画板截获；依据 [Compose 官方手势说明](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures)复核事件处理。

最终 APK 和生产 Web 另做观察者回归：从安卓画板内部向上滑动，画板上边界由 y=1214 移到 y=836，下方积分和「离开房间」正常显示；Web 在画板区域滚动后也可到达下方操作。证据为 `android-observer-before/after.png` 及对应界面树、`web-observer-scroll.png`；鸿蒙先前修复的证据为 `harmony-scroll-fixed.png`。

此前全部游戏的同机界面检查及修复见 [同机流程检查](ONE_NIGHT_CLOUD_AND_OFFLINE_QA.md)，本机/云端记账和猎巫界面证据见 [云端记账与猎巫验收](CLOUD_LEDGER_HUNT_QA.md)。本轮重新运行对应规则和客户端回归。

## 构建、复核与交付

构建日志 `android-web-final-build.log`、`harmony-build.log` 对应 Android debug APK、Harmony 签名 debug HAP、生产 Wasm Web。Java 构建和全部服务测试在 `server-tests.log`；客户端和同机日志分别为 `client-contracts.log`、`offline-tests.log`。跨端检查包含共享设计、6010 个随机工具用例、10206 个圆桌边界检查及 12 组代码变更复核，结果为 `cross-platform.log`。

可重跑：

```text
powershell -ExecutionPolicy Bypass -File server/build.ps1
python scripts/test-cloud-clients.py
python scripts/test-offline-games.py
python scripts/check-cross-platform.py
python scripts/package-game-verification.py --suite cloud-six-web
```

交付目录 `artifacts/cloud-six-web/release/` 包含两端 debug 安装包、JAR、带源码和部署配置的服务 ZIP、Web ZIP、说明、报告、SHA256 校验和与清单。包装脚本先检查回归日志，再从明确目录和文件列表打包，排除本机服务数据、玩家令牌和管理凭据。

本轮未更新现有公网服务。上线前需要部署新 JAR，Web 还需域名、HTTPS 和同域 API 代理；Nginx 示例需在实际服务器填写域名与证书后运行配置检查。手机安装包是本地验证用 debug 构建，并非应用商店签名发布包。未将模拟器和本地服务结果称为所有真机、浏览器版本及公网弱网均已验证。猎巫仍是项目简化版；绘画以抬笔同步、没有内置语音；账本为房间当前局，下一局前需导出需要保留的记录。
