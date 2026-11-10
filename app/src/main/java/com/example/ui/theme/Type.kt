package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val InterFontFamily = FontFamily(
  Font(R.font.inter, FontWeight.Light),
  Font(R.font.inter, FontWeight.Normal),
  Font(R.font.inter, FontWeight.Medium),
  Font(R.font.inter, FontWeight.SemiBold),
  Font(R.font.inter, FontWeight.Bold)
)

// Tabular numeral style for ZAR currency, coin counts, scores, and countdown timers
val StripiTabularStyle = TextStyle(
  fontFamily = InterFontFamily,
  fontWeight = FontWeight.Normal,
  letterSpacing = (-0.42).sp,
  fontFeatureSettings = "tnum, ss01"
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 44.sp,
    lineHeight = 47.sp,
    letterSpacing = (-1.32).sp,
    fontFeatureSettings = "ss01"
  ),
  displayMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.9).sp,
    fontFeatureSettings = "ss01"
  ),
  displaySmall = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 30.sp,
    lineHeight = 35.sp,
    letterSpacing = (-0.6).sp,
    fontFeatureSettings = "ss01"
  ),
  headlineLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 28.sp,
    lineHeight = 33.sp,
    letterSpacing = (-0.56).sp,
    fontFeatureSettings = "ss01"
  ),
  headlineMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 24.sp,
    lineHeight = 29.sp,
    letterSpacing = (-0.42).sp,
    fontFeatureSettings = "ss01"
  ),
  headlineSmall = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.26).sp,
    fontFeatureSettings = "ss01"
  ),
  titleLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 25.sp,
    letterSpacing = (-0.22).sp,
    fontFeatureSettings = "ss01"
  ),
  titleMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.16).sp,
    fontFeatureSettings = "ss01"
  ),
  titleSmall = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.08).sp,
    fontFeatureSettings = "ss01"
  ),
  bodyLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  ),
  bodyMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  ),
  bodySmall = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  ),
  labelLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  ),
  labelMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  ),
  labelSmall = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "ss01"
  )
)
