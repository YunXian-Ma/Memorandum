package com.jfeng2048.memorandum.domain.model

/**
 * 人物关系：由用户自己维护，可新增、改名、删除。
 * 首次启动会写入 [DEFAULT_NAMES] 里的六项，与原型保持一致。
 */
data class Relationship(
    val id: Long,
    val name: String,
    /** 展示用的色板序号，新增关系时按现有数量轮转 */
    val colorIndex: Int,
) {
    companion object {
        /** 默认关系，顺序决定默认色板序号 */
        val DEFAULT_NAMES = listOf("朋友", "同事", "家人", "同学", "客户", "其他")

        /** 主题里可用的关系配色数量，新增关系时按此取模 */
        const val COLOR_COUNT = 8

        /** 关系缺失时的兜底展示（理论上外键保证不会出现） */
        val Unknown = Relationship(id = 0L, name = "未分组", colorIndex = 5)
    }
}
