package org.walks.gamecopilot.lan.discovery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANRoomInfo

private class WasmServiceDiscovery : ServiceDiscovery {
    private val rooms = MutableSharedFlow<LANRoomInfo>()
    private val messages = MutableSharedFlow<Pair<String, LANMessage>>()

    override val discoveredRooms: Flow<LANRoomInfo> = rooms.asSharedFlow()
    override val receivedMessages: Flow<Pair<String, LANMessage>> = messages.asSharedFlow()
    override val isDiscovering: Boolean = false

    override suspend fun startDiscovery(gameTypeFilter: String?) = Unit
    override suspend fun stopDiscovery() = Unit
    override suspend fun broadcastPresence(roomInfo: LANRoomInfo) = Unit
    override suspend fun stopBroadcasting() = Unit
    override suspend fun sendDiscoveryRequest() = Unit
    override suspend fun sendDiscoveryResponse(roomInfo: LANRoomInfo, targetAddress: String) = Unit
    override suspend fun sendMessage(message: LANMessage, targetAddress: String, port: Int) = Unit
    override suspend fun broadcastMessage(message: LANMessage) = Unit
    override fun dispose() = Unit
}

actual fun createServiceDiscovery(port: Int): ServiceDiscovery = WasmServiceDiscovery()
