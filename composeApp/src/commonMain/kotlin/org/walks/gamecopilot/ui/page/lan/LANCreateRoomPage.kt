package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LANCreateRoomPage(
    onRoomCreated: () -> Unit,
    onCancel: () -> Unit,
    viewModel: MainViewmodel = viewModel()
) {
    val lanState by viewModel.lanState.collectAsState()
    var roomName by remember { mutableStateOf("") }
    var hostName by remember { mutableStateOf("") }
    var selectedGameType by remember(lanState.preferredGameType) {
        mutableStateOf(
            if (lanState.preferredGameType == GameType.ALL) GameType.LOCAL_SPY
            else lanState.preferredGameType
        )
    }
    var maxPlayers by remember { mutableStateOf("8") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showGameTypeDialog by remember { mutableStateOf(false) }
    
    val isFormValid = roomName.isNotBlank() && hostName.isNotBlank() && maxPlayers.toIntOrNull()?.let { it in 2..20 } == true
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "创建房间",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("房间名称") },
            placeholder = { Text("输入房间名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.MeetingRoom, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = hostName,
            onValueChange = { hostName = it },
            label = { Text("你的昵称") },
            placeholder = { Text("输入你的昵称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = selectedGameType.displayName,
            onValueChange = {},
            label = { Text("游戏类型") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.VideogameAsset, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { showGameTypeDialog = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "选择游戏类型")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = maxPlayers,
            onValueChange = { 
                if (it.isEmpty() || it.toIntOrNull() != null) {
                    maxPlayers = it
                }
            },
            label = { Text("最大玩家数") },
            placeholder = { Text("2-20") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Group, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("房间密码（可选）") },
            placeholder = { Text("留空表示无密码") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("取消")
            }
            
            Button(
                onClick = {
                    viewModel.handleLANIntent(LANIntent.SetPreferredGameType(selectedGameType))
                    viewModel.handleLANIntent(
                        LANIntent.CreateRoom(
                            roomName = roomName,
                            hostName = hostName,
                            gameType = selectedGameType,
                            maxPlayers = maxPlayers.toInt(),
                            password = password
                        )
                    )
                    onRoomCreated()
                },
                modifier = Modifier.weight(1f),
                enabled = isFormValid
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建")
            }
        }
    }
    
    if (showGameTypeDialog) {
        GameTypeSelectionDialog(
            currentType = selectedGameType,
            onDismiss = { showGameTypeDialog = false },
            onConfirm = { type ->
                selectedGameType = type
                showGameTypeDialog = false
            }
        )
    }
}

@Composable
private fun GameTypeSelectionDialog(
    currentType: GameType,
    onDismiss: () -> Unit,
    onConfirm: (GameType) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentType) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择游戏类型") },
        text = {
            Column {
                GameType.values().filter { it != GameType.ALL }.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedType) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
