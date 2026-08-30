package com.example.chafund.core.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chafund.ui.theme.AppColors

/** A selectable name shown in the [NamePicker] suggestion list. */
data class NameOption(val id: Long, val label: String)

/**
 * Type-ahead picker for attributing an entry to an existing name.
 * Names are created in Settings; this only searches/selects them.
 */
@Composable
fun NamePicker(
    query: String,
    suggestions: List<NameOption>,
    selectedLabel: String,
    hasNames: Boolean,
    onQueryChange: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onClear: () -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Name",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (selectedLabel.isNotEmpty()) {
            // Selected state — chip-like row with a clear button.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, AppColors.BorderLight, shape),
                shape = shape,
                color = AppColors.BalanceFillLight,
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        color = AppColors.BalanceTextLight,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear name",
                            tint = AppColors.BalanceTextLight,
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search a name…", fontSize = 13.sp) },
                singleLine = true,
                isError = error != null,
                shape = shape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                ),
            )

            when {
                !hasNames -> HintText("No names yet — add them in Settings")
                suggestions.isEmpty() -> HintText("No matching name")
                else -> SuggestionList(suggestions = suggestions, onSelect = onSelect, shape = shape)
            }
        }

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SuggestionList(
    suggestions: List<NameOption>,
    onSelect: (Long) -> Unit,
    shape: RoundedCornerShape,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.BorderLight, shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
            suggestions.forEachIndexed { index, option ->
                if (index > 0) HorizontalDivider(thickness = 0.5.dp)
                Text(
                    text = option.label,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option.id) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp),
    )
}
