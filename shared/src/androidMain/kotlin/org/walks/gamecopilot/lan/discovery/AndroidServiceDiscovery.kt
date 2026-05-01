package org.walks.gamecopilot.lan.discovery

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.GameLogger
import org.walks.gamecopilot.lan.data.LANMessage
import org.walks.gamecopilot.lan.data.LANMessageType
import org.walks.gamecopilot.lan.data.LANRoomInfo
import org.walks.gamecopilot.lan.data.LANConstants.DISCOVERY_BROADCAST_INTERVAL
import org.walks.gamecopilot.lan.data.LANConstants.DEFAULT_DISCOVERY_PORT
import java.net.*
import java.nio.charset.StandardCharsets

class AndroidServiceDiscovery(
    private val port: Int = DEFAULT_DISCOVERY_PORT
) : ServiceDiscovery {
    
    private val json = Json { ignoreUnknownKeys = true }
    private var discoverySocket: DatagramSocket? = null
    private var broadcastJob: Job? = null
    private var receiveJob: Job? = null
    private var discoveryJob: Job? = null
    
    private val _discoveredRooms = MutableSharedFlow<LANRoomInfo>(replay = 0)
    override val discoveredRooms: Flow<LANRoomInfo> = _discoveredRooms.asSharedFlow()
    
    private val _receivedMessages = MutableSharedFlow<Pair<String, LANMessage>>(replay = 0)
    override val receivedMessages: Flow<Pair<String, LANMessage>> = _receivedMessages.asSharedFlow()
    
    @Volatile
    private var _isDiscovering = false
    override val isDiscovering: Boolean get() = _isDiscovering
    
    private var currentRoomInfo: LANRoomInfo? = null
    private val discoveredRoomMap = mutableMapOf<String, LANRoomInfo>()
    
    override suspend fun startDiscovery(gameTypeFilter: String?) {
        if (_isDiscovering) return
        
        _isDiscovering = true
        discoveredRoomMap.clear()
        
        try {
            discoverySocket = DatagramSocket(port).apply {
                broadcast = true
                soTimeout = 1000
            }
            
            receiveJob = CoroutineScope(Dispatchers.IO).launch {
                startReceiving()
            }
            
            discoveryJob = CoroutineScope(Dispatchers.IO).launch {
                while (_isDiscovering) {
                    sendDiscoveryRequest()
                    delay(DISCOVERY_BROADCAST_INTERVAL)
                }
            }
            
            GameLogger.info("开始发现服务，端口: $port")
        } catch (e: Exception) {
            GameLogger.error("启动发现服务失败", e)
            _isDiscovering = false
        }
    }
    
    override suspend fun stopDiscovery() {
        _isDiscovering = false
        discoveryJob?.cancel()
        receiveJob?.cancel()
        
        try {
            discoverySocket?.close()
        } catch (e: Exception) {
            GameLogger.error("关闭发现服务失败", e)
        }
        discoverySocket = null
        GameLogger.info("停止发现服务")
    }
    
    override suspend fun broadcastPresence(roomInfo: LANRoomInfo) {
        currentRoomInfo = roomInfo
        
        if (broadcastJob?.isActive == true) return
        
        broadcastJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val message = LANMessage(
                        type = LANMessageType.DISCOVERY_BROADCAST,
                        roomId = roomInfo.roomId,
                        payload = json.encodeToString(roomInfo)
                    )
                    broadcastMessage(message)
                } catch (e: Exception) {
                    GameLogger.error("广播房间信息失败", e)
                }
                delay(DISCOVERY_BROADCAST_INTERVAL)
            }
        }
        
        GameLogger.info("开始广播房间: ${roomInfo.roomName}")
    }
    
    override suspend fun stopBroadcasting() {
        broadcastJob?.cancel()
        broadcastJob = null
        currentRoomInfo = null
        GameLogger.info("停止广播房间")
    }
    
    override suspend fun sendDiscoveryRequest() {
        try {
            val requestMessage = LANMessage(
                type = LANMessageType.DISCOVERY_BROADCAST,
                payload = "DISCOVERY_REQUEST"
            )
            val data = json.encodeToString(requestMessage).toByteArray(StandardCharsets.UTF_8)
            
            val broadcastAddress = getBroadcastAddress()
            val packet = DatagramPacket(data, data.size, broadcastAddress, port)
            
            discoverySocket?.send(packet)
            GameLogger.debug("发送发现请求到: ${broadcastAddress.hostAddress}:$port")
        } catch (e: Exception) {
            GameLogger.error("发送发现请求失败", e)
        }
    }
    
    override suspend fun sendDiscoveryResponse(roomInfo: LANRoomInfo, targetAddress: String) {
        try {
            val responseMessage = LANMessage(
                type = LANMessageType.DISCOVERY_RESPONSE,
                roomId = roomInfo.roomId,
                payload = json.encodeToString(roomInfo)
            )
            sendMessage(responseMessage, targetAddress, port)
        } catch (e: Exception) {
            GameLogger.error("发送发现响应失败", e)
        }
    }
    
    override suspend fun sendMessage(message: LANMessage, targetAddress: String, port: Int) {
        try {
            val socket = discoverySocket ?: DatagramSocket().apply { discoverySocket = this }
            val data = json.encodeToString(message).toByteArray(StandardCharsets.UTF_8)
            val address = InetAddress.getByName(targetAddress)
            val packet = DatagramPacket(data, data.size, address, port)
            socket.send(packet)
        } catch (e: Exception) {
            GameLogger.error("发送消息失败", e)
        }
    }
    
    override suspend fun broadcastMessage(message: LANMessage) {
        try {
            val socket = discoverySocket ?: DatagramSocket().apply { discoverySocket = this }
            val data = json.encodeToString(message).toByteArray(StandardCharsets.UTF_8)
            val broadcastAddress = getBroadcastAddress()
            val packet = DatagramPacket(data, data.size, broadcastAddress, port)
            socket.send(packet)
            GameLogger.debug("广播消息: ${message.type}")
        } catch (e: Exception) {
            GameLogger.error("广播消息失败", e)
        }
    }
    
    private fun startReceiving() {
        val buffer = ByteArray(4096)
        val packet = DatagramPacket(buffer, buffer.size)
        val scope = CoroutineScope(Dispatchers.IO)
        
        while (_isDiscovering) {
            try {
                discoverySocket?.receive(packet)
                val data = String(packet.data, packet.offset, packet.length, StandardCharsets.UTF_8)
                val message = json.decodeFromString<LANMessage>(data)
                val senderAddress = packet.address.hostAddress ?: ""
                
                scope.launch {
                    handleMessage(message, senderAddress)
                }
            } catch (e: SocketTimeoutException) {
                // 超时正常，继续循环
            } catch (e: Exception) {
                if (_isDiscovering) {
                    GameLogger.error("接收消息失败", e)
                }
            }
        }
    }
    
    private suspend fun handleMessage(message: LANMessage, senderAddress: String) {
        when (message.type) {
            LANMessageType.DISCOVERY_BROADCAST -> {
                if (message.payload == "DISCOVERY_REQUEST") {
                    currentRoomInfo?.let { info ->
                        sendDiscoveryResponse(info, senderAddress)
                    }
                } else {
                    try {
                        val roomInfo = json.decodeFromString<LANRoomInfo>(message.payload)
                        if (roomInfo.roomId != currentRoomInfo?.roomId) {
                            val existingRoom = discoveredRoomMap[roomInfo.roomId]
                            if (existingRoom == null || existingRoom.createdAt < roomInfo.createdAt) {
                                discoveredRoomMap[roomInfo.roomId] = roomInfo
                                _discoveredRooms.emit(roomInfo)
                            }
                        }
                    } catch (e: Exception) {
                        GameLogger.error("解析房间信息失败", e)
                    }
                }
            }
            LANMessageType.DISCOVERY_RESPONSE -> {
                try {
                    val roomInfo = json.decodeFromString<LANRoomInfo>(message.payload)
                    val existingRoom = discoveredRoomMap[roomInfo.roomId]
                    if (existingRoom == null || existingRoom.createdAt < roomInfo.createdAt) {
                        discoveredRoomMap[roomInfo.roomId] = roomInfo
                        _discoveredRooms.emit(roomInfo)
                    }
                } catch (e: Exception) {
                    GameLogger.error("解析发现响应失败", e)
                }
            }
            else -> {
                _receivedMessages.emit(Pair(senderAddress, message))
            }
        }
    }
    
    private fun getBroadcastAddress(): InetAddress {
        val wifiManager = getWifiManager()
        if (wifiManager != null) {
            try {
                val dhcpInfo = wifiManager.dhcpInfo
                if (dhcpInfo != null) {

                    val broadcastIp = (dhcpInfo.ipAddress and dhcpInfo.netmask) or (dhcpInfo.netmask.inv())
                    return InetAddress.getByAddress(byteArrayOf(
                        (broadcastIp and 0xFF).toByte(),
                        (broadcastIp shr 8 and 0xFF).toByte(),
                        (broadcastIp shr 16 and 0xFF).toByte(),
                        (broadcastIp shr 24 and 0xFF).toByte()
                    ))
                }
            } catch (e: Exception) {
                GameLogger.error("获取WiFi广播地址失败", e)
            }
        }
        return InetAddress.getByName("255.255.255.255")
    }
    
    @Suppress("DEPRECATION")
    private fun getWifiManager(): android.net.wifi.WifiManager? {
        return try {
            val context = android.os.Looper.myLooper()?.let {
                android.app.Application().applicationContext
            }
            context?.getSystemService(android.content.Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
        } catch (e: Exception) {
            null
        }
    }
    
    override fun dispose() {
        CoroutineScope(Dispatchers.IO).launch {
            stopDiscovery()
            stopBroadcasting()
        }
    }
}

actual fun createServiceDiscovery(port: Int): ServiceDiscovery {
    return AndroidServiceDiscovery(port)
}
