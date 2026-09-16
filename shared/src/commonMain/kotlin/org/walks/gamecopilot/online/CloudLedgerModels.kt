package org.walks.gamecopilot.online

import kotlinx.serialization.Serializable

@Serializable data class LedgerConfig(val preset: String = "electronic", val initialBalance: Long = 1_500_000, val unit: String = "游戏币")
@Serializable data class LedgerAccount(val id: String = "", val name: String = "", val balance: Long = 0)
@Serializable data class LedgerLeg(val from: String = "", val to: String = "", val amount: Long = 0, val fromName: String = "", val toName: String = "")
@Serializable data class LedgerEntry(val id: String = "", val actor: String = "", val memo: String = "", val timestamp: Long = 0,
    val legs: List<LedgerLeg> = emptyList(), val reverses: String = "", val reversed: Boolean = false)
@Serializable data class LedgerRequest(val id: String = "", val from: String = "", val to: String = "", val amount: Long = 0, val memo: String = "")
@Serializable data class CloudLedger(val revision: Long = 0, val preset: String = "electronic", val initialBalance: Long = 1_500_000,
    val unit: String = "游戏币", val accounts: List<LedgerAccount> = emptyList(), val history: List<LedgerEntry> = emptyList(),
    val pending: List<LedgerRequest> = emptyList(), val undoId: String = "") {
    fun accountName(id: String) = when(id) { "bank" -> "银行"; "local" -> "本机"; else -> accounts.firstOrNull { it.id == id }?.name ?: "已离开玩家" }
    fun exportCsv(): String {
        // Guard spreadsheet formula injection even when a nickname or note starts with =, +, - or @.
        fun cell(value: String): String = "\"" + (if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'" else "") + value.replace("\"", "\"\"") + "\""
        return buildString {
            appendLine("交易编号,时间戳,操作人,付款方,收款方,数额,单位,备注,已撤销,撤销对应交易")
            history.forEach { entry -> entry.legs.forEach { leg ->
                appendLine(listOf(entry.id, entry.timestamp.toString(), accountName(entry.actor), leg.fromName.ifBlank { accountName(leg.from) }, leg.toName.ifBlank { accountName(leg.to) },
                    leg.amount.toString(), unit, entry.memo, if(entry.reversed) "是" else "否", entry.reverses).joinToString(",", transform = ::cell))
            } }
            appendLine()
            appendLine("账户,当前余额,单位")
            accounts.forEach { appendLine(listOf(it.name, it.balance.toString(), unit).joinToString(",", transform = ::cell)) }
        }
    }
}

object LedgerScenes {
    val ids = listOf("electronic", "property", "score", "chips")
    val names = listOf("电子银行", "地产资金", "积分赛", "筹码")
    val initial = listOf(1_500_000L, 1500L, 0L, 1000L)
    fun name(id: String) = names[ids.indexOf(id).coerceAtLeast(0)]
    fun quickAmounts(id: String) = when(id) {
        "electronic" -> listOf(10_000L, 50_000L, 100_000L, 200_000L)
        "property" -> listOf(50L, 100L, 200L, 500L)
        "score" -> listOf(1L, 5L, 10L, 20L)
        else -> listOf(10L, 50L, 100L, 200L)
    }
}
