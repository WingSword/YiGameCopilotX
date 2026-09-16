package org.walks.gamecopilot.ui.page.multiplayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
internal fun CloudHostModeNotice(managing: Boolean) {
    val design = LocalAppDesign.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.cornerRadius.card),
        color = if (managing) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (managing) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(design.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(design.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (managing) Icons.Rounded.AdminPanelSettings else Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(design.iconSize.md)
            )
            Text(
                text = if (managing) "房主正在使用管理员模式" else "房主正在使用玩家模式",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
internal fun CloudStatusLabel(text: String, active: Boolean = false) {
    val design = LocalAppDesign.current
    Surface(
        shape = RoundedCornerShape(design.cornerRadius.badge),
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(text, Modifier.padding(horizontal = design.spacing.md, vertical = design.spacing.sm), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun CloudCountSelector(
    title: String,
    value: String,
    decreaseLabel: String,
    increaseLabel: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val design = LocalAppDesign.current
    Column(verticalArrangement = Arrangement.spacedBy(design.spacing.sm)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            shape = RoundedCornerShape(design.cornerRadius.input),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDecrease, enabled = canDecrease) { Text(decreaseLabel) }
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                TextButton(onClick = onIncrease, enabled = canIncrease) { Text(increaseLabel) }
            }
        }
    }
}
