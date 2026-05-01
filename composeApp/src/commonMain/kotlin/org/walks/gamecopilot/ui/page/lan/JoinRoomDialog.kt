package org.walks.gamecopilot.ui.page.lan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.walks.gamecopilot.MainViewmodel
import org.walks.gamecopilot.intent.LANIntent
import org.walks.gamecopilot.lan.data.LANRoomInfo

@Composable
fun JoinRoomDialog(
    roomInfo: LANRoomInfo,
    onDismiss: () -> Unit,
    onJoinSuccess: () -> Unit,
    viewModel: MainViewmodel = viewModel()
) {
    var playerName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val isFormValid = playerName.isNotBlank() && (!roomInfo.hasPassword || password.isNotBlank())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.MeetingRoom,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { 
            Text(text = "加入房间") 
        },
        text = {
            Column {
                Text(
                    text = roomInfo.roomName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "房主: ${roomInfo.hostName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "游戏: ${roomInfo.gameType.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isFormValid) {
                        errorMessage = "请填写所有必填项"
                        return@Button
                    }
                    
                    viewModel.handleLANIntent(
                        LANIntent.JoinRoom(
                            roomInfo = roomInfo,
                            playerName = playerName,
                            password = password
                        )
                    )
                    onJoinSuccess()
                },
                enabled = isFormValid
            ) {
                Text("加入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
