package com.jfeng2048.memorandum.data.seed

import com.jfeng2048.memorandum.data.local.dao.EntryDao
import com.jfeng2048.memorandum.data.local.dao.PersonDao
import com.jfeng2048.memorandum.data.local.dao.RelationshipDao
import com.jfeng2048.memorandum.data.local.entity.EntryEntity
import com.jfeng2048.memorandum.data.local.entity.PersonEntity
import com.jfeng2048.memorandum.data.local.entity.RelationshipEntity
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Relationship

/**
 * 演示数据：与原型里的六个人、二十四条词条一一对应。
 * 时间按「距今多少天 / 小时」生成，保证首次打开时列表上能看到「今天 / 昨天 / 11.20」这样的相对时间。
 */
internal object SeedData {

    private const val HOUR = 3_600_000L
    private const val DAY = 24 * HOUR

    private data class SeedEntry(
        val title: String,
        val category: EntryCategory,
        val content: String,
        val createdDaysAgo: Int,
        val updatedHoursAgo: Long,
    )

    private data class SeedPerson(
        val name: String,
        /** 关系名，写入时按名字映射到关系表主键 */
        val relationship: String,
        val gender: Gender,
        val bio: String,
        val createdDaysAgo: Int,
        val updatedHoursAgo: Long,
        val entries: List<SeedEntry>,
    )

