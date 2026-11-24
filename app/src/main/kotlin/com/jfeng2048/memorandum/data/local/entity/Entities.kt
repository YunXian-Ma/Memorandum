package com.jfeng2048.memorandum.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 人物表 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarUri: String? = null,
    val relationship: String,
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
