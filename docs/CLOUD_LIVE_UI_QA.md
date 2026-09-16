# 云端实测与网络房间界面统一

## 已部署 API 实测（2026-09-16）

目标为原生端默认服务 `http://8.133.216.39:8080`。最终公网测试时间为 18:51:07–18:52:34（Asia/Shanghai），实际执行 **679 次 HTTP 请求、1,199 项断言**。六种玩法各完成一局，再验证第二局的旧请求隔离和进行中房主转交。7 个临时 QA 房间都由自己的合法房主解散，并通过 GET 404 复核；没有读取其他房间，没有调用管理员接口或重启线上服务。

| 玩法 | 真实云端覆盖 | 结果 |
| --- | --- | --- |
| 谁是卧底 | 四人准备及发牌，身份分配，房主结算、重复结算、下一局 | PASS |
| 一夜终极狼人 | 三人发牌确认、夜间行动、讨论、投票、自动结算，中心牌与初始身份保密 | PASS |
| 猎巫镇简化版 | 四人确认、夜间行动、白天投票、村民胜利，存活身份保密 | PASS |
| 桌游记账 | 三人筹码场景：支付、私密收款请求、确认/拒绝/取消、批量发放/收取、均分余数、整批撤销、越权/透支拒绝、结束锁定 | PASS |
| 阿瓦隆 | 五人发牌、选队、组队票、任务牌、三次成功后的刺杀、最终身份揭示 | PASS |
| 你画我猜 | 三人完整轮换，画者私有词、笔画同步、撤销/清空、全员猜中结束、总分结算 | PASS |

共同覆盖创建及加入的同请求重试、原成员凭据恢复、角色私密隔离、普通成员越权拒绝、重复开局不重发身份、游戏/记账/绘画幂等、新局拒绝旧局指令、房主交接和离开后的凭据撤销。规则游戏在成员离开后安全中止；狼人不公开中止局的全员或中心牌，绘画中止隐藏答案。一次创建/加入限流按 `Retry-After: 60` 等待，使用原请求成功继续。

### 线上尚未提供的新接口

| 检查 | 当前响应 | 影响 |
| --- | --- | --- |
| `GET /health` | `200`，`status: ok`、`protocol: 1` | 基础服务正常 |
| 自建房间 `POST /invite` | `404 NOT_FOUND` | 尚不能在这套公开服务完成二维码邀请 |
| 自建房间 `POST /hostview` | `404 NOT_FOUND` | 尚不能在这套公开服务完成房主切换广播及成员提醒 |
| `PUBLIC_WEB_URL` | 因邀请接口缺失无法读取生成结果 | 配置情况未知，不等同于未配置 |

需要使公开 API 运行包含 `RoomInvitation` 和 `hostview` 的最新 JAR，并部署网页版、配置 `PUBLIC_WEB_URL` 后再复核这两条链路。`protocol: 1` 在增量版本之间保持不变，不能单凭健康响应证明所有功能已部署。本次测试不替换或重启线上 Java 服务。

### 新客户端识别旧服务

两端模型把未提供的 `hostView` / `hostViewRevision` 保留为无效缺省值，只有服务器明确返回合法模式与版本才判定支持。Android/Web 使用 `CloudRoom.supportsHostView`，Harmony 使用 `supportsHostView(room)`；`CloudRoomClient` / `CloudRoomStore` 的 `compatibilityMessage` 独立于临时网络错误，正常轮询不会清除升级提示。刷新获得新协议字段后自动恢复，无需重新创建成员。

`/invite`、`/hostview` 返回 `404 NOT_FOUND` 时显示明确升级说明，并保留当前会话。没有本地伪造管理员状态或假装广播成功。离开、解散以及其他现有 HTTP 能力不在数据层被封锁；界面据能力状态展示可用操作。

### 证据与复用

- 公网脚本：[`scripts/test-live-cloud-games.py`](../scripts/test-live-cloud-games.py)
- 最终结果：`artifacts/cloud-live-20260916/api-final/live-cloud-results.json`
- 最终逐游戏日志：`artifacts/cloud-live-20260916/api-final/live-cloud.log`
- 详细 API 报告：`artifacts/cloud-live-20260916/CLOUD_API_QA.md`
- 两端生产客户端契约验证：`python scripts/test-cloud-clients.py`

公网复测命令：

```sh
python scripts/test-live-cloud-games.py --server http://8.133.216.39:8080 --output artifacts/cloud-live-recheck
```

追加 `--probe` 可只测健康、邀请与模式能力。脚本不输出成员 token、邀请 token、房间密钥或私人身份。本段是已部署 Java 服务的 HTTP 验证，不能代替客户端按钮与布局验收；线上重启、90 秒绘画超时、所有随机角色组合和全部账本模板未在此轮公网重复运行。

## 统一后的界面

Android / KMP Web 网络入口、房间、二维码、记账和画板复用现有 AppCard、AppScreen、AppPrimaryAction、AppDialog 与主题；Harmony 对应六个页面引用相同生成设计参数。主要尺寸为 16 页面边距、12 区块间距、14 卡片圆角、20 弹窗圆角、52 主操作高度。次要操作使用主题中性色，游戏/金额/画笔选择可换行，成员状态与账户金额分层对齐。

房主模式条固定在滚动区域之外，翻到账本历史仍可见。身份与权限控制、房主参赛人数、通知去重和重试标识保持原有语义。二维码保留白底、整数模块和四模块留白，正文可滚动，复制/关闭操作保持可达。

## 设备实测发现并修复的问题

### Harmony 解散按钮未能发送请求

在真实公网独立房间中，点击解散并确认后没有退出。原生 NETSTACK 日志显示请求参数错误（401），没有发出 DELETE；之后的 GET 仍可读到该房间。这不是服务端鉴权错误，也不是确认按钮坐标偏差。

