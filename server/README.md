# 桌游助手 · 云端房间服务

Java 17+ 独立服务，发布包内的 `yigame-room-server.jar` 可直接运行，无需 Maven、数据库或第三方 JAR。Android 和 Harmony 客户端使用同一 HTTP JSON 协议。两端默认连接 `http://8.133.216.39:8080`；已有自定义服务设置和房间会话继续使用原地址，也可在「联机 → 网络房间 → 自定义服务器」切换。

支持六种网络玩法：**谁是卧底**（4–16 人）、**一夜终极狼人**（3–10 人，另有三张中央牌）、**猎巫镇简化版**（4–12 人）、**桌游记账**（2–20 人）、**阿瓦隆**（5–10 人）和 **你画我猜**（3–10 人）。身份、游戏行动、计时和记账均由服务端决定；Android、Harmony 和 KMP Web 使用相同协议。

Java 工程位于 Android 仓库的 `server/`；核心文件为 `src/main/java/org/walks/rooms/RoomServer.java` 及 `OneNightGame.java`、`HuntGame.java`、`LedgerGame.java`、`AvalonGame.java`、`DrawingGame.java`。构建输出在 `dist/yigame-room-server.jar`，无需另建 Java 工程。上线新客户端前先更新此 JAR；代码与安装包构建不会自动更新现有云服务器。

## 直接运行发布包

解压 `yigame-room-server.zip`，安装 Java 17 或以上版本。Linux 示例：

```sh
cd yigame-room-server
export ADMIN_TOKEN="$(openssl rand -hex 32)"
export DATA_DIR="$PWD/data"
export HOST=127.0.0.1
export PORT=8080
java -Xms64m -Xmx256m -Dsun.net.httpserver.maxReqTime=15 -Dsun.net.httpserver.maxRspTime=15 -jar yigame-room-server.jar
```

另一个终端检查：`curl -f http://127.0.0.1:8080/health`，应返回 `status: ok`、`protocol: 1`。`ADMIN_TOKEN` 至少 32 字符，保存到仅服务账号可读的环境文件；不要填到手机的房间密钥中。

| 环境变量 | 默认值 | 用途 |
| --- | --- | --- |
| `HOST` | `0.0.0.0` | JAR 监听地址，反向代理部署设置为 `127.0.0.1` |
| `PORT` | `8080` | HTTP 端口 |
| `DATA_DIR` | `data` | 可写持久化目录 |
| `ADMIN_TOKEN` | 必填 | 管理 API 凭据，与玩家 token、房间密钥独立 |
| `WEB_ORIGINS` | 空 | 跨域网页的准确来源，逗号分隔，如 `https://games.example.com`；同域代理无需配置。禁止使用通配符代替来源。 |
| `PUBLIC_WEB_URL` | 空 | 手机邀请二维码的网页入口，如 `https://play.example.com/`；该来源必须将 `/api/` 代理到此服务。配置后使用网页来源作为二维码里的 API 地址，兼容手机默认 HTTP 地址。 |

房主在等待阶段生成二维码，链接只携带独立的入房邀请，不携带自己的成员令牌或房间密钥。未配置 `PUBLIC_WEB_URL` 时允许房主传入 `webUrl`，服务地址来自 `serverUrl`；HTTPS 网页不能使用 HTTP 接口。邀请只在房间等待且有空位时可加入，房间解散后失效。二维码生成器内置 Nayuki v1.8.0（MIT），许可证位于 JAR 内 `META-INF/LICENSE-qrcodegen.txt`。

房主模式是公开的界面状态，不改变房主的参赛资格、身份或权限。`hostview` 使用当前 `roundId` 与 `hostViewRevision`（请求字段名 `revision`）防止旧界面覆盖；同模式重试不产生新记录。房间视图的 `hostViewChanges` 保留最近 20 条 `{revision, mode, hostId}`，与房间一同原子保存。客户端展示当前模式并对新记录弹出提醒；服务器重启、短暂断线不会重发身份或清除模式。开局转为 `player`，下一局等待转为 `admin`。详见仓库 `docs/WEB_AND_SIX_CLOUD_GAMES.md`。

