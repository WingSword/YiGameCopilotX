// OHOS (linuxArm64) actual for expect in commonMain/org/walks/gamecopilot/http/HttpClientEngine.kt
package org.walks.gamecopilot.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.curl.Curl

actual fun createHttpClientEngine(): HttpClientEngine = Curl.create()
