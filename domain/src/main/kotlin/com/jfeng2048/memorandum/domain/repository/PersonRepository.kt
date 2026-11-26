package com.jfeng2048.memorandum.domain.repository

import com.jfeng2048.memorandum.domain.model.ConversationSource
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.PersonConversation
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship
import kotlinx.coroutines.flow.Flow

/**
 * 人物数据的唯一接缝：ViewModel 只依赖这个接口。
 * 第一版由 Room 实现，后续接入云端时只需要换实现。
 */
interface PersonRepository {

    /** 观察全部人物，按更新时间倒序，包含其全部词条 */
    fun observePeople(): Flow<List<Person>>

    /** 观察单个人物，找不到时发射 null */
    fun observePerson(personId: Long): Flow<Person?>

    suspend fun getEntry(entryId: Long): PersonEntry?

    /** 观察全部关系，按用户自定义顺序排列 */
    fun observeRelationships(): Flow<List<Relationship>>

    /** 新增关系，返回新关系 id；颜色按现有数量轮转 */
    suspend fun createRelationship(name: String): Long

    suspend fun renameRelationship(relationshipId: Long, name: String)

    /**
     * 删除关系。
     * 返回 false 表示被人物引用或已是最后一个关系，调用方需要提示原因。
     */
    suspend fun deleteRelationship(relationshipId: Long): Boolean

    /** 观察全部词条分类，按用户自定义顺序排列 */
    fun observeCategories(): Flow<List<EntryCategory>>

    /** 观察每个分类被多少词条引用 */
    fun observeCategoryUsageCounts(): Flow<Map<Long, Int>>

    /** 新增词条分类，返回新分类 id */
    suspend fun createCategory(name: String): Long

    suspend fun renameCategory(categoryId: Long, name: String)

    /** 删除词条分类；返回 false 表示被词条引用或已是最后一个分类 */
    suspend fun deleteCategory(categoryId: Long): Boolean

    suspend fun createPerson(
        name: String,
        relationshipIds: List<Long>,
        gender: Gender,
        bio: String,
    ): Long

    suspend fun updatePerson(
        personId: Long,
        name: String,
        relationshipIds: List<Long>,
        gender: Gender,
        bio: String,
    )

    suspend fun updatePersonRelationships(
        personId: Long,
        relationshipIds: List<Long>,
    )

    /** 删除人物，关联词条由数据库外键级联删除 */
    suspend fun deletePerson(personId: Long)

    suspend fun createEntry(
        personId: Long,
        title: String,
        categoryId: Long,
        content: String,
    ): Long

    suspend fun updateEntry(
        entryId: Long,
        title: String,
        categoryId: Long,
        content: String,
    )

    suspend fun deleteEntry(entryId: Long)

    /** 保存一段用户确认过的原始对话，并刷新人物更新时间 */
    suspend fun saveConversation(personId: Long, content: String, source: ConversationSource): Long

    suspend fun deleteConversation(conversationId: Long)

    /** 全部人物最近收集的对话，按时间倒序 */
    fun observeRecentConversations(): Flow<List<PersonConversation>>

    /** 悬浮窗快速建人：默认挂到「其他」关系（不存在则取第一个） */
    suspend fun quickCreatePerson(name: String, bio: String): Long

    /** 首次启动时写入演示数据，保证一打开就能看到完整界面 */
    suspend fun seedIfNeeded()
}
