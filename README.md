# YiGameCopilotX

基于 Kotlin Multiplatform + Compose Multiplatform 的多端项目，覆盖 Android / iOS / Web(Wasm)。

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

## 参考文档

- [Kotlin Multiplatform 文档](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Kotlin/Wasm](https://kotl.in/wasm/)