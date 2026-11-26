package com.jfeng2048.memorandum.domain.model

/** 性别：PRD 允许为空，这里用「未知」兜底，避免界面出现空文案 */
enum class Gender(val label: String) {
    MALE("男"),
    FEMALE("女"),
    UNKNOWN("未知"),
    ;

    companion object {
        fun fromLabel(label: String?): Gender =
            entries.firstOrNull { it.label == label } ?: UNKNOWN
    }
}
