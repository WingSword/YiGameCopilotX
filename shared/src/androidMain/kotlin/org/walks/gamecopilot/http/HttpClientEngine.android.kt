package org.walks.gamecopilot.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun createHttpClientEngine(): HttpClientEngine = CIO.create()
