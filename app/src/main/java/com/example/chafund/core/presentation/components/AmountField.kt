package com.example.chafund.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Amount (Tk)",
    error: String? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(modifier = modifier) {
        Text(
            text     = label,
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = { input ->
                // Allow digits and at most one decimal point
                val filtered = input.filter { it.isDigit() || it == '.' }
                if (filtered.count { it == '.' } <= 1) onValueChange(filtered)
            },
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder   = { Text("0", fontSize = 14.sp) },
            singleLine    = true,
            isError       = error != null,
            shape         = shape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
        )
        if (error != null) {
            Text(
                text  = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
            )
        }
    }
}
