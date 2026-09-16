package org.walks.gamecopilot.online

/** Presentation only: membership, host authority and assigned identity never change. */
fun CloudRoom.forHostView(managing: Boolean): CloudRoom {
    if(!isHost) return this
    return copy(
        identity = if(managing) null else identity,
        game = game?.let { state -> state.copy(
            notes = if(managing) emptyList() else state.notes,
            prompt = if(managing && state.phase in listOf("DEAL", "NIGHT")) "等待玩家在各自设备上查看身份并完成行动。" else state.prompt,
            options = state.options.filter { (state.phase == "DAY" && it.id == "vote") == managing }
        ) },
        drawing = drawing?.let { state -> if(!managing) state else state.copy(
            canDraw = false, canGuess = false, feedback = "",
            word = if(state.phase in listOf("TURN_RESULT", "RESULT")) state.word else ""
        ) }
    )
}
