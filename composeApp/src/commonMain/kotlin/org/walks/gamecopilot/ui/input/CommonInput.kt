package org.walks.gamecopilot.ui.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CommonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        modifier = Modifier.width(200.dp),
        shape = MaterialTheme.shapes.large,
        textStyle = TextStyle(
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.primary,
            unfocusedTextColor = MaterialTheme.colorScheme.tertiary,

            )
    )
}

@Composable
fun HalfRadioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    radioOption: RadioOption = RadioOption.Left
) {
    val shape = when (radioOption) {
        RadioOption.Left -> MaterialTheme.shapes.large.copy(
            topStart = CornerSize(0),
            bottomStart = CornerSize(0)
        )

        RadioOption.Right -> MaterialTheme.shapes.large.copy(
            topEnd = CornerSize(0),
            bottomEnd = CornerSize(0)
        )

        RadioOption.Top -> MaterialTheme.shapes.large.copy(
            topStart = CornerSize(0),
            topEnd = CornerSize(0)
        )

        RadioOption.Bottom -> MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0)
        )
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        textStyle = TextStyle(
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedContainerColor = backgroundColor,
            focusedContainerColor = MaterialTheme.colorScheme.background,

            )
    )
}

enum class RadioOption {
    Left,
    Right,
    Top,
    Bottom,
}