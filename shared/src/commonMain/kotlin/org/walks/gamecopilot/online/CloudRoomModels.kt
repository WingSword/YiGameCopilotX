package org.walks.gamecopilot.online

import kotlinx.serialization.Serializable

const val CLOUD_SERVER_UPGRADE_MESSAGE = "服务器版本较旧，暂不支持房主模式提醒和扫码邀请。请更新云端服务后重试；仍可离开或解散房间。"

@Serializable data class CloudInvitation(val url: String = "", val modules: List<String> = emptyList())
@Serializable data class CloudHostViewChange(val revision: Int = 0, val mode: String = "player", val hostId: String = "")

@Serializable data class CloudPlayer(val id: String = "", val nickname: String = "", val ready: Boolean = false, val connected: Boolean = false)
@Serializable data class CloudIdentity(val role: String = "", val word: String = "")
@Serializable data class CloudChoice(val id: String = "", val label: String = "")
@Serializable data class CloudRevealed(val nickname: String = "", val initialRole: String = "", val finalRole: String = "", val voteTarget: String = "", val eliminated: Boolean = false)
@Serializable data class CloudHuntSeat(val id: String = "", val nickname: String = "", val alive: Boolean = true, val role: String = "")
@Serializable data class CloudMission(val quest: Int = 1, val fails: Int = 0, val success: Boolean = false)
@Serializable data class CloudAvalon(val leaderId: String = "", val quest: Int = 1, val teamSize: Int = 0,
    val failsNeeded: Int = 1, val rejections: Int = 0, val team: List<String> = emptyList(),
    val missions: List<CloudMission> = emptyList(), val submitted: Int = 0)
@Serializable data class CloudStroke(val id: String = "", val color: String = "#111111", val width: Int = 8,
    val points: List<List<Int>> = emptyList())
@Serializable data class CloudScore(val id: String = "", val nickname: String = "", val score: Int = 0, val guessed: Boolean = false)
@Serializable data class CloudDrawing(val phase: String = "", val stepId: String = "", val turn: Int = 1,
    val totalTurns: Int = 3, val painterId: String = "", val word: String = "", val wordLength: Int = 0,
    val remainingSeconds: Int = 0, val deadline: Long = 0, val revision: Long = 0, val canDraw: Boolean = false,
    val canGuess: Boolean = false, val feedback: String = "", val strokes: List<CloudStroke> = emptyList(),
    val publicLog: List<String> = emptyList(), val scores: List<CloudScore> = emptyList())
@Serializable data class CloudGameView(val phase: String = "", val stepId: String = "", val prompt: String = "",
    val options: List<CloudChoice> = emptyList(), val notes: List<String> = emptyList(), val revealed: List<CloudRevealed> = emptyList(),
    val center: List<String> = emptyList(), val confirmedCount: Int = 0, val votedCount: Int = 0,
    val roster: List<CloudHuntSeat> = emptyList(), val publicLog: List<String> = emptyList(), val livingCount: Int = 0, val day: Int = 1,
    val avalon: CloudAvalon? = null)
@Serializable data class CloudRoom(val roomId: String = "", val gameType: String = "spy", val status: String = "WAITING", val hostId: String = "",
    val selfId: String = "", val maxPlayers: Int = 12, val spyCount: Int = 1, val blankCount: Int = 0,
    val roundId: String = "", val roundNumber: Int = 0, val winner: String = "",
    val players: List<CloudPlayer> = emptyList(), val identity: CloudIdentity? = null,
    val roles: List<String> = emptyList(), val game: CloudGameView? = null,
    val ledgerConfig: LedgerConfig? = null, val ledger: CloudLedger? = null, val witchCount: Int = 1,
    val drawing: CloudDrawing? = null, val hostView: String = "", val hostViewRevision: Int = -1,
    val hostViewChanges: List<CloudHostViewChange> = emptyList()) {
    val isHost get() = selfId.isNotEmpty() && hostId == selfId
    // Missing legacy fields must never look like a synchronized player/admin mode.
    val supportsHostView get() = hostView in listOf("player", "admin") && hostViewRevision >= 0
    val self get() = players.firstOrNull { it.id == selfId }
    val isOneNight get() = gameType == "werewolf"
    val isLedger get() = gameType == "ledger"
    val isHunt get() = gameType == "hunt"
    val isAvalon get() = gameType == "avalon"
    val isDrawing get() = gameType == "drawing"
    val gameTitle get() = when(gameType) { "werewolf" -> "一夜终极狼人"; "ledger" -> "桌游记账"; "hunt" -> "猎巫镇 · 简化版"; "avalon" -> "阿瓦隆"; "drawing" -> "你画我猜"; else -> "谁是卧底" }
    val hasEnoughPlayers get() = if (isLedger) players.size >= 2 else if (isOneNight || isHunt || isAvalon || isDrawing) players.size == maxPlayers else players.size >= 4 && spyCount + blankCount < players.size / 2.0
}
