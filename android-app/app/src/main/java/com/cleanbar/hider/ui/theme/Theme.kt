package com.cleanbar.hider.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CleanBarDarkColorScheme = darkColorScheme(
    primary = CleanBarBlueText,
    onPrimary = CleanBarDarkBg,
    secondary = CleanBarTextSecondary,
    background = CleanBarDarkBg,
    surface = CleanBarDarkSurface,
    onBackground = CleanBarTextPrimary,
    onSurface = CleanBarTextPrimary,
    outline = CleanBarDarkBorder
)

@Composable
fun CleanBarTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CleanBarDarkColorScheme,
        content = content
    )
}
