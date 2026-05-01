package org.walks.gamecopilot.lan.discovery

import kotlinx.coroutines.flow.Flow
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANRoomInfo

interface ServiceDiscovery {
    val discoveredRooms: Flow<LANRoomInfo>
    val receivedMessages: Flow<Pair<String, LANMessage>>
    val isDiscovering: Boolean
    
    suspend fun startDiscovery(gameTypeFilter: String? = null)
    suspend fun stopDiscovery()
    suspend fun broadcastPresence(roomInfo: LANRoomInfo)
    suspend fun stopBroadcasting()
    suspend fun sendDiscoveryRequest()
    suspend fun sendDiscoveryResponse(roomInfo: LANRoomInfo, targetAddress: String)
    suspend fun sendMessage(message: LANMessage, targetAddress: String, port: Int)
    suspend fun broadcastMessage(message: LANMessage)
    fun dispose()
}

expect fun createServiceDiscovery(port: Int = DEFAULT_DISCOVERY_PORT): ServiceDiscovery

const val DEFAULT_DISCOVERY_PORT = 37666
const val DISCOVERY_BROADCAST_INTERVAL = 3000L
const val DISCOVERY_TIMEOUT = 10000L
