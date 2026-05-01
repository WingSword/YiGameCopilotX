package org.walks.gamecopilot.lan.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.walks.gamecopilot.lan.data.ConnectionStatus
import org.walks.gamecopilot.lan.data.LANConnectionState
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANPlayer

private class WasmLANClient : LANClient {
    private val state = MutableStateFlow(LANConnectionState(ConnectionStatus.DISCONNECTED))
    private val messages = MutableSharedFlow<LANMessage>()

    override val connectionState: Flow<LANConnectionState> = state.asStateFlow()
    override val receivedMessages: Flow<LANMessage> = messages.asSharedFlow()
    override val isConnected: Boolean = false
    override val currentPlayer: LANPlayer? = null

    override suspend fun connect(hostAddress: String, port: Int, playerName: String): Boolean = false
    override suspend fun disconnect() = Unit
    override suspend fun sendMessage(message: LANMessage) = Unit
    override suspend fun joinRoom(roomId: String, password: String?): Boolean = false
    override suspend fun leaveRoom() = Unit
    override fun dispose() = Unit
}

actual fun createLANClient(): LANClient = WasmLANClient()
