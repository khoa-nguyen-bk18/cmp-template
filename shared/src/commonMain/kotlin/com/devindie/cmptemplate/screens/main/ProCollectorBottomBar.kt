package com.devindie.cmptemplate.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing

@Composable
fun ProCollectorBottomBar(
    selectedDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme
    val topShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = topShape,
                spotColor = colorScheme.onSurface.copy(alpha = 0.05f),
            ),
        shape = topShape,
        color = colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.spaceMd, vertical = spacing.spaceSm),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainDestination.entries.forEach { destination ->
                BottomNavItem(
                    label = destination.label,
                    icon = if (destination == selectedDestination) {
                        destination.selectedIcon
                    } else {
                        destination.unselectedIcon
                    },
                    selected = destination == selectedDestination,
                    onClick = { onDestinationSelected(destination) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val itemShape = RoundedCornerShape(8.dp)

    val backgroundColor = if (selected) {
        colorScheme.secondaryContainer
    } else {
        colorScheme.surfaceContainerLowest
    }
    val contentColor = if (selected) {
        colorScheme.onSecondaryContainer
    } else {
        colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(itemShape)
            .background(backgroundColor, itemShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                contentDescription = label
            }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
