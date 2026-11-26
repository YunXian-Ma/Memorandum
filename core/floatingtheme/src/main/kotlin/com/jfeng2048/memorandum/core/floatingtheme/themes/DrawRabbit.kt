package com.jfeng2048.memorandum.core.floatingtheme.themes

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPalette
import com.jfeng2048.memorandum.core.floatingtheme.drawPetEyesAndMouth

internal fun DrawScope.drawRabbit(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    palette: FloatingPalette,
    happyEyes: Boolean,
) {
    // 长兔耳
    drawRoundRect(
        color = palette.primaryColor,
        topLeft = Offset(w * 0.24f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.09f),
    )
    drawRoundRect(
        color = palette.primaryColor,
        topLeft = Offset(w * 0.58f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.09f),
    )
    drawRoundRect(
        color = palette.accentColor,
        topLeft = Offset(w * 0.285f, h * 0.11f),
        size = Size(w * 0.09f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.045f),
    )
    drawRoundRect(
        color = palette.accentColor,
        topLeft = Offset(w * 0.625f, h * 0.11f),
        size = Size(w * 0.09f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.045f),
    )
    drawRoundRect(
        color = ink,
        topLeft = Offset(w * 0.24f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.09f),
        style = Stroke(stroke),
    )
    drawRoundRect(
        color = ink,
        topLeft = Offset(w * 0.58f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.09f),
        style = Stroke(stroke),
    )

    // 胖嘟嘟白脸
    drawOval(
        color = palette.primaryColor,
        topLeft = Offset(w * 0.15f, h * 0.34f),
        size = Size(w * 0.70f, h * 0.56f),
    )
    drawOval(
        color = ink,
        topLeft = Offset(w * 0.15f, h * 0.34f),
        size = Size(w * 0.70f, h * 0.56f),
        style = Stroke(stroke),
    )

    drawCircle(cheek, radius = w * 0.068f, center = Offset(w * 0.28f, h * 0.67f))
    drawCircle(cheek, radius = w * 0.068f, center = Offset(w * 0.72f, h * 0.67f))
    drawPetEyesAndMouth(w, h, stroke, ink, eyeY = 0.58f, noseY = 0.65f, happyEyes = happyEyes)
}
