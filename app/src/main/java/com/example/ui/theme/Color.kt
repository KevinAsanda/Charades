package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Stripi-Inspired Design System Tokens (Exact Hex Specification)
// Brand & Accent
val StripiPrimary = Color(0xFF533AFD)        // {colors.primary} Electric Indigo (Signature CTA pill & links)
val StripiPrimaryDeep = Color(0xFF4434D4)    // {colors.primary-deep} Deeper indigo for tags & gradient mid-stops
val StripiPrimaryPress = Color(0xFF2E2B8C)   // {colors.primary-press} Pressed-state lift of primary CTA
val StripiPrimarySoft = Color(0xFF665EFD)    // {colors.primary-soft} Lighter indigo for dark-UI accents & charts
val StripiSubduedHover = Color(0xFFB9B9F9)   // {colors.primary-bg-subdued-hover} Pale indigo soft tag fill
val StripiBrandDark900 = Color(0xFF1C1E54)   // {colors.brand-dark-900} Deep navy for featured tier & dark chrome

// Text & Ink Hierarchy
val StripiInk = Color(0xFF0D253D)            // {colors.ink} Universal deep-navy body text (never pure black)
val StripiInkSecondary = Color(0xFF273951)   // {colors.ink-secondary} Secondary text on white
val StripiInkMute = Color(0xFF64748D)        // {colors.ink-mute} Helper text, captions, table labels
val StripiInkMute2 = Color(0xFF61718A)       // {colors.ink-mute-2} Navigation & axis muted text
val StripiOnPrimary = Color(0xFFFFFFFF)      // {colors.on-primary} Crisp white text on indigo & dark navy

// Surface & Canvas
val StripiCanvas = Color(0xFFFFFFFF)         // {colors.canvas} Default page background
val StripiCanvasSoft = Color(0xFFF6F9FC)     // {colors.canvas-soft} Cool-tinted off-white feature band
val StripiCanvasCream = Color(0xFFF5E9D4)    // {colors.canvas-cream} Warm cream chromatic interlude band
val StripiHairline = Color(0xFFE3E8EE)       // {colors.hairline} 1dp borders on cards & tables
val StripiHairlineInput = Color(0xFFA8C3DE)  // {colors.hairline-input} Cooler hairline on form inputs

// Gradient Mesh & Elevation Accents (Never used as button fills)
val StripiRuby = Color(0xFFEA2261)           // {colors.ruby} Gradient stop & chart/alert highlight
val StripiMagenta = Color(0xFFF96BEE)        // {colors.magenta} Bright pink stop in gradient meshes
val StripiLemon = Color(0xFF9B6829)          // {colors.lemon} Warm sherbet stop in gradient backdrops
val StripiShadowBlue = Color(0xFF003770)     // {colors.shadow-blue} Tinted ambient elevation shadow base

// Locked to Stripi indigo palette to enforce single-accent Color Consistency Lock
val StripiEmerald = StripiPrimaryDeep
