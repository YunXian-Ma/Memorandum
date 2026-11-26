package com.jfeng2048.memorandum.core.floatingtheme

import com.jfeng2048.memorandum.core.floatingtheme.themes.BearTheme
import com.jfeng2048.memorandum.core.floatingtheme.themes.CatTheme
import com.jfeng2048.memorandum.core.floatingtheme.themes.RabbitTheme
import com.jfeng2048.memorandum.core.floatingtheme.themes.ShibaTheme

/** 主题注册表：界面按这个列表渲染选择器，未知 id 回落到默认主题 */
interface FloatingThemeRegistry {
    val all: List<FloatingTheme>
    val default: FloatingTheme
    fun fromId(id: String?): FloatingTheme = all.firstOrNull { it.id == id } ?: default
}

/** 内置主题。新增主题在 [all] 里追加一行即可 */
object FloatingThemes : FloatingThemeRegistry {
    override val all: List<FloatingTheme> = listOf(
        CatTheme,
        ShibaTheme,
        RabbitTheme,
        BearTheme,
    )
    override val default: FloatingTheme = CatTheme
}
