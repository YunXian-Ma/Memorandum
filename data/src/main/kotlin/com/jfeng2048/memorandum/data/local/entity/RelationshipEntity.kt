package com.jfeng2048.memorandum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 关系表：由用户在「关系管理」页维护，人物通过 person_relationship 多对多引用 */
@Entity(tableName = "relationship")
data class RelationshipEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "relationship_id") val id: Long = 0,
    val name: String,
    /** 色板序号，0..COLOR_COUNT-1 */
    @ColumnInfo(name = "color_index") val colorIndex: Int = 0,
    /** 展示顺序，新建的关系排在最后 */
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
