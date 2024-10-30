package org.walks.gamecopilot.ui.input

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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