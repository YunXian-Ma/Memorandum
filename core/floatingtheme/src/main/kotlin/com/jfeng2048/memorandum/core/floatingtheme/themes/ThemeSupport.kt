package com.jfeng2048.memorandum.core.floatingtheme.themes

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.jfeng2048.memorandum.core.floatingtheme.FloatingAvatarStyle
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPalette

/** 把「按宽高比例绘制」的函数适配成主题的 drawAvatar */
internal fun avatarPainter(
    draw: DrawScope.(w: Float, h: Float, stroke: Float, style: FloatingAvatarStyle) -> Unit,
): DrawScope.(FloatingAvatarStyle) -> Unit = { style ->
    draw(size.width, size.height, size.width * 0.045f, style)
}
