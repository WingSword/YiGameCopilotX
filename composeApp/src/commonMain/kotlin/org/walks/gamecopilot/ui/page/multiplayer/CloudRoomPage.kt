package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud as CloudIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.walks.gamecopilot.online.CloudRoomClient as Cloud
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppPrimaryAction
import org.walks.gamecopilot.ui.components.AppScreen
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.ui.components.AppSegmentedControl
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.werewolf.data.WerewolfPresets
import org.walks.gamecopilot.online.CloudRoom
import org.walks.gamecopilot.online.CloudInvitations
import org.walks.gamecopilot.online.forHostView
import org.walks.gamecopilot.online.LedgerScenes
import org.walks.gamecopilot.privacy.PrivacyPolicyDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudRoomEntry(initialGameType: String = "spy", onEntered: () -> Unit) {
    val design = LocalAppDesign.current
    var showPrivacy by remember { mutableStateOf(false) }
    if (showPrivacy) PrivacyPolicyDialog { showPrivacy = false }
    var server by rememberSaveable { mutableStateOf(Cloud.serverAddress) }
    var customServer by rememberSaveable { mutableStateOf(Cloud.serverAddress != Cloud.defaultServer) }
    var nickname by rememberSaveable { mutableStateOf(Cloud.nickname) }
    var code by rememberSaveable { mutableStateOf("") }
    var key by rememberSaveable { mutableStateOf("") }
    var create by rememberSaveable { mutableStateOf(true) }
    var spies by rememberSaveable { mutableIntStateOf(1) }
    var blanks by rememberSaveable { mutableIntStateOf(0) }
    var capacity by rememberSaveable { mutableIntStateOf(12) }
    var gameType by rememberSaveable { mutableStateOf(initialGameType) }
    var wolfCount by rememberSaveable { mutableIntStateOf(5) }
    var ledgerPreset by rememberSaveable { mutableStateOf("electronic") }
    var initialBalance by rememberSaveable { mutableStateOf("1500000") }
    var huntCount by rememberSaveable { mutableIntStateOf(6) }
    var witches by rememberSaveable { mutableIntStateOf(2) }
    var avalonCount by rememberSaveable { mutableIntStateOf(5) }
    var drawCount by rememberSaveable { mutableIntStateOf(3) }
    val busy by Cloud.busy.collectAsState()
    val error by Cloud.error.collectAsState()
    val scope = rememberCoroutineScope()
    val invitation by CloudInvitations.pending.collectAsState()
    val invitationError by CloudInvitations.error.collectAsState()
    LaunchedEffect(invitation) {
        invitation?.let { server = it.server; code = it.roomId; key = ""; create = false; customServer = false }
    }
    remember { Cloud.restore() }
    val hasSession by Cloud.joined.collectAsState()
    val validConfig = !create || if(gameType == "ledger") initialBalance.toLongOrNull()?.let { it in 0..999_999_999_999L } == true else gameType != "spy" || (spies + blanks) * 2 < capacity
    if (hasSession) {
        AppCard {
            AppSectionHeader("继续游玩", subtitle = "你已经加入一个网络房间")
            invitation?.let {
                Text(if(Cloud.matchesInvitation(it)) "你已在这个房间，可直接返回。" else "你已加入另一间房。请先返回原房间并离开，再使用此邀请。", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { CloudInvitations.clear() }) { Text("忽略这次邀请") }
            }
            AppPrimaryAction("返回已加入的房间", onClick = onEntered)
            TextButton(onClick = { showPrivacy = true }) { Text("隐私政策") }
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(design.spacing.lg)) {
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(design.spacing.lg)) {
                Surface(Modifier.size(44.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CloudIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(design.spacing.xs)) {
                    Text(if(invitation != null) "受邀加入房间 ${invitation?.roomId}" else "网络游戏", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(if(invitation != null) "填写昵称即可加入，身份仅自己可见。" else "与不同设备的朋友一起游玩，身份仅自己可见。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if(invitationError.isNotEmpty()) Text(invitationError, color = MaterialTheme.colorScheme.error)
            if(invitation != null) {
                Text("房间号和邀请已自动带入。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { CloudInvitations.clear(); create = true; server = Cloud.serverAddress }) { Text("取消邀请") }
            }
            if(invitation == null) {
                AppSegmentedControl(listOf("创建房间", "加入房间"), if(create) 0 else 1, { if (!busy) create = it == 0 })
            }
            OutlinedTextField(nickname,{nickname = it.take(20)},label={Text("我的昵称")},singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth().semantics { contentDescription = "我的昵称" })
            if (!create && invitation == null) OutlinedTextField(code,{code=it.filter(Char::isDigit).take(6)},label={Text("6 位房间号")},singleLine=true,enabled=!busy,
                modifier=Modifier.fillMaxWidth().semantics { contentDescription = "6 位房间号" },keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
            if(invitation == null) OutlinedTextField(key,{key=it.take(32)},label={Text(if(create) "房间密钥（可自动生成）" else "房间密钥")},
                singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth().semantics { contentDescription = "房间密钥" },visualTransformation=PasswordVisualTransformation())
        }
        if(create) {
            AppCard {
                AppSectionHeader("选择游戏", subtitle = "房主也参与游戏，人数包含房主")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(design.spacing.md)) {
                    listOf("spy" to "谁是卧底", "werewolf" to "一夜终极狼人", "hunt" to "猎巫镇", "ledger" to "桌游记账", "avalon" to "阿瓦隆", "drawing" to "你画我猜").forEach { (id, name) ->
                        FilterChip(gameType == id, { gameType = id }, enabled = !busy, label = { Text(name) })
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                if(gameType == "werewolf") {
                    CloudCountSelector("本局人数", "$wolfCount 人", "减少", "增加", !busy && wolfCount > 3, !busy && wolfCount < 10, { wolfCount-- }, { wolfCount++ })
                    Text(WerewolfPresets.getPresetForPlayerCount(wolfCount).roles.groupingBy { it.displayName }.eachCount().entries.joinToString(" · ") { "${it.key} ×${it.value}" })
                    Text("每人 1 张身份，另有 3 张中央底牌。由云端完成夜间行动和秘密投票。", style = MaterialTheme.typography.bodySmall)
                } else if(gameType in listOf("avalon", "drawing")) {
                    val count = if(gameType == "avalon") avalonCount else drawCount
                    CloudCountSelector("本局人数", "$count 人", "减少人数", "增加人数", !busy && count > if(gameType == "avalon") 5 else 3, !busy && count < 10,
                        { if(gameType == "avalon") avalonCount-- else drawCount-- }, { if(gameType == "avalon") avalonCount++ else drawCount++ })
                    if(gameType == "avalon") {
                        Text("${if(count <= 6) 2 else if(count <= 9) 3 else 4} 名邪恶 · 梅林、派西维尔、刺客、莫甘娜${if(count >= 7) "、莫德雷德" else ""}${if(count == 10) "、奥伯伦" else ""}，其余为忠臣。")
                        Text("基础规则：秘密表决、任务、刺杀。连续五次否决则邪恶胜；7 人及以上的第四次任务需要两张失败牌。不含湖中仙女等扩展。", style = MaterialTheme.typography.bodySmall)
                    } else Text("每人轮流画一次，每轮 90 秒。猜中得 10 分，画者得 5 分；请用画图表达，不写文字或数字。", style = MaterialTheme.typography.bodySmall)
                } else if(gameType == "hunt") {
                    Text("单身份简化玩法 · 每人一张身份")
                    CloudCountSelector("本局人数", "$huntCount 人", "减少人数", "增加人数", !busy && huntCount > 4, !busy && huntCount < 12,
                        { huntCount--; witches = minOf(witches, huntCount-2) }, { huntCount++ })
                    CloudCountSelector("身份配置", "$witches 女巫 · 1 警长", "减少女巫", "增加女巫", !busy && witches > 1, !busy && witches < huntCount-2, { witches-- }, { witches++ })
                    Text("沿用本机简化玩法，无原版审判牌、手牌与阴谋传染。警长可自守；夜间女巫目标和放逐最高票并列时无人出局，弃权不计票。", style = MaterialTheme.typography.bodySmall)
                } else if(gameType == "ledger") {
                    Text("记账场景（可修改起始余额）")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(design.spacing.md)) {
                        LedgerScenes.ids.forEach { id -> FilterChip(ledgerPreset == id, { ledgerPreset = id; initialBalance = LedgerScenes.initial[LedgerScenes.ids.indexOf(id)].toString() }, enabled = !busy, label = { Text(LedgerScenes.name(id)) }) }
                    }
                    OutlinedTextField(initialBalance, { initialBalance = it.filter { c -> c in '0'..'9' }.take(12) }, label = { Text("每人起始余额") }, singleLine = true,
                        enabled = !busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Text("2–20 人 · 房主负责银行和批量操作 · 其他玩家确认自己的付款", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("人数上限", style=MaterialTheme.typography.labelLarge)
                    AppSegmentedControl(listOf("8 人","12 人","16 人"),listOf(8,12,16).indexOf(capacity),{if (!busy) capacity=listOf(8,12,16)[it]})
                    Text("卧底人数", style=MaterialTheme.typography.labelLarge)
                    AppSegmentedControl(listOf("1 人","2 人","3 人"),spies-1,{if (!busy) spies=it+1})
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("加入 1 名白板",Modifier.weight(1f)); Switch(blanks==1,{blanks=if(it) 1 else 0},enabled=!busy)
                    }
                }
            }
        }
        if(invitation == null) AppCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if(customServer) "自定义服务" else "默认服务", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { customServer = !customServer; if (!customServer) server = Cloud.defaultServer }, enabled = !busy) {
                    Text(if (customServer) "使用默认服务" else "自定义服务器")
                }
            }
            if (customServer) OutlinedTextField(server, { server = it }, label = { Text("服务器地址") },
                singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "服务器地址" }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
        }
        if (!validConfig) Text(if(gameType == "ledger") "请输入有效的起始余额" else "卧底与白板合计需少于人数上限的一半，请增加人数上限或减少特殊身份。", color = MaterialTheme.colorScheme.error)
        if(error.isNotEmpty()) Text(error,color=MaterialTheme.colorScheme.error)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("加入后，昵称与本局操作会发送至所选服务器；公开信息对同房成员可见，私密身份按游戏阶段显示。",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (server.trim().startsWith("http://", ignoreCase = true)) Text("当前房间连接未加密，请使用玩家代号和专用房间密钥。",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { showPrivacy = true }) { Text("隐私政策与删除方式") }
        }
        AppPrimaryAction(text = if(busy) "连接中…" else if(create) "创建房间" else "加入房间", onClick={scope.launch {
            if(Cloud.enter(server,nickname,code,key,create,when(gameType) { "werewolf" -> wolfCount; "hunt" -> huntCount; "avalon" -> avalonCount; "drawing" -> drawCount; "ledger" -> 20; else -> capacity },spies,blanks,gameType,
                if(gameType == "werewolf") WerewolfPresets.getPresetForPlayerCount(wolfCount).roles.map { it.name } else emptyList(), ledgerPreset, initialBalance.toLongOrNull() ?: 0, witches, invitation?.token.orEmpty())) onEntered()
        }},enabled=!busy && validConfig && server.isNotBlank() && nickname.isNotBlank())
    }
}

@Composable
fun CloudRoomPage(onBack: () -> Unit) {
    val design = LocalAppDesign.current
    val room by Cloud.room.collectAsState()
    val error by Cloud.error.collectAsState()
    val busy by Cloud.busy.collectAsState()
    var reveal by remember { mutableStateOf(false) }
    var revealKey by remember { mutableStateOf(false) }
    val managing = room?.hostView == "admin"
    val modeSnackbar = remember { SnackbarHostState() }
    val modeMessages = remember(room?.roomId) { Channel<String>(Channel.UNLIMITED) }
    LaunchedEffect(modeMessages) { for(message in modeMessages) modeSnackbar.showSnackbar(message, duration = SnackbarDuration.Short) }
    DisposableEffect(modeMessages) { onDispose { modeMessages.close() } }
    var seenModeRevision by remember(room?.roomId) { mutableIntStateOf(-1) }
    LaunchedEffect(room?.roomId, room?.hostViewRevision) {
        val current = room ?: return@LaunchedEffect
        val previous = seenModeRevision; seenModeRevision = current.hostViewRevision
        if(previous >= 0) current.hostViewChanges.filter { it.revision > previous }.forEach {
            modeMessages.trySend(if(it.mode == "admin") "房主正在使用管理员模式" else "房主已切回玩家模式")
        }
    }
    var showInvitation by remember { mutableStateOf(false) }
    LaunchedEffect(room?.status) { showInvitation = false }
    LaunchedEffect(room?.isHost == true && managing) { reveal = false; revealKey = false; showInvitation = false }
    var confirm by remember { mutableStateOf("") }
    var confirmRoundId by remember { mutableStateOf("") }
    var confirmWinner by remember { mutableStateOf("") }
    var winner by remember { mutableStateOf("未判定") }
    var active by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if(event == Lifecycle.Event.ON_STOP) { active = false; reveal = false; revealKey = false; showInvitation = false }
            if(event == Lifecycle.Event.ON_START) active = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(active) { if(active) { while(true) { Cloud.refresh(); delay(Cloud.pollDelayMillis) } } }
    LaunchedEffect(room?.roundId, room?.status, room?.game?.stepId) { reveal = false; confirm = ""; winner = "未判定" }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
        AppScreen(
            title = "网络房间",
            subtitle = room?.let { "${it.gameTitle} · 房间 ${it.roomId}" },
            modifier = Modifier.widthIn(max = 720.dp),
            actions = { TextButton(onClick=onBack) { Text("收起") } }
        ) {
            room?.takeIf { it.supportsHostView }?.let { CloudHostModeNotice(it.hostView == "admin") }
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(bottom=100.dp),
                verticalArrangement=Arrangement.spacedBy(design.spacing.lg)) {
                if(error.isNotEmpty()) Text(error,color=MaterialTheme.colorScheme.error)
                val current = room
                if(current == null) {
                    Text(if(error.isEmpty()) "正在恢复房间…" else "可返回联机页重新加入")
                    TextButton(onClick=onBack) { Text("返回联机页") }
                } else {
                    if (!current.supportsHostView) {
                        AppCard {
                            AppSectionHeader("云端服务需要更新", subtitle = "房间 ${current.roomId}")
                            Text(Cloud.compatibilityMessage, style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = { scope.launch { Cloud.refresh() } }, enabled = !busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("更新后重试") }
                        }
                    } else {
                        val management = current.isHost && managing
                        val playerMode = !current.isHost || !managing
                        val display = current.forHostView(management)
                        if(current.isHost) {
                            AppCard {
                                AppSectionHeader("房主视图", subtitle = "切换时会提醒所有成员")
                                AppSegmentedControl(listOf("玩家模式", "管理员模式"), if(managing) 1 else 0, { if(!busy) scope.launch { Cloud.action("hostview", buildJsonObject {
                                    put("roundId", current.roundId); put("revision", current.hostViewRevision); put("hostView", if(it == 1) "admin" else "player")
                                }) } })
                                Text(if(managing) "管理房间。你仍然参赛；切换界面不会改变身份或人数。" else "你的身份和行动。房间操作可在管理员模式中使用。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        AppCard {
                            AppSectionHeader("${current.gameTitle} · 第 ${if (current.status == "WAITING") current.roundNumber + 1 else current.roundNumber} 局",
                                action = { CloudStatusLabel(when(current.status) { "WAITING" -> "待开局"; "FINISHED" -> "已结束"; else -> "进行中" }, current.status == "PLAYING") })
                            Text("房间号", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            SelectionContainer { Text(current.roomId,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold,letterSpacing=4.sp) }
                            if(Cloud.roomKey.isNotEmpty()) Row(verticalAlignment=Alignment.CenterVertically) {
                                Text("密钥",Modifier.weight(1f))
                                TextButton(onClick={revealKey=!revealKey}) { Text(if(revealKey) "隐藏" else "查看密钥") }
                            }
                            if(revealKey) SelectionContainer { Text(Cloud.roomKey,fontSize=20.sp) }
                            Text(if(current.isAvalon || current.isDrawing) "${current.players.size}/${current.maxPlayers} 人" else if(current.isLedger) "${current.players.size}/${current.maxPlayers} 人 · ${LedgerScenes.name(current.ledgerConfig?.preset.orEmpty())} · 起始 ${current.ledgerConfig?.initialBalance ?: 0}" else if(current.isHunt) "${current.players.size}/${current.maxPlayers} 人 · ${current.witchCount} 女巫 · 1 警长" else if(current.isOneNight) "${current.players.size}/${current.maxPlayers} 人 · 3 张中央底牌" else "${current.players.size}/${current.maxPlayers} 人 · ${current.spyCount} 卧底 · ${current.blankCount} 白板")
                            if(current.isHunt) Text("简化规则：警长可自守；女巫目标与放逐最高票并列时无人出局。女巫或非女巫全部出局时结算胜负。无审判牌、手牌和阴谋传染。", style = MaterialTheme.typography.bodySmall)
                            if(management && current.status == "WAITING") OutlinedButton(onClick = { showInvitation = true }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("邀请玩家 · 二维码") }
                        }
                        if(current.status == "WAITING") {
                            AppCard {
                                AppSectionHeader("等待玩家", subtitle = "已准备 ${current.players.count { it.ready && it.connected }} / ${current.players.size} 人")
                                current.players.forEachIndexed { index, p ->
                                    if(index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), horizontalArrangement = Arrangement.spacedBy(design.spacing.md), verticalAlignment=Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(p.nickname, style = MaterialTheme.typography.bodyLarge, fontWeight = if(p.id == current.selfId) FontWeight.SemiBold else FontWeight.Normal)
                                            if(p.id == current.hostId || p.id == current.selfId) Text(listOfNotNull(if(p.id == current.hostId) "房主" else null, if(p.id == current.selfId) "我" else null).joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        CloudStatusLabel(if(!p.connected) "重连中" else if(p.ready) "已准备" else "未准备", p.connected && p.ready)
                                        if(management && p.id!=current.selfId) TextButton(onClick={confirm="kick:${p.id}"},enabled=!busy) { Text("移出") }
                                    }
                                }
                            }
                            if(management) {
                                Text(if(current.isLedger) "至少 2 人，所有人准备后开账。中途离开会结束记账并保留余额与记录。" else if(current.gameType != "spy") "需要 ${current.maxPlayers} 人全部加入并准备。" else "至少 4 人，所有人准备后开始；卧底与白板合计需少于一半。",style=MaterialTheme.typography.bodySmall)
                                if (current.self?.ready == false) Button(onClick={scope.launch {Cloud.action("ready",buildJsonObject {put("ready",true)})}},enabled=!busy) { Text("我准备好了") }
                                AppPrimaryAction(text = if(current.isLedger) "开始共同记账" else if(current.isDrawing) "开始你画我猜" else "开始并分发身份",
                                    onClick={scope.launch { Cloud.action("start",buildJsonObject { put("roundNumber",current.roundNumber) }) }},
                                    enabled=!busy && error.isEmpty() && current.hasEnoughPlayers && current.players.all { it.ready && it.connected })
                            } else if(current.self?.ready == true) OutlinedButton(onClick={scope.launch {Cloud.action("ready",buildJsonObject {put("ready",false)})}},
                                enabled=!busy,modifier=Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("取消准备") }
                            else AppPrimaryAction("我准备好了", onClick={scope.launch {Cloud.action("ready",buildJsonObject {put("ready",true)})}}, enabled=!busy)
                        } else {
                            if(current.isHost && managing && current.status == "PLAYING" && !current.isLedger) Text("自己的投票、任务或作画操作，请切换到玩家模式。")
                            if(current.isLedger) key(management) { CloudLedgerPanel(display, busy, management) }
                            else if(current.isDrawing) CloudDrawingPanel(display, busy, management, playerMode)
                            else if(playerMode) Surface(Modifier.fillMaxWidth().clickable { reveal=!reveal },shape=RoundedCornerShape(design.cornerRadius.card),color=MaterialTheme.colorScheme.primaryContainer, contentColor=MaterialTheme.colorScheme.onPrimaryContainer) {
                                Column(Modifier.fillMaxWidth().padding(design.spacing.xxl),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(design.spacing.lg)) {
                                    Text(if(reveal) current.identity?.role.orEmpty() else "我的身份",style=MaterialTheme.typography.titleMedium)
                                    Text(if(reveal) current.identity?.word.orEmpty() else "轻点查看",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                                    Text(if(reveal) "再次轻点隐藏" else "仅自己可见",style=MaterialTheme.typography.bodySmall)
                                }
                            }
                            if(current.isOneNight || current.isHunt || current.isAvalon) CloudGamePanel(display, reveal, busy, management)
                            if(current.status=="FINISHED") {
                                AppSectionHeader("本局结束", subtitle = current.winner)
                                if(management) AppPrimaryAction("再来一局", onClick={if(current.isLedger) { confirmRoundId=current.roundId; confirm="next" } else scope.launch {Cloud.action("next",buildJsonObject {put("roundId",current.roundId)})}},enabled=!busy)
                            } else if(current.isLedger) {
                                if(management) OutlinedButton(onClick = { confirmRoundId = current.roundId; confirmWinner = "记账结束"; confirm = "finish" }, enabled = !busy) { Text("结束记账") }
                            } else if(current.gameType == "spy") {
                                Text("轮流描述词条，讨论后投票。",color=MaterialTheme.colorScheme.onSurfaceVariant)
                                if(management) {
                                    AppSegmentedControl(listOf("平民","卧底","白板","未判定"),listOf("平民","卧底","白板","未判定").indexOf(winner),{winner=listOf("平民","卧底","白板","未判定")[it]})
                                    AppPrimaryAction("结束并记录本局", onClick={confirmRoundId=current.roundId;confirmWinner=winner;confirm="finish"},enabled=!busy)
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        TextButton(onClick={confirm="leave"},enabled=!busy) {Text("离开房间")}
                        if(current.isHost && (!current.supportsHostView || managing)) TextButton(onClick={confirm="close"},enabled=!busy) {Text("解散房间",color=MaterialTheme.colorScheme.error)}
                    }
                }
            }
        }
        SnackbarHost(modeSnackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
    }
    if(showInvitation && room?.isHost == true && room?.status == "WAITING") CloudInvitationDialog { showInvitation = false }
    if(confirm.isNotEmpty()) AlertDialog(onDismissRequest={confirm=""},title={Text(when { confirm=="next"->"开启新账本"; confirm=="close"->"解散房间"; confirm=="finish"->"结束本局"; confirm.startsWith("kick:")->"移出玩家"; else->"离开房间" })},
        text={Text(when {confirm=="next"->"将清空本局余额和交易记录，重新准备。需要保留的账本请先导出。";confirm=="close"->"所有玩家都会离开房间。";confirm=="finish" && room?.isLedger==true->"结束后账本只读，仍可查看和导出。待确认收款不会扣款。";confirm=="finish"->"记录本局结果：$confirmWinner。";confirm.startsWith("kick:")->"该玩家需要重新输入密钥加入。";confirm=="leave" && room?.isLedger==true && room?.status=="PLAYING"->"离开将结束共同记账，保留本局余额和记录；待确认收款不会扣款。";else->"房主离开后将自动转交给下一位玩家。"})},
        confirmButton={TextButton(onClick={
            val command=confirm;confirm=""
            scope.launch {
                val ok=when {
                    command=="close"->Cloud.closeRoom()
                    command=="finish"->Cloud.action("finish",buildJsonObject {put("roundId",confirmRoundId);put("winner",confirmWinner)})
                    command=="next"->Cloud.action("next",buildJsonObject {put("roundId",confirmRoundId)})
                    command.startsWith("kick:")->Cloud.action("kick",buildJsonObject {put("playerId",command.removePrefix("kick:"))})
                    else->Cloud.action("leave")
                }
                if(ok && command in listOf("close","leave")) onBack()
            }
        }) {Text("确认")}}, dismissButton={TextButton(onClick={confirm=""}) {Text("取消")}})
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CloudGamePanel(room: CloudRoom, reveal: Boolean, busy: Boolean, managing: Boolean) {
    val game = room.game ?: return
    val scope = rememberCoroutineScope()
    var selected by remember(game.stepId) { mutableStateOf(setOf<String>()) }
    AppCard {
        AppSectionHeader(when(game.phase) { "DEAL" -> "查看身份"; "NIGHT" -> "夜间行动"; "DAY" -> "白天讨论"; "VOTE", "TEAM_VOTE" -> "秘密投票"; "TEAM" -> "队长组队"; "QUEST" -> "执行任务"; "ASSASSINATE" -> "刺杀梅林"; else -> "对局结果" })
        // Action prompts and notes may contain role information; share the identity privacy switch.
        if (managing || reveal || game.phase in listOf("DAY", "VOTE", "RESULT", "ABORTED", "TEAM", "TEAM_VOTE", "QUEST", "ASSASSINATE")) {
            Text(game.prompt)
            if(reveal) game.notes.forEach { Text(it, color = MaterialTheme.colorScheme.primary) }
            game.options.forEach { choice ->
                if(choice.id == "team") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(LocalAppDesign.current.spacing.md)) {
                        room.players.forEach { p ->
                            FilterChip(p.id in selected, { selected = if(p.id in selected) selected - p.id else selected + p.id }, enabled = !busy && (p.id in selected || selected.size < (game.avalon?.teamSize ?: 0)), label = { Text(p.nickname + if(p.id == room.selfId) "（我）" else "") })
                        }
                    }
                }
                Button(onClick = { scope.launch { Cloud.action("game", buildJsonObject { put("roundId",room.roundId); put("stepId",game.stepId); put("choice",choice.id); if(choice.id == "team") put("targets", JsonArray(selected.map(::JsonPrimitive))) }) } },
                    enabled = !busy && (choice.id != "team" || selected.size == game.avalon?.teamSize), modifier = Modifier.fillMaxWidth()) { Text(choice.label) }
            }
        } else Text("轻点上方身份卡，查看自己的行动提示。")
        if(game.phase == "DEAL") Text("已确认 ${game.confirmedCount}/${room.players.size}")
        if(game.phase == "VOTE") Text("已投票 ${game.votedCount}/${if(room.isHunt) game.livingCount else room.players.size}")
        game.avalon?.let { a ->
            Text("第 ${a.quest} 次任务 · 队长：${room.players.firstOrNull { it.id == a.leaderId }?.nickname.orEmpty()}")
            Text("出征 ${a.teamSize} 人 · 连续否决 ${a.rejections}/5")
            if(a.team.isNotEmpty()) Text("队伍：" + room.players.filter { it.id in a.team }.joinToString("、") { it.nickname })
            if(game.phase in listOf("TEAM_VOTE", "QUEST")) Text("已提交 ${a.submitted}/${if(game.phase == "QUEST") a.team.size else room.players.size}")
            a.missions.forEach { Text("任务 ${it.quest}：${if(it.success) "成功" else "失败"} · ${it.fails} 张失败牌") }
            game.publicLog.takeLast(12).forEach { Text(it) }
        }
        if(room.isHunt) {
            Text("第 ${game.day} 天 · ${game.livingCount} 人存活")
            game.roster.forEach { Text("${it.nickname} · ${if(it.alive) "存活" else "出局"}${if(it.role.isNotEmpty()) " · ${it.role}" else ""}") }
            game.publicLog.takeLast(12).forEach { Text(it) }
        }
        game.revealed.forEach { p -> Text("${p.nickname}：${p.initialRole} → ${p.finalRole} · 投给 ${p.voteTarget}${if(p.eliminated) " · 出局" else ""}") }
        if(game.center.isNotEmpty()) Text("中央底牌：${game.center.joinToString("、")}")
    }
}
