# 发行渠道与联机功能

2026-09-22：按用户决定停止使用已退款的腾讯云服务器。完整版默认 API 继续使用朋友原有服务器 `http://8.133.216.39:8080`。本轮不迁移房间数据、不修改 DNS、不继续腾讯云备案。

## Android

| 渠道 | 包名 | 多设备房间 | 更新来源 | 构建命令 |
| --- | --- | --- | --- | --- |
| 国内商店 `domestic` | `org.walks.gamecopilot.domestic` | 关闭 | 国内应用商店 | `./gradlew :composeApp:assembleDomesticRelease` |
| Google Play `googlePlay` | `org.walks.gamecopilot` | 完整 | Google Play | `./gradlew :composeApp:bundleGooglePlayRelease` |
| 独立分发 `direct` | `org.walks.gamecopilot` | 完整 | GitHub Releases | `./gradlew :composeApp:assembleDirectRelease` |
| F-Droid `fdroid` | `org.walks.gamecopilot` | 完整 | F-Droid 客户端 | `./gradlew :composeApp:assembleFdroidRelease` |

渠道固定在构建产物 BuildConfig 中，设置、二维码、远端配置均不能切换。国内版不显示联机大厅、游玩方式选择和云端账本入口，同时关闭局域网；导航恢复和直接导航会拦截房间页面；生产客户端不恢复旧房间会话，不创建房间 HTTP 连接。单机工具、同机发牌、主持流程、本地账本及统计保留。国内版移除 Wi-Fi / 组播相关权限。

国内版可与完整版共存，数据分别保存，完整版 APK 无法覆盖国内版。**此前 vivo 草稿和备案材料的包名是 `org.walks.gamecopilot`，不能复用为国内版特征信息。** 正式上架前应以国内版签名包重新导出公开证书信息，并核对后台草稿是否允许修改包名。

Google Play 和 F-Droid 包不显示外部 APK 更新入口；独立版关于页的「获取新版」手动打开发布页，目前没有自动下载安装器。F-Droid flavor 是可构建的功能渠道，**不代表已进入 F-Droid 仓库或满足全部收录条件**；正式收录还需核对许可证、依赖、源码构建及签名要求。不同签名的 F-Droid 和独立包不能互相覆盖安装。

GitHub Actions `Build & Release Android Channels` 分别构建四种 APK，验证渠道、包名、权限、非调试状态及正式签名。Google Play 额外产出 AAB；只有 direct APK 自动附加到标签对应的 GitHub Release。其余包在各自命名的 Actions artifact 中，避免独立更新误下载国内版。

```powershell
python scripts/prepare-android-filing-info.py --channel domestic --apk composeApp/build/outputs/apk/domestic/release/composeApp-domestic-release.apk --output-dir artifacts/domestic-filing
./gradlew :composeApp:assembleDomesticDebug :composeApp:assembleDirectDebug
```

调试或发布都应明确选择渠道；`assembleDebug` / `assembleRelease` 会一次构建四种 APK。

Google Play 新提交版本自 2026-08-31 起需要 targetSdk 36。本工程统一使用 compileSdk 36，并仅将 Google Play 的 targetSdk 提升到 36；其余渠道仍为 35。发布校验会拒绝低于 36 的 Google Play APK。依据：[Google Play 目标 API 要求](https://support.google.com/googleplay/android-developer/answer/11926878?hl=zh-Hans)。

## Harmony 与 Web

鸿蒙仍使用相邻 `YiGameCopilotX-Harmony` 原生 ArkUI 工程，由 Android 主仓库脚本负责构建。Hvigor 将渠道写入生成的 BuildProfile，默认 domestic，显式设置 `YIGAME_DISTRIBUTION=direct` 才开启完整版。未知渠道构建失败。AppGallery 发布脚本强制 domestic，不接受完整版参数。

```powershell
python scripts/build-harmony-release.py --output-dir artifacts/harmony-domestic
python scripts/build-harmony-device.py --channel domestic
python scripts/build-harmony-device.py --channel direct
```

鸿蒙国内版同样屏蔽 UI、房间路由、旧云会话恢复和 LAN/云客户端入口。保留已有 bundleName 与 AppGallery 证书；direct HAP 是已登记设备的测试包，不是任意设备可安装的正式分发包。独立发布需另外匹配 Harmony 分发签名资质，本轮未更换证书或迁移到 Compose。

Web 保留完整云房间与扫码加入；原生 iOS 默认策略也保留完整功能，本轮不新增 iOS 发布流程。

## 服务与上架边界

已有自定义服务地址保留。原服务器部署版本需单独核对，尤其邀请及管理员视图能力；本地测试通过不代表旧服务器已更新。Java 部署方法见 `server/README.md`，测试可使用本地 API。

这次只拆分房间功能，不把国内版描述成完全断网应用：用户自行配置的可选 AI 请求仍保留，INTERNET 权限需如实披露。隐私政策、APP 备案是否适用及商店分类应以实际功能和平台审核为准。更换分发渠道不能自动免除在中国内地服务器提供完整联机服务的备案与资质要求。

腾讯云工作表和 9 月 18 日 vivo 材料保留为历史记录，不是当前渠道的审核证明。

## 验证

`python scripts/test-cloud-clients.py` 检查四种 Kotlin 渠道、禁用时的旧会话/邀请/路由、鸿蒙禁用时零 HTTP，以及完整版两端真实客户端对本地 Java 的完整对局与恢复流程。

`python scripts/test-ci-android-release.py` 验证错误渠道、权限、签名等检查器。`python scripts/check-cross-platform.py` 跟踪两端源码核对。实际构建与设备检查结果见本次验收报告。
