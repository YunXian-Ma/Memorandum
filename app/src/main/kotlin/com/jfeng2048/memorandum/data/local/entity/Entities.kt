package com.jfeng2048.memorandum.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
 * relationshipId 外键 + RESTRICT：关系被人物引用时不允许删除。
 */
@Entity(
    tableName = "persons",
    foreignKeys = [
        ForeignKey(
            entity = RelationshipEntity::class,
            parentColumns = ["id"],
            childColumns = ["relationshipId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("relationshipId")],
)
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarUri: String? = null,
    val relationshipId: Long,
    val gender: String? = null,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * 词条表。
 * personId 外键 + CASCADE：删除人物时词条随之删除（PRD 7.3）。
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
    ],
    indices = [Index("personId")],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val title: String,
    val category: String,
    val content: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
