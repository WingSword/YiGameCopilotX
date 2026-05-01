package org.walks.gamecopilot.ui.page.monopoly

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.data.entity.MonopolyPlayer
import org.walks.gamecopilot.data.entity.MonopolyTransaction
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.lan.lanRoomManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonopolyMoneyPage(
    viewModel: MainViewmodel = viewModel()
) {
    val isHost by lanRoomManager.isHost.collectAsState()
    val players by lanRoomManager.players.collectAsState()
    
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<MonopolyPlayer?>(null) }
    
    var monopolyPlayers by remember { mutableStateOf(listOf<MonopolyPlayer>()) }
    var transactions by remember { mutableStateOf(listOf<MonopolyTransaction>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "大富翁银钱管理",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isHost) {
                    IconButton(onClick = { showAddPlayerDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "添加玩家")
                    }
                }
                IconButton(onClick = { showTransactionDialog = true }) {
                    Icon(Icons.Default.Payment, contentDescription = "交易")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
        
        if (monopolyPlayers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无玩家",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isHost) {
                        TextButton(onClick = { showAddPlayerDialog = true }) {
                            Text("添加玩家")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    id = "player_${System.currentTimeMillis()}",
                    name = name
                )
                monopolyPlayers = monopolyPlayers + newPlayer
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(monopolyPlayers)
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
                
                monopolyPlayers = monopolyPlayers.map { player ->
                    when (player.id) {
                        fromId -> player.copy(balance = player.balance - amount)
                        toId -> player.copy(balance = player.balance + amount)
                        else -> player
                    }
                }
                
                transactions = transactions + transaction
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(monopolyPlayers)
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
                monopolyPlayers = monopolyPlayers.map {
                    if (it.id == updatedPlayer.id) updatedPlayer else it
                }
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(monopolyPlayers)
                    )
                }
                
                selectedPlayer = null
            },
            onDelete = {
                monopolyPlayers = monopolyPlayers.filter { it.id != player.id }
                
                if (isHost) {
                    viewModel.handleLANIntent(
                        LANIntent.SyncGameState(monopolyPlayers)
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加玩家") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("玩家名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name) },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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
    
    val isValid = amount.toLongOrNull()?.let { it > 0 } == true && 
        (fromPlayerId != null || toPlayerId != null)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建交易") },
        text = {
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        fromPlayerId,
                        toPlayerId,
                        amount.toLong(),
                        description.ifEmpty { "转账" }
                    )
                },
                enabled = isValid
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑玩家") },
        text = {
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
                    onValueChange = { balance = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("余额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
                Button(
                    onClick = {
                        onSave(player.copy(
                            name = name,
                            balance = balance.toLongOrNull() ?: player.balance
                        ))
                    }
                ) {
                    Text("保存")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatMoney(amount: Long): String {
    return when {
        amount >= 100000000 -> String.format("%.2f亿", amount / 100000000.0)
        amount >= 10000 -> String.format("%.2f万", amount / 10000.0)
        else -> amount.toString()
    }
}
