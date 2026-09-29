import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.lan.data.*
import org.walks.gamecopilot.lan.server.AndroidLANHostServer

private class Messages : WebSocket.Listener {
    val received = LinkedBlockingQueue<String>()
    private val fragments = StringBuilder()
    override fun onOpen(socket: WebSocket) { socket.request(1) }
    override fun onText(socket: WebSocket, data: CharSequence, last: Boolean): java.util.concurrent.CompletionStage<*>? {
        fragments.append(data)
        if (last) { received.add(fragments.toString()); fragments.setLength(0) }
        socket.request(1)
        return null
    }
    fun next(): LANMessage = Json.decodeFromString(checkNotNull(received.poll(5, TimeUnit.SECONDS)))
}

fun main() = runBlocking {
    val host = AndroidLANHostServer()
    val port = ServerSocket(0).use { it.localPort }
    try {
        // start() must return while the listening server remains alive.
        repeat(2) { round ->
            withTimeout(8_000) { check(host.start(port)) }
            check(host.isRunning)
            val listener = Messages()
            val client = HttpClient.newHttpClient()
            val socket = client.newWebSocketBuilder().buildAsync(
                URI("ws://127.0.0.1:$port/lan?playerId=regression-$round&playerName=Test"), listener
            ).get(5, TimeUnit.SECONDS)
            try {
                check(listener.next().type == LANMessageType.JOIN_RESPONSE)
                withTimeout(5_000) { host.connectedPlayers.first { it.size == 1 } }
                val otherListener = Messages()
                val other = client.newWebSocketBuilder().buildAsync(
                    URI("ws://127.0.0.1:$port/lan?playerId=other-$round&playerName=Other"), otherListener
                ).get(5, TimeUnit.SECONDS)
                try {
                    check(otherListener.next().type == LANMessageType.JOIN_RESPONSE)
                    withTimeout(5_000) { host.connectedPlayers.first { it.size == 2 } }
                    val broadcast = LANMessage(type = LANMessageType.ROOM_STATE_SYNC, payload = "all players")
                    host.broadcast(broadcast)
                    for (messages in listOf(listener, otherListener)) {
                        val received = messages.next()
                        check(received.type == broadcast.type && received.payload == broadcast.payload)
                    }
                    other.sendClose(1000, "done").get(5, TimeUnit.SECONDS)
                    check(listener.next().type == LANMessageType.PLAYER_LEFT)
                } finally { other.abort() }
                socket.sendText(Json.encodeToString(LANMessage(type = LANMessageType.HEARTBEAT)), true).join()
                check(listener.next().type == LANMessageType.HEARTBEAT)
                socket.sendClose(1000, "done").get(5, TimeUnit.SECONDS)
                withTimeout(5_000) { host.connectedPlayers.first { it.isEmpty() } }
            } finally { socket.abort(); client.close() }
            withTimeout(8_000) { host.stop() }
            check(!host.isRunning)
            check(host.connectedPlayers.first().isEmpty())
        }
        ServerSocket(port).use {
            withTimeout(8_000) { check(!host.start(port)) }
            check(!host.isRunning)
        }
        withTimeout(8_000) { check(host.start(port)) }
        withTimeout(8_000) { host.stop() }
        println("PASS: start returns; two-client broadcast; welcome/heartbeat; stop/restart; bind failure/retry")
    } finally { host.stop() }
}
