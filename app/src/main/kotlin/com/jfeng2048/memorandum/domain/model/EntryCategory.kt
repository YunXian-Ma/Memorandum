package com.jfeng2048.memorandum.domain.model

/** 词条分类：顺序即表单里的展示顺序，与原型 CATS 一致 */
enum class EntryCategory(val label: String) {
    CAREER("职业"),
    HOBBY("兴趣爱好"),
    PERSONALITY("性格特点"),
    LIFESTYLE("生活习惯"),
    DIET("饮食偏好"),
    EDUCATION("教育经历"),
    WORK("工作经历"),
    DATE("重要日期"),
    OTHER("其他"),
    ;

    companion object {
        fun fromLabel(label: String?): EntryCategory =
            entries.firstOrNull { it.label == label } ?: OTHER
    }
}
