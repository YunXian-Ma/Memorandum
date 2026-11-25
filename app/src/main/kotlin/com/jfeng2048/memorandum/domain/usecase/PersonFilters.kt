package com.jfeng2048.memorandum.domain.usecase

import com.jfeng2048.memorandum.domain.model.HomeSort
import com.jfeng2048.memorandum.domain.model.Person
import java.text.Collator
import java.util.Locale

/**
 * 首页的筛选 / 搜索 / 排序规则。
 * 放在领域层且不依赖 Android，便于直接用 JUnit 验证。
 */
object PersonFilters {

    /** 关键词命中姓名、简介、词条名称或词条描述中的任意一项 */
    fun matches(person: Person, keyword: String): Boolean {
        val query = keyword.trim()
        if (query.isEmpty()) return true
        return person.name.contains(query, ignoreCase = true) ||
            person.bio.contains(query, ignoreCase = true) ||
            person.entries.any {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }
    }

    fun filter(
        people: List<Person>,
        query: String,
        relationshipId: Long?,
    ): List<Person> = people.filter { person ->
        (relationshipId == null || person.relationships.any { it.id == relationshipId }) &&
            matches(person, query)
    }

    fun sort(people: List<Person>, sort: HomeSort): List<Person> = when (sort) {
        HomeSort.RECENT -> people.sortedByDescending { it.updatedAt }
        // 中文按拼音排序，避免出现按 Unicode 码点排的「假排序」
        HomeSort.NAME -> people.sortedWith(
            compareBy(Collator.getInstance(Locale.CHINA)) { it.name },
        )
    }
}
