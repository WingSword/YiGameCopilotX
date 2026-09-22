package org.walks.gamecopilot.data.entity

enum class OperationMode(
    val title: String,
    val description: String
) {
    LOCAL(
        title = "同机游玩",
        description = "同一设备传递查看身份"
    ),
    LAN(
        title = "局域网模式",
        description = "同一 WiFi 下创建/加入房间"
    ),
    ONLINE(
        title = "网络房间",
        description = "各自使用设备，由云端发牌并同步对局"
    )
}