## Docker 部署

包内包含源码和 Dockerfile，可在云服务器构建镜像：

```sh
cp .env.example .env
# 在 .env 中填写 ADMIN_TOKEN，可用 openssl rand -hex 32 生成。
chmod 600 .env
docker compose up -d --build
curl -f http://127.0.0.1:8080/health
docker compose logs --tail=50 rooms
```

容器以非 root 用户运行，持久化使用 `rooms-data` 命名卷，默认只映射本机 `127.0.0.1:8080`。重建镜像、重启容器不会丢失房间或统计，**不要使用 `docker compose down -v` 删除数据卷**。

尚未准备域名时，可以先在 `.env` 设置 `BIND_ADDRESS=0.0.0.0`，安全组仅放行测试设备访问 8080，然后在两端填写 `http://服务器IP:8080` 做临时联调。正式提供给玩家使用时配置 HTTPS，将绑定地址改回 `127.0.0.1`。

## HTTPS 与后台运行

`deploy/nginx.conf` 是反向代理示例。准备域名和证书后替换 `YOUR_DOMAIN` 及证书路径，将配置放入 Nginx 的 `http` 上下文，执行 `nginx -t` 后重载。客户端填写 `https://域名`，无需附加 `/api/v1`。

Web 发布包为 `YiGameCopilotX-Web.zip`，解压到 `/opt/yigame/web`，保留字体和资源目录。上述 Nginx 配置同时提供静态网页及同域 API；HTTPS 网页默认访问本站 API，不会直接请求 HTTP IP。Android/Harmony 内置地址仍保留原默认值，自定义地址不会写进安装包。跨域部署需设置 `WEB_ORIGINS`，这不解除浏览器的 HTTPS 混合内容限制。

Web 用本地存储保留服务器生成的随机成员 ID 和令牌，无需硬件标识。刷新后通过「联机 → 返回已加入的房间」恢复；清理网站数据、换浏览器或结束无痕会话后不保证恢复。不要把昵称或房间号当作玩家身份凭据。当前 Web 只提供网络房间，局域网发现需要原生端。

示例对创建/加入请求做限流，不限制正常 GET 轮询；公网代理关闭 `/api/v1/admin/`。管理接口通过服务器本机或 SSH 隧道使用。不要在反向代理日志中记录 Authorization 头或请求体。

不用 Docker 时，创建系统用户 `yigame`，把 JAR 放在 `/opt/yigame-rooms/`，把管理凭据写入同目录 `.env`，使用 `deploy/yigame-rooms.service`。`systemd` 自动建立 `/var/lib/yigame-rooms`，限制数据目录权限并在失败后重启。

## 客户端流程

1. 房主填写昵称。房间密钥可填写 6–32 个字符，留空时由服务器安全随机生成；房间号由服务器分配。自建服务时使用「自定义服务器」修改地址。
2. 房主将房间号、密钥告诉其他玩家。每位玩家在同一服务地址下输入两项信息加入，房间内昵称不可重复。
3. 玩家准备后，房主点击「开始并分发身份」。服务端决定词组、洗牌和分配身份。每个成员只能读取自己的身份；房主没有全员身份查询接口。
4. 客户端通常每 2 秒同步一次房间状态，你画我猜每 800 毫秒同步；后台停止轮询并隐藏身份。短暂断网、关闭应用或服务器重启后，使用持久化 token 恢复原成员及原身份，不会再创建一个玩家。
5. 卧底由房主结束并选择胜方；狼人由全员确认初始身份后进入夜晚，各人在自己的设备执行私有指令，白天讨论后由房主开启投票，全员提交后服务端自动结算。随后可以「再来一局」，旧身份和夜间记录清空。重复操作不会再次换牌或计数。
6. 房主可移出等待中的玩家、解散房间；主动离开时房主权限转给名单中的下一位。断网不会立即踢人，重连超过 20 秒显示离线，开局前房主可移出该玩家。

## API v1

