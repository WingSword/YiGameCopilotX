package org.walks.gamecopilot.event

sealed class NavigationEvent {
    data class NavigateTo(
        val route: String,
        val args: Map<String, String> = emptyMap(),
        val popUpToRoute: String? = null,
        val inclusive: Boolean = false
    ) : NavigationEvent()

    object PopBackStack : NavigationEvent()
    data class PopUpTo(val route: String, val inclusive: Boolean) : NavigationEvent()
}
