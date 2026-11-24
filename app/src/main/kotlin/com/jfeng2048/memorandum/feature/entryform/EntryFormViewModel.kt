package com.jfeng2048.memorandum.feature.entryform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfeng2048.memorandum.core.navigation.Routes
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 新建 / 编辑词条；新建时默认选中「兴趣爱好」，与原型一致 */
@HiltViewModel
class EntryFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PersonRepository,
) : ViewModel() {

    private val personId: Long = savedStateHandle.get<Long>(Routes.ARG_PERSON_ID) ?: 0L
    private val entryId: Long? = savedStateHandle.get<Long>(Routes.ARG_ENTRY_ID)

    private val _uiState = MutableStateFlow(
        EntryFormUiState(isEditing = entryId != null),
    )
    val uiState: StateFlow<EntryFormUiState> = _uiState.asStateFlow()

    private val _effects = Channel<EntryFormEffect>(Channel.BUFFERED)
    val effects: Flow<EntryFormEffect> = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onEvent(event: EntryFormEvent) {
        when (event) {
            is EntryFormEvent.TitleChanged -> _uiState.update {
                it.copy(title = event.value)
            }

            is EntryFormEvent.CategoryChanged -> _uiState.update {
                it.copy(category = event.category)
            }

            is EntryFormEvent.ContentChanged -> _uiState.update {
                it.copy(content = event.value)
            }

            EntryFormEvent.Save -> save()
        }
    }

    private fun load() {
        viewModelScope.launch {
            runCatching {
                val person = repository.observePerson(personId).first()
                val entry = entryId?.let { repository.getEntry(it) }
                person to entry
            }.onSuccess { (person, entry) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        personName = person?.name.orEmpty(),
                        title = entry?.title.orEmpty(),
                        category = entry?.category ?: EntryCategory.HOBBY,
                        content = entry?.content.orEmpty(),
                        errorMessage = if (person == null) "没有找到这个人物" else null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "读取失败",
                    )
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                if (entryId == null) {
                    repository.createEntry(
                        personId = personId,
                        title = state.title.trim(),
                        category = state.category,
                        content = state.content.trim(),
                    )
                } else {
                    repository.updateEntry(
                        entryId = entryId,
                        title = state.title.trim(),
                        category = state.category,
                        content = state.content.trim(),
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _effects.send(EntryFormEffect.Saved)
            }.onFailure { error ->
                val message = error.message ?: "保存失败"
                _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                _effects.send(EntryFormEffect.ShowToast(message))
            }
        }
    }
}
