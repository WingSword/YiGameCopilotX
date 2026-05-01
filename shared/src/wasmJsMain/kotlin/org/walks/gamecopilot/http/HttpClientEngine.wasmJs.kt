package org.walks.gamecopilot.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createHttpClientEngine(): HttpClientEngine = Js.create()
