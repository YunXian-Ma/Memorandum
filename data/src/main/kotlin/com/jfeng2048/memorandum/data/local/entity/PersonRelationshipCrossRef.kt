package com.jfeng2048.memorandum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 人物与关系的多对多关联表：
 * - 删除人物时级联删除关联（CASCADE）
 * - 关系被人物引用时不允许删除（RESTRICT）
 */
@Entity(
    tableName = "person_relationship",
    primaryKeys = ["person_id", "relationship_id"],
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["person_id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RelationshipEntity::class,
            parentColumns = ["relationship_id"],
            childColumns = ["relationship_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("person_id"), Index("relationship_id")],
)
data class PersonRelationshipCrossRef(
    @ColumnInfo(name = "person_id") val personId: Long,
    @ColumnInfo(name = "relationship_id") val relationshipId: Long,
)
