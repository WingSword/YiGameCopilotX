package org.walks.gamecopilot.navigation

/**
 *  Created by Wing at 11:26 on 2025/4/24
 *
 */
enum class NaviRoute(val route: String, val label: String, val type: Int = 0) {
    HOME("start", "首页"),
    SETTING("setting", "设置"),
    LOCAL_SPY("localSpy", "本地卧底", 1),
    ROOM("room", "房间", 1),
    RANDOM("random", "随机工具"),
    AWALONG("awalong", "阿瓦隆", 1)
    ;

    fun findNaviByRoute(route: String): NaviRoute {
        return entries.find { it.route == route } ?: HOME
    }
}