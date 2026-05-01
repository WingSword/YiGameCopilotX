package org.walks.gamecopilot.lan.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANPlayer

private class WasmLANHostServer : LANHostServer {
    private val players = MutableStateFlow<List<LANPlayer>>(emptyList())
    private val messages = MutableSharedFlow<Pair<String, LANMessage>>()

    override val isRunning: Boolean = false
    override val port: Int = DEFAULT_SERVER_PORT
    override val connectedPlayers: Flow<List<LANPlayer>> = players.asStateFlow()
    override val receivedMessages: Flow<Pair<String, LANMessage>> = messages.asSharedFlow()

    override suspend fun start(port: Int): Boolean = false
    override suspend fun stop() = Unit
    override suspend fun broadcast(message: LANMessage) = Unit
    override suspend fun sendToPlayer(playerId: String, message: LANMessage) = Unit
    override suspend fun kickPlayer(playerId: String, reason: String) = Unit
    override fun getLocalIpAddress(): String = "127.0.0.1"
    override fun dispose() = Unit
}

actual fun createLANHostServer(): LANHostServer = WasmLANHostServer()