    private val people = listOf(
        SeedPerson(
            name = "陈知远",
            relationship = "朋友",
            gender = Gender.MALE,
            bio = "大学室友，做建筑设计。周三晚上通常在城西球场。",
            createdDaysAgo = 250,
            updatedHoursAgo = 6,
            entries = listOf(
                SeedEntry("篮球", EntryCategory.HOBBY, "每周三晚在城西打半场，穿 24 号。膝盖有旧伤，别约太激烈的。", 250, 6),
                SeedEntry("建筑设计", EntryCategory.CAREER, "在一家做旧改的小事务所，最近跟一个学校改造项目。", 250, 30),
                SeedEntry("不吃香菜", EntryCategory.DIET, "点外卖要备注，火锅蘸料也不放。", 200, 120),
                SeedEntry("3 月 12 日生日", EntryCategory.DATE, "去年送的是一双球鞋，今年可以换别的。", 250, 168),
                SeedEntry("慢热", EntryCategory.PERSONALITY, "第一次见面话不多，熟了之后停不下来。", 230, 240),
            ),
        ),
        SeedPerson(
            name = "林嘉禾",
            relationship = "同事",
            gender = Gender.FEMALE,
            bio = "Android 组同事，管性能和启动优化。说话很快。",
            createdDaysAgo = 170,
            updatedHoursAgo = 30,
            entries = listOf(
                SeedEntry("Android 开发工程师", EntryCategory.CAREER, "负责性能与启动优化，Kotlin 和 Compose 都很熟。", 170, 30),
                SeedEntry("浅烘手冲", EntryCategory.DIET, "不加糖不加奶。下午三点后不喝，会睡不着。", 168, 40),
                SeedEntry("六点起床", EntryCategory.LIFESTYLE, "先去跑五公里，九点前一定在工位。", 160, 60),
                SeedEntry("浙江大学 计算机", EntryCategory.EDUCATION, "2016 级，和我在同一个社团。", 140, 100),
                SeedEntry("猫叫「电量」", EntryCategory.HOBBY, "一只橘猫，照片存在她工位显示器上。", 100, 140),
            ),
        ),
        SeedPerson(
            name = "苏晚",
            relationship = "同学",
            gender = Gender.FEMALE,
            bio = "高中同学，现在在杭州画插画。不太回消息，但会寄明信片。",
            createdDaysAgo = 280,
            updatedHoursAgo = 52,
            entries = listOf(
                SeedEntry("插画师", EntryCategory.CAREER, "接书籍和杂志的活，最近在画一本植物图鉴。", 280, 52),
                SeedEntry("街头摄影", EntryCategory.HOBBY, "随身一台 X100V，只拍傍晚和下雨天。", 275, 80),
                SeedEntry("内向但不怕生", EntryCategory.PERSONALITY, "人多的场合话少，一对一能聊很久。", 260, 120),
                SeedEntry("11 月 2 日生日", EntryCategory.DATE, "去年寄了一张她画的明信片。", 260, 180),
            ),
        ),
        SeedPerson(
            name = "周叙白",
            relationship = "客户",
            gender = Gender.MALE,
            bio = "甲方项目负责人。沟通直接，不喜欢电话。",
            createdDaysAgo = 80,
            updatedHoursAgo = 20,
            entries = listOf(
                SeedEntry("项目经理", EntryCategory.CAREER, "甲方这边的负责人，节点卡得很死。", 80, 20),
                SeedEntry("沟通直接", EntryCategory.PERSONALITY, "不喜欢寒暄，邮件一句话能说完就一句话。", 78, 50),
                SeedEntry("不吃辣", EntryCategory.DIET, "团建点菜要留两个不辣的。", 70, 90),
            ),
        ),
        SeedPerson(
            name = "外婆",
            relationship = "家人",
            gender = Gender.FEMALE,
            bio = "住临安。五点起床，春天一定做腌笃鲜。",
            createdDaysAgo = 320,
            updatedHoursAgo = 40,
            entries = listOf(
                SeedEntry("腌笃鲜", EntryCategory.DIET, "春天一定做，咸肉提前两天泡。", 320, 40),
                SeedEntry("五点起床", EntryCategory.LIFESTYLE, "先去菜场，回来才叫我们起床。", 320, 70),
                SeedEntry("膝盖不好", EntryCategory.OTHER, "上楼要扶栏杆，别让她拎重的东西。", 300, 110),
                SeedEntry("农历八月十六生日", EntryCategory.DATE, "今年是十月六号，记得打电话。", 300, 150),
            ),
        ),
        SeedPerson(
            name = "何岸",
            relationship = "同事",
            gender = Gender.MALE,
            bio = "产品经理，健身，午饭只吃轻食。",
            createdDaysAgo = 45,
            updatedHoursAgo = 74,
            entries = listOf(
                SeedEntry("产品经理", EntryCategory.CAREER, "负责这一版的需求，改稿很快。", 45, 74),
                SeedEntry("健身", EntryCategory.HOBBY, "一周五练，午饭只吃轻食。", 44, 90),
                SeedEntry("咖啡因不耐受", EntryCategory.DIET, "只喝 decaf，下午茶别点美式。", 40, 120),
            ),
        ),
    )

    suspend fun insert(
        personDao: PersonDao,
        relationshipDao: RelationshipDao,
        entryDao: EntryDao,
        now: Long,
    ) {
        // 先写入默认关系，后续人物通过「名字 → 主键」建立外键
        val relationshipIds = Relationship.DEFAULT_NAMES.mapIndexed { index, name ->
            name to relationshipDao.insert(
                RelationshipEntity(
                    name = name,
                    colorIndex = index,
                    sortOrder = index,
                    createdAt = now,
                ),
            )
        }.toMap()

        people.forEach { seed ->
            val personId = personDao.insert(
                PersonEntity(
                    name = seed.name,
                    relationshipId = relationshipIds.getValue(seed.relationship),
                    gender = seed.gender.label,
                    description = seed.bio,
                    createdAt = now - seed.createdDaysAgo * DAY,
                    updatedAt = now - seed.updatedHoursAgo * HOUR,
                ),
            )
            seed.entries.forEach { entry ->
                entryDao.insert(
                    EntryEntity(
                        personId = personId,
                        title = entry.title,
                        category = entry.category.label,
                        content = entry.content,
                        createdAt = now - entry.createdDaysAgo * DAY,
                        updatedAt = now - entry.updatedHoursAgo * HOUR,
                    ),
                )
            }
        }
    }
}
