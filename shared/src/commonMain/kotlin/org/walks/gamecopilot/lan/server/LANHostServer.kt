package org.walks.gamecopilot.lan.server

import kotlinx.coroutines.flow.Flow
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANPlayer

interface LANHostServer {
    val isRunning: Boolean
    val port: Int
    val connectedPlayers: Flow<List<LANPlayer>>
    val receivedMessages: Flow<Pair<String, LANMessage>>
    
    suspend fun start(port: Int = 0): Boolean
    suspend fun stop()
    suspend fun broadcast(message: LANMessage)
    suspend fun sendToPlayer(playerId: String, message: LANMessage)
    suspend fun kickPlayer(playerId: String, reason: String = "")
    fun getLocalIpAddress(): String
    fun dispose()
}

expect fun createLANHostServer(): LANHostServer

const val DEFAULT_SERVER_PORT = 37667
const val HEARTBEAT_INTERVAL = 5000L
const val CONNECTION_TIMEOUT = 15000L
