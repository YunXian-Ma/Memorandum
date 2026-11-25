package com.jfeng2048.memorandum.feature.detail

import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.PersonEntry
import com.jfeng2048.memorandum.domain.model.Relationship

/** 详情页状态：词条浮层的选中 id 也放在这里，保证旋转屏幕后不丢 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val person: Person? = null,
    val allRelationships: List<Relationship> = emptyList(),
    val relationshipSheetOpen: Boolean = false,
    val draftRelationshipIds: Set<Long> = emptySet(),
    val selectedCategoryId: Long? = null,
    val selectedEntryId: Long? = null,
    val tab: DetailTab = DetailTab.ENTRIES,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
) {
    /** 该人物实际出现过的分类 */
    val categories: List<EntryCategory>
        get() = person?.entries
            ?.map { it.category }
            ?.distinctBy { it.id }
            .orEmpty()

    val visibleEntries: List<PersonEntry>
        get() {
            val all = person?.entries.orEmpty()
            val categoryId = selectedCategoryId
            return if (categoryId == null) all else all.filter { it.category.id == categoryId }
        }

    val selectedEntry: PersonEntry?
        get() = selectedEntryId?.let { id ->
            person?.entries?.firstOrNull { it.id == id }
        }
}

enum class DetailTab { ENTRIES, CONVERSATIONS }

sealed interface DetailEvent {
    data class SwitchTab(val tab: DetailTab) : DetailEvent
    data class DeleteConversation(val conversationId: Long) : DetailEvent
    data class SelectCategory(val categoryId: Long?) : DetailEvent
    data class SelectEntry(val entryId: Long?) : DetailEvent
    data object OpenRelationshipEditor : DetailEvent
    data object DismissRelationshipEditor : DetailEvent
    data class ToggleDraftRelationship(val relationshipId: Long) : DetailEvent
    data object SaveRelationships : DetailEvent
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
