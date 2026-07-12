package org.walks.gamecopilot.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.walks.gamecopilot.data.entity.GameMode
import org.walks.gamecopilot.mmkv.MMKV_GAME_STATS_KEY
import org.walks.gamecopilot.mmkv.MMKVUtils
import kotlin.time.Clock

/**
 * 单局游戏记录
 */
@Serializable
data class GameRecord(
    val gameModeOrdinal: Int,
    val gameModeName: String,
    val playerCount: Int,
    val startTime: Long,
    val durationMillis: Long,
    val winner: String
)

/**
 * 单机游戏数据统计管理器
 *
 * 设计说明（暂定方案）：
 * - 在 HomePage -> navigateByMode（单机入口）统一记录每局开始
 * - 不侵入各游戏内部状态机，保证改动面小、风险低
 * - 数据持久化到 MMKV，格式为 JSON 列表
 */
object GameStatsManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val recordLimit = 200

    private val records = mutableListOf<GameRecord>()

    val allRecords: List<GameRecord> get() = records.toList()

    init {
        loadFromStorage()
    }

    /**
     * 记录一局单机游戏开始（在进入游戏页时调用）
     */
    fun recordGameStart(gameMode: GameMode, playerCount: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        val record = GameRecord(
            gameModeOrdinal = gameMode.ordinal,
            gameModeName = gameMode.title,
            playerCount = playerCount.coerceAtLeast(0),
            startTime = now,
            durationMillis = 0L,
            winner = ""
        )
        records.add(record)
        if (records.size > recordLimit) {
            records.subList(0, records.size - recordLimit).clear()
        }
        saveToStorage()
    }

    /**
     * 更新最近一局的结果（结束时调用，暂未在游戏内部埋点，留作后续扩展）
     */
    fun updateLastRecordResult(winner: String, durationMillis: Long = 0L) {
        if (records.isEmpty()) return
        val lastIndex = records.lastIndex
        records[lastIndex] = records[lastIndex].copy(
            winner = winner,
            durationMillis = durationMillis
        )
        saveToStorage()
    }

    /**
     * 各游戏累计局数
     */
    fun countByGameMode(): Map<GameMode, Int> {
        return records.groupingBy { GameMode.entries.getOrElse(it.gameModeOrdinal) { GameMode.SPY_MAIN } }
            .eachCount()
    }

    /**
     * 总局数
     */
    fun totalGames(): Int = records.size

    /**
     * 最近一次游玩时间戳，0 表示从未游玩
     */
    fun lastPlayedTime(): Long = records.maxOfOrNull { it.startTime } ?: 0L

    /**
     * 累计参与玩家总人次
     */
    fun totalPlayerParticipations(): Int = records.sumOf { it.playerCount }

    private fun loadFromStorage() {
        try {
            val raw = MMKVUtils.getString(MMKV_GAME_STATS_KEY, "")
            if (raw.isNotEmpty()) {
                records.clear()
                records.addAll(json.decodeFromString(ListSerializer(GameRecord.serializer()), raw))
            }
        } catch (_: Exception) {
            // 解析失败时保持空记录，不阻塞 UI
        }
    }

    private fun saveToStorage() {
        try {
            val raw = json.encodeToString(ListSerializer(GameRecord.serializer()), records)
            MMKVUtils.put(MMKV_GAME_STATS_KEY, raw)
        } catch (_: Exception) {
            // 持久化失败静默处理
        }
    }

    /**
     * 清空所有统计（用于设置页"清除数据"等场景）
     */
    fun clearAll() {
        records.clear()
        saveToStorage()
    }
}
