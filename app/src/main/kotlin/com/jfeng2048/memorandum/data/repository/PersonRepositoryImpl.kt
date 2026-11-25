package com.jfeng2048.memorandum.data.repository

import com.jfeng2048.memorandum.data.local.dao.CategoryDao
import com.jfeng2048.memorandum.data.local.dao.ConversationDao
import com.jfeng2048.memorandum.data.local.dao.EntryDao
import com.jfeng2048.memorandum.data.local.dao.PersonDao
import com.jfeng2048.memorandum.data.local.dao.RelationshipDao
import com.jfeng2048.memorandum.data.local.entity.CategoryEntity
import com.jfeng2048.memorandum.data.local.entity.ConversationEntity
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.PersonRelationshipCrossRef
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import com.jfeng2048.memorandum.data.local.prefs.AppPreferences
import com.jfeng2048.memorandum.data.mapper.toDomain
import com.jfeng2048.memorandum.data.seed.SeedData
import com.jfeng2048.memorandum.domain.model.ConversationSource
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.PersonConversation
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class PersonRepositoryImpl @Inject constructor(
    private val personDao: PersonDao,
    private val relationshipDao: RelationshipDao,
    private val categoryDao: CategoryDao,
    private val entryDao: EntryDao,
    private val conversationDao: ConversationDao,
    private val preferences: AppPreferences,
) : PersonRepository {

    override fun observePeople(): Flow<List<Person>> =
        personDao.observeAllWithEntries().map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observePerson(personId: Long): Flow<Person?> =
        personDao.observeWithEntries(personId).map { it?.toDomain() }

    override fun observeRelationships(): Flow<List<Relationship>> =
        relationshipDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun createRelationship(name: String): Long {
        val count = relationshipDao.count()
        return relationshipDao.insert(
            RelationshipEntity(
                name = name,
                colorIndex = count % Relationship.COLOR_COUNT,
                sortOrder = count,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun renameRelationship(relationshipId: Long, name: String) {
        val existing = relationshipDao.findById(relationshipId) ?: return
        relationshipDao.update(existing.copy(name = name))
    }

    override suspend fun deleteRelationship(relationshipId: Long): Boolean {
        // 至少保留一个关系，且被人物引用时不允许删除（外键 RESTRICT 的第二道保险）
        if (relationshipDao.count() <= 1) return false
        if (personDao.countByRelationship(relationshipId) > 0) return false
        relationshipDao.deleteById(relationshipId)
        return true
    }

    override fun observeCategories(): Flow<List<EntryCategory>> =
        categoryDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeCategoryUsageCounts(): Flow<Map<Long, Int>> =
        entryDao.observeCategoryUsageCounts().map { rows ->
            rows.associate { it.categoryId to it.count }
        }

    override suspend fun createCategory(name: String): Long {
        val count = categoryDao.count()
        return categoryDao.insert(
            CategoryEntity(
                name = name,
                colorIndex = count % EntryCategory.COLOR_COUNT,
                sortOrder = count,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun renameCategory(categoryId: Long, name: String) {
        val existing = categoryDao.findById(categoryId) ?: return
        categoryDao.update(existing.copy(name = name))
    }

    override suspend fun deleteCategory(categoryId: Long): Boolean {
        if (categoryDao.count() <= 1) return false
        if (entryDao.countByCategory(categoryId) > 0) return false
        categoryDao.deleteById(categoryId)
        return true
    }

    override suspend fun getEntry(entryId: Long): PersonEntry? =
        entryDao.findWithCategoryById(entryId)?.toDomain()

    override suspend fun createPerson(
        name: String,
        relationshipIds: List<Long>,
        gender: Gender,
        bio: String,
    ): Long {
        val now = System.currentTimeMillis()
        val personId = personDao.insert(
            PersonEntity(
                name = name,
                gender = gender.label,
                description = bio.takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now,
            ),
        )
        val refs = relationshipIds.distinct().map { relId ->
            PersonRelationshipCrossRef(personId = personId, relationshipId = relId)
        }
        if (refs.isNotEmpty()) {
            personDao.insertPersonRelationships(refs)
        }
        return personId
    }

    override suspend fun updatePerson(
        personId: Long,
        name: String,
        relationshipIds: List<Long>,
        gender: Gender,
        bio: String,
    ) {
        val existing = personDao.findById(personId) ?: return
        val now = System.currentTimeMillis()
        personDao.update(
            existing.copy(
                name = name,
                gender = gender.label,
                description = bio.takeIf { it.isNotBlank() },
                updatedAt = now,
            ),
        )
        personDao.deletePersonRelationships(personId)
        val refs = relationshipIds.distinct().map { relId ->
            PersonRelationshipCrossRef(personId = personId, relationshipId = relId)
        }
        if (refs.isNotEmpty()) {
            personDao.insertPersonRelationships(refs)
        }
    }

    override suspend fun updatePersonRelationships(
        personId: Long,
        relationshipIds: List<Long>,
    ) {
        personDao.deletePersonRelationships(personId)
        val refs = relationshipIds.distinct().map { relId ->
            PersonRelationshipCrossRef(personId = personId, relationshipId = relId)
        }
        if (refs.isNotEmpty()) {
            personDao.insertPersonRelationships(refs)
        }
        personDao.touch(personId, System.currentTimeMillis())
    }

    override suspend fun deletePerson(personId: Long) {
        // 词条由外键 CASCADE 级联删除
        personDao.deleteById(personId)
    }

    override suspend fun createEntry(
        personId: Long,
        title: String,
        categoryId: Long,
        content: String,
    ): Long {
        val now = System.currentTimeMillis()
        val entryId = entryDao.insert(
            EntryEntity(
                personId = personId,
                title = title,
                categoryId = categoryId,
                content = content.takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now,
            ),
        )
        personDao.touch(personId, now)
        return entryId
    }

    override suspend fun updateEntry(
        entryId: Long,
        title: String,
        categoryId: Long,
        content: String,
    ) {
        val existing = entryDao.findById(entryId) ?: return
        val now = System.currentTimeMillis()
        entryDao.update(
            existing.copy(
                title = title,
                categoryId = categoryId,
                content = content.takeIf { it.isNotBlank() },
                updatedAt = now,
            ),
        )
        personDao.touch(existing.personId, now)
    }

    override suspend fun deleteEntry(entryId: Long) {
        val existing = entryDao.findById(entryId) ?: return
        entryDao.deleteById(entryId)
        personDao.touch(existing.personId, System.currentTimeMillis())
    }

    override suspend fun saveConversation(
        personId: Long,
        content: String,
        source: ConversationSource,
    ): Long {
        val now = System.currentTimeMillis()
        val id = conversationDao.insert(
            ConversationEntity(
                personId = personId,
                content = content,
                source = source.name,
                createdAt = now,
            ),
        )
        personDao.touch(personId, now)
        return id
    }

    override suspend fun deleteConversation(conversationId: Long) {
        conversationDao.deleteById(conversationId)
    }

    override fun observeRecentConversations(): Flow<List<PersonConversation>> =
        conversationDao.observeRecent().map { rows -> rows.map { it.toDomain() } }

    override suspend fun quickCreatePerson(name: String, bio: String): Long {
        val relationships = relationshipDao.observeAll().first()
        val default = relationships.firstOrNull { it.name == "其他" } ?: relationships.firstOrNull()
        return createPerson(
            name = name,
            relationshipIds = listOfNotNull(default?.id),
            gender = Gender.UNKNOWN,
            bio = bio,
        )
    }

    override suspend fun seedIfNeeded() {
        val now = System.currentTimeMillis()
        // 默认分类在老库升级（version 2 -> 3 重建表）后也要能补齐
        if (categoryDao.count() == 0) {
            SeedData.insertDefaultCategories(categoryDao, now)
        }
        // 业务上至少保留一个关系，若为空说明发生了破坏性建库/清表，需重新补齐默认关系
        if (relationshipDao.count() == 0) {
            SeedData.insertDefaultRelationships(relationshipDao, now)
        }
        if (preferences.isSeeded) return
        preferences.isSeeded = true
    }
}
