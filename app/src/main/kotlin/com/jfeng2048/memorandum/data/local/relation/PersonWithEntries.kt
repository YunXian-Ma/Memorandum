package com.jfeng2048.memorandum.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity

/** 人物 + 关系 + 其全部词条，一次查询拿到完整档案 */
data class PersonWithEntries(
    @Embedded val person: PersonEntity,
    @Relation(parentColumn = "relationshipId", entityColumn = "id")
    val relationship: RelationshipEntity?,
    @Relation(parentColumn = "id", entityColumn = "personId")
    val entries: List<EntryEntity>,
)
