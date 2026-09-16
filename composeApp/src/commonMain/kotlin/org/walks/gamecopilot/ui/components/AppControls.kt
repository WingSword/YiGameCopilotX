package org.walks.gamecopilot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.theme.LocalAppDesign

@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val design = LocalAppDesign.current
    val shape = RoundedCornerShape(design.cornerRadius.md)

    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.62f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AppBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppIconButton(
        icon = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = "返回",
        onClick = onClick,
        modifier = modifier,
        containerColor = Color.Transparent
    )
}

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.size(12.dp))
            Row(content = action)
        }
    }
}

@Composable
fun AppPrimaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    icon: ImageVector? = Icons.AutoMirrored.Rounded.ArrowForward
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(LocalAppDesign.current.cornerRadius.button)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = if (icon != null) Modifier.weight(1f) else Modifier,
                    textAlign = if (icon != null) TextAlign.Start else TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (icon != null) {
                    Spacer(modifier = Modifier.size(12.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun AppSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val design = LocalAppDesign.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(design.cornerRadius.md),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(design.cornerRadius.sm))
                        .background(
                            if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
                        )
                        .clickable { onSelected(index) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AppChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val design = LocalAppDesign.current
    val shape = RoundedCornerShape(design.cornerRadius.card)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.56f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.62f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.size(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
