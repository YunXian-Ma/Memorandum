package com.jfeng2048.memorandum.core.floatingtheme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** 主题配色：悬浮球底色、描边、小窝屋顶等都从这里取 */
data class FloatingPalette(
    val primaryColor: Color,
    val accentColor: Color,
    val softBg: Color,
    val roofColor: Color,
)

/** 主题里所有面向用户的文案，新增主题时一并提供，界面层不再硬编码「猫窝」之类的词 */
data class FloatingThemeCopy(
    /** 主题名，如「橘猫」 */
    val displayName: String,
    /** 小窝名，如「猫窝」 */
    val homeName: String,
    val homeSubtitle: String,
    val wentHomeToast: String,
    /** 首页开关卡片的引导语 */
    val idleHint: String,
    /** 悬浮卡片里的陪伴语 */
    val companionTip: String,
)

/** 头像绘制时的上下文，避免每个主题重复计算描边与墨色 */
data class FloatingAvatarStyle(
    val palette: FloatingPalette,
    val happyEyes: Boolean,
    val ink: Color,
    val cheek: Color,
)

/**
 * 一个悬浮窗主题 = 标识 + 配色 + 文案 + 绘制函数。
 *
 * 新增主题只需：在 `themes/` 下新建一个文件声明一个 [FloatingTheme]，
 * 再把它加进 [FloatingThemes.all]。界面、服务、设置页都按注册表渲染，无需任何改动。
 * 后续若要做成可下载的主题包，只需让 [FloatingThemeRegistry] 从外部来源装载即可。
 */
data class FloatingTheme(
    /** 持久化用的稳定 id，发布后不要改 */
    val id: String,
    val copy: FloatingThemeCopy,
    val palette: FloatingPalette,
    /** 在 0..size 的正方形画布上绘制头像 */
    val drawAvatar: DrawScope.(FloatingAvatarStyle) -> Unit,
)
