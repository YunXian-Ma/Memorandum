package com.jfeng2048.memorandum.core.floatingtheme.themes

import androidx.compose.ui.graphics.Color
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPalette
import com.jfeng2048.memorandum.core.floatingtheme.FloatingTheme
import com.jfeng2048.memorandum.core.floatingtheme.FloatingThemeCopy

val RabbitTheme = FloatingTheme(
    id = "rabbit",
    copy = FloatingThemeCopy(
        displayName = "白兔",
        homeName = "草窝",
        homeSubtitle = "拖回草窝休息",
        wentHomeToast = "白兔钻回草窝啦",
        idleHint = "选一只小宠物陪你在其他 App 随时记一笔",
        companionTip = "白兔陪你记录，先挑个人吧",
    ),
    palette = FloatingPalette(
        primaryColor = Color(0xFFFFFFFF),
        accentColor = Color(0xFFFFB5C2),
        softBg = Color(0xFFFFF0F4),
        roofColor = Color(0xFF7BB661),
    ),
    drawAvatar = avatarPainter { w, h, stroke, s ->
        drawRabbit(w, h, stroke, s.ink, s.cheek, s.palette, s.happyEyes)
    },
)
