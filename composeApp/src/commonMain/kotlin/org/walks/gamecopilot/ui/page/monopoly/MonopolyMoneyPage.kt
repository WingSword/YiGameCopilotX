package org.walks.gamecopilot.ui.page.monopoly

import org.walks.gamecopilot.distribution.AppDistribution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.foundation.text.selection.SelectionContainer
import org.walks.gamecopilot.online.*
import org.walks.gamecopilot.data.entity.MonopolyLedgerRules
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.currentTimeMillis
import org.walks.gamecopilot.data.entity.MonopolyGameState
import org.walks.gamecopilot.data.entity.MonopolyPlayer
import org.walks.gamecopilot.data.entity.MonopolyTransaction
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.mmkv.MMKV_MONOPOLY_LEDGER_KEY
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppEmptyState
import org.walks.gamecopilot.ui.components.AppScreen

private const val MAX_MONEY = 999_999_999_999L
private const val MAX_PLAYERS = 20
private const val MAX_TRANSACTIONS = 100

private val monopolyJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** A standalone local ledger that keeps working without a LAN room. */
@Suppress("UNUSED_PARAMETER")
@Composable
fun MonopolyMoneyPage(
    viewModel: MainViewmodel,
    onCloud: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var ledger by remember { mutableStateOf(normalizePlayerColors(loadLedger())) }
    var roundTableOverride by remember { mutableStateOf<Boolean?>(null) }
    var showScene by remember { mutableStateOf(false) }
    var showBatch by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }
    val unit = when(ledger.preset) { "score" -> "分"; "chips" -> "筹码"; else -> "游戏币" }
    var showHistory by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<MonopolyPlayer?>(null) }
    var playerPendingDeletion by remember { mutableStateOf<MonopolyPlayer?>(null) }
    var showUndoConfirmation by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var noticeMessage by remember { mutableStateOf<String?>(null) }

    fun saveLedger(updated: MonopolyGameState) {
        val normalized = normalizePlayerColors(updated.copy(transactions = MonopolyLedgerRules.retainGroups(updated.transactions)))
        ledger = normalized
        MMKVUtils.put(
            MMKV_MONOPOLY_LEDGER_KEY,
            monopolyJson.encodeToString(MonopolyGameState.serializer(), normalized)
        )
    }

    fun playerName(id: String?, snapshot: String?): String {
        if (id == null) return "银行"
        return ledger.players.firstOrNull { it.id == id }?.name
            ?: snapshot
            ?: "已删除玩家"
    }

    fun canUndoLatest(): Boolean = runCatching { MonopolyLedgerRules.undo(ledger) }.isSuccess
    fun applyTransactions(entries: List<MonopolyTransaction>) {
        runCatching { MonopolyLedgerRules.apply(ledger, entries) }.onSuccess(::saveLedger)
            .onFailure { noticeMessage = it.message ?: "请检查金额与账户" }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
    val wide = maxWidth >= 600.dp || maxWidth > maxHeight
    val roundTable = roundTableOverride ?: wide
    val modeColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onSurface)
    AppScreen(
        title = "桌游记账", onBack = onBack,
        actions = {
            IconButton(onClick = { showResetConfirmation = true }, enabled = ledger.players.isNotEmpty()) {
                Icon(Icons.Outlined.RestartAlt, "重置本局余额")
            }
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !roundTable, onClick = { roundTableOverride = false }, label = { Text("列表") }, colors = modeColors)
            FilterChip(selected = roundTable, onClick = { roundTableOverride = true }, label = { Text("圆桌") }, colors = modeColors)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = { showScene = true }) { Text(LedgerScenes.name(ledger.preset) + " · " + unit) }
            TextButton(onClick = { showBatch = true }, enabled = ledger.players.isNotEmpty()) { Text("全员收付") }
            if (AppDistribution.roomsEnabled) TextButton(onClick = onCloud) { Text("云端记账") }
        }
        if (roundTable) {
            RoundTableBoard(players = ledger.players, recent = { recentPlayerTransaction(it, ledger.transactions) },
                onAdd = { showAddPlayerDialog = true }, onNew = { showTransactionDialog = true },
                onHistory = { showHistory = true }, onEdit = { selectedPlayer = it }, modifier = Modifier.weight(1f).fillMaxWidth())
        } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (ledger.players.size >= MAX_PLAYERS) {
                        noticeMessage = "最多添加 $MAX_PLAYERS 位玩家"
                    } else {
                        showAddPlayerDialog = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("添加玩家")
            }
            Button(
                onClick = { showTransactionDialog = true },
                enabled = ledger.players.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("新建收支")
            }
        }

        val recentCard: @Composable () -> Unit = {
            RecentTransactionsCard(ledger.transactions, ::playerName, canUndoLatest(), onExport = { showExport = true },
                onUndo = { showUndoConfirmation = true }, onClear = { showClearConfirmation = true })
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ledger.players.isEmpty()) {
                item {
                    AppEmptyState(
                        title = "还没有玩家",
                        description = "添加玩家后，就可以记录银行收支和玩家转账。",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp)
                    )
                }
            } else {
                item {
                    Text(
                        text = "玩家 · ${ledger.players.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(ledger.players, key = { it.id }) { player ->
                    PlayerBalanceCard(player = player, recent = recentPlayerTransaction(player, ledger.transactions), onEdit = { selectedPlayer = player })
                }
            }

            if (!wide) item { recentCard() }
        }
        if (wide) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { recentCard() }
        }
        }
    }

    }
    }

    if (showScene) LedgerSceneDialog(ledger.preset, ledger.initialBalance, { showScene = false }) { preset, initial ->
        saveLedger(ledger.copy(preset = preset, initialBalance = initial, transactions = emptyList(),
            players = ledger.players.map { it.copy(balance = initial) }))
        showScene = false
    }
    if (showBatch) LedgerBatchDialog(ledger.players.size, ledger.preset, { showBatch = false }) { receive, amount ->
        val group = "batch_${currentTimeMillis()}_${kotlin.random.Random.nextInt()}"
        applyTransactions(ledger.players.map { player -> MonopolyTransaction(
            fromPlayerId = if(receive) null else player.id, toPlayerId = if(receive) player.id else null,
            amount = amount, description = if(receive) "全员发放" else "全员缴纳", fromPlayerName = if(receive) null else player.name,
            toPlayerName = if(receive) player.name else null, batchId = group) })
        showBatch = false
    }
    if (showExport) AppDialog(title = "账本 CSV · 长按复制", onDismiss = { showExport = false }) {
        SelectionContainer { Text(ledger.toExportLedger(unit).exportCsv()) }
    }
    if (showHistory) {
        AppDialog(title = "最近交易 · ${ledger.transactions.size}", onDismiss = { showHistory = false }) {
            RecentTransactionsCard(ledger.transactions, ::playerName, canUndoLatest(), onExport = { showExport = true },
                onUndo = { showUndoConfirmation = true }, onClear = { showClearConfirmation = true },
                showHeading = false, limit = MAX_TRANSACTIONS)
        }
    }

    if (showAddPlayerDialog) {
        AddPlayerDialog(
            suggestedName = "玩家 ${ledger.players.size + 1}",
            initialBalance = ledger.initialBalance,
            existingNames = ledger.players.map { it.name },
            onDismiss = { showAddPlayerDialog = false },
            onAdd = { name ->
                val player = MonopolyPlayer(
                    id = "player_${currentTimeMillis()}_${ledger.players.size}",
                    name = name,
                    balance = ledger.initialBalance
                )
                saveLedger(ledger.copy(players = ledger.players + player))
                showAddPlayerDialog = false
            }
        )
    }

    if (showTransactionDialog) {
        TransactionDialog(
            players = ledger.players, preset = ledger.preset,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { fromId, toId, amount, description ->
                applyTransactions(listOf(MonopolyTransaction(fromId, toId, amount, description,
                    fromPlayerName = ledger.players.firstOrNull { it.id == fromId }?.name,
                    toPlayerName = ledger.players.firstOrNull { it.id == toId }?.name)))
                showTransactionDialog = false
            }
        )
    }

    selectedPlayer?.let { player ->
        EditPlayerDialog(
            player = player,
            existingPlayers = ledger.players,
            onDismiss = { selectedPlayer = null },
            onSave = { name, balance ->
                val current = ledger.players.firstOrNull { it.id == player.id }
                if(current != null) {
                    val delta = balance - current.balance
                    val total = kotlin.math.abs(delta)
                    val parts = if(total > MAX_MONEY) listOf(MAX_MONEY, total - MAX_MONEY) else if(total > 0) listOf(total) else emptyList()
                    val group = "adjust_${currentTimeMillis()}_${kotlin.random.Random.nextInt()}"
                    val entries = parts.map { amount -> MonopolyTransaction(if(delta > 0) null else player.id, if(delta > 0) player.id else null,
                        amount, "手动调账", fromPlayerName = if(delta > 0) null else current.name, toPlayerName = if(delta > 0) current.name else null, batchId = group) }
                    runCatching { if(entries.isEmpty()) ledger else MonopolyLedgerRules.apply(ledger, entries) }
                        .onSuccess { state -> saveLedger(state.copy(players = state.players.map { if(it.id == player.id) it.copy(name = name) else it })) }
                        .onFailure { noticeMessage = it.message }
                }
                selectedPlayer = null
            },
            onDelete = {
                selectedPlayer = null
                playerPendingDeletion = player
            }
        )
    }

    playerPendingDeletion?.let { player ->
        ConfirmationDialog(
            title = "删除玩家",
            message = "确定删除“${player.name}”吗？历史交易会保留当时的姓名。",
            confirmText = "删除",
            destructive = true,
            onDismiss = { playerPendingDeletion = null },
            onConfirm = {
                saveLedger(ledger.copy(players = ledger.players.filterNot { it.id == player.id }))
                playerPendingDeletion = null
            }
        )
    }

    if (showUndoConfirmation) {
        val latest = ledger.transactions.firstOrNull()
        if (latest != null) {
            ConfirmationDialog(
                title = "撤销上一笔",
                message = "撤销“${latest.description}”共 ${MonopolyLedgerRules.latest(ledger.transactions).size} 项收支？整批恢复对应余额。",
                confirmText = "撤销",
                onDismiss = { showUndoConfirmation = false },
                onConfirm = {
                    runCatching { MonopolyLedgerRules.undo(ledger) }.onSuccess(::saveLedger).onFailure { noticeMessage = it.message }
                    showUndoConfirmation = false
                }
            )
        }
    }

    if (showResetConfirmation) {
        ConfirmationDialog(title = "重置本局余额", message = "保留玩家，每人余额恢复为 ${ledger.initialBalance} $unit，清除交易记录。此操作无法撤销。",
            confirmText = "重置", onDismiss = { showResetConfirmation = false }, onConfirm = {
                saveLedger(ledger.copy(players = ledger.players.map { MonopolyPlayer(id = it.id, name = it.name, balance = ledger.initialBalance, colorIndex = it.colorIndex) }, transactions = emptyList()))
                showResetConfirmation = false
            })
    }

    if (showClearConfirmation) {
        ConfirmationDialog(
            title = "清空账本",
            message = "所有玩家、余额和交易记录都会被删除，此操作无法撤销。",
            confirmText = "清空",
            destructive = true,
            onDismiss = { showClearConfirmation = false },
            onConfirm = {
                saveLedger(MonopolyGameState())
                showClearConfirmation = false
            }
        )
    }

    noticeMessage?.let { message ->
        AppDialog(
            title = "提示",
            onDismiss = { noticeMessage = null },
            actions = { Button(onClick = { noticeMessage = null }) { Text("知道了") } }
        ) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerBalanceCard(player: MonopolyPlayer, recent: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(Color(RoundTableLayout.colors[player.colorIndex.coerceIn(0, 19)]), CircleShape))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(recent, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (player.isBankrupt) {
                    Text("已破产", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
            Text(
                text = "${formatMoney(player.balance)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (player.balance < 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑${player.name}")
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    transactions: List<MonopolyTransaction>,
    playerName: (String?, String?) -> String,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onExport: () -> Unit,
    showHeading: Boolean = true,
    limit: Int = 20
) {
    AppCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showHeading) "最近交易 · ${transactions.size}" else "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (transactions.isNotEmpty()) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "撤销上一笔")
                }
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "清空账本",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        TextButton(onClick = onExport) { Text("导出账本") }
        if (transactions.isEmpty()) {
            Text(
                text = "完成第一笔收支后会显示在这里",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            transactions.take(limit).forEach { transaction ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${playerName(transaction.fromPlayerId, transaction.fromPlayerName)} → " +
                                    playerName(transaction.toPlayerId, transaction.toPlayerName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${formatMoney(transaction.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPlayerDialog(
    suggestedName: String,
    initialBalance: Long,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf(suggestedName) }
    val normalizedName = name.trim()
    val isDuplicate = existingNames.any { it.trim().equals(normalizedName, ignoreCase = true) }

    AppDialog(
        title = "添加玩家",
        subtitle = "新玩家起始余额：$initialBalance",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) { Text("取消") }
            Button(
                onClick = { onAdd(normalizedName) },
                enabled = normalizedName.isNotEmpty() && !isDuplicate
            ) { Text("添加") }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 20) name = it },
            label = { Text("玩家名称") },
            supportingText = { if (isDuplicate) Text("已有同名玩家，请换一个名字") },
            isError = isDuplicate,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDialog(
    players: List<MonopolyPlayer>,
    preset: String,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?, Long, String) -> Unit
) {
    var fromPlayerId by remember { mutableStateOf<String?>(null) }
    var toPlayerId by remember { mutableStateOf(players.firstOrNull()?.id) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    val parsedAmount = amount.toLongOrNull()
    val isValid = parsedAmount != null && parsedAmount in 1..MAX_MONEY &&
            (fromPlayerId != null || toPlayerId != null) && fromPlayerId != toPlayerId

    AppDialog(
        title = "新建收支",
        subtitle = "银行可作为付款方或收款方",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) { Text("取消") }
            Button(
                onClick = {
                    val defaultDescription = when {
                        fromPlayerId == null -> "银行发放"
                        toPlayerId == null -> "缴纳给银行"
                        else -> "转账"
                    }
                    onConfirm(
                        fromPlayerId,
                        toPlayerId,
                        parsedAmount ?: 0,
                        description.trim().ifEmpty { defaultDescription }
                    )
                },
                enabled = isValid
            ) { Text("确认") }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ParticipantDropdown(
                title = "付款方",
                players = players,
                selectedId = fromPlayerId,
                expanded = expandedFrom,
                onExpandedChange = { expandedFrom = it },
                onSelected = { fromPlayerId = it; expandedFrom = false }
            )
            ParticipantDropdown(
                title = "收款方",
                players = players,
                selectedId = toPlayerId,
                expanded = expandedTo,
                onExpandedChange = { expandedTo = it },
                onSelected = { toPlayerId = it; expandedTo = false }
            )
            TextButton(onClick = { val previous = fromPlayerId; fromPlayerId = toPlayerId; toPlayerId = previous }) { Text("交换收付双方") }
            Text("常用金额", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LedgerScenes.quickAmounts(preset).forEach { quickAmount ->
                    OutlinedButton(
                        onClick = { amount = quickAmount.toString() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(formatMoney(quickAmount), maxLines = 1, fontSize = 12.sp)
                    }
                }
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter(Char::isDigit).take(12) },
                label = { Text("金额") },
                placeholder = { Text("也可以输入其他金额") },
                supportingText = {
                    if (parsedAmount != null && parsedAmount > MAX_MONEY) {
                        Text("金额不能超过 ${formatMoney(MAX_MONEY)}")
                    }
                },
                isError = parsedAmount != null && parsedAmount > MAX_MONEY,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(40) },
                label = { Text("备注（可选）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantDropdown(
    title: String,
    players: List<MonopolyPlayer>,
    selectedId: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String?) -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = players.firstOrNull { it.id == selectedId }?.name ?: "银行",
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            DropdownMenuItem(text = { Text("银行") }, onClick = { onSelected(null) })
            players.forEach { player ->
                DropdownMenuItem(text = { Text(player.name) }, onClick = { onSelected(player.id) })
            }
        }
    }
}

@Composable
private fun EditPlayerDialog(
    player: MonopolyPlayer,
    existingPlayers: List<MonopolyPlayer>,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(player.id) { mutableStateOf(player.name) }
    var balance by remember(player.id) { mutableStateOf(player.balance.toString()) }
    val normalizedName = name.trim()
    val parsedBalance = balance.toLongOrNull()
    val isDuplicate = existingPlayers.any {
        it.id != player.id && it.name.trim().equals(normalizedName, ignoreCase = true)
    }
    val validBalance = parsedBalance != null && parsedBalance in -MAX_MONEY..MAX_MONEY

    AppDialog(
        title = "编辑玩家",
        subtitle = "可以直接调整余额或删除玩家",
        onDismiss = onDismiss,
        actions = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("删除") }
            TextButton(onClick = onDismiss) { Text("取消") }
            Button(
                onClick = { onSave(normalizedName, parsedBalance ?: player.balance) },
                enabled = normalizedName.isNotEmpty() && !isDuplicate && validBalance
            ) { Text("保存") }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 20) name = it },
                label = { Text("玩家名称") },
                supportingText = { if (isDuplicate) Text("已有同名玩家，请换一个名字") },
                isError = isDuplicate,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = balance,
                onValueChange = { input ->
                    balance = input.filterIndexed { index, char ->
                        char.isDigit() || (char == '-' && index == 0)
                    }.take(13)
                },
                label = { Text("余额") },
                supportingText = {
                    if (parsedBalance != null && parsedBalance !in -MAX_MONEY..MAX_MONEY) {
                        Text("余额不能超过正负 ${formatMoney(MAX_MONEY)}")
                    }
                },
                isError = balance.isNotBlank() && !validBalance,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    destructive: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AppDialog(
        title = title,
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) { Text("取消") }
            Button(
                onClick = onConfirm,
                colors = if (destructive) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else ButtonDefaults.buttonColors()
            ) { Text(confirmText) }
        }
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun loadLedger(): MonopolyGameState {
    val raw = MMKVUtils.getString(MMKV_MONOPOLY_LEDGER_KEY, "")
    if (raw.isBlank()) return MonopolyGameState()
    return runCatching {
        monopolyJson.decodeFromString(MonopolyGameState.serializer(), raw)
    }.getOrDefault(MonopolyGameState()).let { state ->
        state.copy(transactions = state.transactions.take(MAX_TRANSACTIONS))
    }
}

internal fun formatMoney(amount: Long): String {
    val sign = if (amount < 0) "-" else ""
    val absolute = if (amount == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(amount)
    return when {
        absolute >= 100_000_000 -> "$sign${trimDecimal((absolute / 1_000_000).toDouble() / 100)}亿"
        absolute >= 10_000 -> "$sign${trimDecimal((absolute / 100).toDouble() / 100)}万"
        else -> amount.toString()
    }
}

private fun trimDecimal(value: Double): String {
    val whole = value.toLong()
    return if (value == whole.toDouble()) whole.toString() else value.toString()
}

internal fun normalizePlayerColors(state: MonopolyGameState): MonopolyGameState {
    val colors = RoundTableLayout.assignColors(state.players.map { it.colorIndex })
    return state.copy(players = state.players.mapIndexed { i, player -> player.copy(colorIndex = colors[i]) })
}

internal fun recentPlayerTransaction(player: MonopolyPlayer, transactions: List<MonopolyTransaction>): String {
    val latest = transactions.firstOrNull { it.fromPlayerId == player.id || it.toPlayerId == player.id }
        ?: return "暂无收支"
    val sign = if (latest.toPlayerId == player.id) "+" else "−"
    return "$sign${formatMoney(latest.amount)} · ${latest.description}"
}
