package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal fun DrawScope.drawPetEyesAndMouth(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    eyeY: Float,
    noseY: Float,
    happyEyes: Boolean,
    darkNose: Boolean = false,
) {
    if (happyEyes) {
        // 准备回家时的弯弯笑眼
        val leftArc = Path().apply {
            moveTo(w * 0.30f, h * (eyeY + 0.02f))
            quadraticTo(w * 0.36f, h * (eyeY - 0.06f), w * 0.42f, h * (eyeY + 0.02f))
        }
        val rightArc = Path().apply {
            moveTo(w * 0.58f, h * (eyeY + 0.02f))
            quadraticTo(w * 0.64f, h * (eyeY - 0.06f), w * 0.70f, h * (eyeY + 0.02f))
        }
        drawPath(leftArc, color = ink, style = Stroke(stroke * 1.05f, cap = StrokeCap.Round))
        drawPath(rightArc, color = ink, style = Stroke(stroke * 1.05f, cap = StrokeCap.Round))
    } else {
        // 黑豆眼 + 高光
        drawCircle(ink, radius = w * 0.052f, center = Offset(w * 0.36f, h * eyeY))
        drawCircle(ink, radius = w * 0.052f, center = Offset(w * 0.64f, h * eyeY))
        drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.345f, h * (eyeY - 0.015f)))
        drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.625f, h * (eyeY - 0.015f)))
    }

    // 小鼻子
    drawOval(
        color = if (darkNose) ink else Color(0xFFFF8FA3),
        topLeft = Offset(w * 0.455f, h * (noseY - 0.025f)),
        size = Size(w * 0.09f, h * 0.058f),
    )

    // 微笑小嘴 (w 形)
    val mouth = Path().apply {
        moveTo(w * 0.41f, h * (noseY + 0.06f))
        quadraticTo(w * 0.455f, h * (noseY + 0.11f), w * 0.50f, h * (noseY + 0.055f))
        quadraticTo(w * 0.545f, h * (noseY + 0.11f), w * 0.59f, h * (noseY + 0.06f))
    }
    drawPath(mouth, color = ink, style = Stroke(stroke * 0.85f, cap = StrokeCap.Round))
}
