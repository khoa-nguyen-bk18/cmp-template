package com.devindie.cmptemplate.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Stitch roundness: ROUND_EIGHT base with sm=4, default=8, md=12, lg=16, xl=24. */
internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

internal val PillShape = RoundedCornerShape(9999.dp)
