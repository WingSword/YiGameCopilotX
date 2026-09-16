package org.walks.gamecopilot.ui.page.game.localspy.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import org.walks.gamecopilot.ui.components.CommonTopBar
import org.walks.gamecopilot.ui.components.TopBarAction

@Composable
fun GameHeaderView(
    onBack: () -> Unit,
    isWordLibraryExpanded: Boolean,
    onToggleWordLibrary: (Boolean) -> Unit,
    onShowWordsDialog: () -> Unit,
    onShowGuide: () -> Unit
) {
    CommonTopBar(
        title = "谁是卧底",
        subtitle = "配置词库，依次传机查看身份",
        onBack = onBack,
        actions = listOf(
            TopBarAction(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = "查看词库",
                onClick = onShowWordsDialog
            ),
            TopBarAction(
                icon = if (isWordLibraryExpanded) {
                    Icons.Rounded.ExpandLess
                } else {
                    Icons.Rounded.ExpandMore
                },
                contentDescription = if (isWordLibraryExpanded) "折叠词库" else "展开词库",
                onClick = { onToggleWordLibrary(!isWordLibraryExpanded) }
            ),
            TopBarAction(
                icon = Icons.Rounded.Info,
                contentDescription = "传机流程",
                onClick = onShowGuide
            )
        )
    )
}
