package com.jfeng2048.memorandum.feature.category

/** 词条分类管理页状态 */
data class CategoryUiState(
    val isLoading: Boolean = true,
    val items: List<CategoryItemUi> = emptyList(),
    /** 新建 / 重命名的输入框，null 表示弹窗关闭 */
    val draft: CategoryDraft? = null,
    /** 待确认删除的分类，null 表示没有弹窗 */
    val pendingDelete: CategoryItemUi? = null,
    val errorMessage: String? = null,
)

data class CategoryItemUi(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val entryCount: Int,
)

data class CategoryDraft(
    val id: Long?,
    val name: String,
) {
    val isRename: Boolean get() = id != null
    val canConfirm: Boolean get() = name.isNotBlank()
}

sealed interface CategoryEvent {
    data object StartCreate : CategoryEvent
    data class StartRename(val id: Long) : CategoryEvent
    data class DraftChanged(val value: String) : CategoryEvent
    data object ConfirmDraft : CategoryEvent
    data object DismissDraft : CategoryEvent
    data class RequestDelete(val id: Long) : CategoryEvent
    data object ConfirmDelete : CategoryEvent
    data object DismissDelete : CategoryEvent
    data object Retry : CategoryEvent
}

sealed interface CategoryEffect {
    data class ShowToast(val message: String) : CategoryEffect
}
