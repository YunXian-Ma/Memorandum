package com.jfeng2048.memorandum.core.floatingtheme.themes

import androidx.compose.ui.graphics.Color
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPalette
import com.jfeng2048.memorandum.core.floatingtheme.FloatingTheme
import com.jfeng2048.memorandum.core.floatingtheme.FloatingThemeCopy

val CatTheme = FloatingTheme(
    id = "cat",
    copy = FloatingThemeCopy(
        displayName = "橘猫",
        homeName = "猫窝",
        homeSubtitle = "拖回猫窝休息",
        wentHomeToast = "橘猫回猫窝打盹啦",
        idleHint = "选一只小宠物陪你在其他 App 随时记一笔",
        companionTip = "橘猫陪你记录，先挑个人吧",
    ),
    palette = FloatingPalette(
        primaryColor = Color(0xFFF4A259),
        accentColor = Color(0xFFFFD8A8),
        softBg = Color(0xFFFFF4E6),
        roofColor = Color(0xFFE07A5F),
    ),
    drawAvatar = avatarPainter { w, h, stroke, s ->
        drawCat(w, h, stroke, s.ink, s.cheek, s.palette, s.happyEyes)
    },
)
