package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AeldrinColorScheme = darkColorScheme(
    primary = GildedAmber,
    onPrimary = VoidObsidian,
    primaryContainer = MutedBronze,
    onPrimaryContainer = TextPrimary,
    secondary = AstralViolet,
    onSecondary = VoidObsidian,
    secondaryContainer = VoidPurple,
    onSecondaryContainer = SoftLavender,
    tertiary = SoulEmerald,
    onTertiary = VoidObsidian,
    error = CrimsonBlood,
    onError = TextPrimary,
    background = VoidObsidian,
    onBackground = TextPrimary,
    surface = DeepSlate,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedCard,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

val AeldrinShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun AeldrinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AeldrinColorScheme,
        typography = Typography,
        shapes = AeldrinShapes,
        content = content
    )
}
