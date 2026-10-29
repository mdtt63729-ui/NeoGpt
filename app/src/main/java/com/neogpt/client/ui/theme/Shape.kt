package com.neogpt.client.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NeoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val ComposerShape = RoundedCornerShape(28.dp)
val UserBubbleShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 6.dp)
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val PillShape = RoundedCornerShape(50)
