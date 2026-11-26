package com.jfeng2048.memorandum.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 词条分类管理：分类的增删改，以及「有多少条词条在用」的统计。
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: PersonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CategoryEffect>(Channel.BUFFERED)
    val effects: Flow<CategoryEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    fun onEvent(event: CategoryEvent) {
        when (event) {
            CategoryEvent.StartCreate -> _uiState.update {
                it.copy(draft = CategoryDraft(id = null, name = ""))
            }

            is CategoryEvent.StartRename -> {
                val item = _uiState.value.items.firstOrNull { it.id == event.id } ?: return
                _uiState.update {
                    it.copy(draft = CategoryDraft(id = item.id, name = item.name))
                }
            }

            is CategoryEvent.DraftChanged -> _uiState.update { state ->
                state.copy(draft = state.draft?.copy(name = event.value))
            }

            CategoryEvent.ConfirmDraft -> confirmDraft()
            CategoryEvent.DismissDraft -> _uiState.update { it.copy(draft = null) }

            is CategoryEvent.RequestDelete -> {
                val item = _uiState.value.items.firstOrNull { it.id == event.id } ?: return
                _uiState.update { it.copy(pendingDelete = item) }
            }

            CategoryEvent.ConfirmDelete -> confirmDelete()
            CategoryEvent.DismissDelete -> _uiState.update { it.copy(pendingDelete = null) }
            CategoryEvent.Retry -> observe()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = combine(
            repository.observeCategories(),
            repository.observeCategoryUsageCounts(),
        ) { categories, usageCounts ->
            categories.map { category ->
                CategoryItemUi(
                    id = category.id,
                    name = category.name,
                    colorIndex = category.colorIndex,
                    entryCount = usageCounts[category.id] ?: 0,
                )
            }
        }
            .onStart {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "未知错误")
                }
            }
            .onEach { items ->
                _uiState.update { it.copy(isLoading = false, items = items, errorMessage = null) }
            }
            .launchIn(viewModelScope)
    }

    private fun confirmDraft() {
        val draft = _uiState.value.draft ?: return
        val name = draft.name.trim()
        if (name.isEmpty()) return
        val duplicated = _uiState.value.items.any { it.id != draft.id && it.name == name }
        if (duplicated) {
            viewModelScope.launch { toast("已经有「$name」这个分类了") }
            return
        }
        viewModelScope.launch {
            runCatching {
                if (draft.id == null) {
                    repository.createCategory(name)
                } else {
                    repository.renameCategory(draft.id, name)
                }
            }.onSuccess {
                _uiState.update { it.copy(draft = null) }
                toast(if (draft.id == null) "已添加「$name」" else "已重命名")
            }.onFailure { error ->
                toast(error.message ?: "保存失败")
            }
        }
    }

    private fun confirmDelete() {
        val item = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            val deleted = runCatching { repository.deleteCategory(item.id) }
                .getOrDefault(false)
            _uiState.update { it.copy(pendingDelete = null) }
            toast(
                when {
                    deleted -> "已删除「${item.name}」"
                    item.entryCount > 0 -> "还有 ${item.entryCount} 条词条在用，先改掉再删"
                    else -> "至少要保留一个分类"
                },
            )
        }
    }

    private suspend fun toast(message: String) {
        _effects.send(CategoryEffect.ShowToast(message))
    }
}
