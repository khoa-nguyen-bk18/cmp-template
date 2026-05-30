package com.devindie.cmptemplate.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing

@Composable
internal fun MainTopBar(
    storeName: String,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAppSpacing.current
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, colorScheme.outlineVariant),
        ) {
            // Placeholder until store logo asset is wired (Stitch HTML uses remote logo URL).
        }
        Spacer(modifier = Modifier.width(spacing.spaceSm))
        Text(
            text = storeName,
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onCartClick,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Shopping cart" },
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = colorScheme.primary,
            )
        }
    }
}
