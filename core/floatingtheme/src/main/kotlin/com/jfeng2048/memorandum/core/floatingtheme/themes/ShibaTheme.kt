package com.jfeng2048.memorandum.core.floatingtheme.themes

import androidx.compose.ui.graphics.Color
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPalette
import com.jfeng2048.memorandum.core.floatingtheme.FloatingTheme
import com.jfeng2048.memorandum.core.floatingtheme.FloatingThemeCopy

val ShibaTheme = FloatingTheme(
    id = "shiba",
    copy = FloatingThemeCopy(
        displayName = "柴犬",
        homeName = "小木屋",
        homeSubtitle = "拖回小木屋休息",
        wentHomeToast = "柴犬回小木屋啦",
        idleHint = "选一只小宠物陪你在其他 App 随时记一笔",
        companionTip = "柴犬陪你记录，先挑个人吧",
    ),
    palette = FloatingPalette(
        primaryColor = Color(0xFFE29D48),
        accentColor = Color(0xFFFFF5E1),
        softBg = Color(0xFFFFF7E8),
        roofColor = Color(0xFFC86D44),
    ),
    drawAvatar = avatarPainter { w, h, stroke, s ->
        drawShiba(w, h, stroke, s.ink, s.cheek, s.palette, s.happyEyes)
    },
)
