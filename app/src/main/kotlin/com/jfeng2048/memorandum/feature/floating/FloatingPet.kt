package com.jfeng2048.memorandum.feature.floating

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

/**
 * 悬浮窗可选宠物：每种宠物自带配色、小窝名称与回家提示文案。
 */
enum class FloatingPet(
    val id: String,
    val displayName: String,
    val homeName: String,
    val homeSubtitle: String,
    val wentHomeToast: String,
    val primaryColor: Color,
    val accentColor: Color,
    val softBg: Color,
    val roofColor: Color,
) {
    CAT(
        id = "cat",
        displayName = "橘猫",
        homeName = "猫窝",
        homeSubtitle = "拖回猫窝休息",
        wentHomeToast = "橘猫回猫窝打盹啦",
        primaryColor = Color(0xFFF4A259),
        accentColor = Color(0xFFFFD8A8),
        softBg = Color(0xFFFFF4E6),
        roofColor = Color(0xFFE07A5F),
    ),
    SHIBA(
        id = "shiba",
        displayName = "柴犬",
        homeName = "小木屋",
        homeSubtitle = "拖回小木屋休息",
        wentHomeToast = "柴犬回小木屋啦",
        primaryColor = Color(0xFFE29D48),
        accentColor = Color(0xFFFFF5E1),
        softBg = Color(0xFFFFF7E8),
        roofColor = Color(0xFFC86D44),
    ),
    RABBIT(
        id = "rabbit",
        displayName = "白兔",
        homeName = "草窝",
        homeSubtitle = "拖回草窝休息",
        wentHomeToast = "白兔钻回草窝啦",
        primaryColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFFFFB5C2),
        softBg = Color(0xFFFFF0F4),
        roofColor = Color(0xFF7BB661),
    ),
    BEAR(
        id = "bear",
        displayName = "小熊",
        homeName = "树洞",
        homeSubtitle = "拖回树洞休息",
        wentHomeToast = "小熊回树洞睡午觉啦",
        primaryColor = Color(0xFFC68B59),
        accentColor = Color(0xFFF3D5B5),
        softBg = Color(0xFFFBF3EC),
        roofColor = Color(0xFF6B8E55),
    ),
    ;

    companion object {
        fun fromId(id: String?): FloatingPet =
            entries.firstOrNull { it.id == id } ?: CAT
    }
}

/**
 * 矢量绘制的可爱宠物头像。
 * [happyEyes] 为 true 时展示弯弯笑眼（拖进小窝准备回家时）。
 */
@Composable
fun PetAvatar(
    pet: FloatingPet,
    size: Dp,
    modifier: Modifier = Modifier,
    happyEyes: Boolean = false,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.045f
        val ink = Color(0xFF2B2D33)
        val cheek = Color(0xFFFF9AA2).copy(alpha = 0.65f)

        when (pet) {
            FloatingPet.CAT -> drawCat(w, h, stroke, ink, cheek, pet, happyEyes)
            FloatingPet.SHIBA -> drawShiba(w, h, stroke, ink, cheek, pet, happyEyes)
            FloatingPet.RABBIT -> drawRabbit(w, h, stroke, ink, cheek, pet, happyEyes)
            FloatingPet.BEAR -> drawBear(w, h, stroke, ink, cheek, pet, happyEyes)
        }
    }
}

private fun DrawScope.drawCat(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    pet: FloatingPet,
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
    drawPath(leftEar, color = pet.primaryColor)
    drawPath(rightEar, color = pet.primaryColor)
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
        color = pet.primaryColor,
        topLeft = Offset(w * 0.13f, h * 0.23f),
        size = Size(w * 0.74f, h * 0.65f),
    )
    // 奶油下巴
    drawOval(
        color = pet.accentColor,
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

private fun DrawScope.drawShiba(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    pet: FloatingPet,
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
    drawPath(leftEar, color = pet.primaryColor)
    drawPath(rightEar, color = pet.primaryColor)
    drawPath(leftEar, color = ink, style = Stroke(stroke))
    drawPath(rightEar, color = ink, style = Stroke(stroke))

    // 脸型
    drawOval(
        color = pet.primaryColor,
        topLeft = Offset(w * 0.13f, h * 0.24f),
        size = Size(w * 0.74f, h * 0.64f),
    )
    // 柴犬标志性白色脸颊与豆眉
    drawOval(
        color = pet.accentColor,
        topLeft = Offset(w * 0.19f, h * 0.48f),
        size = Size(w * 0.62f, h * 0.39f),
    )
    drawOval(
        color = pet.accentColor,
        topLeft = Offset(w * 0.32f, h * 0.37f),
        size = Size(w * 0.10f, h * 0.06f),
    )
    drawOval(
        color = pet.accentColor,
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

private fun DrawScope.drawRabbit(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    pet: FloatingPet,
    happyEyes: Boolean,
) {
    // 长兔耳
    drawRoundRect(
        color = pet.primaryColor,
        topLeft = Offset(w * 0.24f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.09f),
    )
    drawRoundRect(
        color = pet.primaryColor,
        topLeft = Offset(w * 0.58f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.42f),
        cornerRadius = CornerRadius(w * 0.09f),
    )
    drawRoundRect(
        color = pet.accentColor,
        topLeft = Offset(w * 0.285f, h * 0.11f),
        size = Size(w * 0.09f, h * 0.28f),
        cornerRadius = CornerRadius(w * 0.045f),
    )
    drawRoundRect(
        color = pet.accentColor,
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
        color = pet.primaryColor,
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

private fun DrawScope.drawBear(
    w: Float,
    h: Float,
    stroke: Float,
    ink: Color,
    cheek: Color,
    pet: FloatingPet,
    happyEyes: Boolean,
) {
    // 圆熊耳
    drawCircle(pet.primaryColor, radius = w * 0.14f, center = Offset(w * 0.24f, h * 0.28f))
    drawCircle(pet.primaryColor, radius = w * 0.14f, center = Offset(w * 0.76f, h * 0.28f))
    drawCircle(pet.accentColor, radius = w * 0.07f, center = Offset(w * 0.24f, h * 0.28f))
    drawCircle(pet.accentColor, radius = w * 0.07f, center = Offset(w * 0.76f, h * 0.28f))
    drawCircle(ink, radius = w * 0.14f, center = Offset(w * 0.24f, h * 0.28f), style = Stroke(stroke))
    drawCircle(ink, radius = w * 0.14f, center = Offset(w * 0.76f, h * 0.28f), style = Stroke(stroke))

    // 圆脸与浅色吻部
    drawOval(
        color = pet.primaryColor,
        topLeft = Offset(w * 0.14f, h * 0.24f),
        size = Size(w * 0.72f, h * 0.64f),
    )
    drawOval(
        color = pet.accentColor,
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

private fun DrawScope.drawPetEyesAndMouth(
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

/**
 * 宠物的家图标（替代传统回收垃圾桶）：小屋 + 拱形门洞 + 暖光。
 * [hovered] 为 true 时屋门亮起暖黄灯光，表示欢迎宠物回家。
 */
@Composable
fun PetHomeIcon(
    pet: FloatingPet,
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
        drawPath(roof, color = pet.roofColor)
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
