package com.jfeng2048.memorandum.feature.personform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfeng2048.memorandum.core.navigation.Routes
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 新建 / 编辑人物共用一套表单状态：
 * - 路由里没有 personId → 新建
 * - 路由里带 personId → 编辑，先加载一次做预填
 */
@HiltViewModel
class PersonFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PersonRepository,
) : ViewModel() {

    private val personId: Long? = savedStateHandle.get<Long>(Routes.ARG_PERSON_ID)

    private val _uiState = MutableStateFlow(
        PersonFormUiState(
            isLoading = personId != null,
            isEditing = personId != null,
        ),
    )
    val uiState: StateFlow<PersonFormUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PersonFormEffect>(Channel.BUFFERED)
    val effects: Flow<PersonFormEffect> = _effects.receiveAsFlow()

    init {
        observeRelationships()
        if (personId != null) load()
    }

    fun onEvent(event: PersonFormEvent) {
        when (event) {
            is PersonFormEvent.NameChanged -> _uiState.update {
                it.copy(name = event.value)
            }

            is PersonFormEvent.RelationshipChanged -> _uiState.update {
                it.copy(relationshipId = event.relationshipId)
            }

            is PersonFormEvent.GenderChanged -> _uiState.update {
                it.copy(gender = event.gender)
            }

            is PersonFormEvent.BioChanged -> _uiState.update {
                it.copy(bio = event.value)
            }

            PersonFormEvent.Save -> save()
            PersonFormEvent.Retry -> if (personId != null) load()
        }
    }

    /**
     * 关系选项实时跟随关系表。
     * 新建时默认选中第一个关系；编辑时 [load] 会覆盖成人物当前关系。
     */
    private fun observeRelationships() {
        repository.observeRelationships()
            .onEach { relationships ->
                _uiState.update { state ->
                    val stillValid = relationships.any { it.id == state.relationshipId }
                    state.copy(
                        relationships = relationships,
                        relationshipId = when {
                            stillValid -> state.relationshipId
                            else -> relationships.firstOrNull()?.id
                        },
                    )
                }
            }
            .catch { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "关系读取失败") }
            }
            .launchIn(viewModelScope)
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.observePerson(personId!!).first() }
                .onSuccess { person ->
                    if (person == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "没有找到这个人物")
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = person.name,
                                relationshipId = person.relationship.id,
                                gender = person.gender,
                                bio = person.bio,
                                errorMessage = null,
                            )
                        }
                    }
                }
                .onFailure { error ->
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
        val relationshipId = state.relationshipId
        if (!state.canSave || relationshipId == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                if (personId == null) {
                    repository.createPerson(
                        name = state.name.trim(),
                        relationshipId = relationshipId,
                        gender = state.gender,
                        bio = state.bio.trim(),
                    )
                } else {
                    repository.updatePerson(
                        personId = personId,
                        name = state.name.trim(),
                        relationshipId = relationshipId,
                        gender = state.gender,
                        bio = state.bio.trim(),
                    )
                    personId
                }
            }.onSuccess { savedId ->
                _uiState.update { it.copy(isSaving = false) }
                _effects.send(
                    if (personId == null) {
                        PersonFormEffect.Created(savedId)
                    } else {
                        PersonFormEffect.Saved
                    },
                )
            }.onFailure { error ->
                val message = error.message ?: "保存失败"
                _uiState.update { it.copy(isSaving = false, errorMessage = message) }
                _effects.send(PersonFormEffect.ShowToast(message))
            }
        }
    }
}
