package com.jfeng2048.memorandum.feature.personform

import com.jfeng2048.memorandum.domain.model.Gender
import com.jfeng2048.memorandum.domain.model.Relationship

data class PersonFormUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    /** 关系选项来自用户维护的关系表 */
    val relationships: List<Relationship> = emptyList(),
    val selectedRelationshipIds: Set<Long> = emptySet(),
    val gender: Gender = Gender.UNKNOWN,
    val bio: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val primarySelectedRelationship: Relationship?
        get() = relationships.firstOrNull { it.id in selectedRelationshipIds }

    val canSave: Boolean get() = name.isNotBlank() && selectedRelationshipIds.isNotEmpty() && !isSaving
}

sealed interface PersonFormEvent {
    data class NameChanged(val value: String) : PersonFormEvent
    data class RelationshipToggled(val relationshipId: Long) : PersonFormEvent
    data class GenderChanged(val gender: Gender) : PersonFormEvent
    data class BioChanged(val value: String) : PersonFormEvent
    data object Save : PersonFormEvent
    data object Retry : PersonFormEvent
}

sealed interface PersonFormEffect {
    /** 新建成功后进入人物详情 */
    data class Created(val personId: Long) : PersonFormEffect

    /** 编辑保存成功，返回上一页 */
    data object Saved : PersonFormEffect

    data class ShowToast(val message: String) : PersonFormEffect
}
