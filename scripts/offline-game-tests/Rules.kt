import org.walks.gamecopilot.werewolf.*
import org.walks.gamecopilot.werewolf.data.*
import org.walks.gamecopilot.data.entity.LocalSpyEntity

fun main() {
    val configs = org.walks.gamecopilot.awalong.AwalongConfig.entries
    check(configs.all { it.process.size == 5 && it.role.size == it.playerNum })
    for (n in 5..10) check(org.walks.gamecopilot.awalong.AwalongCustomConfig(blueCount=n-2,redCount=2).process.size == 5)
    val avalon = org.walks.gamecopilot.awalong.data.AwalongGameState(roleList=mutableListOf(org.walks.gamecopilot.awalong.AwalongRole.MEILING))
    val a22 = avalon.copy(dayList=mutableListOf(1,-1,1,-1).mapIndexed { i,r -> org.walks.gamecopilot.awalong.data.AwalongGameDayEntity(day=i,taskResult=r) }.toMutableList())
    check(org.walks.gamecopilot.awalong.AwalongGameLogic.checkGameEnd(a22)==null)
    check(org.walks.gamecopilot.awalong.AwalongGameLogic.checkGameEnd(a22.copy(dayList=(a22.dayList+org.walks.gamecopilot.awalong.data.AwalongGameDayEntity(day=4,taskResult=1)).toMutableList()))?.winner=="蓝方")

    var rounds=0
    for(preset in WerewolfPresets.presets) repeat(80) {
        var g=WerewolfGameLogic.initializeGame(preset,emptyList())
        check(g.players.size==preset.playerCount && g.centerCards.size==3)
        val active=g.nightActionOrder.map { g.players[it].initialRole.nightOrder }.filter { it>0 }
        check(active==active.sorted())
        for(step in g.nightActionOrder.indices) {
            g=g.copy(currentNightStep=step)
            val p=g.players[g.nightActionOrder[step]]
            val target=g.players.first { it.id!=p.id }.id
            val others=g.players.filter { it.id!=p.id }
            g=when(p.initialRole) {
                WerewolfRole.WEREWOLF -> if(WerewolfGameLogic.isLoneWolf(g,p.id)) WerewolfGameLogic.executeWerewolfPeekCenter(g,0).first else g
                WerewolfRole.SEER -> WerewolfGameLogic.executeSeerViewCenter(g,0,1).first
                WerewolfRole.ROBBER -> WerewolfGameLogic.executeRobberSwap(g,p.id,target).first
                WerewolfRole.TROUBLEMAKER -> WerewolfGameLogic.executeTroublemakerSwap(g,others[0].id,others[1].id).first
                WerewolfRole.DRUNK -> WerewolfGameLogic.executeDrunkSwap(g,p.id,0).first
                WerewolfRole.INSOMNIAC -> { check(WerewolfGameLogic.getInsomniacResult(g,p.id).second.isNotBlank()); g }
                else -> g
            }
        }
        g=WerewolfGameLogic.finalizeNightActions(g)
        check((g.players.map { it.currentRole }+g.centerCards.map { it.role }).groupingBy { it }.eachCount()==preset.roles.groupingBy { it }.eachCount())
        g=g.copy(players=g.players.map { it.copy(voteTarget=(it.id+1)%g.playerCount) })
        g=WerewolfGameLogic.resolveVotes(g)
        check(g.players.all { it.isAlive })
        check(WerewolfGameLogic.winnerText(g).isNotEmpty())
        rounds++
    }
    fun state(vararg roles: WerewolfRole) = WerewolfGameState(playerCount=roles.size,players=roles.mapIndexed { i,r -> WerewolfPlayer(i,"P$i",r,r) },centerCards=listOf(CenterCard(0,WerewolfRole.WEREWOLF),CenterCard(1,WerewolfRole.VILLAGER),CenterCard(2,WerewolfRole.SEER)))
    var g=state(WerewolfRole.DOPPELGANGER,WerewolfRole.ROBBER,WerewolfRole.INSOMNIAC,WerewolfRole.WEREWOLF).copy(nightActionOrder=listOf(0,3,1,2))
    g=WerewolfGameLogic.executeDoppelgangerAction(g,0,1).first
    g=WerewolfGameLogic.confirmNightResult(g)
    check(g.doppelgangerFollowUpStep==1 && g.nightActionOrder==listOf(0,0,3,1,2))
    g=WerewolfGameLogic.executeRobberSwap(g.copy(currentNightStep=1),0,2).first
    g=WerewolfGameLogic.confirmNightResult(g).copy(currentNightStep=3)
    val copiedRobber=WerewolfGameLogic.executeRobberSwap(g,1,2)
    check(copiedRobber.second.contains("化身幽灵"))
    g=WerewolfGameLogic.finalizeNightActions(copiedRobber.first)
    check(g.players.map { it.currentRole }==listOf(WerewolfRole.INSOMNIAC,WerewolfRole.ROBBER,WerewolfRole.ROBBER,WerewolfRole.WEREWOLF))
    g=state(WerewolfRole.HUNTER,WerewolfRole.WEREWOLF,WerewolfRole.VILLAGER)
    g=WerewolfGameLogic.resolveVotes(g.copy(players=g.players.mapIndexed { i,p -> p.copy(voteTarget=if(i==0)1 else 0) }))
    check(g.eliminatedPlayerIds.toSet()==setOf(0,1) && !g.hunterPending)
    check(WerewolfGameLogic.determineWinner(g)==WerewolfFaction.VILLAGER)
    g=state(WerewolfRole.TANNER,WerewolfRole.WEREWOLF,WerewolfRole.VILLAGER, WerewolfRole.VILLAGER)
    g=WerewolfGameLogic.resolveVotes(g.copy(players=g.players.mapIndexed { i,p -> p.copy(voteTarget=listOf(1,0,0,1)[i]) }))
    check(WerewolfGameLogic.determineWinners(g).toSet()==setOf(WerewolfFaction.INDEPENDENT,WerewolfFaction.VILLAGER))
    g=state(WerewolfRole.INSOMNIAC,WerewolfRole.ROBBER,WerewolfRole.TROUBLEMAKER,WerewolfRole.DRUNK)
    g=WerewolfGameLogic.executeRobberSwap(g,1,0).first
    g=WerewolfGameLogic.executeTroublemakerSwap(g,0,3).first
    g=WerewolfGameLogic.executeDrunkSwap(g,3,0).first
    check(WerewolfGameLogic.getInsomniacResult(g,0).second.contains("酒鬼"))
    for(n in 4..16) for(spies in 1..n/3) for(blank in 0..spies) repeat(20) {
        val spy=LocalSpyEntity(totalPlayerNumber=n,spyNum=spies,blackNum=blank)
        spy.refreshGame(); check(spy.spies.distinct().size==spies)
        check((1..n).count { spy.optIdentity(it)=="[空白]" }==blank)
        check((1..n).all { spy.optIdentity(it).isNotBlank() })
    }
    testLedgerRules()
    val damaged=LocalSpyEntity(totalPlayerNumber=4,spyNum=9,blackNum=20)
    damaged.refreshGame(); check(damaged.spyNum==1 && damaged.blackNum==1)
    println("PASS Kotlin offline: $rounds complete one-night rounds; swaps, hunter, joint victory, 4–16 player spy/blank distribution and corrupt-config repair")
}
