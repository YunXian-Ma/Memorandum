package com.jfeng2048.memorandum.feature.relationship

/** 关系管理页状态 */
data class RelationshipUiState(
    val isLoading: Boolean = true,
    val items: List<RelationshipItemUi> = emptyList(),
    /** 新建 / 重命名的输入框，null 表示弹窗关闭 */
    val draft: RelationshipDraft? = null,
    /** 待确认删除的关系，null 表示没有弹窗 */
    val pendingDelete: RelationshipItemUi? = null,
    val errorMessage: String? = null,
)

data class RelationshipItemUi(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val peopleCount: Int,
)

data class RelationshipDraft(
    val id: Long?,
    val name: String,
) {
    val isRename: Boolean get() = id != null
    val canConfirm: Boolean get() = name.isNotBlank()
}

sealed interface RelationshipEvent {
    data object StartCreate : RelationshipEvent
    data class StartRename(val id: Long) : RelationshipEvent
    data class DraftChanged(val value: String) : RelationshipEvent
    data object ConfirmDraft : RelationshipEvent
    data object DismissDraft : RelationshipEvent
    data class RequestDelete(val id: Long) : RelationshipEvent
    data object ConfirmDelete : RelationshipEvent
    data object DismissDelete : RelationshipEvent
    data object Retry : RelationshipEvent
}

sealed interface RelationshipEffect {
    data class ShowToast(val message: String) : RelationshipEffect
}
