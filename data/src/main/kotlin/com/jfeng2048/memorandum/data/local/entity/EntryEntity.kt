package com.jfeng2048.memorandum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 词条表。
 * person_id 外键 + CASCADE：删除人物时词条随之删除。
 * category_id 外键 + RESTRICT：分类被词条引用时不允许删除。
 */
@Entity(
    tableName = "entry",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["person_id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("person_id"), Index("category_id")],
)
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id") val id: Long = 0,
    @ColumnInfo(name = "person_id") val personId: Long,
    val title: String,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val content: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
