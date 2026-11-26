package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.ui.graphics.Color

/** 主题配色：悬浮球底色、描边、小窝屋顶等都从这里取 */
data class FloatingPalette(
    val primaryColor: Color,
    val accentColor: Color,
    val softBg: Color,
    val roofColor: Color,
)

/** 主题里所有面向用户的文案，界面层不硬编码「猫窝」之类的词 */
data class FloatingThemeCopy(
    val displayName: String,
    val homeName: String,
    val homeSubtitle: String,
    val wentHomeToast: String,
    val idleHint: String,
    val companionTip: String,
)

/** 一张主题图片的位置：来自 assets 或本地文件（外部主题包） */
data class ThemeImageRef(val assetPath: String? = null, val filePath: String? = null) {
    val key: String get() = assetPath?.let { "asset:$it" } ?: "file:$filePath"
}

/** 主题图片集合；happy / hover 缺省时回落到普通态 */
data class FloatingThemeImages(
    val avatar: ThemeImageRef,
    val avatarHappy: ThemeImageRef?,
    val home: ThemeImageRef,
    val homeHover: ThemeImageRef?,
)

/**
 * 一个悬浮窗主题 = 标识 + 配色 + 文案 + 图片资源，纯数据，由 `theme.json` 解析而来。
 * 新增主题只需放一个包含 theme.json 与 PNG 的目录，无需任何代码。
 */
data class FloatingTheme(
    /** 持久化用的稳定 id，发布后不要改 */
    val id: String,
    val copy: FloatingThemeCopy,
    val palette: FloatingPalette,
    val images: FloatingThemeImages,
)