所有 POST 使用 `Content-Type: application/json`。玩家请求使用 `Authorization: Bearer <token>`，管理接口使用独立的管理员 token。错误格式为 `{"code":"...","message":"..."}`。成功响应不缓存。

| 方法与路径 | 请求 | 响应/权限 |
| --- | --- | --- |
| `GET /health` | 无 | 服务健康和协议版本 |
| `POST /api/v1/rooms` | `nickname, roomKey, requestId, gameType, maxPlayers` 与对应玩法配置 | `gameType` 为 `spy / werewolf / hunt / ledger / avalon / drawing`；返回 `token, roomKey, room` |
| `POST /api/v1/rooms/{6位房间号}/join` | `nickname, requestId` 与 `roomKey` 或 `inviteToken` | 加入；返回 token 与房间视图 |
| `POST …/{id}/invite` | `webUrl?, serverUrl` | 房主在等待阶段生成入房链接及二维码矩阵；不能作为成员凭据使用 |
| `POST …/{id}/hostview` | `roundId, revision, hostView: player/admin` | 房主切换界面；仍然参赛，向所有成员同步模式与切换记录 |
| `GET /api/v1/rooms/{id}` | 玩家 token | 房间、公开成员、自己的 `identity` |
| `POST …/{id}/ready` | `ready: boolean` | 等待阶段设置准备状态 |
| `POST …/{id}/start` | `roundNumber` | 仅房主；所有人在线且准备；只分发一次 |
| `POST …/{id}/game` | `roundId, stepId, choice, targets?` | 狼人/猎巫/阿瓦隆行动；仅接受当前成员视图 `game.options` 中的选择 |
| `POST …/{id}/ledger` | `roundId, requestId, revision, operation` 与操作参数 | 记账指令，详情见下文 |
| `POST …/{id}/draw` | `roundId, stepId, requestId, operation` 与操作参数 | 绘画、猜词及回合推进，详情见下文 |
| `POST …/{id}/finish` | `roundId, winner?` | 房主结束卧底或记账；卧底胜方为 `平民/卧底/白板/未判定`，记账固定为“记账结束”；其他游戏由规则自动结算 |
| `POST …/{id}/next` | `roundId` | 仅房主；结束后清除身份，重新准备 |
| `POST …/{id}/kick` | `playerId` | 仅房主，等待阶段 |
| `POST …/{id}/leave` | `{}` | 撤销自己的 token，必要时转交房主 |
| `DELETE …/{id}` | 玩家 token | 仅房主解散 |
| `GET /api/v1/admin/stats` | 管理 token | 活跃房间、累计房间/开始/完成局数、最近 1000 局 |
| `GET /api/v1/admin/rooms` | 管理 token | 房间号、状态、人数、时间；无词条或凭据 |
| `DELETE /api/v1/admin/rooms/{id}` | 管理 token | 强制解散 |

房间视图字段：`roomId, gameType, status, hostId, selfId, maxPlayers, spyCount, blankCount, roundId, roundNumber, winner, players, identity`。`players` 只包含 `id, nickname, ready, connected`。`identity` 开局前为 `null`，开局后为当前成员的 `{role, word}`。

狼人另有公开牌组 `roles` 和个人视图 `game`：`phase, stepId, prompt, options:[{id,label}], notes, confirmedCount, votedCount, revealed, center`。阶段依次为 `DEAL → NIGHT → DAY → VOTE → RESULT`；只有 `RESULT` 揭示全员初始/最终身份、投票及三张底牌。`identity` 始终是自己的初始身份；换牌后不会在白天偷偷更新为最终身份。`notes` 仅为自己的合法查验结果。成员列表、房主接口和其他成员视图不包含私有牌或夜间行动者。

客户端把服务端给出的 `stepId` 和 `choice` 原样回传，不在手机上决定换牌或胜负。同一成员、同一步骤、同一选择可安全重试；改票、越权行动、旧轮次、非法目标均拒绝。狼人成员主动离开会将局面标为 `ABORTED` 并结束该轮，不公开身份；短暂断网保留原局等待恢复。

