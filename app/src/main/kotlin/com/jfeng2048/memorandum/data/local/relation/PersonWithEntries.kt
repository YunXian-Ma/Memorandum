package com.jfeng2048.memorandum.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity

/** 人物 + 其全部词条，一次查询拿到完整档案 */
data class PersonWithEntries(
    @Embedded val person: PersonEntity,
    @Relation(parentColumn = "id", entityColumn = "personId")
    val entries: List<EntryEntity>,
)
