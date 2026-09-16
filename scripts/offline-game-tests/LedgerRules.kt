import org.walks.gamecopilot.data.entity.*

fun testLedgerRules() {
    var scenarios = 0
    for(preset in listOf("electronic", "property", "score", "chips")) for(count in 2..20) {
        val original = MonopolyGameState(players = (1..count).map { MonopolyPlayer("p$it", "玩家$it", 1000, colorIndex = it-1) }, preset = preset, initialBalance = 1000)
        val entries = original.players.map { MonopolyTransaction(null, it.id, 10, "全员发放", batchId = "batch") }
        val posted = MonopolyLedgerRules.apply(original, entries)
        check(posted.players.all { it.balance == 1010L } && posted.transactions.size == count)
        check(MonopolyLedgerRules.undo(posted) == original)
        check(posted.preset == preset && posted.initialBalance == 1000L)
        val payment = MonopolyTransaction("p1", "p2", 500, "转账")
        val transferred = MonopolyLedgerRules.apply(posted, listOf(payment))
        check(transferred.players.sumOf { it.balance } == posted.players.sumOf { it.balance })
        check(MonopolyLedgerRules.undo(transferred) == posted)
        check(runCatching { MonopolyLedgerRules.apply(original, listOf(payment.copy(toPlayerId = "missing"))) }.isFailure)
        check(runCatching { MonopolyLedgerRules.apply(original, listOf(payment.copy(amount = 0))) }.isFailure)
        check(runCatching { MonopolyLedgerRules.apply(original, listOf(payment.copy(toPlayerId = "p1"))) }.isFailure)
        check(runCatching { MonopolyLedgerRules.apply(original, listOf(payment.copy(amount = 1001))) }.isFailure == (preset == "chips"))
        val badBatch = entries.mapIndexed { index, entry -> if(index == count - 1) entry.copy(amount = MonopolyLedgerRules.LIMIT) else entry }
        check(runCatching { MonopolyLedgerRules.apply(original, badBatch) }.isFailure)
        check(original.players.all { it.balance == 1000L })
        scenarios++
    }
    val history = (0..9).flatMap { batch -> (1..17).map { MonopolyTransaction(null, "p$it", 1, "batch", batchId = "$batch") } }
    val retained = MonopolyLedgerRules.retainGroups(history)
    check(retained.size == 85 && retained.groupBy { it.batchId }.values.all { it.size == 17 })
    val legacy = MonopolyGameState(players = listOf(MonopolyPlayer("a", "A", 1000)), transactions = listOf(MonopolyTransaction(null, "a", 5, "旧交易")))
    check(MonopolyLedgerRules.undo(legacy).players.single().balance == 995L)
    println("PASS Kotlin ledger: $scenarios scene/player-count cases, integer boundaries, conservation, atomic batches, whole-group undo, history retention, legacy defaults")
}
