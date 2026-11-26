package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** 宠物的家图标；[hovered] 为 true 时用亮灯图 */
@Composable
fun FloatingHomeIcon(
    theme: FloatingTheme,
    hovered: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val ref = if (hovered) theme.images.homeHover ?: theme.images.home else theme.images.home
    ThemeImage(ref, size, modifier)
}
