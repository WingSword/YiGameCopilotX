package org.walks.gamecopilot.distribution

/** Fixed by the installed artifact, never by preferences, an invitation or a remote flag. */
enum class DistributionChannel(val id: String, val label: String, val roomsEnabled: Boolean) {
    DOMESTIC("domestic", "国内商店版", false),
    GOOGLE_PLAY("googlePlay", "Google Play 版", true),
    DIRECT("direct", "独立分发版", true),
    FDROID("fdroid", "F-Droid 版", true);

    companion object {
        fun fromId(id: String): DistributionChannel = entries.single { it.id == id }
    }
}

object AppDistribution {
    val channel: DistributionChannel = platformDistributionChannel()
    val roomsEnabled: Boolean get() = channel.roomsEnabled
    val onlineAiEnabled: Boolean get() = channel != DistributionChannel.DOMESTIC
    val externalUpdatesEnabled: Boolean get() = channel == DistributionChannel.DIRECT
    const val ROOMS_UNAVAILABLE = "此发行版本仅支持同机游玩"

    fun requireRooms() = check(roomsEnabled) { ROOMS_UNAVAILABLE }
}
