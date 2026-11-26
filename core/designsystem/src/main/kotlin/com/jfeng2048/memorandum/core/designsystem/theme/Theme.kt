package com.jfeng2048.memorandum.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 第一版只提供浅色主题：原型是「白纸 + 发丝线」的纸面感。
 * 深色模式属于 PRD 中的后续优化项，接入时只需要在这里补 darkColorScheme，
 * 并把页面中的固定色替换为 MaterialTheme.colorScheme 的语义色。
 */
private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = SurfaceMuted,
    onPrimaryContainer = Ink,
    secondary = BrandBlue,
    onSecondary = Color.White,
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = Hairline,
    outlineVariant = DividerSoft,
    error = DangerRed,
    onError = Color.White,
)

@Composable
fun MemorandumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
