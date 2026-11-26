package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay

/**
 * 会动的宠物：按 [state] 播放主题里配置的精灵图动画；
 * 主题没有该状态的动画时显示静态头像。动画资源完全来自主题包，这里不含任何绘制。
 */
@Composable
fun FloatingPet(
    theme: FloatingTheme,
    state: PetState,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val anim = theme.animations[state] ?: theme.animations[PetState.IDLE].takeIf { state != PetState.HAPPY && state != PetState.DRAGGING }
    if (anim == null) {
        FloatingAvatar(theme, size, modifier, happyEyes = state == PetState.HAPPY)
        return
    }
    val context = LocalContext.current.applicationContext
    var sheet by remember(anim.sheet.key) { mutableStateOf<ImageBitmap?>(cache.get(anim.sheet.key)) }
    var frame by remember(anim.sheet.key) { mutableIntStateOf(0) }
    LaunchedEffect(anim.sheet.key) { sheet = loadThemeImage(context, anim.sheet) ?: sheet }
    LaunchedEffect(anim.sheet.key, sheet != null) {
        if (sheet == null) return@LaunchedEffect
        while (true) {
            delay(1000L / anim.fps)
            frame = (frame + 1) % anim.frames
        }
    }
    val bmp = sheet
    if (bmp == null) {
        FloatingAvatar(theme, size, modifier, happyEyes = state == PetState.HAPPY)
        return
    }
    Canvas(modifier.size(size)) {
        val fw = bmp.width / anim.frames
        drawImage(
            image = bmp,
            srcOffset = IntOffset(fw * (frame % anim.frames), 0),
            srcSize = IntSize(fw, bmp.height),
            dstSize = IntSize(this.size.width.toInt(), this.size.height.toInt()),
        )
    }
}