原因是通用请求适配为无请求体的 DELETE 也设置了 `extraData: ''`。已改为类型化 `HttpRequestOptions`，只有非空请求体才设置 `extraData`，GET / DELETE 均省略该属性。使用原模拟器签名覆盖安装、保留原会话后，再次从同一公网房间解散成功，返回创建/加入页且恢复房间入口消失。

证据位于 `artifacts/cloud-live-20260916/harmony-ui/close-verification.log`、`close-after.hilog` 和 `close-fix-*.json/png`。原契约适配器没有模拟原生参数校验，本轮补充此边界。

### 安卓答案之书

安卓已对齐现有鸿蒙翻页：封面向左收起、正文不旋转，首次 520ms 展开，再次点击先 300ms 合上再自动展开下一答案。动画中重复点击不重复取值，离页后返回继续当前阶段。

119 项状态回归通过；Android 模拟器实际验证首翻、单次点击换页、快速三连点、翻页中切工具返回、深色、宽屏双页和矮屏滚动。测试后恢复设备尺寸及原主题，崩溃缓冲为空。截图、录屏与逐项记录见 `artifacts/cloud-live-20260916/android-ui/ANSWER_BOOK_QA.md`。

### Web 连接失败后无法继续

实际浏览器跨域连接失败时，Ktor 3.3.3 Wasm 将 fetch 拒绝包装成 Kotlin `Error`，原来只捕获 `Exception` 的入口会漏掉它。已在网络操作边界捕获 `Throwable`，仍优先重抛协程取消，并在退出时释放忙碌状态；失败保留会话和待提交请求，避免恢复后重复记账。

新增 24 项错误、取消与原请求重试检查通过。最终生产 Web 包在浏览器重现相同连接失败后，显示“连接失败，请检查服务地址和网络后重试”，创建按钮恢复可点。切回测试代理后重试创建成功，并通过界面解散该临时公网房间。没有放宽跨域限制或改变正式默认服务地址。机制、依赖源码依据和契约结果见 `artifacts/cloud-live-20260916/TRANSPORT_FAILURE_QA.md`，截图为 `web-fetch-failure-recovered.png`、`web-fetch-retry-success.png` 和 `web-fetch-room-cleaned.png`。

## 三端真实界面联调

使用 Android 模拟器、Harmony 模拟器及 390 × 844 的浏览器视口，连接本机最新 Java 服务。Android 房主创建记账房间，生成二维码；从手机页面截图实际解码二维码后，用其完整链接打开浏览器，完成预填房间、填写昵称、加入与准备。此项验证了二维码内容与浏览器入房链路，没有使用手机相机扫码，也不代表公网域名已经部署。

三端同房后逐项检查：

- 房主仍参与记账；切换玩家/管理员界面时，成员收到状态变更提醒，滚动到账本底部仍能看到固定模式条。
- Android 房主一次批量发放 10 分，三端看到三人各 10 分、公共池 0、交易记录 1。
- Web 成员通过收款人选择、快捷金额、支付预览和确认，支付 5 分给 Harmony 成员；三端一致显示 Android 10 / Harmony 15 / Web 5、公共池 0、交易记录 2。
- 房主结束记账后，各端显示结算并保留相同余额及记录，收支入口消失。
- 房主解散后，成员收到房间已解散提示，点击“返回联机页”恢复入口，会话清除。测试房间已清理。
- 普通成员只显示个人收支权限；窄屏选择、对话框及交易历史能够完整操作。

证据位于 `artifacts/cloud-live-20260916/android-ui/`、`harmony-ui/`，以及 `web-invite-joined.png`、`web-host-player.png`、`web-ledger-scrolled-admin.png`、`web-ledger-transfer-preview.png`、`web-ledger-transfer-result.png`、`web-ledger-ended-readonly.png`。

三端还分别连接实际公开服务验证旧版本提示、刷新及离开/解散按钮。客户端明确提示升级服务，不将缺少协议字段的房间显示成已支持管理员广播。

## 构建与检查

Android APK 和生产 Wasm Web 构建通过，Harmony HAP 使用现有模拟器签名构建并覆盖安装；保留设备应用数据。最终客户端回归包括 69 项邀请/视图策略、Kotlin/Harmony 各 40 项兼容性检查、24 项传输失败检查，以及两种客户端担任房主的完整游戏流程。

跨端检查通过 6,010 项随机工具对照、10,206 项圆桌布局对照，12 组已跟踪功能没有未复核源码变化。答案之书另有 119 项状态检查。日志为 `android-web-final-build.log`、`harmony-final-build.log`、`compatibility-client-tests-final.log`、`cross-platform.log` 和 `answer-book-tests.log`。

未改动的 Java 规则与单机/同机引擎沿用上一轮 `artifacts/room-invites/` 的完整回归证据，没有将旧日志伪记为本次重跑。本轮新增证据为已部署 API、三端界面、网络恢复和答案之书检查。交付包清单、大小及 SHA256 位于 `artifacts/cloud-live-20260916/release/`。

## 部署边界与后续主线

最新二维码和模式广播的界面验证使用本机最新 Java 服务，不算作公网这两项能力已经通过。网页版通过本机代理连接已部署服务的验证，也不能替代公网 Web 域名、HTTPS、反向代理和真实手机相机验收。

本轮没有修改发布默认服务地址，没有替换云端 JAR，没有迁移整套鸿蒙 UI。后续同仓同分支打包与 ovCompose 页面验证的具体步骤见 [KMP 主线方案](KMP_HARMONY_MAINLINE.md)。当前 HAP 仍由 ArkUI / Hvigor 工程生成。
