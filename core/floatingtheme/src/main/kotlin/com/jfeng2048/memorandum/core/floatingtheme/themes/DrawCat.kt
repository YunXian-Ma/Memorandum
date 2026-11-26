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

internal fun DrawScope.drawCat(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    palette: FloatingPalette,
    happyEyes: Boolean,
) {
    // 尖尖猫耳
    val leftEar = Path().apply {
        moveTo(w * 0.22f, h * 0.42f)
        lineTo(w * 0.16f, h * 0.12f)
        lineTo(w * 0.44f, h * 0.25f)
        close()
    }
    val rightEar = Path().apply {
        moveTo(w * 0.78f, h * 0.42f)
        lineTo(w * 0.84f, h * 0.12f)
        lineTo(w * 0.56f, h * 0.25f)
        close()
    }
    drawPath(leftEar, color = palette.primaryColor)
    drawPath(rightEar, color = palette.primaryColor)
    drawPath(leftEar, color = ink, style = Stroke(stroke))
    drawPath(rightEar, color = ink, style = Stroke(stroke))

    // 耳内粉色
    val innerLeft = Path().apply {
        moveTo(w * 0.24f, h * 0.36f)
        lineTo(w * 0.21f, h * 0.20f)
        lineTo(w * 0.37f, h * 0.27f)
        close()
    }
    val innerRight = Path().apply {
        moveTo(w * 0.76f, h * 0.36f)
        lineTo(w * 0.79f, h * 0.20f)
        lineTo(w * 0.63f, h * 0.27f)
        close()
    }
    drawPath(innerLeft, color = Color(0xFFFFB5C2))
    drawPath(innerRight, color = Color(0xFFFFB5C2))

    // 圆润小脸
    drawOval(
        color = palette.primaryColor,
        topLeft = Offset(w * 0.13f, h * 0.23f),
        size = Size(w * 0.74f, h * 0.65f),
    )
    // 奶油下巴
    drawOval(
        color = palette.accentColor,
        topLeft = Offset(w * 0.26f, h * 0.53f),
        size = Size(w * 0.48f, h * 0.33f),
    )
    // 额头虎斑
    val stripeColor = Color(0xFFD97736)
    drawLine(stripeColor, Offset(w * 0.50f, h * 0.25f), Offset(w * 0.50f, h * 0.36f), stroke * 1.1f, StrokeCap.Round)
    drawLine(stripeColor, Offset(w * 0.41f, h * 0.27f), Offset(w * 0.43f, h * 0.35f), stroke * 0.9f, StrokeCap.Round)
    drawLine(stripeColor, Offset(w * 0.59f, h * 0.27f), Offset(w * 0.57f, h * 0.35f), stroke * 0.9f, StrokeCap.Round)

    drawOval(
        color = ink,
        topLeft = Offset(w * 0.13f, h * 0.23f),
        size = Size(w * 0.74f, h * 0.65f),
        style = Stroke(stroke),
    )

    // 腮红与胡须
    drawCircle(cheek, radius = w * 0.065f, center = Offset(w * 0.27f, h * 0.62f))
    drawCircle(cheek, radius = w * 0.065f, center = Offset(w * 0.73f, h * 0.62f))
    drawLine(ink, Offset(w * 0.08f, h * 0.56f), Offset(w * 0.21f, h * 0.59f), stroke * 0.7f, StrokeCap.Round)
    drawLine(ink, Offset(w * 0.08f, h * 0.66f), Offset(w * 0.21f, h * 0.64f), stroke * 0.7f, StrokeCap.Round)
    drawLine(ink, Offset(w * 0.92f, h * 0.56f), Offset(w * 0.79f, h * 0.59f), stroke * 0.7f, StrokeCap.Round)
    drawLine(ink, Offset(w * 0.92f, h * 0.66f), Offset(w * 0.79f, h * 0.64f), stroke * 0.7f, StrokeCap.Round)

    drawPetEyesAndMouth(w, h, stroke, ink, eyeY = 0.53f, noseY = 0.61f, happyEyes = happyEyes)
}
