# 渠道拆分验收记录

范围：国内商店版屏蔽多设备房间，Google Play、独立分发、F-Droid 保留完整版；Harmony 同步国内/完整策略，Web 保留完整版。实现和使用方法见 [发行渠道说明](DISTRIBUTION_CHANNELS.md)。证据目录为 `artifacts/distribution-20260922/`。

## 构建与签名

- Android 四个渠道 release APK 均构建成功，逐一验证正式证书一致、非调试清单、渠道元数据和包名。国内版包名为 `org.walks.gamecopilot.domestic`，未包含 Wi-Fi / 组播权限；国内、Play、F-Droid 均未声明安装 APK 权限。
- Google Play AAB 构建成功，`jarsigner` 验证通过；从 AAB 读取的签名证书 SHA-256 与已验证 APK 相同。
- 国内版 release lint：0 errors、36 warnings、2 hints。未声称全部警告已清除。
- Harmony 国内 AppGallery `.app` 使用 Release SDK 构建，正式分发 profile 与签名校验通过；direct 设备调试 HAP 构建通过，生成的 BuildProfile 渠道分别为 domestic / direct。
- Web `wasmJsBrowserDistribution` 完整构建通过。
- 初次多渠道 debug 构建遇到本机旧缓存脚本重复注入 AAR 和 Kotlin 编译堆不足。移除本机重复注入，增加 Kotlin 编译堆并限制 worker 后，完成上述四渠道正式构建。没有把最初失败的 debug 构建记为通过。

最终文件、大小和 SHA-256 见 [交付清单](../artifacts/distribution-20260922/release/manifest.json)。国内包公开证书及匹配的新包名信息位于 `artifacts/distribution-20260922/domestic/`，不含签名私钥或个人身份文件。

## 行为与规则

`scripts/test-cloud-clients.py` 已执行：

- 四种 Kotlin 渠道的路由、房间开关、外部更新策略和未知渠道拒绝检查。
- 国内 Kotlin 客户端面对有效旧会话、待重试账目/画板及旧邀请时，不恢复会话、不接受新邀请、不创建 HTTP 传输对象；创建、加入、动作、关闭、邀请及待重试操作均被阻断。
- 国内 Harmony 生产 Store 对同类旧会话和动作拒绝执行，测试 HTTP 服务收到的请求数保持为零，原存储数据不被改写。
- 完整版两端各 40 项真实 HTTP 契约检查；Kotlin 69 项邀请/视图策略检查、24 项传输错误与取消检查。
- 本地 Java 服务与两端生产客户端完成一夜狼人整局、记账、猎巫镇、阿瓦隆和你画我猜的完整流程；覆盖私密信息、动作重试、服务/客户端重启和下一局。卧底基础协议由 HTTP 合约覆盖。

`scripts/test-offline-games.py` 通过：两端各 640 局狼人规则、卧底配置修复与身份分配、猎巫镇主持、阿瓦隆、绘画和记账规则；账本两端各 76 组场景/人数边界。渠道改动没有修改这些规则引擎。

`scripts/test-ci-android-release.py`：11 项自动检查通过，另 1 项需显式输入真实 APK 的可选 SDK 兼容测试跳过；实际四个发布包已另外验签。

`scripts/check-cross-platform.py`：109 个设计 token、29 个颜色角色、6,010 项随机规则、10,206 项圆桌布局检查通过，13 组功能源码复核记录无漂移。

## Android 界面实测

使用独立的 `emulator-5566` 只读 AVD 副本，安装实际签名 release 包，以 UI 树定位操作并保留截图。已有模拟器被其他操作使用后，切到该副本继续；不清理原 AVD 数据。

- 国内首页无联机大厅、局域网/网络模式选择，五种同机入口保留。
- 卧底配置、开始、四位玩家的隐藏身份卡与身份词显示均可操作。
- 国内「我的」显示国内商店版，无外部 APK 更新入口；本机账本无云端入口。添加玩家后以银行发放 10,000，余额从 1,500,000 变为 1,510,000，交易数从 0 变为 1。
- 工具页保留答案之书、转盘、硬币、骰子和新增入口。
- 独立完整版首页保留同机/局域网/网络模式，联机大厅包含卧底、狼人、猎巫镇、记账、阿瓦隆和绘画六种类型。
- Play 与 F-Droid 的关于页显示对应渠道，滚动至底部确认无「获取新版」；独立版保留该入口。

本轮只对 Android 做界面设备操作，Harmony 做了原生编译、源码配对及生产 Store 行为检查，Web 做了完整生产构建；不把这些检查描述成 Harmony 真机或 Web 浏览器界面全流程验收。测试副本启动时出现过系统 UI 无响应，恢复后继续；这不是应用崩溃证据。

## 线上与上架状态

原朋友服务器 `http://8.133.216.39:8080/health` 返回 `{"status":"ok","protocol":1}`。本轮不覆盖线上 Java 包、不迁移数据，不把本地对局测试视为线上全部功能验证；旧服务器是否包含最新二维码邀请和管理员视图仍需部署版本核对。

腾讯云已退款，旧工作表标记为历史。没有继续备案、修改 DNS、推送 GitHub、触发远程 Actions 或向商店提交。F-Droid 仅完成发行渠道，不等于官方收录。国内包仍有用户自配的可选 AI 功能，应如实准备隐私说明；关闭房间不自动保证备案或上架审核通过。旧 vivo 包名材料需按国内版重新核对。
