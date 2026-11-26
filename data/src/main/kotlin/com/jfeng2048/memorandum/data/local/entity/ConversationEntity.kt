package com.jfeng2048.memorandum.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 原始对话表：保存用户确认后的聊天文本，随人物级联删除 */
@Entity(
    tableName = "conversation",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["person_id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("person_id")],
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "conversation_id") val id: Long = 0,
    @ColumnInfo(name = "person_id") val personId: Long,
    val content: String,
    /** "CLIPBOARD" / "MANUAL" */
    val source: String,
    @ColumnInfo(name = "conversation_time") val conversationTime: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
