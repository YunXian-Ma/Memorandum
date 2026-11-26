package com.jfeng2048.memorandum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 人物表 */
@Entity(tableName = "person")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "person_id") val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "avatar_uri") val avatarUri: String? = null,
    val gender: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
