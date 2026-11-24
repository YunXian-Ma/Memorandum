package com.jfeng2048.memorandum.feature.relationship

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
 * 关系管理：关系的增删改，以及「有多少人在用」的统计。
 * 只要关系表或人物表发生变化，列表就会自动刷新。
 */
@HiltViewModel
class RelationshipViewModel @Inject constructor(
    private val repository: PersonRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelationshipUiState())
    val uiState: StateFlow<RelationshipUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RelationshipEffect>(Channel.BUFFERED)
    val effects: Flow<RelationshipEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    fun onEvent(event: RelationshipEvent) {
        when (event) {
            RelationshipEvent.StartCreate -> _uiState.update {
                it.copy(draft = RelationshipDraft(id = null, name = ""))
            }

            is RelationshipEvent.StartRename -> {
                val item = _uiState.value.items.firstOrNull { it.id == event.id } ?: return
                _uiState.update {
                    it.copy(draft = RelationshipDraft(id = item.id, name = item.name))
                }
            }

            is RelationshipEvent.DraftChanged -> _uiState.update { state ->
                state.copy(draft = state.draft?.copy(name = event.value))
            }

            RelationshipEvent.ConfirmDraft -> confirmDraft()
            RelationshipEvent.DismissDraft -> _uiState.update { it.copy(draft = null) }

            is RelationshipEvent.RequestDelete -> {
                val item = _uiState.value.items.firstOrNull { it.id == event.id } ?: return
                _uiState.update { it.copy(pendingDelete = item) }
            }

            RelationshipEvent.ConfirmDelete -> confirmDelete()
            RelationshipEvent.DismissDelete -> _uiState.update { it.copy(pendingDelete = null) }
            RelationshipEvent.Retry -> observe()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = combine(
            repository.observeRelationships(),
            repository.observePeople(),
        ) { relationships, people ->
            relationships.map { relationship ->
                RelationshipItemUi(
                    id = relationship.id,
                    name = relationship.name,
                    colorIndex = relationship.colorIndex,
                    peopleCount = people.count { it.relationship.id == relationship.id },
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
            viewModelScope.launch { toast("已经有「$name」这个关系了") }
            return
        }
        viewModelScope.launch {
            runCatching {
                if (draft.id == null) {
                    repository.createRelationship(name)
                } else {
                    repository.renameRelationship(draft.id, name)
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
            val deleted = runCatching { repository.deleteRelationship(item.id) }
                .getOrDefault(false)
            _uiState.update { it.copy(pendingDelete = null) }
            toast(
                when {
                    deleted -> "已删除「${item.name}」"
                    item.peopleCount > 0 -> "还有 ${item.peopleCount} 个人在用，先改掉再删"
                    else -> "至少要保留一个关系"
                },
            )
        }
    }

    private suspend fun toast(message: String) {
        _effects.send(RelationshipEffect.ShowToast(message))
    }
}
