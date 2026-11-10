package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StripiBrandDark900
import com.example.ui.theme.StripiCanvas
import com.example.ui.theme.StripiCanvasCream
import com.example.ui.theme.StripiCanvasSoft
import com.example.ui.theme.StripiHairline
import com.example.ui.theme.StripiHairlineInput
import com.example.ui.theme.StripiInk
import com.example.ui.theme.StripiInkMute
import com.example.ui.theme.StripiInkSecondary
import com.example.ui.theme.StripiLemon
import com.example.ui.theme.StripiMagenta
import com.example.ui.theme.StripiOnPrimary
import com.example.ui.theme.StripiPrimary
import com.example.ui.theme.StripiPrimaryDeep
import com.example.ui.theme.StripiPrimaryPress
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiShadowBlue
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTabularStyle
import com.example.ui.theme.StripiTypeTokens

/**
 * Stripi Spacing Scale Tokens (8px base unit with sub-tokens)
 */
object StripiSpacing {
  val xxs = 2.dp
  val xs = 4.dp
  val sm = 8.dp
  val md = 12.dp
  val lg = 16.dp
  val xl = 24.dp
  val xxl = 32.dp
  val huge = 64.dp
}

/**
 * Stripi Border Radius Scale Tokens
 */
object StripiRounded {
  val xs = RoundedCornerShape(4.dp)
  val sm = RoundedCornerShape(6.dp)
  val md = RoundedCornerShape(8.dp)
  val lg = RoundedCornerShape(12.dp)
  val xl = RoundedCornerShape(16.dp)
  val pill = RoundedCornerShape(percent = 50)
}

/**
 * Maps a Deck ID to a standardized vector glyph (no raw emojis in UI per Section 3.D).
 */
fun deckIconForId(deckId: String): ImageVector {
  return when (deckId) {
    "core_kwaito" -> Icons.Default.MusicNote
    "core_2000s_tv" -> Icons.Default.Tv
    "core_slang" -> Icons.Default.Forum
    "core_tuckshop" -> Icons.Default.Fastfood
    "core_sports" -> Icons.Default.SportsSoccer
    "exp_pantsula" -> Icons.Default.DirectionsRun
    "exp_school_days" -> Icons.Default.School
    "exp_road_trip" -> Icons.Default.DirectionsCar
    "exp_memes" -> Icons.Default.Celebration
    else -> Icons.Default.AutoAwesome
  }
}

/**
 * Standardized vector icon badge for decks (`{rounded.md}` 8dp).
 */
@Composable
fun StripiDeckIconBadge(
  deckId: String,
  isSelected: Boolean = false,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(44.dp)
      .clip(StripiRounded.md)
      .background(
        if (isSelected) StripiSubduedHover.copy(alpha = 0.45f)
        else StripiCanvasSoft
      )
      .border(1.dp, if (isSelected) StripiPrimary else StripiHairline, StripiRounded.md),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = deckIconForId(deckId),
      contentDescription = null,
      tint = if (isSelected) StripiPrimary else StripiInk,
      modifier = Modifier.size(20.dp)
    )
  }
}

/**
 * Stripi Signature Atmospheric Gradient Mesh Backdrop.
 * Pastel cream -> sherbet lemon -> lavender -> electric indigo -> ruby -> magenta
 * washed organically across the upper third of the screen.
 */
