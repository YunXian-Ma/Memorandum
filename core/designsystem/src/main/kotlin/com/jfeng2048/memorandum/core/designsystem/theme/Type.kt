package com.jfeng2048.memorandum.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 字体气质：
 * - 人名用衬线体（系统 Serif 对应原型里的 Noto Serif SC）
 * - 正文与控件全部无衬线
 */
val PersonNameTextStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = 29.sp,
    lineHeight = 36.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.5.sp,
)

/** 日期、数字这类需要一点「刻度感」的文字 */
val NumericTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontSize = 13.5.sp,
    lineHeight = 20.sp,
    fontWeight = FontWeight.SemiBold,
)

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontSize = 33.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontSize = 27.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = TextStyle(
        fontSize = 21.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = TextStyle(
        fontSize = 16.5.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.5.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
)