`roles` 可省略（使用既有 3–10 人预设）或填写人数加三张牌。支持 `WEREWOLF, MINION, DOPPELGANGER, SEER, ROBBER, TROUBLEMAKER, DRUNK, INSOMNIAC, HUNTER, VILLAGER, MASON_A, MASON_B, TANNER`；狼人至多两张、村民至多三张，其他各一张，守夜人成对。当前两端创建界面提供预设牌组，底层协议也支持自定义牌组。

规则依据 [One Night Ultimate Werewolf 原版说明书](https://www.daroolz.com/wp-content/uploads/2022/05/OneNightUltimateWerewolf-rules.pdf)：初始身份决定夜间行动，最终身份决定阵营；化身幽灵保留物理牌，按复制角色执行即时或延后行动；猎人被处决后自动带走其投票对象；最高票至少两票才处决，并列最高票全部处决；皮匠与村民可同时获胜，也可能无人获胜。三人预设沿用项目已有单狼人入门组合。

协议保持 v1 的增量兼容，旧卧底快照缺少 `gameType` 时按卧底恢复。新增玩法需要对应更新后的两端客户端；不能让仅认识卧底或狼人的旧客户端加入新玩法。

### 猎巫镇：单身份简化玩法

规则查阅来源：[发行方游戏介绍](https://www.facadegames.com/products/salem-1692)与[原版第二版规则书](https://gamers-hq.de/media/pdf/fc/7d/89/FINAL_Salem_Rulebook_-2nd_Edition.pdf)。原版有多张审判牌、手牌回合、指控和阴谋传染；原版警长不可自守。本项目保留既有单身份简化玩法，入口和房间明确标注差异，不声称实现完整 Salem 1692。

创建参数为 `gameType: "hunt", maxPlayers: 4..12, witchCount: 1..maxPlayers-2`，另有一名警长，其余为村民。需配置人数全部加入、在线且准备后开始。每人只能读取自己的身份，女巫可看到同伴名单。

全员确认身份后进入同步夜间阶段；存活村民也需确认等待，警长可选存活玩家守护或跳过（本机规则允许自守），各女巫分别选目标或跳过。女巫有效目标的最高票唯一时执行，并列则无人被杀，警长成功守护可阻止该次出局。夜晚结果公布后房主开启白天秘密投票，不能投自己；弃权不计票，最高票并列则无人被放逐。已出局成员不能再行动或投票，但出局房主仍可推进讨论。

女巫全部出局则村民阵营胜利，非女巫全部出局则女巫阵营胜利；每次夜晚/放逐后立即检查。`game.roster` 只公布存活状态及已出局身份，结算或中止后揭示全员；`publicLog` 保留最近 60 条公开事件，`livingCount/day` 用于展示进度。夜间不公开具体行动者及其目标。主动离开会中止本局；短暂断网保留身份等待恢复。

### 阿瓦隆

创建参数为 `gameType: "avalon", maxPlayers: 5..10, roles?`。默认梅林、派西维尔、刺客、莫甘娜，7 人起加入莫德雷德，10 人加入奥伯伦，其余忠臣；邪恶人数依次为 2/2/3/3/3/4。基础规则依据[原版规则书公开副本](https://www.scribd.com/doc/288728587/The-Resistance-Avalon-Rules-en)，不含扩展角色玩法。

全员确认私密身份后进入 `TEAM → TEAM_VOTE → QUEST` 循环。队长随机起始、按座位轮换；超过半数同意才通过，平票否决，连续五次否决邪恶胜。善良只可出成功牌；7 人以上仅第四次任务需要两张失败牌。三次失败邪恶胜，三次成功进入 `ASSASSINATE`；刺客猜中梅林邪恶胜，否则善良胜，之后才公开全员身份。梅林不见莫德雷德，派西维尔不区分梅林与莫甘娜，奥伯伦不参与邪恶互认。

`game.avalon` 返回队长、任务轮数、所需人数、队伍、否决次数、已提交人数和任务历史。`/game` 的 `choice=team` 另带所选成员 `targets`；之后为 `approve/reject`、`success/fail`、`player:<id>`。全员组队票提交完才公开，单人的任务牌始终不公开。同一步骤重复相同请求可安全重试，改票、非法队伍或旧步骤拒绝。主动离开中止本局。

### 你画我猜

创建参数为 `gameType: "drawing", maxPlayers: 3..10`，人数全部加入、在线且准备后开始。参考 [skribbl 官方介绍](https://skribbl.io/)的轮流作画和猜词流程，本项目固定每人画一次、每轮 90 秒，猜中者 +10、画者为每位猜中者 +5，最高分可并列获胜。答案只有当前画者可读，结束本轮才公开。

`room.drawing` 提供画者、阶段、截止时间、画板版本、笔画、积分及个人猜词反馈。阶段为 `DRAW_READY → DRAWING → TURN_RESULT`，最后进入 `RESULT`。全部猜中或服务端计时到期自动结束本轮；房主可确认提前结束，再推进下一轮或总成绩。主动离开中止整局，短暂断网保留原成员。

`/draw` 操作为 `start/stroke/undo/clear/guess/end/next`，`tick` 只可推进已经到期的轮次。全部指令携带 `roundId, stepId, requestId`；画板修改另带 `revision`。笔画用 `points: [[x,y],...]`、`color`、`width`，坐标 0–1000、每笔 1–96 点、每轮最多 120 笔、宽度 2–30、七种预设颜色。猜词字段为 `guess`，每人每轮最多 100 次、间隔至少 600 毫秒。客户端抬笔后同步整笔，并在发送前持久化请求；完整指纹阻止响应丢失后的重试重复绘画或加分。

### 桌游记账

创建参数为 `gameType: "ledger", maxPlayers: 2..20, ledgerPreset, initialBalance?`。场景是可调整模板：`electronic` 起始 1500000、`property` 起始 1500、`score` 起始 0、`chips` 起始 1000。`initialBalance` 可覆盖模板，范围 `0..999999999999`。记账不是某个桌游规则的强制实现，数额均为虚拟游戏币、分或筹码。

至少两位成员在线且准备后开账，一名成员对应一个账户，另有 `pot` 公共池；`bank` 是无限银行，不计入有限账户余额。`ledgerConfig` 在等候阶段公开场景，开账后 `ledger` 返回 `revision, preset, unit, initialBalance, accounts, history, pending, undoId`。全部余额和已入账记录向房间成员公开；待确认收款仅付款人和收款人可见。

每个 `/ledger` 请求都携带本局 `roundId`、随机 `requestId`（32–100 字符）以及最新 `revision`。服务器先检查已成功请求的指纹，再检查版本，所以响应丢失后重试不会重复扣款；不同新指令同时提交同一版本，最多一笔成功。客户端发出指令前持久化完整请求，响应不确定时阻止新增交易并提供“确认上一笔结果”，应用重启后仍复用原请求。

| `operation` | 额外参数 | 权限与行为 |
| --- | --- | --- |
| `transfer` | `from, to, amount, memo?` | 玩家支付自己余额；房主可代记其他账户及银行/公共池 |
| `request` | `from, to, amount, memo?` | `to` 必须为本人；向另一个玩家发起收款，尚不扣款 |
| `approve / reject` | `entryId` | 仅请求中的付款人确认/拒绝；房主不能代确认 |
| `cancel` | `entryId` | 仅收款人取消自己的请求 |
| `grant` | `targets[], amount, memo?` | 房主从银行给所选玩家各发放该数额 |
| `collect` | 同上 | 房主从所选玩家各收取该数额到公共池 |
| `split` | 同上 | 房主将公共池中的总额均分给所选玩家；余数按座位顺序逐人分配，每人至少 1 个单位 |
| `undo` | `entryId` | 房主撤销最近一笔有效交易，整批反向记账；保留原记录与撤销关联 |

金额必须为整数 `1..999999999999`，收付账户必须不同。余额限制为正负同一上限；筹码账户及公共池不得透支。批量操作先检查全部账户，再一次提交；撤销也检查所有余额限制。每局最多 2000 个成功记账指令，指纹、历史和待确认请求随快照持久化，最多 30 个待确认请求。结束或成员主动离开后账本锁定并保留查看/CSV 导出；待确认请求不会自动扣款。点击“再来一局”前先导出需要保留的明细，新局清空账本并重新准备。

本机记账保留最近 100 项收支，截断时保留完整批次；全员收付整批撤销，手动改余额会记录调账收支。两端场景、快捷金额与边界校验一致，已有本机账本按原电子银行模板兼容读取。

创建/加入的 `requestId` 使用 32–100 字符随机标识。网络超时重试必须复用同一个标识和同一请求体；这样返回同一个成员。修改表单后换新标识。`roundId/roundNumber` 防止旧请求误操作新的一局。常见错误：400 参数错误、401 会话失效、403 密钥或权限错误、404 房间不存在、409 人数/准备/轮次冲突、410 已过期、429 限流、503 存储或服务异常。

客户端会在发送创建/加入请求之前持久化请求标识与请求参数，相同参数可在页面重建或应用重启后继续重试。`STALE_ROUND` 会刷新房间，用户确认当前局次后重新操作；不会自动把旧操作套用到新的一局。`SESSION_EXPIRED`、`ROOM_NOT_FOUND`、`ROOM_EXPIRED` 会清除失效会话；其他网络或接口故障保留 token。429 按 `Retry-After` 秒数暂停轮询和操作，未提供时等待 60 秒。

客户端契约验证：在工程根目录运行 `python scripts/test-cloud-clients.py`，使用本机 Gradle 缓存和 DevEco SDK 对生产 Kotlin 客户端与 ArkTS Store 运行同一套真实 HTTP 测试。

管理统计示例：

```sh
curl -H "Authorization: Bearer $ADMIN_TOKEN" http://127.0.0.1:8080/api/v1/admin/stats
```

云端统计属于服务端运营数据，与手机「我的 → 对局统计」中的本地记录分开保存。手机重置本地统计不会删除服务器统计。

## 持久化与容量

适用于小规模聚会服务：单实例、最多 500 个房间，记账最多 20 人，其他游戏人数上限各自校验。房间在最近一次成员/游戏操作后 12 小时过期，每分钟清理一次。历史对局摘要保留最近 1000 局，累计计数不随摘要淘汰；账本明细保留在该房间当前局中。

数据以 JSON 快照保存：临时文件写入、强制落盘，再原子替换。落盘失败时整个操作返回 503，同时回滚内存状态。启动发现损坏快照会报错，避免静默丢失数据。进程锁阻止两个实例同时写同一数据目录。

房间密钥采用独立盐值及 PBKDF2-HMAC-SHA256 校验，玩家 token 使用安全随机数生成。为支持创建请求重试，快照含恢复凭据，因此数据目录权限仅限服务账号；备份同样需要保密。此版本不支持横向多副本共享 JSON 文件，需要扩容时应迁移到数据库及集中式会话存储。

备份时先停止服务，再备份整个 `DATA_DIR`/Docker 数据卷；恢复时把快照放回相同位置，确认服务账号可读写后启动。管理 token 可通过修改环境变量并重启进行轮换。

## 从源码构建和验证

Linux/macOS：`sh build.sh`。Windows：`powershell -ExecutionPolicy Bypass -File build.ps1`。JDK 17+ 即可，完全离线构建。

测试会启动真实 HTTP 服务，验证创建/加入、错误密钥、身份隔离、越权拒绝、并发开局、幂等结算、重启恢复、房主交接、离开/解散、持久化故障回滚、限流和 JSON 边界。狼人测试运行 640 个完整预设局，每一步都经过持久化往返，并覆盖复制、换牌、猎人和特殊胜负。测试数据放在 `build/rooms-test-*`，与运行数据分开。仓库根目录 `scripts/test-cloud-clients.py` 还会使用真正的 Kotlin 客户端、ArkTS Store 和 Java 服务完成跨端整局、重启及非法请求测试。
