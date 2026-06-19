package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.LANRoomInfo
import org.walks.gamecopilot.ui.components.AppCard
import org.walks.gamecopilot.ui.components.AppDialog

@Composable
fun JoinRoomDialog(
    roomInfo: LANRoomInfo,
    onDismiss: () -> Unit,
    onJoinSuccess: () -> Unit,
    viewModel: MainViewmodel
) {
    var playerName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingJoin by remember { mutableStateOf(false) }
    val lanState by viewModel.lanState.collectAsState()
    
    val isFormValid = playerName.isNotBlank() && (!roomInfo.hasPassword || password.isNotBlank())
    val canSubmit = isFormValid && !pendingJoin

    LaunchedEffect(lanState.currentRoom, lanState.isHost, pendingJoin) {
        if (pendingJoin &&
            !lanState.isHost &&
            lanState.currentRoom?.roomInfo?.roomId == roomInfo.roomId
        ) {
            pendingJoin = false
            onJoinSuccess()
        }
    }

    LaunchedEffect(lanState.error, lanState.connectionState.status, pendingJoin) {
        if (pendingJoin && lanState.error != null) {
            errorMessage = lanState.error
            pendingJoin = false
        } else if (pendingJoin && lanState.connectionState.status == ConnectionStatus.ERROR) {
            errorMessage = lanState.connectionState.message.ifBlank { "加入房间失败，请稍后重试" }
            pendingJoin = false
        }
    }

    AppDialog(
        title = "加入房间",
        subtitle = "确认房间信息后输入你的玩家昵称。",
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
                onClick = {
                    if (!isFormValid) {
                        errorMessage = "请填写所有必填项"
                        return@Button
                    }

                    errorMessage = null
                    pendingJoin = true
                    viewModel.handleLANIntent(
                        LANIntent.JoinRoom(
                            roomInfo = roomInfo,
                            playerName = playerName,
                            password = password
                        )
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.weight(1f)
            ) {
                if (pendingJoin) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("加入")
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = roomInfo.roomName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "房主: ${roomInfo.hostName} · ${roomInfo.gameType.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                text = "玩家信息",
                style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            OutlinedTextField(
                    value = playerName,
                    onValueChange = { 
                        playerName = it
                        errorMessage = null
                    },
                    label = { Text("你的昵称") },
                    placeholder = { Text("输入昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = errorMessage != null && playerName.isBlank()
                )
                
                if (roomInfo.hasPassword) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMessage = null
                        },
                        label = { Text("房间密码") },
                        placeholder = { Text("输入密码") },
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
                                    contentDescription = if (showPassword) "隐藏" else "显示"
                                )
                            }
                        },
                        isError = errorMessage != null && password.isBlank()
                    )
                }
                
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
    }
}
