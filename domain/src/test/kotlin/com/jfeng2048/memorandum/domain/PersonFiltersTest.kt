package com.jfeng2048.memorandum.domain

import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.HomeSort
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship
import com.jfeng2048.memorandum.domain.usecase.PersonFilters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonFiltersTest {

    private val friend = Relationship(id = 1L, name = "朋友", colorIndex = 0)
    private val colleague = Relationship(id = 2L, name = "同事", colorIndex = 1)
    private val classmate = Relationship(id = 4L, name = "同学", colorIndex = 3)
    private val hobby = EntryCategory(id = 2L, name = "兴趣爱好", colorIndex = 1)

    private fun person(
        id: Long,
        name: String,
        relationships: List<Relationship>,
        bio: String = "",
        updatedAt: Long = 0L,
        entries: List<PersonEntry> = emptyList(),
    ) = Person(
        id = id,
        name = name,
        avatarUri = null,
        relationships = relationships,
        gender = Gender.UNKNOWN,
        bio = bio,
        createdAt = 0L,
        updatedAt = updatedAt,
        entries = entries,
    )

    private fun entry(
        personId: Long,
        title: String,
        content: String,
        category: EntryCategory = hobby,
    ) = PersonEntry(
        id = title.hashCode().toLong(),
        personId = personId,
        title = title,
        category = category,
        content = content,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `关键词可以命中词条标题`() {
        val chen = person(
            id = 1,
            name = "陈知远",
            relationships = listOf(friend),
            entries = listOf(entry(1, "篮球", "每周三晚在城西打半场")),
        )

        assertTrue(PersonFilters.matches(chen, "篮球"))
    }

    @Test
    fun `关键词可以命中词条描述`() {
        val chen = person(
            id = 1,
            name = "陈知远",
            relationships = listOf(friend),
            entries = listOf(entry(1, "篮球", "膝盖有旧伤，别约太激烈的")),
        )

        assertTrue(PersonFilters.matches(chen, "旧伤"))
    }

    @Test
    fun `关键词不会误命中无关人物`() {
        val person = person(id = 2, name = "苏晚", relationships = listOf(classmate))

        assertFalse(PersonFilters.matches(person, "篮球"))
    }

    @Test
    fun `关系筛选只保留对应关系`() {
        val people = listOf(
            person(id = 1, name = "陈知远", relationships = listOf(friend)),
            person(id = 2, name = "林嘉禾", relationships = listOf(colleague, friend)),
            person(id = 3, name = "何岸", relationships = listOf(colleague)),
        )

        val result = PersonFilters.filter(people, query = "", relationshipId = colleague.id)

        assertEquals(listOf(2L, 3L), result.map { it.id })
    }

    @Test
    fun `最近更新排序为倒序`() {
        val people = listOf(
            person(id = 1, name = "陈知远", relationships = listOf(friend), updatedAt = 100L),
            person(id = 2, name = "林嘉禾", relationships = listOf(colleague), updatedAt = 300L),
            person(id = 3, name = "何岸", relationships = listOf(colleague), updatedAt = 200L),
        )

        val result = PersonFilters.sort(people, HomeSort.RECENT)

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }
}
