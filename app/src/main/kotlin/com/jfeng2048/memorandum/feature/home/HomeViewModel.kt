package com.jfeng2048.memorandum.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfeng2048.memorandum.domain.model.HomeSort
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.model.Relationship
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import com.jfeng2048.memorandum.domain.usecase.PersonFilters
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 首页：组合「筛选条件」与「数据库 Flow」，单向数据流。
 * 搜索、关系筛选、排序全部在内存里完成，数据量是本地通讯录级别，足够快。
 * 关系筛选条来自关系表，用户新增/删除关系后会自动刷新。
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PersonRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedRelationshipId = MutableStateFlow<Long?>(null)
    private val sort = MutableStateFlow(HomeSort.RECENT)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = _effects.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        startObserving()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.QueryChanged -> query.value = event.value
            is HomeEvent.FilterChanged -> selectedRelationshipId.value = event.relationshipId
            HomeEvent.ToggleSort -> sort.value =
                if (sort.value == HomeSort.RECENT) HomeSort.NAME else HomeSort.RECENT

            HomeEvent.RandomPick -> pickRandom()
            HomeEvent.Retry -> startObserving()
        }
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = combine(
            query,
            selectedRelationshipId,
            sort,
            repository.observePeople(),
            repository.observeRelationships(),
        ) { q, relationshipId, s, people, relationships ->
            Filters(q, relationshipId, s) to PageData(people, relationships)
        }
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
            .onEach { (filters, data) ->
                _uiState.value = buildState(data, filters)
            }
            .launchIn(viewModelScope)
    }

    private fun buildState(data: PageData, filters: Filters): HomeUiState {
        // 选中的关系被删除后自动回到「全部」，避免列表空白又找不到原因
        val selectedId = filters.relationshipId
            ?.takeIf { id -> data.relationships.any { it.id == id } }
        val visible = PersonFilters.sort(
            PersonFilters.filter(data.people, filters.query, selectedId),
            filters.sort,
        )
        return HomeUiState(
            isLoading = false,
            query = filters.query,
            selectedRelationshipId = selectedId,
            relationships = data.relationships,
            sort = filters.sort,
            people = visible.map { person ->
                PersonCardUi(
                    id = person.id,
                    name = person.name,
                    relationship = person.relationship,
                    bio = person.bio,
                    entryCount = person.entryCount,
                    updatedAt = person.updatedAt,
                )
            },
            totalPeople = data.people.size,
            totalEntries = data.people.sumOf { it.entryCount },
        )
    }

    private fun pickRandom() {
        viewModelScope.launch {
            val people = repository.observePeople().first()
            if (people.isEmpty()) return@launch
            _effects.send(HomeEffect.OpenPerson(people.random().id))
        }
    }

    private data class Filters(
        val query: String,
        val relationshipId: Long?,
        val sort: HomeSort,
    )

    private data class PageData(
        val people: List<Person>,
        val relationships: List<Relationship>,
    )
}
