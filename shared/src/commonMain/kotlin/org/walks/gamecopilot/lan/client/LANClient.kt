package org.walks.gamecopilot.lan.client

import kotlinx.coroutines.flow.Flow
import org.walks.gamecopilot.lan.data.LANConnectionState
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANPlayer

interface LANClient {
    val connectionState: Flow<LANConnectionState>
    val receivedMessages: Flow<LANMessage>
    val isConnected: Boolean
    val currentPlayer: LANPlayer?
    
    suspend fun connect(hostAddress: String, port: Int, playerName: String): Boolean
    suspend fun disconnect()
    suspend fun sendMessage(message: LANMessage)
    suspend fun joinRoom(roomId: String, password: String?): Boolean
    suspend fun leaveRoom()
    fun dispose()
}

expect fun createLANClient(): LANClient