@Composable
fun StripiGradientMeshHero(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(StripiCanvas)
  ) {
    Canvas(
      modifier = Modifier.matchParentSize()
    ) {
      val w = size.width
      val h = size.height

      // Horizontal pastel cream-to-lavender base band
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            StripiCanvasCream.copy(alpha = 0.78f),
            StripiSubduedHover.copy(alpha = 0.34f),
            StripiCanvas
          ),
          startY = 0f,
          endY = h
        )
      )

      // Top-left warm sherbet / lemon organic blob
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            StripiLemon.copy(alpha = 0.22f),
            StripiCanvasCream.copy(alpha = 0.45f),
            Color.Transparent
          ),
          center = Offset(w * 0.14f, h * 0.14f),
          radius = w * 0.55f
        ),
        center = Offset(w * 0.14f, h * 0.14f),
        radius = w * 0.55f
      )

      // Top-right Electric Indigo & Primary Deep mesh anchor
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            StripiPrimary.copy(alpha = 0.36f),
            StripiPrimaryDeep.copy(alpha = 0.20f),
            StripiSubduedHover.copy(alpha = 0.24f),
            Color.Transparent
          ),
          center = Offset(w * 0.86f, h * 0.16f),
          radius = w * 0.68f
        ),
        center = Offset(w * 0.86f, h * 0.16f),
        radius = w * 0.68f
      )

      // Mid-right Ruby & Magenta atmospheric flare
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            StripiRuby.copy(alpha = 0.26f),
            StripiMagenta.copy(alpha = 0.18f),
            Color.Transparent
          ),
          center = Offset(w * 0.60f, h * 0.30f),
          radius = w * 0.56f
        ),
        center = Offset(w * 0.60f, h * 0.30f),
        radius = w * 0.56f
      )

      // Diagonal architectural white canvas cut at bottom of hero
      val slicePath = Path().apply {
        moveTo(0f, h * 0.88f)
        lineTo(w, h * 0.76f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
      }
      drawPath(
        path = slicePath,
        color = StripiCanvas
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding),
      content = content
    )
  }
}

/**
 * `button-primary-pill` & `button-primary-pill-pressed`:
 * Background `{colors.primary}` (#533AFD), pressed `{colors.primary-press}` (#2E2B8C),
 * text `{colors.on-primary}` (#FFFFFF), type `{typography.button-md}` (16sp, weight 400),
 * rounded `{rounded.pill}` (9999px), single-line label, tactile scale feedback.
 */
@Composable
fun StripiPrimaryPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val containerColor by animateColorAsState(
    targetValue = when {
      !enabled -> StripiHairline
      isPressed -> StripiPrimaryPress
      else -> StripiPrimary
    },
    label = "PrimaryPillBg"
  )
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.98f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
    label = "PrimaryPillScale"
  )

  Button(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    modifier = modifier
      .heightIn(min = 44.dp)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
    shape = StripiRounded.pill,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = StripiOnPrimary,
      disabledContainerColor = StripiHairline,
      disabledContentColor = StripiInkMute
    ),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = StripiTypeTokens.buttonMd.copy(
          color = if (enabled) StripiOnPrimary else StripiInkMute
        )
      )
      if (trailingContent != null) {
        Spacer(modifier = Modifier.width(6.dp))
        trailingContent()
      }
    }
  }
}

/**
 * `button-secondary`:
 * Background `{colors.canvas}` (#FFFFFF), text `{colors.primary}` (#533AFD),
 * 1dp `{colors.primary}` border, `{typography.button-md}`, `{rounded.pill}`.
 */
@Composable
fun StripiSecondaryPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.98f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
    label = "SecondaryPillScale"
  )

  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    modifier = modifier
      .heightIn(min = 44.dp)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
    shape = StripiRounded.pill,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    border = BorderStroke(1.dp, if (enabled) StripiPrimary else StripiHairline),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = StripiCanvas,
      contentColor = if (isPressed) StripiPrimaryPress else StripiPrimary,
      disabledContainerColor = StripiCanvasSoft,
      disabledContentColor = StripiInkMute
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = StripiTypeTokens.buttonMd.copy(
          color = when {
            !enabled -> StripiInkMute
            isPressed -> StripiPrimaryPress
            else -> StripiPrimary
          }
        )
      )
    }
  }
}

/**
 * `button-on-dark`:
 * Background `{colors.brand-dark-900}` (#1C1E54), text `{colors.on-primary}` (#FFFFFF),
 * `{typography.button-md}`, `{rounded.pill}`.
 */
