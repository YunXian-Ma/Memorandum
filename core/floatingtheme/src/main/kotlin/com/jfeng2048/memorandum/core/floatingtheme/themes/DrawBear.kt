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

internal fun DrawScope.drawBear(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    palette: FloatingPalette,
    happyEyes: Boolean,
) {
    // 圆熊耳
    drawCircle(palette.primaryColor, radius = w * 0.14f, center = Offset(w * 0.24f, h * 0.28f))
    drawCircle(palette.primaryColor, radius = w * 0.14f, center = Offset(w * 0.76f, h * 0.28f))
    drawCircle(palette.accentColor, radius = w * 0.07f, center = Offset(w * 0.24f, h * 0.28f))
    drawCircle(palette.accentColor, radius = w * 0.07f, center = Offset(w * 0.76f, h * 0.28f))
    drawCircle(ink, radius = w * 0.14f, center = Offset(w * 0.24f, h * 0.28f), style = Stroke(stroke))
    drawCircle(ink, radius = w * 0.14f, center = Offset(w * 0.76f, h * 0.28f), style = Stroke(stroke))

    // 圆脸与浅色吻部
    drawOval(
        color = palette.primaryColor,
        topLeft = Offset(w * 0.14f, h * 0.24f),
        size = Size(w * 0.72f, h * 0.64f),
    )
    drawOval(
        color = palette.accentColor,
        topLeft = Offset(w * 0.32f, h * 0.54f),
        size = Size(w * 0.36f, h * 0.26f),
    )
    drawOval(
        color = ink,
        topLeft = Offset(w * 0.14f, h * 0.24f),
        size = Size(w * 0.72f, h * 0.64f),
        style = Stroke(stroke),
    )

    drawCircle(cheek, radius = w * 0.06f, center = Offset(w * 0.27f, h * 0.62f))
    drawCircle(cheek, radius = w * 0.06f, center = Offset(w * 0.73f, h * 0.62f))
    drawPetEyesAndMouth(w, h, stroke, ink, eyeY = 0.51f, noseY = 0.60f, happyEyes = happyEyes, darkNose = true)
}
