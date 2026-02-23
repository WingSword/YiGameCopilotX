package org.walks.gamecopilot.navigation

enum class NaviRoute(val route: String, val label: String, val type: Int = 0) {
    HOME("start", "首页"),
    RANDOM("random", "随机工具"),
    SETTING("setting", "设置"),
    LOCAL_SPY("localSpy", "本地卧底", 1),
    ROOM("room", "房间", 1),
    AWALONG("awalong", "阿瓦隆", 1),
    DRAW_GUESS("drawGuess", "你画我猜", 1),
    DRAW_BOARD("drawBoard", "画板", 1)
    ;

    fun findNaviByRoute(route: String): NaviRoute {
        return entries.find { it.route == route } ?: HOME
    }
}