package org.walks.gamecopilot.theme

import androidx.compose.runtime.Composable

/** Keep native system controls legible when the in-app theme differs from the OS. */
@Composable
expect fun PlatformSystemBars(darkTheme: Boolean)
