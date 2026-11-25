package com.jfeng2048.memorandum.data.mapper

import com.jfeng2048.memorandum.data.local.entity.CategoryEntity
import com.jfeng2048.memorandum.data.local.entity.ConversationEntity
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import com.jfeng2048.memorandum.data.local.relation.EntryWithCategory
import com.jfeng2048.memorandum.data.local.relation.PersonWithEntries
import com.jfeng2048.memorandum.domain.model.ConversationSource
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.PersonConversation
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship

/** 数据库实体 → 领域模型；数据库里存中文标签，读出来时兜底到「其他 / 未知」 */
fun PersonWithEntries.toDomain(): Person = Person(
    id = person.id,
    name = person.name,
    avatarUri = person.avatarUri,
    relationships = relationships
        .sortedWith(compareBy<RelationshipEntity> { it.sortOrder }.thenBy { it.id })
        .map { it.toDomain() }
        .ifEmpty { listOf(Relationship.Unknown) },
    gender = Gender.fromLabel(person.gender),
    bio = person.description.orEmpty(),
    createdAt = person.createdAt,
    updatedAt = person.updatedAt,
    entries = entries
        .sortedByDescending { it.entry.updatedAt }
        .map { it.toDomain() },
    conversations = conversations
        .sortedByDescending { it.createdAt }
        .map { it.toDomain() },
)

fun RelationshipEntity.toDomain(): Relationship = Relationship(
    id = id,
    name = name,
    colorIndex = colorIndex,
)

fun CategoryEntity.toDomain(): EntryCategory = EntryCategory(
    id = id,
    name = name,
    colorIndex = colorIndex,
)

fun EntryWithCategory.toDomain(): PersonEntry = PersonEntry(
    id = entry.id,
    personId = entry.personId,
    title = entry.title,
    category = category?.toDomain() ?: EntryCategory.Unknown,
    content = entry.content.orEmpty(),
    createdAt = entry.createdAt,
    updatedAt = entry.updatedAt,
)

fun ConversationEntity.toDomain(): PersonConversation = PersonConversation(
    id = id,
    personId = personId,
    content = content,
    source = ConversationSource.fromName(source),
    createdAt = createdAt,
)
