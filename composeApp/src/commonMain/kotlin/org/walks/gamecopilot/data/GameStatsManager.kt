package org.walks.gamecopilot.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 * 仅在游戏真正开始后写入记录，并在对应游戏结束时补全结果与时长。
 * 数据持久化到 MMKV，同时通过 [recordsFlow] 向界面发布最新快照。
 */
object GameStatsManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val recordLimit = 200
    private const val rapidStartGuardMillis = 1_500L

    private val records = mutableListOf<GameRecord>()
    private val _recordsFlow = MutableStateFlow<List<GameRecord>>(emptyList())

    val allRecords: List<GameRecord> get() = records.toList()
    val recordsFlow: StateFlow<List<GameRecord>> = _recordsFlow.asStateFlow()

    init {
        loadFromStorage()
    }

    /**
     * 记录一局单机游戏开始。
     *
     * 相同玩法、相同人数的未结算记录在 1.5 秒内只保留一条，避免重复点击或
     * Compose 重组导致重复计数。
     */
    fun recordGameStart(gameMode: GameMode, playerCount: Int) {
        val now = Clock.System.now().toEpochMilliseconds()
        val safePlayerCount = playerCount.coerceAtLeast(0)
        val lastRecord = records.lastOrNull()
        if (
            lastRecord != null &&
            lastRecord.gameModeOrdinal == gameMode.ordinal &&
            lastRecord.playerCount == safePlayerCount &&
            lastRecord.winner.isEmpty() &&
            now - lastRecord.startTime in 0 until rapidStartGuardMillis
        ) {
            return
        }
        val record = GameRecord(
            gameModeOrdinal = gameMode.ordinal,
            gameModeName = gameMode.title,
            playerCount = safePlayerCount,
            startTime = now,
            durationMillis = 0L,
            winner = ""
        )
        records.add(record)
        if (records.size > recordLimit) {
            records.subList(0, records.size - recordLimit).clear()
        }
        persistAndPublish()
    }

    /**
     * 更新最后一条记录的结果。保留此接口以兼容已有调用；新代码应优先使用
     * [completeLatestGame]，避免将结果误写到其他玩法。
     */
    fun updateLastRecordResult(winner: String, durationMillis: Long = 0L) {
        if (records.isEmpty()) return
        val lastIndex = records.lastIndex
        records[lastIndex] = records[lastIndex].copy(
            winner = winner,
            durationMillis = durationMillis
        )
        persistAndPublish()
    }

    /**
     * 完成最近一局尚未结算的指定游戏，并根据开局时间计算对局时长。
     */
    fun completeLatestGame(gameMode: GameMode, winner: String) {
        val recordIndex = records.indexOfLast {
            it.gameModeOrdinal == gameMode.ordinal && it.winner.isEmpty()
        }
        if (recordIndex < 0) return

        val now = Clock.System.now().toEpochMilliseconds()
        val record = records[recordIndex]
        records[recordIndex] = record.copy(
            winner = winner,
            durationMillis = (now - record.startTime).coerceAtLeast(0L)
        )
        persistAndPublish()
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
                records.addAll(
                    json.decodeFromString(ListSerializer(GameRecord.serializer()), raw)
                        .takeLast(recordLimit)
                )
            }
        } catch (_: Exception) {
            // 解析失败时保持空记录，不阻塞 UI
            records.clear()
        } finally {
            publishRecords()
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

    private fun persistAndPublish() {
        saveToStorage()
        publishRecords()
    }

    private fun publishRecords() {
        _recordsFlow.value = records.toList()
    }

    /**
     * 清空所有统计（用于设置页"清除数据"等场景）
     */
    fun clearAll() {
        records.clear()
        persistAndPublish()
    }
}
