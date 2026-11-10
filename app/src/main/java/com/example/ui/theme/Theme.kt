package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StripiColorScheme = lightColorScheme(
  primary = StripiPrimary,
  onPrimary = StripiOnPrimary,
  primaryContainer = StripiSubduedHover,
  onPrimaryContainer = StripiPrimaryDeep,
  secondary = StripiBrandDark900,
  onSecondary = StripiOnPrimary,
  secondaryContainer = StripiCanvasCream,
  onSecondaryContainer = StripiInk,
  tertiary = StripiRuby,
  onTertiary = StripiOnPrimary,
  background = StripiCanvas,
  onBackground = StripiInk,
  surface = StripiCanvas,
  onSurface = StripiInk,
  surfaceVariant = StripiCanvasSoft,
  onSurfaceVariant = StripiInkMute,
  outline = StripiHairline,
  outlineVariant = StripiHairlineInput,
  error = StripiRuby,
  onError = StripiOnPrimary
)

@Composable
fun MzansiCharadesTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = StripiColorScheme,
    typography = Typography,
    content = content
  )
}
