package com.jfeng2048.memorandum.feature.detail

import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry

/** 详情页状态：词条浮层的选中 id 也放在这里，保证旋转屏幕后不丢 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val person: Person? = null,
    val selectedCategory: EntryCategory? = null,
    val selectedEntryId: Long? = null,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
) {
    /** 该人物实际出现过的分类，按分类枚举顺序排列 */
    val categories: List<EntryCategory>
        get() = person?.entries
            ?.map { it.category }
            ?.distinct()
            ?.sortedBy { it.ordinal }
            .orEmpty()

    val visibleEntries: List<PersonEntry>
        get() {
            val all = person?.entries.orEmpty()
            val category = selectedCategory
            return if (category == null) all else all.filter { it.category == category }
        }

    val selectedEntry: PersonEntry?
        get() = selectedEntryId?.let { id ->
            person?.entries?.firstOrNull { it.id == id }
        }
}

sealed interface DetailEvent {
    data class SelectCategory(val category: EntryCategory?) : DetailEvent
    data class SelectEntry(val entryId: Long?) : DetailEvent
    data class DeleteEntry(val entryId: Long) : DetailEvent
    data object DeletePerson : DetailEvent
    data object EditPerson : DetailEvent
    data object AddEntry : DetailEvent
    data class EditEntry(val entryId: Long) : DetailEvent
    data object Back : DetailEvent
    data object Retry : DetailEvent
}

sealed interface DetailEffect {
    data object Back : DetailEffect
    data class EditPerson(val personId: Long) : DetailEffect
    data class AddEntry(val personId: Long) : DetailEffect
    data class EditEntry(val personId: Long, val entryId: Long) : DetailEffect
    data class ShowToast(val message: String) : DetailEffect
}
