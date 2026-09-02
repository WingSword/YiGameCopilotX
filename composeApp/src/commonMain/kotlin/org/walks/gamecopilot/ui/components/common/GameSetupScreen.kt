package org.walks.gamecopilot.ui.components.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.walks.gamecopilot.ui.components.AppIconButton
import org.walks.gamecopilot.ui.components.AppScreen

@Composable
fun GameSetupScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onShowRules: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    AppScreen(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        onBack = onBack,
        actions = {
            if (onShowRules != null) {
                AppIconButton(
                    icon = Icons.Rounded.Info,
                    contentDescription = "游戏规则",
                    onClick = onShowRules
                )
            }
            actions()
        },
        content = content
    )
}
