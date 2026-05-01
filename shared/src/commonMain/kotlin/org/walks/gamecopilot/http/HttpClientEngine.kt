package org.walks.gamecopilot.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import kotlin.time.Duration.Companion.seconds

expect fun createHttpClientEngine(): HttpClientEngine

val wsClient = HttpClient(createHttpClientEngine()) {
    install(WebSockets) {
        pingInterval = 20.seconds
    }
}
