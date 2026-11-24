package com.jfeng2048.memorandum.feature.home

import com.jfeng2048.memorandum.domain.model.HomeSort
import com.jfeng2048.memorandum.domain.model.Relationship

/** 首页 UI 状态：页面只读它，不自己拼业务 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val relationship: Relationship? = null,
    val sort: HomeSort = HomeSort.RECENT,
    val people: List<PersonCardUi> = emptyList(),
    val totalPeople: Int = 0,
    val totalEntries: Int = 0,
    val errorMessage: String? = null,
)

/** 列表行的展示模型，避免把整棵词条树带进 Composable */
data class PersonCardUi(
    val id: Long,
    val name: String,
    val relationship: Relationship,
    val bio: String,
    val entryCount: Int,
    val updatedAt: Long,
)

sealed interface HomeEvent {
    data class QueryChanged(val value: String) : HomeEvent
    data class FilterChanged(val relationship: Relationship?) : HomeEvent
    data object ToggleSort : HomeEvent

    /** 随便翻一个：随机进入某个人物 */
    data object RandomPick : HomeEvent

    data object Retry : HomeEvent
}

sealed interface HomeEffect {
    data class OpenPerson(val personId: Long) : HomeEffect
}
