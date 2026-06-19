package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.GameType
import org.walks.gamecopilot.ui.components.AppDialog
import org.walks.gamecopilot.ui.components.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LANCreateRoomPage(
    onRoomCreated: () -> Unit,
    onCancel: () -> Unit,
    viewModel: MainViewmodel
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
    var showPassword by remember { mutableStateOf(false) }
    var showGameTypeDialog by remember { mutableStateOf(false) }
    var pendingCreate by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    
    val isFormValid = roomName.isNotBlank() && hostName.isNotBlank() && maxPlayers.toIntOrNull()?.let { it in 2..20 } == true
    val canSubmit = isFormValid && !pendingCreate

    LaunchedEffect(
        lanState.currentRoom,
        lanState.isHost,
        lanState.connectionState.status,
        pendingCreate
    ) {
        if (pendingCreate &&
            lanState.isHost &&
            lanState.currentRoom != null &&
            lanState.connectionState.status == ConnectionStatus.CONNECTED
        ) {
            pendingCreate = false
            onRoomCreated()
        }
    }

    LaunchedEffect(lanState.error, lanState.connectionState.status, pendingCreate) {
        if (pendingCreate && lanState.error != null) {
            formError = lanState.error
            pendingCreate = false
        } else if (pendingCreate && lanState.connectionState.status == ConnectionStatus.ERROR) {
            formError =
                lanState.connectionState.message.ifBlank { "创建房间失败，请检查网络或端口占用" }
            pendingCreate = false
        }
    }

    AppScreen(
        title = "创建房间",
        subtitle = "设置游戏类型、玩家人数和房间密码后即可组局。"
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = roomName,
                onValueChange = {
                    roomName = it
                    formError = null
                },
                label = { Text("房间名称") },
                placeholder = { Text("输入房间名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = hostName,
                onValueChange = {
                    hostName = it
                    formError = null
                },
                label = { Text("你的昵称") },
                placeholder = { Text("输入你的昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            )

            OutlinedTextField(
                value = selectedGameType.displayName,
                onValueChange = {},
                label = { Text("游戏类型") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGameTypeDialog = true },
                leadingIcon = {
                    Icon(Icons.Default.VideogameAsset, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showGameTypeDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "选择游戏类型")
                    }
                }
            )

            OutlinedTextField(
                value = maxPlayers,
                onValueChange = {
                    if (it.isEmpty() || it.toIntOrNull() != null) {
                        maxPlayers = it
                        formError = null
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

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    formError = null
                },
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

            formError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

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
                    formError = null
                    pendingCreate = true
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
                },
                modifier = Modifier.weight(1f),
                enabled = canSubmit
            ) {
                if (pendingCreate) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("创建")
                }
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

    AppDialog(
        title = "选择游戏类型",
        subtitle = "创建后该房间会按所选游戏同步准备流程。",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { onConfirm(selectedType) },
                modifier = Modifier.weight(1f)
            ) {
                Text("确定")
            }
        }
    ) {
            Column {
                GameType.values().filter { it != GameType.ALL }.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
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
    }
}
