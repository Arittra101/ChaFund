package com.example.chafund.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockedMonthBadge(
    monthLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text     = "Current month",
            fontSize = 10.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text       = monthLabel,
            fontSize   = 15.sp,
            fontWeight = FontWeight.W500,
            color      = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun LockIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier  = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Auto-set",
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text     = "auto",
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
