package com.jfeng2048.memorandum.data.repository

import com.jfeng2048.memorandum.data.local.dao.EntryDao
import com.jfeng2048.memorandum.data.local.dao.PersonDao
import com.jfeng2048.memorandum.data.local.dao.RelationshipDao
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import com.jfeng2048.memorandum.data.local.prefs.AppPreferences
import com.jfeng2048.memorandum.data.mapper.toDomain
import com.jfeng2048.memorandum.data.seed.SeedData
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PersonRepositoryImpl @Inject constructor(
    private val personDao: PersonDao,
    private val relationshipDao: RelationshipDao,
    private val entryDao: EntryDao,
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

    override suspend fun getEntry(entryId: Long): PersonEntry? =
        entryDao.findById(entryId)?.toDomain()

    override suspend fun createPerson(
        name: String,
        relationshipId: Long,
        gender: Gender,
        bio: String,
    ): Long {
        val now = System.currentTimeMillis()
        return personDao.insert(
            PersonEntity(
                name = name,
                relationshipId = relationshipId,
                gender = gender.label,
                description = bio.takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updatePerson(
        personId: Long,
        name: String,
        relationshipId: Long,
        gender: Gender,
        bio: String,
    ) {
        val existing = personDao.findById(personId) ?: return
        personDao.update(
            existing.copy(
                name = name,
                relationshipId = relationshipId,
                gender = gender.label,
                description = bio.takeIf { it.isNotBlank() },
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun deletePerson(personId: Long) {
        // 词条由外键 CASCADE 级联删除
        personDao.deleteById(personId)
    }

    override suspend fun createEntry(
        personId: Long,
        title: String,
        category: EntryCategory,
        content: String,
    ): Long {
        val now = System.currentTimeMillis()
        val entryId = entryDao.insert(
            EntryEntity(
                personId = personId,
                title = title,
                category = category.label,
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
        category: EntryCategory,
        content: String,
    ) {
        val existing = entryDao.findById(entryId) ?: return
        val now = System.currentTimeMillis()
        entryDao.update(
            existing.copy(
                title = title,
                category = category.label,
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

    override suspend fun seedIfNeeded() {
        if (preferences.isSeeded) return
        // 只补默认关系，不预置人物与词条；关系表为空时写入，避免用户删光后又被灌回
        if (relationshipDao.count() == 0) {
            SeedData.insertDefaultRelationships(relationshipDao, System.currentTimeMillis())
        }
        preferences.isSeeded = true
    }
}
