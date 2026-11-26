package com.jfeng2048.memorandum.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** 悬浮助手的用户设置。主题只存 id，具体主题由 core:floatingtheme 的注册表解析 */
interface FloatingSettings {
    val enabledFlow: StateFlow<Boolean>
    val themeIdFlow: StateFlow<String>

    var enabled: Boolean
    var themeId: String

    /** 悬浮球是否停靠在屏幕左侧 */
    var ballIsLeft: Boolean

    /** 悬浮球纵向位置占屏幕高度的比例，适配横竖屏 */
    var ballYRatio: Float
}
