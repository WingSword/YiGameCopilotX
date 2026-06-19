package org.walks.gamecopilot.ui.page.monopoly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.currentTimeMillis
import org.walks.gamecopilot.data.entity.MonopolyPlayer
import org.walks.gamecopilot.data.entity.MonopolyTransaction
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.lanRoomManager
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppEmptyState
import org.walks.gamecopilot.ui.components.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonopolyMoneyPage(
    viewModel: MainViewmodel
) {
    val isHost by lanRoomManager.isHost.collectAsState()
    
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<MonopolyPlayer?>(null) }
    
    var monopolyPlayers by remember { mutableStateOf(listOf<MonopolyPlayer>()) }
    var transactions by remember { mutableStateOf(listOf<MonopolyTransaction>()) }

    AppScreen(
        title = "大富翁银钱管理",
        subtitle = "记录玩家余额和银行交易",
        actions = {
            if (isHost) {
                IconButton(onClick = { showAddPlayerDialog = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "添加玩家")
                }
            }
            IconButton(
                onClick = { showTransactionDialog = true },
                enabled = monopolyPlayers.isNotEmpty()
            ) {
                Icon(Icons.Default.Payment, contentDescription = "交易")
            }
        }
    ) {
        if (monopolyPlayers.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppEmptyState(
                    title = "暂无玩家",
                    description = if (isHost) "先添加玩家，再开始记账。" else "等待房主添加玩家。",
                    icon = Icons.Default.AttachMoney
                )
                if (isHost) {
                    TextButton(onClick = { showAddPlayerDialog = true }) {
                        Text("添加玩家")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(monopolyPlayers, key = { it.id }) { player ->
                    PlayerBalanceCard(
                        player = player,
                        onEdit = if (isHost) {
                            { selectedPlayer = player }
                        } else null
                    )
                }
            }
        }

        AppCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "最近交易",
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (transactions.isEmpty()) {
                    Text(
                        text = "暂无交易记录",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    transactions.take(5).forEach { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }
    
    if (showAddPlayerDialog) {
        AddPlayerDialog(
            onDismiss = { showAddPlayerDialog = false },
            onAdd = { name ->
                val newPlayer = MonopolyPlayer(
                    id = "player_${currentTimeMillis()}",
                    name = name
                )
                val updatedPlayers = monopolyPlayers + newPlayer
                monopolyPlayers = updatedPlayers
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(updatedPlayers)
                    )
                }
                
                showAddPlayerDialog = false
            }
        )
    }
    
    if (showTransactionDialog) {
        TransactionDialog(
            players = monopolyPlayers,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { fromId, toId, amount, description ->
                val transaction = MonopolyTransaction(
                    fromPlayerId = fromId,
                    toPlayerId = toId,
                    amount = amount,
                    description = description
                )

                val updatedPlayers = monopolyPlayers.map { player ->
                    when (player.id) {
                        fromId -> player.copy(balance = player.balance - amount)
                        toId -> player.copy(balance = player.balance + amount)
                        else -> player
                    }
                }
                monopolyPlayers = updatedPlayers
                
                transactions = transactions + transaction
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(updatedPlayers)
                    )
                }
                
                showTransactionDialog = false
            }
        )
    }
    
    selectedPlayer?.let { player ->
        EditPlayerDialog(
            player = player,
            onDismiss = { selectedPlayer = null },
            onSave = { updatedPlayer ->
                val updatedPlayers = monopolyPlayers.map {
                    if (it.id == updatedPlayer.id) updatedPlayer else it
                }
                monopolyPlayers = updatedPlayers
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(updatedPlayers)
                    )
                }
                
                selectedPlayer = null
            },
            onDelete = {
                val updatedPlayers = monopolyPlayers.filter { it.id != player.id }
                monopolyPlayers = updatedPlayers
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(updatedPlayers)
                    )
                }
                
                selectedPlayer = null
            }
        )
    }
}

