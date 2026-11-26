package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** 主题头像图片；[happyEyes] 为 true 时用笑眼图（拖进小窝准备回家时），主题没提供则用普通图 */
@Composable
fun FloatingAvatar(
    theme: FloatingTheme,
    size: Dp,
    modifier: Modifier = Modifier,
    happyEyes: Boolean = false,
) {
    val ref = if (happyEyes) theme.images.avatarHappy ?: theme.images.avatar else theme.images.avatar
    ThemeImage(ref, size, modifier)
}