@Composable
fun StripiDarkPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.98f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
    label = "DarkPillScale"
  )

  Button(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    modifier = modifier
      .heightIn(min = 44.dp)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
    shape = StripiRounded.pill,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = StripiBrandDark900,
      contentColor = StripiOnPrimary,
      disabledContainerColor = StripiHairline,
      disabledContentColor = StripiInkMute
    ),
    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = StripiTypeTokens.buttonMd.copy(
          color = if (enabled) StripiOnPrimary else StripiInkMute
        )
      )
    }
  }
}

/**
 * `card-feature-light`:
 * Background `{colors.canvas}` (#FFFFFF), text `{colors.ink}` (#0D253D),
 * rounded `{rounded.lg}` (12dp), 1dp `{colors.hairline}` border, Level 1 tinted shadow.
 */
@Composable
fun StripiFeatureCardLight(
  modifier: Modifier = Modifier,
  borderColor: Color = StripiHairline,
  borderWidth: Dp = 1.dp,
  backgroundColor: Color = StripiCanvas,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(24.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val baseModifier = modifier
    .shadow(
      elevation = 2.dp,
      shape = StripiRounded.lg,
      ambientColor = StripiShadowBlue.copy(alpha = 0.08f),
      spotColor = StripiShadowBlue.copy(alpha = 0.08f)
    )
    .clip(StripiRounded.lg)
    .background(backgroundColor)
    .border(borderWidth, borderColor, StripiRounded.lg)

  val cardModifier = if (onClick != null) {
    baseModifier
      .clickable { onClick() }
      .padding(contentPadding)
  } else {
    baseModifier.padding(contentPadding)
  }

  Column(
    modifier = cardModifier,
    content = content
  )
}

/**
 * `card-pricing-featured`:
 * Background `{colors.brand-dark-900}` (#1C1E54), text `{colors.on-primary}` (#FFFFFF),
 * rounded `{rounded.lg}` (12dp), Level 2 shadow.
 */
@Composable
fun StripiFeaturedDarkCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(24.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val baseModifier = modifier
    .shadow(
      elevation = 8.dp,
      shape = StripiRounded.lg,
      ambientColor = StripiShadowBlue.copy(alpha = 0.16f),
      spotColor = StripiShadowBlue.copy(alpha = 0.12f)
    )
    .clip(StripiRounded.lg)
    .background(StripiBrandDark900)

  val cardModifier = if (onClick != null) {
    baseModifier
      .clickable { onClick() }
      .padding(contentPadding)
  } else {
    baseModifier.padding(contentPadding)
  }

  Column(
    modifier = cardModifier,
    content = content
  )
}

/**
 * `card-cream-band`:
 * Background `{colors.canvas-cream}` (#F5E9D4), text `{colors.ink}` (#0D253D),
 * rounded `{rounded.lg}` (12dp). Warm chromatic interlude between blue/white sections.
 */
@Composable
fun StripiCreamCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(24.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val baseModifier = modifier
    .clip(StripiRounded.lg)
    .background(StripiCanvasCream)
    .border(1.dp, StripiLemon.copy(alpha = 0.22f), StripiRounded.lg)

  val cardModifier = if (onClick != null) {
    baseModifier
      .clickable { onClick() }
      .padding(contentPadding)
  } else {
    baseModifier.padding(contentPadding)
  }

  Column(
    modifier = cardModifier,
    content = content
  )
}

/**
 * `card-dashboard-mockup`:
 * Background `{colors.canvas}` (#FFFFFF), text `{colors.ink}` (#0D253D),
 * typography `{typography.body-tabular}` (`tnum`), rounded `{rounded.lg}` (12dp),
 * padding 24dp, Level 2 tinted shadow (`rgba(0,55,112,0.08)`).
 */
@Composable
fun StripiDashboardMockupCard(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(24.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = modifier
      .shadow(
        elevation = 10.dp,
        shape = StripiRounded.lg,
        ambientColor = StripiShadowBlue.copy(alpha = 0.14f),
        spotColor = StripiShadowBlue.copy(alpha = 0.10f)
      )
      .clip(StripiRounded.lg)
      .background(StripiCanvas)
      .border(1.dp, StripiHairline, StripiRounded.lg)
      .padding(contentPadding),
    content = content
  )
}

/**
 * `pill-tag-soft`:
 * Background `{colors.primary-bg-subdued-hover}` (#B9B9F9), text `{colors.primary-deep}` (#4434D4),
 * typography `{typography.micro-cap}` (10sp, weight 400, lh 1.15, ls 0.1sp),
 * rounded `{rounded.pill}`, padding `4px 8px`.
 */
@Composable
fun StripiSoftPillTag(
  text: String,
  modifier: Modifier = Modifier,
  containerColor: Color = StripiSubduedHover,
  contentColor: Color = StripiPrimaryDeep
) {
  Surface(
    modifier = modifier,
    shape = StripiRounded.pill,
    color = containerColor
  ) {
    Text(
      text = text.uppercase(),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      style = StripiTypeTokens.microCap.copy(
        color = contentColor
      )
    )
  }
}

/**
 * `text-input` & `text-input-focused`:
 * Background `{colors.canvas}` (#FFFFFF), text `{colors.ink}` (#0D253D),
 * typography `{typography.body-md}` (15sp, weight 300), rounded `{rounded.sm}` (6dp),
 * unfocused border 1dp `{colors.hairline-input}` (#A8C3DE),
 * focused border `{colors.primary}` (#533AFD).
 * Label always ABOVE input (`gap-2` / 6dp), optional helper/error below input.
 */
@Composable
fun StripiTextInput(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  trailingIcon: (@Composable () -> Unit)? = null,
  helperText: String? = null,
  errorText: String? = null
) {
  Column(modifier = modifier) {
    if (label.isNotBlank()) {
      Text(
        text = label,
        style = StripiTypeTokens.caption.copy(
          color = StripiInkSecondary
        ),
        modifier = Modifier.padding(bottom = 6.dp)
      )
    }
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = {
        Text(
          text = placeholder,
          style = StripiTypeTokens.bodyMd.copy(color = StripiInkMute)
        )
      },
      trailingIcon = trailingIcon,
      keyboardOptions = keyboardOptions,
      singleLine = true,
      textStyle = StripiTypeTokens.bodyMd.copy(color = StripiInk),
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 44.dp),
      shape = StripiRounded.sm,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = if (errorText != null) StripiRuby else StripiPrimary,
        unfocusedBorderColor = if (errorText != null) StripiRuby else StripiHairlineInput,
        focusedContainerColor = StripiCanvas,
        unfocusedContainerColor = StripiCanvas,
        focusedTextColor = StripiInk,
        unfocusedTextColor = StripiInk,
        cursorColor = StripiPrimary
      )
    )
    if (!errorText.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = errorText,
        style = StripiTypeTokens.micro.copy(color = StripiRuby)
      )
    } else if (!helperText.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = helperText,
        style = StripiTypeTokens.micro.copy(color = StripiInkMute)
      )
    }
  }
}

/**
 * Tabular numeral text helper (`{typography.body-tabular}`, `tnum`)
 * for ZAR currency (`R29.99`), coin balances, scores, and countdown timers.
 */
@Composable
fun StripiTabularText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = StripiInk,
  fontSize: Int = 14,
  fontWeight: FontWeight = FontWeight.Light
) {
  Text(
    text = text,
    modifier = modifier,
    style = StripiTabularStyle.copy(
      color = color,
      fontSize = fontSize.sp,
      fontWeight = fontWeight,
      lineHeight = (fontSize * 1.25).sp
    )
  )
}
