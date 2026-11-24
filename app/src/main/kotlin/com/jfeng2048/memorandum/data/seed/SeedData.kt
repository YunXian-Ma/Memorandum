package com.jfeng2048.memorandum.data.seed

import com.jfeng2048.memorandum.data.local.dao.RelationshipDao
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import com.jfeng2048.memorandum.domain.model.Relationship

/**
 * 首次启动只写入默认关系（朋友 / 同事 / 家人 / 同学 / 客户 / 其他）。
 * 人物与词条不再预置，用户从空通讯录开始录入。
 */
internal object SeedData {

    suspend fun insertDefaultRelationships(relationshipDao: RelationshipDao, now: Long) {
        Relationship.DEFAULT_NAMES.forEachIndexed { index, name ->
            relationshipDao.insert(
                RelationshipEntity(
                    name = name,
                    colorIndex = index,
                    sortOrder = index,
                    createdAt = now,
                ),
            )
        }
    }
}
