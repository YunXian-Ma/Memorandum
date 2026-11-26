package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * 宠物的家图标（替代传统回收垃圾桶）：小屋 + 拱形门洞 + 暖光。
 * [hovered] 为 true 时屋门亮起暖黄灯光，表示欢迎宠物回家。
 */
@Composable
fun FloatingHomeIcon(
    palette: FloatingPalette,
    hovered: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.045f
        val ink = Color(0xFF2B2D33)
        val wallColor = Color(0xFFFFFBF4)
        val doorColor = if (hovered) Color(0xFFFFD166) else Color(0xFFD9C7B2)

        // 小屋主体
        drawRoundRect(
            color = wallColor,
            topLeft = Offset(w * 0.18f, h * 0.42f),
            size = Size(w * 0.64f, h * 0.46f),
            cornerRadius = CornerRadius(w * 0.10f),
        )
        drawRoundRect(
            color = ink,
            topLeft = Offset(w * 0.18f, h * 0.42f),
            size = Size(w * 0.64f, h * 0.46f),
            cornerRadius = CornerRadius(w * 0.10f),
            style = Stroke(stroke),
        )

        // 拱形门洞
        val door = Path().apply {
            moveTo(w * 0.37f, h * 0.88f)
            lineTo(w * 0.37f, h * 0.64f)
            quadraticTo(w * 0.50f, h * 0.47f, w * 0.63f, h * 0.64f)
            lineTo(w * 0.63f, h * 0.88f)
            close()
        }
        drawPath(door, color = doorColor)
        drawPath(door, color = ink, style = Stroke(stroke * 0.85f))

        // 屋顶（随宠物主题色变化）
        val roof = Path().apply {
            moveTo(w * 0.08f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.14f)
            lineTo(w * 0.92f, h * 0.48f)
            close()
        }
        drawPath(roof, color = palette.roofColor)
        drawPath(roof, color = ink, style = Stroke(stroke))

        // 屋顶小圆窗/徽章
        drawCircle(
            color = if (hovered) Color(0xFFFFF3B0) else Color.White.copy(alpha = 0.9f),
            radius = w * 0.065f,
            center = Offset(w * 0.50f, h * 0.35f),
        )
        drawCircle(
            color = ink,
            radius = w * 0.065f,
            center = Offset(w * 0.50f, h * 0.35f),
            style = Stroke(stroke * 0.7f),
        )
    }
}
