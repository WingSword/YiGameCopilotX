package org.walks.gamecopilot.privacy

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.distribution.AppDistribution

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    val full = AppDistribution.onlineAiEnabled
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("隐私政策") },
        text = {
            SelectionContainer {
                Text(if (full) FullPrivacyPolicy.BODY else DomesticPrivacyPolicy.BODY,
                    Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        dismissButton = {
            if (full) TextButton(onClick = { uriHandler.openUri(FullPrivacyPolicy.URL) }) { Text("查看网页版") }
        }
    )
}
