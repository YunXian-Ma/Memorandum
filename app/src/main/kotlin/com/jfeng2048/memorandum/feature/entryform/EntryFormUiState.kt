package com.jfeng2048.memorandum.feature.entryform

import com.jfeng2048.memorandum.domain.model.EntryCategory

data class EntryFormUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val personName: String = "",
    val title: String = "",
    val categories: List<EntryCategory> = emptyList(),
    val categoryId: Long? = null,
    val content: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedCategory: EntryCategory?
        get() = categories.firstOrNull { it.id == categoryId }

    val canSave: Boolean get() = title.isNotBlank() && categoryId != null && !isSaving
}

sealed interface EntryFormEvent {
    data class TitleChanged(val value: String) : EntryFormEvent
    data class CategoryChanged(val categoryId: Long) : EntryFormEvent
    data class ContentChanged(val value: String) : EntryFormEvent
    data object Save : EntryFormEvent
}

sealed interface EntryFormEffect {
    data object Saved : EntryFormEffect
    data class ShowToast(val message: String) : EntryFormEffect
}
