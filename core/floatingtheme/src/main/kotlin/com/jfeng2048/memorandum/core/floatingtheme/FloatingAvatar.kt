package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

private val AvatarInk = Color(0xFF2B2D33)
private val AvatarCheek = Color(0xFFFF9AA2).copy(alpha = 0.65f)

/** 矢量绘制的主题头像；[happyEyes] 为 true 时展示弯弯笑眼（拖进小窝准备回家时） */
@Composable
fun FloatingAvatar(
    theme: FloatingTheme,
    size: Dp,
    modifier: Modifier = Modifier,
    happyEyes: Boolean = false,
) {
    Canvas(modifier = modifier.size(size)) {
        theme.drawAvatar(this, FloatingAvatarStyle(theme.palette, happyEyes, AvatarInk, AvatarCheek))
    }
}
