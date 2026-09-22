import kotlinx.coroutines.runBlocking
import org.walks.gamecopilot.distribution.AppDistribution
import org.walks.gamecopilot.distribution.DistributionChannel
import org.walks.gamecopilot.mmkv.MMKVUtils
import org.walks.gamecopilot.navigation.NaviRoute
import org.walks.gamecopilot.online.CloudInvitations
import org.walks.gamecopilot.online.CloudRoomClient as Cloud

fun main() = runBlocking {
    val domestic = System.getenv("YIGAME_TEST_CHANNEL") == "domestic"
    check(AppDistribution.roomsEnabled == !domestic)
    check(AppDistribution.externalUpdatesEnabled == (AppDistribution.channel == DistributionChannel.DIRECT))
    check(runCatching { DistributionChannel.fromId("unknown") }.isFailure)
    val roomRoutes = setOf(NaviRoute.ROOM, NaviRoute.MULTIPLAYER, NaviRoute.CLOUD_LEDGER,
        NaviRoute.LAN_CREATE_ROOM, NaviRoute.LAN_DISCOVERY, NaviRoute.LAN_LOBBY)
    NaviRoute.entries.forEach { check(it.available == (!domestic || it !in roomRoutes)) }
    if (domestic) {
        // A valid persisted membership and invitation must not revive an old network session.
        val saved = """{"server":"http://127.0.0.1:1","token":"member-token","roomId":"123456","roomKey":"secret1"}"""
        MMKVUtils.put("cloud_room_session_v1", saved)
        MMKVUtils.put("cloud_ledger_pending_v1", "pending-ledger")
        MMKVUtils.put("cloud_draw_pending_v1", "pending-drawing")
        MMKVUtils.put("cloud_join_invitation_v1", """{"server":"http://127.0.0.1:1","roomId":"123456","token":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}""")
        check(!Cloud.restore())
        Cloud.refresh()
        check(!Cloud.enter("http://127.0.0.1:1", "host", "", "secret1", true, 6, 1, 0))
        check(Cloud.error.value == AppDistribution.ROOMS_UNAVAILABLE)
        check(!Cloud.enter("http://127.0.0.1:1", "guest", "123456", "secret1", false, 6, 1, 0))
        check(!Cloud.action("ready"))
        check(!Cloud.closeRoom())
        check(Cloud.invitation() == null)
        check(!Cloud.ledgerAction(null) && !Cloud.drawAction(null))
        check(!Cloud.joined.value && Cloud.room.value == null)
        check(Cloud.pendingLedger.value.isEmpty() && Cloud.pendingDraw.value.isEmpty())
        check(MMKVUtils.getString("cloud_room_session_v1", "") == saved)
        CloudInvitations.configureWeb("http://127.0.0.1:1")
        CloudInvitations.receive("#room=123456&invite=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&server=http%3A%2F%2F127.0.0.1%3A1")
        check(CloudInvitations.pending.value == null && CloudInvitations.error.value.isEmpty())
        // Stronger than expecting HTTP failure: no transport was ever instantiated.
        val transport = Cloud.javaClass.getDeclaredField("client\$delegate").apply { isAccessible = true }.get(Cloud) as Lazy<*>
        check(!transport.isInitialized())
    }
    println("PASS ${AppDistribution.channel.id}: routes, update policy, room/session/invitation isolation")
}
