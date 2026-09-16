package org.walks.gamecopilot.data.entity

/** Local ledger operations use the same integer and overdraft limits as cloud accounts. */
object MonopolyLedgerRules {
    const val LIMIT = 999_999_999_999L
    fun apply(state: MonopolyGameState, entries: List<MonopolyTransaction>): MonopolyGameState {
        require(entries.isNotEmpty()) { "请先选择玩家" }
        return state.copy(players = balances(state, entries, false), transactions = retainGroups(entries + state.transactions))
    }
    fun undo(state: MonopolyGameState): MonopolyGameState {
        val entries = latest(state.transactions)
        require(entries.isNotEmpty()) { "没有可撤销的交易" }
        return state.copy(players = balances(state, entries, true), transactions = state.transactions.drop(entries.size))
    }
    fun latest(history: List<MonopolyTransaction>): List<MonopolyTransaction> {
        val id = history.firstOrNull()?.batchId.orEmpty()
        return if(id.isEmpty()) history.take(1) else history.takeWhile { it.batchId == id }
    }
    fun retainGroups(history: List<MonopolyTransaction>): List<MonopolyTransaction> {
        val result = mutableListOf<MonopolyTransaction>()
        var remaining = history
        while(remaining.isNotEmpty()) {
            val group = latest(remaining)
            if(result.size + group.size > 100) break
            result.addAll(group); remaining = remaining.drop(group.size)
        }
        return result
    }
    private fun balances(state: MonopolyGameState, entries: List<MonopolyTransaction>, reverse: Boolean): List<MonopolyPlayer> {
        require(entries.size <= 20) { "一次最多为 20 人记账" }
        val deltas = mutableMapOf<String, Long>()
        entries.forEach { entry ->
            require(entry.amount in 1..LIMIT && entry.fromPlayerId != entry.toPlayerId) { "请选择不同的收付双方与有效金额" }
            listOfNotNull(entry.fromPlayerId, entry.toPlayerId).forEach { id -> require(state.players.any { it.id == id }) { "相关玩家已被删除，无法完成交易" } }
            val amount = if(reverse) -entry.amount else entry.amount
            entry.fromPlayerId?.let { deltas[it] = deltas.getOrElse(it) { 0 } - amount }
            entry.toPlayerId?.let { deltas[it] = deltas.getOrElse(it) { 0 } + amount }
        }
        return state.players.map { player ->
            val balance = player.balance + deltas.getOrElse(player.id) { 0 }
            require(balance in -LIMIT..LIMIT) { "交易后余额超出允许范围" }
            require(state.preset != "chips" || balance >= 0) { "筹码不足，请调整金额" }
            player.copy(balance = balance)
        }
    }
}
