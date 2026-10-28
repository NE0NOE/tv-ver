package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.R

// Bundled local Google Fonts for offline reliability on Android 6 TV hardware
val WorkSansFontFamily = FontFamily(
    Font(R.font.work_sans, FontWeight.Normal),
    Font(R.font.work_sans, FontWeight.Medium),
    Font(R.font.work_sans, FontWeight.SemiBold),
    Font(R.font.work_sans, FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

// Strictly weighted >= 400 and >= 15sp per 10-Foot Experience Specification
val ApexTypography = Typography(
    // display-tv: Work Sans, 56sp, 700, 68sp lineHeight, -0.02em
    displayLarge = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 68.sp,
        letterSpacing = (-0.02).em
    ),
    // headline-xl: Work Sans, 40sp, 700, 48sp lineHeight, -0.01em
    headlineLarge = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.01).em
    ),
    // headline-lg: Work Sans, 32sp, 600, 40sp lineHeight
    headlineMedium = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    // headline-md: Work Sans, 26sp, 600, 34sp lineHeight
    headlineSmall = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    // Title styles for cards & rails
    titleLarge = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // body-xl: Inter, 22sp, 500, 32sp lineHeight
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 32.sp
    ),
    // body-lg: Inter, 18sp, 400, 26sp lineHeight
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    // body-md: Inter, 16sp, 400, 24sp lineHeight
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // label-tv-focus: Work Sans, 18sp, 700, 24sp lineHeight, 0.04em
    labelLarge = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.04.em
    ),
    // label-key-legend: Work Sans, 15sp, 600, 20sp lineHeight, 0.05em
    labelMedium = TextStyle(
        fontFamily = WorkSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.05.em
    ),
    // Minimum allowed size is 15sp (fine weights <400 and sizes <15sp forbidden)
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.03.em
    )
)