@Composable
private fun PlayerBalanceCard(
    player: MonopolyPlayer,
    onEdit: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = player.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                if (player.isBankrupt) {
                    Text(
                        text = "已破产",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¥${formatMoney(player.balance)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (player.balance < 0) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                
                if (onEdit != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: MonopolyTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = transaction.description,
            fontSize = 14.sp
        )
        Text(
            text = "¥${formatMoney(transaction.amount)}",
            fontSize = 14.sp,
            color = if (transaction.fromPlayerId == null) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AppDialog(
        title = "添加玩家",
        subtitle = "新玩家会使用默认起始资金",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            Button(
                onClick = { onAdd(name) },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("玩家名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDialog(
    players: List<MonopolyPlayer>,
    onDismiss: () -> Unit,
    onConfirm: (fromId: String?, toId: String?, amount: Long, description: String) -> Unit
) {
    var fromPlayerId by remember { mutableStateOf<String?>(null) }
    var toPlayerId by remember { mutableStateOf<String?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    val parsedAmount = amount.toLongOrNull()
    val isValid = parsedAmount?.let { it > 0 } == true &&
            (fromPlayerId != null || toPlayerId != null) &&
            fromPlayerId != toPlayerId

    AppDialog(
        title = "新建交易",
        subtitle = "银行可作为付款方或收款方",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
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
                        parsedAmount ?: 0L,
                        description.ifEmpty { defaultDescription }
                    )
                },
                enabled = isValid
            ) {
                Text("确认")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedFrom,
                    onExpandedChange = { expandedFrom = it }
                ) {
                    OutlinedTextField(
                        value = players.find { it.id == fromPlayerId }?.name ?: "银行",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("付款方") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedFrom,
                        onDismissRequest = { expandedFrom = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("银行") },
                            onClick = {
                                fromPlayerId = null
                                expandedFrom = false
                            }
                        )
                        players.forEach { player ->
                            DropdownMenuItem(
                                text = { Text(player.name) },
                                onClick = {
                                    fromPlayerId = player.id
                                    expandedFrom = false
                                }
                            )
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedTo,
                    onExpandedChange = { expandedTo = it }
                ) {
                    OutlinedTextField(
                        value = players.find { it.id == toPlayerId }?.name ?: "银行",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("收款方") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedTo,
                        onDismissRequest = { expandedTo = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("银行") },
                            onClick = {
                                toPlayerId = null
                                expandedTo = false
                            }
                        )
                        players.forEach { player ->
                            DropdownMenuItem(
                                text = { Text(player.name) },
                                onClick = {
                                    toPlayerId = player.id
                                    expandedTo = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("备注（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
    }
}

@Composable
private fun EditPlayerDialog(
    player: MonopolyPlayer,
    onDismiss: () -> Unit,
    onSave: (MonopolyPlayer) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(player.name) }
    var balance by remember { mutableStateOf(player.balance.toString()) }

    AppDialog(
        title = "编辑玩家",
        subtitle = "可以直接调整余额或删除玩家",
        onDismiss = onDismiss,
        actions = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            Button(
                onClick = {
                    onSave(
                        player.copy(
                            name = name,
                            balance = balance.toLongOrNull() ?: player.balance
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("玩家名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = balance,
                onValueChange = { input ->
                    balance = input.filterIndexed { index, c ->
                        c.isDigit() || (c == '-' && index == 0)
                    }
                },
                label = { Text("余额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatMoney(amount: Long): String {
    return when {
        amount >= 100000000 -> {
            val value = amount / 100000000.0
            val formatted = (value * 100).toLong().toDouble() / 100.0
            "$formatted" + "亿"
        }

        amount >= 10000 -> {
            val value = amount / 10000.0
            val formatted = (value * 100).toLong().toDouble() / 100.0
            "$formatted" + "万"
        }
        else -> amount.toString()
    }
}
