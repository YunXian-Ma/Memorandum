package com.jfeng2048.memorandum.domain.model

/**
 * 词条分类：存储在本地数据库 `categories` 表，支持用户在分类管理页增删改。
 * 颜色由 [colorIndex] 决定，避免改名或增删顺序变化导致色点跳变。
 */
data class EntryCategory(
    val id: Long,
    val name: String,
    val colorIndex: Int,
) {
    val label: String get() = name

    companion object {
        const val COLOR_COUNT = 9

        val DEFAULT_NAMES = listOf(
            "兴趣爱好",
            "性格特点",
            "生活习惯",
            "重要日期",
        )

        val Unknown = EntryCategory(id = 0L, name = "其他", colorIndex = 8)
    }
}
