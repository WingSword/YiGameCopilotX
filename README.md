# YiGameCopilotX

基于 Kotlin Multiplatform + Compose Multiplatform 的多端项目，覆盖 Android / iOS / Web(Wasm)。

Android 与相邻 Harmony 工程的同步流程、共享工具定义和检查入口见 [两端同步开发](docs/CROSS_PLATFORM_DEVELOPMENT.md)。提交前运行 `python scripts/check-cross-platform.py`。

网络房间支持手机/网页二维码邀请和参赛房主的玩家/管理员界面切换，所有成员可见模式提醒。见 [部署配置](docs/WEB_AND_SIX_CLOUD_GAMES.md) 和 [本轮验证报告](docs/ROOM_INVITATIONS_QA.md)；Java 工程在 [server](server/README.md)。

2026-09-16 已完成已部署云端的六游戏回归、网络界面风格统一及安卓答案之书翻页对齐；线上新接口的部署缺口和设备实测修复见 [云端与界面验证](docs/CLOUD_LIVE_UI_QA.md)。后续统一仓库及鸿蒙打包的边界见 [KMP 主线方案](docs/KMP_HARMONY_MAINLINE.md)。

## 项目结构

- `composeApp`：跨平台 UI 与主要业务（`commonMain` + `wasmJsMain` + `iosMain` + `androidMain`，并直接作为
  Android 应用模块）。
- `shared`：跨平台共享能力（网络、数据、工具等）。
- `iosApp`：iOS 工程入口。

## 环境要求

- JDK 17+（建议 17 或 21）
- Android Studio（支持 AGP 9）
- Android SDK（compileSdk / targetSdk 35）

## 快速开始（优先 Web）

1. 启动 Web 开发端（推荐）  
   `./gradlew runWeb`  
   或  
   `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`

2. 构建 Android Debug 包  
   `./gradlew :composeApp:assembleDebug`

3. 检查任务列表  
   `./gradlew tasks`

## Gradle/构建优化

- 升级 AGP 到 `9.0.1`。
- 升级 Gradle Wrapper 到 `9.3.0-rc-1`（高于 AGP 9 的最低要求 `9.1.0`）。
- Android 与 Kotlin 编译目标统一到 JVM 17。
- 启用 `parallel` / `caching` / `configuration-cache`。
- 新增根任务 `runWeb`，降低 Web 端调试启动成本。

## 常见问题

- 若下载 Gradle 发行包时报 SSL Handshake 错误，通常是网络或代理证书链问题。可先检查：
    - 系统代理/VPN 配置
    - 企业证书注入
    - 是否可直连 `https://services.gradle.org`
- 当前仓库默认已切换到华为镜像：
    - `https://mirrors.huaweicloud.com/gradle/gradle-9.3.0-rc-1-bin.zip`

## GitHub 签名构建

在仓库 Actions 中选择 **Build & Release APK**，使用 **Run workflow** 并选择需要打包的分支。该手动流程生成 Android 签名 Release APK，产物位于构建记录的 `apk` 附件；只有推送 `v*` 标签才会创建 GitHub Release。

仓库需配置 `KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD` 四个 Actions secrets。签名材料由构建环境临时使用，不提交到源码。当前流程仅打包 Android；Harmony 仍使用相邻 ArkUI 工程及本机签名构建，后续统一主线步骤见 [KMP / Harmony 方案](docs/KMP_HARMONY_MAINLINE.md)。

## 参考文档

- [Java 云端房间部署与 API](server/README.md)
- [云端记账、猎巫镇与双端验证](docs/CLOUD_LEDGER_HUNT_QA.md)
- [一夜狼人接入与所有游戏同机流程检查](docs/ONE_NIGHT_CLOUD_AND_OFFLINE_QA.md)
- [Android / Harmony 同步开发规范](docs/CROSS_PLATFORM_DEVELOPMENT.md)
- [最新云端实测、界面与网络恢复验收](docs/CLOUD_LIVE_UI_QA.md)
- [2026-09-06 双端优化、安装包及验证记录](artifacts/refinement-20260906/VERIFICATION.md)
- [Kotlin Multiplatform 文档](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Kotlin/Wasm](https://kotl.in/wasm/)
