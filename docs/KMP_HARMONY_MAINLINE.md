# 以 KMP 工程作为 Android / Harmony 统一主线

核查日期：2026-09-16。本文记录当前代码与后续实施步骤；本轮没有搬动 Harmony 工程，也没有更换 Kotlin、Compose 或鸿蒙构建工具链。

## 结论与范围

可以让当前 `YiGameCopilotX` 仓库成为 Android、Web、Java 服务和 Harmony 的唯一开发主线，同一次代码变更、同一版本记录和同一构建入口交付各端。

第一阶段应保留已经运行的 ArkUI 鸿蒙应用，将它纳入主仓库并继续配对开发。这样能够从同一分支打出 Android APK、Web 资源和 Harmony HAP，但 Harmony 仍由 Hvigor 构建。将现有 Compose 页面直接编译到鸿蒙属于下一阶段的渲染与工具链迁移，不能通过添加一个 Gradle 任务就自动完成。

## 当前工程证据

| 检查项 | 当前内容 | 对迁移的影响 |
| --- | --- | --- |
| `composeApp/build.gradle.kts` | 配置 Android、iOS、Wasm；没有 OHOS UI target 或鸿蒙宿主桥接 | 当前 Compose 应用无法直接生成 HAP |
| `shared/build.gradle.kts` | `linuxArm64("ohosArm64")`，产物类型为 Native shared library，HTTP 使用 Curl | 名称包含 OHOS 不代表已经使用鸿蒙工具链；现有“ELF ABI 兼容”注释不能替代实际链接和真机运行验证 |
| `shared/src/ohosArm64Main/.../mmkv/PlatformSettings.ohos.kt` | 实际使用 `InMemorySettings` | 直接用于正式鸿蒙应用会丢失重启后的设置、房间会话等数据，需要接入系统 Preferences |
| 同目录下 `lan/client`、`lan/server`、`lan/discovery` | 三个平台入口均抛出 `UnsupportedOperationException` | 局域网能力尚未实现；异常中的兼容 APK 提示也不能作为原生 HarmonyOS 的解决方案 |
| `build_ohos_check.gradle.kts` | 临时探测草稿，未接入 `settings.gradle.kts`，未创建实际应用打包流程 | 不能把此文件视为鸿蒙编译已经接通的证据 |
| 相邻 `YiGameCopilotX-Harmony/entry` | 页面、Store、平台服务为 ArkTS / ArkUI；`oh-package.json5` 未声明 KMP 依赖；未找到 C++ / `libshared` 桥 | 正式 HAP 当前仍使用独立的原生实现 |
| 相邻 Harmony 目录版本管理 | 本次检查没有 `.git`；目录位于主仓库之外 | 单独保留该目录不能保证同一分支检出后可复现所有平台 |
| `scripts/build-harmony-device.py` | 主仓库调用相邻 Harmony 工程的 Hvigor `assembleHap`，临时使用设备调试签名，完成后恢复配置 | 已有可复用的主工程打包入口，不需要重复包装一份相同脚本 |
| `scripts/build-harmony-release.py` | Hvigor `assembleApp`，检查 Release SDK、版本、包签名和发布描述文件 | 正式交付验证已有基础，应保留这些检查 |

项目当前版本为 Kotlin `2.3.20`、Compose Multiplatform `1.9.3`、AGP `9.0.1`。这组版本来自 `gradle/libs.versions.toml`；不能假设社区鸿蒙分支与其二进制兼容。

## 第一阶段：同仓、同分支，保留 ArkUI

建议目录结构如下，`harmonyApp/` 是后续目标目录，本轮尚未创建：

```text
YiGameCopilotX/
  composeApp/       Android / Web / iOS Compose 页面
  shared/           KMP 纯规则、协议和客户端公共代码
  harmonyApp/       当前 ArkUI 鸿蒙应用及其 Hvigor 工程
  server/           Java 房间服务
  cross-platform/  两端共享定义、功能映射和复核记录
  scripts/         生成、测试、各端构建和产物校验
```

实施顺序：

