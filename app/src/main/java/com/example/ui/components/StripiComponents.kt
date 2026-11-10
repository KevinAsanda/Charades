package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.StripiRuby
import com.example.ui.theme.StripiSubduedHover
import com.example.ui.theme.StripiTabularStyle

/**
 * Stripi Signature Atmospheric Gradient Mesh Backdrop.
 * Occupies the upper hero band of a screen and blends pastel cream, warm sherbet amber,
 * soft lavender, electric indigo, ruby crimson, and magenta into a crisp white canvas.
 */
@Composable
fun StripiGradientMeshHero(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(StripiCanvas)
  ) {
    Canvas(
      modifier = Modifier
        .matchParentSize()
    ) {
      val w = size.width
      val h = size.height

      // Base soft cream-to-white vertical wash
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            StripiCanvasCream.copy(alpha = 0.72f),
            StripiSubduedHover.copy(alpha = 0.30f),
            StripiCanvas
          ),
          startY = 0f,
          endY = h
        )
      )

      // Top-right Electric Indigo & Lavender mesh orb
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            StripiPrimary.copy(alpha = 0.34f),
            StripiSubduedHover.copy(alpha = 0.22f),
            Color.Transparent
          ),
          center = Offset(w * 0.88f, h * 0.18f),
          radius = w * 0.65f
        ),
        center = Offset(w * 0.88f, h * 0.18f),
        radius = w * 0.65f
      )

      // Center-right Ruby & Magenta atmospheric flare
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            StripiRuby.copy(alpha = 0.24f),
            StripiMagenta.copy(alpha = 0.16f),
            Color.Transparent
          ),
          center = Offset(w * 0.62f, h * 0.32f),
          radius = w * 0.55f
        ),
        center = Offset(w * 0.62f, h * 0.32f),
        radius = w * 0.55f
      )

      // Top-left Warm Sherbet / Lemon glow
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            StripiLemon.copy(alpha = 0.20f),
            StripiCanvasCream.copy(alpha = 0.40f),
            Color.Transparent
          ),
          center = Offset(w * 0.15f, h * 0.12f),
          radius = w * 0.50f
        ),
        center = Offset(w * 0.15f, h * 0.12f),
        radius = w * 0.50f
      )

      // Diagonal architectural slice at bottom of hero
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
 * Primary CTA Pill Button: #533AFD background, #FFFFFF text, 9999dp pill radius.
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
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.heightIn(min = 48.dp),
    shape = RoundedCornerShape(percent = 50),
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = StripiPrimary,
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
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Medium,
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
 * Secondary / Ghost Pill Button: #FFFFFF background, 1dp #E3E8EE border, #533AFD text.
 */
@Composable
fun StripiSecondaryPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null
) {
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.heightIn(min = 48.dp),
    shape = RoundedCornerShape(percent = 50),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    border = BorderStroke(1.dp, StripiHairline),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = StripiCanvas,
      contentColor = StripiPrimary,
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
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Medium,
          color = if (enabled) StripiPrimary else StripiInkMute
        )
      )
    }
  }
}

/**
 * Dark Navy Pill Button: #1C1E54 background, #FFFFFF text.
 */
@Composable
fun StripiDarkPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: (@Composable () -> Unit)? = null
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.heightIn(min = 48.dp),
    shape = RoundedCornerShape(percent = 50),
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
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
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.Medium,
          color = if (enabled) StripiOnPrimary else StripiInkMute
        )
      )
    }
  }
}

/**
 * Card - Feature Light (`card-feature-light`):
 * Background #FFFFFF, border 1dp solid #E3E8EE, radius 12dp.
 */
@Composable
fun StripiFeatureCardLight(
  modifier: Modifier = Modifier,
  borderColor: Color = StripiHairline,
  borderWidth: Dp = 1.dp,
  backgroundColor: Color = StripiCanvas,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(20.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .clip(RoundedCornerShape(12.dp))
      .background(backgroundColor)
      .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(contentPadding)
  } else {
    modifier
      .clip(RoundedCornerShape(12.dp))
      .background(backgroundColor)
      .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
      .padding(contentPadding)
  }

  Column(
    modifier = cardModifier,
    content = content
  )
}

/**
 * Card - Featured Dark (`card-featured-dark`):
 * Background #1C1E54, text #FFFFFF, radius 12dp.
 */
@Composable
fun StripiFeaturedDarkCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(20.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .clip(RoundedCornerShape(12.dp))
      .background(StripiBrandDark900)
      .clickable { onClick() }
      .padding(contentPadding)
  } else {
    modifier
      .clip(RoundedCornerShape(12.dp))
      .background(StripiBrandDark900)
      .padding(contentPadding)
  }

  Column(
    modifier = cardModifier,
    content = content
  )
}

/**
 * Card - Editorial Cream (`card-editorial-cream`):
 * Background #F5E9D4, text #0D253D, radius 12dp.
 */
@Composable
fun StripiCreamCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(20.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .clip(RoundedCornerShape(12.dp))
      .background(StripiCanvasCream)
      .border(1.dp, StripiLemon.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(contentPadding)
  } else {
    modifier
      .clip(RoundedCornerShape(12.dp))
      .background(StripiCanvasCream)
      .border(1.dp, StripiLemon.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
      .padding(contentPadding)
  }

  Column(
    modifier = cardModifier,
    content = content
  )
}

/**
 * Soft Pill Tag (`#B9B9F9` fill, `#4434D4` text, 9999dp pill radius).
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
    shape = RoundedCornerShape(percent = 50),
    color = containerColor
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        color = contentColor
      )
    )
  }
}

/**
 * Stripi Form Input (`input-text`):
 * Background #FFFFFF, radius 6dp, border 1dp solid #A8C3DE, focus #533AFD, label above input.
 */
@Composable
fun StripiTextInput(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  placeholder: String,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  trailingIcon: (@Composable () -> Unit)? = null
) {
  Column(modifier = modifier) {
    if (label.isNotBlank()) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Medium,
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
          style = MaterialTheme.typography.bodyMedium.copy(color = StripiInkMute)
        )
      },
      trailingIcon = trailingIcon,
      keyboardOptions = keyboardOptions,
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(6.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = StripiPrimary,
        unfocusedBorderColor = StripiHairlineInput,
        focusedContainerColor = StripiCanvas,
        unfocusedContainerColor = StripiCanvas,
        focusedTextColor = StripiInk,
        unfocusedTextColor = StripiInk,
        cursorColor = StripiPrimary
      )
    )
  }
}

/**
 * Tabular numeral text helper for prices (R29.99), coin balances, scores, and timers.
 */
@Composable
fun StripiTabularText(
  text: String,
  modifier: Modifier = Modifier,
  color: Color = StripiInk,
  fontSize: Int = 18,
  fontWeight: FontWeight = FontWeight.Normal
) {
  Text(
    text = text,
    modifier = modifier,
    style = StripiTabularStyle.copy(
      color = color,
      fontSize = fontSize.sp,
      fontWeight = fontWeight
    )
  )
}
