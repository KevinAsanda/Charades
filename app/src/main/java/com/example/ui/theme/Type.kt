package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Open-source analogue to Sohne (`sohne-var`) per Stripi Design System:
 * Inter loaded at weight 300 (Light) and 400 (Regular) with `ss01` enabled globally,
 * negative tracking on display/heading tiers, and `tnum` on numeric/currency cells.
 */
val InterFontFamily = FontFamily(
  Font(R.font.inter, FontWeight.Light),
  Font(R.font.inter, FontWeight.Normal),
  Font(R.font.inter, FontWeight.Medium),
  Font(R.font.inter, FontWeight.SemiBold),
  Font(R.font.inter, FontWeight.Bold)
)

val HankenGroteskFontFamily = FontFamily(
  Font(R.font.hanken_grotesk_regular, FontWeight.Normal),
  Font(R.font.hanken_grotesk_medium, FontWeight.Medium),
  Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold),
  Font(R.font.hanken_grotesk_bold, FontWeight.Bold)
)

object StripiTypeTokens {
  // {typography.display-xxl}: 56px, weight 300, lh 1.03, ls -1.4px, ss01
  val displayXxl = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 56.sp,
    lineHeight = 57.7.sp,
    letterSpacing = (-1.4).sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.display-xl}: 48px, weight 300, lh 1.15, ls -0.96px, ss01
  val displayXl = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 48.sp,
    lineHeight = 55.2.sp,
    letterSpacing = (-0.96).sp,
    fontFeatureSettings = "ss01"
  )

  // Mobile stepped Hero display (36px per Responsive Behavior: "Mobile < 768px: display drops 56 -> 36px")
  val displayMobileHero = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 36.sp,
    lineHeight = 39.6.sp,
    letterSpacing = (-0.96).sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.display-lg}: 32px, weight 300, lh 1.1, ls -0.64px, ss01
  val displayLg = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 32.sp,
    lineHeight = 35.2.sp,
    letterSpacing = (-0.64).sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.display-md}: 26px, weight 300, lh 1.12, ls -0.26px, ss01
  val displayMd = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 26.sp,
    lineHeight = 29.1.sp,
    letterSpacing = (-0.26).sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.heading-lg}: 22px, weight 300, lh 1.1, ls -0.22px, ss01
  val headingLg = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 22.sp,
    lineHeight = 24.2.sp,
    letterSpacing = (-0.22).sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.heading-md}: 20px, weight 300, lh 1.4, ls -0.2px, ss01
  val headingMd = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.2).sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.heading-sm}: 18px, weight 300, lh 1.4, ls 0, ss01
  val headingSm = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 18.sp,
    lineHeight = 25.2.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.body-lg}: 16px, weight 300, lh 1.4, ls 0, ss01
  val bodyLg = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 16.sp,
    lineHeight = 22.4.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.body-md}: 15px, weight 300, lh 1.4, ls 0, ss01
  val bodyMd = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.body-tabular}: 14px, weight 300, lh 1.4, ls -0.42px, tnum + ss01
  val bodyTabular = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 14.sp,
    lineHeight = 19.6.sp,
    letterSpacing = (-0.42).sp,
    fontFeatureSettings = "tnum, ss01"
  )

  // {typography.button-md}: 16px, weight 400, lh 1.0, ls 0, ss01
  val buttonMd = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.button-sm}: 14px, weight 400, lh 1.0, ls 0, ss01
  val buttonSm = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.caption}: 13px, weight 400, lh 1.4, ls -0.39px, tnum + ss01
  val caption = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.2.sp,
    letterSpacing = (-0.39).sp,
    fontFeatureSettings = "tnum, ss01"
  )

  // {typography.micro}: 11px, weight 300, lh 1.4, ls 0, ss01
  val micro = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 11.sp,
    lineHeight = 15.4.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )

  // {typography.micro-cap}: 10px, weight 400, lh 1.15, ls 0.1px, ss01
  val microCap = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    lineHeight = 11.5.sp,
    letterSpacing = 0.1.sp,
    fontFeatureSettings = "ss01"
  )
}

// Tabular numeral style for ZAR currency, coin counts, scores, and countdown timers
val StripiTabularStyle = StripiTypeTokens.bodyTabular

val Typography = Typography(
  displayLarge = StripiTypeTokens.displayMobileHero, // 36sp stepped mobile hero (weight 300, -0.96sp)
  displayMedium = StripiTypeTokens.displayLg,        // 32sp (weight 300, -0.64sp)
  displaySmall = StripiTypeTokens.displayMd,         // 26sp (weight 300, -0.26sp)
  headlineLarge = StripiTypeTokens.displayLg,        // 32sp (weight 300, -0.64sp)
  headlineMedium = StripiTypeTokens.displayMd,       // 26sp (weight 300, -0.26sp)
  headlineSmall = StripiTypeTokens.headingLg,        // 22sp (weight 300, -0.22sp)
  titleLarge = StripiTypeTokens.headingMd,           // 20sp (weight 300, -0.20sp)
  titleMedium = StripiTypeTokens.headingSm,          // 18sp (weight 300, 0sp)
  titleSmall = StripiTypeTokens.bodyLg,              // 16sp (weight 300, 0sp)
  bodyLarge = StripiTypeTokens.bodyLg,               // 16sp (weight 300, 1.4 lh, ss01)
  bodyMedium = StripiTypeTokens.bodyMd,              // 15sp (weight 300, 1.4 lh, ss01)
  bodySmall = StripiTypeTokens.caption,              // 13sp (weight 400, -0.39sp, tnum)
  labelLarge = StripiTypeTokens.buttonMd,            // 16sp (weight 400, 1.0 lh)
  labelMedium = StripiTypeTokens.buttonSm,           // 14sp (weight 400, 1.0 lh)
  labelSmall = StripiTypeTokens.micro                // 11sp (weight 300, 1.4 lh)
)