1. 先建立迁移前基线。记录两端版本、源码状态及现有 APK/HAP 的校验值；运行当前跨端检查与游戏回归。保留相邻 Harmony 原目录，待新目录编译、安装和回归通过后再决定停用旧入口。
2. 按源码清单将 Harmony 工程导入 `harmonyApp/`：包括 AppScope、entry 源码及资源、Hvigor/依赖配置和必要工程文件。排除 `build/`、`.hvigor/`、`oh_modules/`、SDK、`.release-tools/`、IDE 缓存、artifacts 及本机签名材料。保留引用的第三方许可证。
3. 将本机相关配置拆为可提交模板和本地覆盖。仓库不包含签名口令、私钥、发布证书描述文件或开发机绝对路径；本地/CI 从受控配置注入实际值。生成的 `build-profile.json5` 要保持 Hvigor 可读，且不能通过打印配置泄露签名材料。
4. 调整已有打包脚本的工程定位：优先读取显式 `--harmony` / 环境配置，其次采用仓库内 `harmonyApp/`；迁移过渡期可以兼容旧相邻目录。工具位置、输出目录同样参数化，保留调试签名恢复和正式包完整校验。不要将 Hvigor 伪装成 Kotlin Native 编译结果。
5. 更新依赖旧相邻路径的脚本与文档。至少检查 `check-cross-platform.py`、客户端/离线测试、产物打包、设计资源生成和两个 Harmony 构建脚本。现有跨端检查可显式使用 `--harmony <路径>`；尚未提供的构建参数需要先实现，不能提前写成已经可用的命令。
6. 统一版本记录和交付清单。一次发布记录同一源码版本及 Android/Harmony/Web/Java 对应产物的 SHA256；保留各自平台的合法版本字段、签名流程和部署步骤。协议和功能变更需在同一轮完成客户端配对及 Java 回归。
7. 在新目录完成干净构建、双端安装和三端联机。CI 再接入同一检查链，只有必需产物与测试全部成功才输出可发布包。当前仓库尚未证明已配置这种 CI，不能将计划描述为已上线能力。

第一阶段验收标准：从同一分支检出，加上已声明的 SDK/本地签名配置，即可在不依赖旧相邻 Harmony 源码目录的情况下构建 Android 和 Harmony；应用行为保持现有功能，跨端检查仍能发现未配对的代码变更。

这一阶段继续沿用 [跨端开发规范](CROSS_PLATFORM_DEVELOPMENT.md)：共享规则、协议、主题和资源优先只维护一份；Android Compose 与 Harmony ArkUI 页面仍需要配对实现。源码同仓不会自动消除两套 UI。

## 第二阶段：ovCompose 单页面验证

JetBrains 当前官方 Compose Multiplatform 支持列表没有 HarmonyOS。腾讯 ovCompose 示例展示了 OHOS 工具链与宿主接入路径；Kuikly 也支持鸿蒙，但其 Compose 包名和运行方式有自身适配，不能当作当前 `androidx.compose` 工程的直接替换。

建议先用独立验证模块接入答案之书或随机工具页，继续保留 ArkUI 应用和当前 Android/Web 构建作为回归基线。具体步骤：

1. 锁定所选 OHOS Kotlin fork、Compose fork、Skiko/桥接 HAR、Gradle/AGP 和 DevEco SDK 的兼容组合；记录发布来源和许可证。先验证示例工程能够在当前开发机和目标设备运行，再评估与现有 `2.3.20 / 1.9.3 / 9.0.1` 的差异。不要为了验证页直接降级整个应用。
2. 建立鸿蒙宿主到共享 UI 的入口、Native 库和资源打包。验证冷启动、退出、返回、旋转/窗口变化、应用前后台及销毁重建；确认没有不可回收的渲染器或协程。
3. 接入一个真实页面及其生产数据，保持按钮、文字、动画和无障碍行为。答案之书可直接覆盖封面、520ms 展开、300ms 合上再展开、快速点击、离页再进入、单页/双页、大字体、深色和矮屏滚动。
4. 再验证系统能力：可持久化的 Preferences、网络引擎、会话恢复、剪贴板、震动、网络权限、时钟和局域网发现/连接。云房间需要进一步检查身份私密性、超时重试、管理员提示、二维码和浏览器加入流程；不能用内存 Settings 或 LAN 占位实现作为验收结果。
5. 跑完当前六款云游戏、单机/同机回归和跨端对比，测量启动、内存和动画表现；建立明确可回退到 ArkUI 页的开关或独立构建。仅在这些结果可接受后，逐页扩展共享 UI 范围。

该验证通过后才能决定是否将 Android 与 Harmony 的页面收敛到同一 Compose 源码。迁移期间应保留 Java 服务裁决、身份隔离与现有协议；为了共享页面而将私密游戏状态复制到客户端会改变既有安全边界。

## 本轮答案之书回归

Android/Web 页面已按当前 Harmony 动作模型调整：正文不旋转，封面向左收起；再次点击自动完成合上与换页。状态回归脚本为 `scripts/test-answer-book.py`，覆盖重复点击、旧答案保留到合页结束、过期阶段回调、离页后的阶段恢复和连续答案不重复。2026-09-16 执行通过 119 项检查，输出存于 `artifacts/cloud-live-20260916/answer-book-tests.log`。

这个状态测试不替代设备视觉验收，也不证明 ovCompose 已经接通。当前鸿蒙页面仍为原有 ArkUI 实现。

## 参考资料

以下外部资料于 2026-09-16 检索核对；实施迁移前需再次确认相应版本及工具链文档：

- [JetBrains：Compose Multiplatform 平台及版本兼容说明](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html)：官方支持平台边界。
- [Tencent TDS：ovCompose 示例工程](https://github.com/Tencent-TDS/ovCompose-sample)：OHOS fork、共享库和鸿蒙宿主桥接示例。
- [Tencent TDS：KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI)：鸿蒙支持、Windows 工具链与 `com.tencent.kuikly.compose` 包名差异。
