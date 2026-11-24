package com.jfeng2048.memorandum.feature.entryform

import com.jfeng2048.memorandum.domain.model.EntryCategory

data class EntryFormUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val personName: String = "",
    val title: String = "",
    val category: EntryCategory = EntryCategory.HOBBY,
    val content: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean get() = title.isNotBlank() && !isSaving
}

sealed interface EntryFormEvent {
    data class TitleChanged(val value: String) : EntryFormEvent
    data class CategoryChanged(val category: EntryCategory) : EntryFormEvent
    data class ContentChanged(val value: String) : EntryFormEvent
    data object Save : EntryFormEvent
}

sealed interface EntryFormEffect {
    data object Saved : EntryFormEffect
    data class ShowToast(val message: String) : EntryFormEffect
}
