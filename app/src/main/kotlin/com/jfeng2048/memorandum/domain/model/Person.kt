package com.jfeng2048.memorandum.domain.model

/**
 * 人物档案。entries 是这个人名下的全部词条，按更新时间倒序。
 * 时间字段统一用 epoch millis，展示时再格式化。
 */
data class Person(
    val id: Long,
    val name: String,
    val avatarUri: String?,
    val relationship: Relationship,
    val gender: Gender,
    val bio: String,
    val createdAt: Long,
    val updatedAt: Long,
    val entries: List<PersonEntry>,
) {
    val entryCount: Int get() = entries.size
    val categoryCount: Int get() = entries.map { it.category.id }.distinct().size
}

/** 描述词条：一个人物下的最小记录单位 */
data class PersonEntry(
    val id: Long,
    val personId: Long,
    val title: String,
    val category: EntryCategory,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
)
