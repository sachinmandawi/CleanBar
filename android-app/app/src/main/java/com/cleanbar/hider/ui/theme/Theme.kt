package com.cleanbar.hider.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NotionDarkColorScheme = darkColorScheme(
    primary = NotionBlueText,
    onPrimary = NotionDarkBg,
    secondary = NotionTextSecondary,
    background = NotionDarkBg,
    surface = NotionDarkSurface,
    surfaceVariant = NotionDarkCard,
    onBackground = NotionTextPrimary,
    onSurface = NotionTextPrimary,
    outline = NotionDarkBorder
)

@Composable
fun CleanBarTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NotionDarkColorScheme,
        content = content
    )
}
