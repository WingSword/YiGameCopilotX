package org.walks.gamecopilot.data.entity

enum class OperationMode(
    val title: String,
    val description: String
) {
    LOCAL(
        title = "单机模式",
        description = "同一设备传递查看身份"
    ),
    LAN(
        title = "局域网模式",
        description = "同一 WiFi 下创建/加入房间"
    ),
    ONLINE(
        title = "网络模式",
        description = "跨网络联机（规划中）"
    )
}
