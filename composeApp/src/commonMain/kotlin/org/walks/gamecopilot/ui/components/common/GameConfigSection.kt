package org.walks.gamecopilot.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.walks.gamecopilot.theme.LocalAppDesign
import org.walks.gamecopilot.ui.components.AppSectionHeader
import org.walks.gamecopilot.ui.picker.WeSingleColumnPicker

data class GameConfigItem(
    val title: String,
    val value: String,
    val options: List<String>,
    val color: Color,
    val onValueChange: (String) -> Unit
)

@Composable
fun GameConfigSection(
    title: String,
    configItems: List<GameConfigItem>,
    modifier: Modifier = Modifier
) {
    var showPickerIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppSectionHeader(
            title = title,
            subtitle = "点击项目修改本局配置"
        )
        configItems.forEachIndexed { index, config ->
            SmallConfigButton(
                title = config.title,
                value = config.value,
                onClick = { showPickerIndex = index },
                color = config.color
            )
        }
    }

    configItems.forEachIndexed { index, config ->
        WeSingleColumnPicker(
            visible = showPickerIndex == index,
            title = "选择${config.title}",
            range = config.options,
            value = config.options.indexOf(config.value).takeIf { it >= 0 } ?: 0,
            onCancel = { showPickerIndex = -1 },
            onChange = { selectedIndex ->
                config.onValueChange(config.options[selectedIndex])
                showPickerIndex = -1
            }
        )
    }
}

@Composable
fun SmallConfigButton(
    title: String,
    value: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val design = LocalAppDesign.current
    val shape = RoundedCornerShape(design.cornerRadius.md)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.62f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
