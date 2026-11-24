package com.jfeng2048.memorandum.data.mapper

import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.relation.PersonWithEntries
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship

/** 数据库实体 → 领域模型；数据库里存中文标签，读出来时兜底到「其他 / 未知」 */
fun PersonWithEntries.toDomain(): Person = Person(
    id = person.id,
    name = person.name,
    avatarUri = person.avatarUri,
    relationship = Relationship.fromLabel(person.relationship),
    gender = Gender.fromLabel(person.gender),
    bio = person.description.orEmpty(),
    createdAt = person.createdAt,
    updatedAt = person.updatedAt,
    entries = entries
        .sortedByDescending { it.updatedAt }
        .map { it.toDomain() },
)

fun EntryEntity.toDomain(): PersonEntry = PersonEntry(
    id = id,
    personId = personId,
    title = title,
    category = EntryCategory.fromLabel(category),
    content = content.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)
