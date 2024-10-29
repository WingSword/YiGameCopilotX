package org.walks.gamecopilot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

