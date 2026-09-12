package com.jasongrech.carlocator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * No custom font is bundled — system Default carries the display/body roles, kept
 * reliable across builds. Distinctiveness comes from weight, tracking, and case
 * instead: a heavy tight-set display face, tracked-out uppercase labels for section
 * eyebrows, and — since GPS coordinates are raw numeric data, not prose — Monospace
 * for anything that shows a lat/lng.
 */

val Mono = FontFamily.Monospace

val AppTypography = Typography(
    // "CAR LOCATOR" top title
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        letterSpacing = (-0.4).sp
    ),
    // Row titles: device name, spot address
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
    // Section eyebrows: "PARKING DETECTION", "HOME ZONE"
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp
    ),
    // Pill button labels
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp
    ),
    // Body / helper copy
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Captions / timestamps
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
