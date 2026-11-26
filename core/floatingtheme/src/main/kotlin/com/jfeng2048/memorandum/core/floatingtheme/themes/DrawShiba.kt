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

internal fun DrawScope.drawShiba(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    palette: FloatingPalette,
    happyEyes: Boolean,
) {
    // 立耳
    val leftEar = Path().apply {
        moveTo(w * 0.20f, h * 0.44f)
        lineTo(w * 0.18f, h * 0.14f)
        lineTo(w * 0.42f, h * 0.26f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(w * 0.80f, h * 0.44f)
        lineTo(w * 0.82f, h * 0.14f)
        lineTo(w * 0.58f, h * 0.26f)
        close()
    }
    drawPath(leftEar, color = palette.primaryColor)
    drawPath(rightEar, color = palette.primaryColor)
    drawPath(leftEar, color = ink, style = Stroke(stroke))
    drawPath(rightEar, color = ink, style = Stroke(stroke))

    // 脸型
    drawOval(
        color = palette.primaryColor,
        topLeft = Offset(w * 0.13f, h * 0.24f),
        size = Size(w * 0.74f, h * 0.64f),
    )
    // 柴犬标志性白色脸颊与豆眉
    drawOval(
        color = palette.accentColor,
        topLeft = Offset(w * 0.19f, h * 0.48f),
        size = Size(w * 0.62f, h * 0.39f),
    )
    drawOval(
        color = palette.accentColor,
        topLeft = Offset(w * 0.32f, h * 0.37f),
        size = Size(w * 0.10f, h * 0.06f),
    )
    drawOval(
        color = palette.accentColor,
        topLeft = Offset(w * 0.58f, h * 0.37f),
        size = Size(w * 0.10f, h * 0.06f),
    )
    drawOval(
        color = ink,
        topLeft = Offset(w * 0.13f, h * 0.24f),
        size = Size(w * 0.74f, h * 0.64f),
        style = Stroke(stroke),
    )

    drawCircle(cheek, radius = w * 0.065f, center = Offset(w * 0.26f, h * 0.62f))
    drawCircle(cheek, radius = w * 0.065f, center = Offset(w * 0.74f, h * 0.62f))
    drawPetEyesAndMouth(w, h, stroke, ink, eyeY = 0.52f, noseY = 0.60f, happyEyes = happyEyes, darkNose = true)
}
