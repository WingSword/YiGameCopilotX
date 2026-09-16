package org.walks.gamecopilot.ui.page.monopoly

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.online.LedgerScenes
import org.walks.gamecopilot.online.CloudLedger
import org.walks.gamecopilot.online.LedgerAccount
import org.walks.gamecopilot.online.LedgerEntry
import org.walks.gamecopilot.online.LedgerLeg
import org.walks.gamecopilot.data.entity.MonopolyGameState
import org.walks.gamecopilot.ui.components.AppDialog

internal fun MonopolyGameState.toExportLedger(unit: String) = CloudLedger(preset = preset, initialBalance = initialBalance, unit = unit,
    accounts = players.map { LedgerAccount(it.id, it.name, it.balance) },
    history = transactions.asReversed().mapIndexed { index, entry -> LedgerEntry(id = "${entry.timestamp}:$index", actor = "local", memo = entry.description,
        timestamp = entry.timestamp, legs = listOf(LedgerLeg(entry.fromPlayerId ?: "bank", entry.toPlayerId ?: "bank", entry.amount,
            entry.fromPlayerName.orEmpty(), entry.toPlayerName.orEmpty()))) })

@Composable
internal fun LedgerSceneDialog(currentPreset: String, currentInitial: Long, onDismiss: () -> Unit, onApply: (String, Long) -> Unit) {
    var preset by remember { mutableStateOf(currentPreset) }
    var amount by remember { mutableStateOf(currentInitial.toString()) }
    val parsed = amount.toLongOrNull()
    AppDialog(title = "场景与起始余额", onDismiss = onDismiss, actions = {
        TextButton(onClick = onDismiss) { Text("取消") }
        Button(onClick = { onApply(preset, parsed ?: 0) }, enabled = parsed != null && parsed in 0..999_999_999_999L) { Text("应用并重置本局") }
    }) {
        LedgerScenes.ids.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { id -> FilterChip(preset == id, { preset = id; amount = LedgerScenes.initial[LedgerScenes.ids.indexOf(id)].toString() }, label = { Text(LedgerScenes.name(id)) }) }
        } }
        OutlinedTextField(amount, { amount = it.filter { c -> c in '0'..'9' }.take(12) }, label = { Text("每人起始余额") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Text("保留玩家和座位颜色，重置所有余额并清除交易记录。请先导出需要保留的账本。")
    }
}

@Composable
internal fun LedgerBatchDialog(count: Int, preset: String, onDismiss: () -> Unit, onApply: (Boolean, Long) -> Unit) {
    var receive by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    val parsed = amount.toLongOrNull()
    AppDialog(title = "全员收付", onDismiss = onDismiss, actions = {
        TextButton(onClick = onDismiss) { Text("取消") }
        Button(onClick = { onApply(receive, parsed ?: 0) }, enabled = count > 0 && parsed != null && parsed in 1..999_999_999_999L) { Text("确认入账") }
    }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(receive, { receive = true }, label = { Text("银行发给每人") })
            FilterChip(!receive, { receive = false }, label = { Text("每人交给银行") })
        }
        Row { LedgerScenes.quickAmounts(preset).forEach { value -> TextButton(onClick = { amount = value.toString() }) { Text(value.toString()) } } }
        OutlinedTextField(amount, { amount = it.filter { c -> c in '0'..'9' }.take(12) }, label = { Text("每人金额") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        if(parsed != null) Text("$count 位玩家，每人 ${if(receive) "+" else "−"}$parsed，合计 ${parsed * count}。撤销时会整批撤销。")
    }
}
