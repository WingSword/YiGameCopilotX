package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import org.walks.gamecopilot.online.*
import org.walks.gamecopilot.online.CloudRoomClient as Cloud
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppDialogActions
import org.walks.gamecopilot.ui.components.AppEmptyState
import org.walks.gamecopilot.ui.components.AppPrimaryAction
import org.walks.gamecopilot.ui.components.AppScreen
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
fun LedgerCloudEntryPage(onBack: () -> Unit, onEntered: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AppScreen(title = "共同记账", subtitle = "每人使用自己的设备，实时查看收支", onBack = onBack, modifier = Modifier.widthIn(max = 680.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 80.dp)) {
                CloudRoomEntry(initialGameType = "ledger", onEntered = onEntered)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudLedgerPanel(room: CloudRoom, busy: Boolean, manage: Boolean = room.isHost) {
    val design = LocalAppDesign.current
    val ledger = room.ledger ?: return
    var operation by remember(room.roundId) { mutableStateOf("") }
    var export by remember { mutableStateOf(false) }
    var historyCount by remember { mutableIntStateOf(10) }
    var confirm by remember { mutableStateOf<JsonObject?>(null) }
    var confirmation by remember { mutableStateOf("") }
    val pending by Cloud.pendingLedger.collectAsState()
    val scope = rememberCoroutineScope()
    val canWrite = room.status == "PLAYING" && !busy && pending.isEmpty()
    fun command(op: String, id: String) = buildJsonObject {
        put("roundId", room.roundId); put("revision", ledger.revision); put("operation", op); put("entryId", id)
    }
    AppCard {
        AppSectionHeader("${LedgerScenes.name(ledger.preset)} · ${ledger.unit}", subtitle = "每人起始 ${ledger.initialBalance} · 公共池独立记账")
        ledger.accounts.forEachIndexed { index, account ->
            if(index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Row(Modifier.fillMaxWidth().padding(vertical = design.spacing.sm), horizontalArrangement = Arrangement.spacedBy(design.spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Text(account.name + if(account.id == room.selfId) "（我）" else "", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(account.balance.toString(), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium,
                    color = if(account.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            }
        }
        if(ledger.preset == "chips") Text("筹码与公共池均不可透支", style = MaterialTheme.typography.bodySmall)
        else Text("玩家可记负数；公共池不可透支。数额为桌游虚拟单位。", style = MaterialTheme.typography.bodySmall)
    }
    if (pending.isNotEmpty()) {
        AppCard {
            Text("有一笔操作尚未确认，请先获取结果。", style = MaterialTheme.typography.bodyMedium)
            AppPrimaryAction("确认上一笔结果", onClick = { scope.launch { Cloud.ledgerAction() } }, enabled = !busy)
        }
    }
    if(room.status == "PLAYING") {
        AppCard {
            AppSectionHeader(if(manage) "银行与收支" else "我的收支", subtitle = if(manage) "房主可代记所有账户；批量操作会一次完成。" else "可支付自己的余额；收款需要对方确认。")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { operation = "transfer" }, enabled = canWrite, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("支付 / 发放") }
                OutlinedButton(onClick = { operation = "request" }, enabled = canWrite, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("向玩家收款") }
            }
            if(manage) FlowRow(horizontalArrangement = Arrangement.spacedBy(design.spacing.md)) {
                listOf("grant" to "批量发放", "collect" to "收至公共池", "split" to "均分公共池").forEach { (id, name) ->
                    OutlinedButton(onClick = { operation = id }, enabled = canWrite) { Text(name) }
                }
            }
        }
        ledger.pending.forEach { request ->
            AppCard {
                Text("待确认 · ${ledger.accountName(request.from)} → ${ledger.accountName(request.to)}")
                Text("${request.amount} ${ledger.unit} · ${request.memo}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if(request.from == room.selfId) {
                        Button(onClick = { confirm = command("approve", request.id); confirmation = "向 ${ledger.accountName(request.to)} 支付 ${request.amount} ${ledger.unit}？" }, enabled = canWrite) { Text("确认付款") }
                        TextButton(onClick = { scope.launch { Cloud.ledgerAction(command("reject", request.id)) } }, enabled = canWrite) { Text("拒绝") }
                    } else TextButton(onClick = { scope.launch { Cloud.ledgerAction(command("cancel", request.id)) } }, enabled = canWrite) { Text("取消收款") }
                }
            }
        }
    }
    AppCard {
        AppSectionHeader("交易记录 · ${ledger.history.size}", action = { TextButton(onClick = { export = true }) { Text("导出账本") } })
        if(manage && ledger.undoId.isNotEmpty()) OutlinedButton(onClick = {
            confirm = command("undo", ledger.undoId)
            val entry = ledger.history.first { it.id == ledger.undoId }
            confirmation = "撤销「${entry.memo}」？\n" + entry.legs.joinToString("\n") { "${ledger.accountName(it.to)} → ${ledger.accountName(it.from)}：${it.amount} ${ledger.unit}" }
        }, enabled = canWrite) { Text("撤销上一笔") }
        if(ledger.history.isEmpty()) AppEmptyState("还没有交易记录", "完成第一笔收支后会显示在这里")
        ledger.history.asReversed().take(historyCount).forEach { entry ->
            HorizontalDivider()
            Text(entry.memo + if(entry.reversed) "（已撤销）" else "", fontWeight = FontWeight.Medium)
            entry.legs.forEach { leg -> Text("${ledger.accountName(leg.from)} → ${ledger.accountName(leg.to)}  ${leg.amount} ${ledger.unit}") }
            Text("记录人：${ledger.accountName(entry.actor)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if(ledger.history.size > historyCount) TextButton(onClick = { historyCount += 20 }) { Text("查看更多记录") }
    }
    if(operation.isNotEmpty()) LedgerCommandDialog(room, operation, canWrite, manage, onDismiss = { operation = "" }) { body ->
        scope.launch { if(Cloud.ledgerAction(body)) operation = "" }
    }
    confirm?.let { body ->
        AlertDialog(onDismissRequest = { confirm = null }, title = { Text("确认记账") }, text = { Text(confirmation) },
            confirmButton = { TextButton(onClick = { confirm = null; scope.launch { Cloud.ledgerAction(body) } }, enabled = canWrite) { Text("确认") } },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("取消") } })
    }
    if(export) AppDialog(title = "导出账本", subtitle = "CSV 格式，可选择文字后复制", onDismiss = { export = false }, modifier = Modifier.widthIn(max = 560.dp),
        actions = { AppDialogActions("完成", onConfirm = { export = false }) }) {
        SelectionContainer { Text(ledger.exportCsv(), style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun LedgerCommandDialog(room: CloudRoom, operation: String, canWrite: Boolean, manage: Boolean, onDismiss: () -> Unit, onSubmit: (JsonObject) -> Unit) {
    val ledger = room.ledger ?: return
    var revision by remember { mutableLongStateOf(ledger.revision) }
    var from by remember { mutableStateOf(if(operation == "request") ledger.accounts.firstOrNull { it.id != "pot" && it.id != room.selfId }?.id.orEmpty() else room.selfId) }
    var to by remember { mutableStateOf(if(operation == "request") room.selfId else "bank") }
    var amount by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var targets by remember { mutableStateOf(ledger.accounts.filter { it.id != "pot" }.map { it.id }) }
    val batch = operation in listOf("grant", "collect", "split")
    val parsed = amount.toLongOrNull() ?: 0
    val valid = parsed in 1..999_999_999_999L && (if(batch) targets.isNotEmpty() && (operation != "split" || parsed >= targets.size) else from.isNotEmpty() && from != to)
    val names = mapOf("transfer" to "支付 / 发放", "request" to "向玩家收款", "grant" to "批量发放", "collect" to "收至公共池", "split" to "均分公共池")
    AppDialog(title = names[operation].orEmpty(), onDismiss = onDismiss, modifier = Modifier.widthIn(max = 480.dp), actions = {
        AppDialogActions(
            confirmText = if(operation == "request") "发送收款请求" else "确认入账",
            confirmEnabled = valid && canWrite && revision == ledger.revision,
            onDismiss = onDismiss,
            onConfirm = { onSubmit(buildJsonObject {
                put("roundId", room.roundId); put("revision", revision); put("operation", operation); put("from", from); put("to", to)
                put("amount", parsed); put("memo", memo.trim()); put("targets", JsonArray(targets.map(::JsonPrimitive)))
            }) }
        )
    }) {
        if(batch) {
            Text("参与玩家（按座位顺序分配余数）")
            ledger.accounts.filter { it.id != "pot" }.forEach { account ->
                Row { Checkbox(account.id in targets, { checked -> targets = if(checked) targets + account.id else targets - account.id }); Text(account.name, Modifier.padding(top = 12.dp)) }
            }
        } else {
            LedgerAccountPicker("付款方", from, ledger.accounts.filter { if(operation == "request") it.id != room.selfId && it.id != "pot" else manage || it.id == room.selfId } +
                if(manage && operation != "request") listOf(LedgerAccount("bank", "银行")) else emptyList()) { from = it }
            LedgerAccountPicker("收款方", to, if(operation == "request") ledger.accounts.filter { it.id == room.selfId } else ledger.accounts + LedgerAccount("bank", "银行")) { to = it }
            if(operation == "transfer" && manage) TextButton(onClick = { val previous = from; from = to; to = previous }) { Text("交换收付双方") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LedgerScenes.quickAmounts(ledger.preset).forEach { value -> TextButton(onClick = { amount = value.toString() }, contentPadding = PaddingValues(4.dp), modifier = Modifier.weight(1f)) { Text(value.toString()) } }
        }
        OutlinedTextField(amount, { amount = it.filter { c -> c in '0'..'9' }.take(12) }, label = { Text(if(operation == "split") "均分总额" else if(batch) "每人金额" else "金额") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(memo, { memo = it.take(60) }, label = { Text("备注（可选）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if(valid) {
            Text("确认前预览", fontWeight = FontWeight.Bold)
            if(batch) {
                val ordered = ledger.accounts.filter { it.id in targets }
                ordered.forEachIndexed { index, account ->
                    val share = if(operation == "split") parsed / ordered.size + if(index < parsed % ordered.size) 1 else 0 else parsed
                    Text("${account.name} ${if(operation == "collect") "−" else "+"}$share ${ledger.unit}")
                }
                Text("合计 ${if(operation == "split") parsed else parsed * targets.size} ${ledger.unit}")
            } else Text("${ledger.accountName(from)} → ${ledger.accountName(to)}：$parsed ${ledger.unit}" + if(operation == "request") "\n对方确认后才会入账" else "")
        }
        if(revision != ledger.revision) {
            Text("账本已更新，请核对最新余额后继续。", color = MaterialTheme.colorScheme.error)
            Text(ledger.accounts.joinToString(" · ") { "${it.name} ${it.balance}" })
            TextButton(onClick = { revision = ledger.revision }) { Text("已核对最新余额") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerAccountPicker(label: String, selected: String, accounts: List<LedgerAccount>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(accounts.firstOrNull { it.id == selected }?.name.orEmpty(), {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
        ExposedDropdownMenu(expanded, { expanded = false }) { accounts.forEach { account ->
            DropdownMenuItem(text = { Text("${account.name}${if(account.id == "bank") "" else " · ${account.balance}"}") }, onClick = { onSelect(account.id); expanded = false })
        } }
    }
}
