package com.jfeng2048.memorandum.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfeng2048.memorandum.core.navigation.Routes
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 人物详情：只订阅单个人物的 Flow，词条增删后列表自动刷新。
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PersonRepository,
) : ViewModel() {

    private val personId: Long = savedStateHandle.get<Long>(Routes.ARG_PERSON_ID) ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<DetailEffect>(Channel.BUFFERED)
    val effects: Flow<DetailEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        startObserving()
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.SelectCategory -> _uiState.update {
                it.copy(selectedCategoryId = event.categoryId)
            }

            is DetailEvent.SelectEntry -> _uiState.update {
                it.copy(selectedEntryId = event.entryId)
            }

            is DetailEvent.DeleteEntry -> deleteEntry(event.entryId)
            DetailEvent.DeletePerson -> deletePerson()
            DetailEvent.EditPerson -> _effects.trySend(DetailEffect.EditPerson(personId))
            DetailEvent.AddEntry -> _effects.trySend(DetailEffect.AddEntry(personId))
            is DetailEvent.EditEntry -> _effects.trySend(
                DetailEffect.EditEntry(personId, event.entryId),
            )

            DetailEvent.Back -> _effects.trySend(DetailEffect.Back)
            DetailEvent.Retry -> startObserving()
        }
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = repository.observePerson(personId)
            .onStart {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "未知错误",
                    )
                }
            }
            .onEach { person ->
                _uiState.update { state ->
                    val categoryId = state.selectedCategoryId?.takeIf { selected ->
                        person?.entries?.any { it.category.id == selected } == true
                    }
                    val entryId = state.selectedEntryId?.takeIf { selected ->
                        person?.entries?.any { it.id == selected } == true
                    }
                    state.copy(
                        isLoading = false,
                        person = person,
                        notFound = person == null,
                        selectedCategoryId = categoryId,
                        selectedEntryId = entryId,
                        errorMessage = null,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun deleteEntry(entryId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteEntry(entryId) }
                .onSuccess {
                    _uiState.update { it.copy(selectedEntryId = null) }
                    _effects.send(DetailEffect.ShowToast("已删除这条词条"))
                }
                .onFailure { error ->
                    _effects.send(DetailEffect.ShowToast(error.message ?: "删除失败"))
                }
        }
    }

    private fun deletePerson() {
        viewModelScope.launch {
            runCatching { repository.deletePerson(personId) }
                .onSuccess { _effects.send(DetailEffect.Back) }
                .onFailure { error ->
                    _effects.send(DetailEffect.ShowToast(error.message ?: "删除失败"))
                }
        }
    }
}
