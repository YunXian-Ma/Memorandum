package com.jfeng2048.memorandum.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 词条分类表：由用户在「分类管理」页维护，词条通过 categoryId 引用 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorIndex: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long,
)

/** 关系表：由用户在「关系管理」页维护，人物通过 relationshipId 引用 */
@Entity(tableName = "relationships")
data class RelationshipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** 色板序号，0..COLOR_COUNT-1 */
    val colorIndex: Int = 0,
    /** 展示顺序，新建的关系排在最后 */
    val sortOrder: Int = 0,
    val createdAt: Long,
)

/**
 * 人物表。
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarUri: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * 人物与关系的多对多关联表：
 * - 删除人物时级联删除关联（CASCADE）
 * - 关系被人物引用时不允许删除（RESTRICT）
 */
@Entity(
    tableName = "person_relationships",
    primaryKeys = ["personId", "relationshipId"],
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RelationshipEntity::class,
            parentColumns = ["id"],
            childColumns = ["relationshipId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("personId"), Index("relationshipId")],
)
data class PersonRelationshipCrossRef(
    val personId: Long,
    val relationshipId: Long,
)

/**
 * 词条表。
 * personId 外键 + CASCADE：删除人物时词条随之删除（PRD 7.3）。
 * categoryId 外键 + RESTRICT：分类被词条引用时不允许删除。
 */
@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("personId"), Index("categoryId")],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val title: String,
    val categoryId: Long,
    val content: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/** 原始对话表：保存用户确认后的聊天文本，随人物级联删除 */
@Entity(
    tableName = "conversations",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("personId")],
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val content: String,
    /** "CLIPBOARD" / "MANUAL" */
    val source: String,
    val conversationTime: Long? = null,
    val createdAt: Long,
)
