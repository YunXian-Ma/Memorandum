package com.jfeng2048.memorandum.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.jfeng2048.memorandum.data.local.entity.CategoryEntity
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.PersonRelationshipCrossRef
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity

data class EntryWithCategory(
    @Embedded val entry: EntryEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?,
)

/** 人物 + 关系 + 其全部词条，一次查询拿到完整档案 */
data class PersonWithEntries(
    @Embedded val person: PersonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PersonRelationshipCrossRef::class,
            parentColumn = "personId",
            entityColumn = "relationshipId",
        ),
    )
    val relationships: List<RelationshipEntity>,
    @Relation(entity = EntryEntity::class, parentColumn = "id", entityColumn = "personId")
    val entries: List<EntryWithCategory>,
)
