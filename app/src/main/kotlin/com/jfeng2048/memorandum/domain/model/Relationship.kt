package com.jfeng2048.memorandum.domain.model

/** 人物关系：第一版固定六项，与原型、PRD 一致 */
enum class Relationship(val label: String) {
    FRIEND("朋友"),
    COLLEAGUE("同事"),
    FAMILY("家人"),
    CLASSMATE("同学"),
    CLIENT("客户"),
    OTHER("其他"),
    ;

    companion object {
        fun fromLabel(label: String?): Relationship =
            entries.firstOrNull { it.label == label } ?: OTHER
    }
}
