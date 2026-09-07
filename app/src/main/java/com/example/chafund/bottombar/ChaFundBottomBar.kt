package com.example.chafund.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.chafund.navigation.Navigator
import com.example.chafund.navigation.TopLevelDestination
import com.example.chafund.ui.theme.AppColors

/**
 * Flat, bordered bottom navigation matching the app's design language: no elevation/shadow,
 * a 0.5dp top border, compact 11sp labels, and the blue fill-pill + blue text used across
 * metric cards and badges to mark the active destination.
 */
@Composable
fun ChaFundBottomBar(
    navController: NavController,
    navigator: Navigator,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 0.5.dp, color = AppColors.BorderLight)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TopLevelDestination.entries.forEach { dest ->
                    val selected = backStackEntry?.destination?.hasRoute(dest.route::class) == true
                    BottomBarItem(
                        dest = dest,
                        selected = selected,
                        onClick = { navigator.navigateToTopLevel(dest) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    dest: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = AppColors.BalanceTextLight
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val contentColor = if (selected) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) AppColors.BalanceFillLight else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = dest.icon,
                contentDescription = dest.label,
                modifier = Modifier.size(22.dp),
                tint = contentColor,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = dest.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.W500 else FontWeight.W400,
            color = contentColor,
        )
    }
}
