package org.walks.gamecopilot.navigation

enum class NaviRoute(val route: String, val label: String, val type: Int = 0) {
    HOME("start", "首页"),
    RANDOM("random", "随机工具"),
    MULTIPLAYER("multiplayer", "联机"),
    STATS("stats", "信息"),
    SETTING("setting", "设置"),
    LOCAL_SPY("localSpy", "本地卧底", 1),
    ROOM("room", "房间", 1),
    AWALONG("awalong", "阿瓦隆配置", 1),
    AWALONG_GAME("awalongGame", "阿瓦隆", 1),
    HUNT_TOWN("huntTown", "猎巫镇", 1),
    DRAW_GUESS("drawGuess", "你画我猜", 1),
    DRAW_BOARD("drawBoard", "画板", 1),
    ONE_NIGHT_WEREWOLF("oneNightWerewolf", "一夜狼人配置", 1),
    ONE_NIGHT_WEREWOLF_GAME("oneNightWerewolfGame", "一夜狼人", 1),
    LAN_DISCOVERY("lanDiscovery", "局域网发现", 1),
    LAN_CREATE_ROOM("lanCreateRoom", "创建房间", 1),
    LAN_LOBBY("lanLobby", "房间大厅", 1)
    ;

    fun findNaviByRoute(route: String): NaviRoute {
        return entries.find { it.route == route } ?: HOME
    }
}
