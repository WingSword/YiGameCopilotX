import org.walks.gamecopilot.online.*

fun main() {
    var checks = 0
    fun verify(value: Boolean) { checks++; check(value) { "Invitation/view check $checks failed" } }
    val valid = "#room=123456&invite=${"A".repeat(32)}&server=https%3A%2F%2Fplay.example.com"
    CloudInvitations.configureWeb("https://play.example.com/game/")
    CloudInvitations.receive(valid)
    verify(CloudInvitations.pending.value == CloudJoinInvitation("https://play.example.com", "123456", "A".repeat(32)))
    CloudInvitations.configureWeb("https://play.example.com/game/")
    verify(CloudInvitations.pending.value?.roomId == "123456")
    for(bad in listOf(valid + "&room=654321", valid + "&extra=x", valid.replace("123456", "abcdef"),
        valid.replace("A".repeat(32), "short"), valid.replace("https%3A", "http%3A"),
        valid.replace("https%3A%2F%2Fplay.example.com", "javascript%3Aalert(1)"),
        valid.replace("play.example.com", "user%40play.example.com"), valid.replace("play.example.com", "play.example.com%2Fapi"))) {
        CloudInvitations.receive(bad); verify(CloudInvitations.pending.value == null && CloudInvitations.error.value.isNotBlank())
    }
    CloudInvitations.configureWeb("http://localhost:8000/")
    CloudInvitations.receive(valid.replace("https%3A", "http%3A"))
    verify(CloudInvitations.pending.value != null)
    CloudInvitations.clear(); verify(CloudInvitations.pending.value == null && CloudInvitations.error.value.isEmpty())
    val base = CloudRoom(selfId="host", hostId="host", players=listOf(CloudPlayer("host"), CloudPlayer("guest")), identity=CloudIdentity("梅林", "秘密"))
    for(phase in listOf("DEAL", "NIGHT", "DAY", "VOTE", "TEAM", "TEAM_VOTE", "QUEST", "ASSASSINATE", "RESULT")) {
        val room = base.copy(game=CloudGameView(phase=phase, prompt="秘密行动", notes=listOf("秘密知识"), options=listOf(CloudChoice(if(phase=="DAY") "vote" else "confirm"))))
        val admin = room.forHostView(true); val player = room.forHostView(false)
        verify(admin.identity == null && admin.game!!.notes.isEmpty() && admin.players == room.players && admin.isHost)
        verify(player.identity == room.identity && player.game!!.notes == room.game!!.notes)
        verify(admin.game!!.options.isNotEmpty() == (phase == "DAY"))
        verify(player.game!!.options.isNotEmpty() == (phase != "DAY"))
        verify(room.copy(selfId="guest").forHostView(true) == room.copy(selfId="guest"))
    }
    for(phase in listOf("DRAW_READY", "DRAWING", "TURN_RESULT", "RESULT")) {
        val room = base.copy(drawing=CloudDrawing(phase=phase, word="火锅", canDraw=true, canGuess=true, feedback="私密反馈"))
        val admin = room.forHostView(true)
        verify(!admin.drawing!!.canDraw && !admin.drawing!!.canGuess && admin.drawing!!.feedback.isEmpty())
        verify(admin.drawing!!.word.isNotBlank() == (phase in listOf("TURN_RESULT", "RESULT")))
        verify(room.forHostView(false).drawing == room.drawing)
    }
    println("PASS Kotlin invitations/view policy: $checks checks; QR fragment parsing, persisted pending join, invalid links, HTTPS and private actions")
}
