package com.jfeng2048.memorandum.core.floatingtheme.themes

import androidx.compose.ui.graphics.Color
import com.jfeng2048.memorandum.core.floatingtheme.FloatingPalette
import com.jfeng2048.memorandum.core.floatingtheme.FloatingTheme
import com.jfeng2048.memorandum.core.floatingtheme.FloatingThemeCopy

val BearTheme = FloatingTheme(
    id = "bear",
    copy = FloatingThemeCopy(
        displayName = "小熊",
        homeName = "树洞",
        homeSubtitle = "拖回树洞休息",
        wentHomeToast = "小熊回树洞睡午觉啦",
        idleHint = "选一只小宠物陪你在其他 App 随时记一笔",
        companionTip = "小熊陪你记录，先挑个人吧",
    ),
    palette = FloatingPalette(
        primaryColor = Color(0xFFC68B59),
        accentColor = Color(0xFFF3D5B5),
        softBg = Color(0xFFFBF3EC),
        roofColor = Color(0xFF6B8E55),
    ),
    drawAvatar = avatarPainter { w, h, stroke, s ->
        drawBear(w, h, stroke, s.ink, s.cheek, s.palette, s.happyEyes)
    },
)
