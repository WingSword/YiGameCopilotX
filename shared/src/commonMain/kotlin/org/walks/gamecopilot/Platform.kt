package org.walks.gamecopilot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// 初始化（需平台适配）
expect fun initMMKV(context: Any?)