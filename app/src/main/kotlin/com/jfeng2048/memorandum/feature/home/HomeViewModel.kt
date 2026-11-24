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
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PersonRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val relationship = MutableStateFlow<Relationship?>(null)
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
            is HomeEvent.FilterChanged -> relationship.value = event.relationship
            HomeEvent.ToggleSort -> sort.value =
                if (sort.value == HomeSort.RECENT) HomeSort.NAME else HomeSort.RECENT

            HomeEvent.RandomPick -> pickRandom()
            HomeEvent.Retry -> startObserving()
        }
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = combine(query, relationship, sort) { q, r, s ->
            Filters(q, r, s)
        }
            .combine(repository.observePeople()) { filters, people ->
                filters to people
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
            .onEach { (filters, people) ->
                _uiState.value = buildState(people, filters)
            }
            .launchIn(viewModelScope)
    }

    private fun buildState(people: List<Person>, filters: Filters): HomeUiState {
        val visible = PersonFilters.sort(
            PersonFilters.filter(people, filters.query, filters.relationship),
            filters.sort,
        )
        return HomeUiState(
            isLoading = false,
            query = filters.query,
            relationship = filters.relationship,
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
            totalPeople = people.size,
            totalEntries = people.sumOf { it.entryCount },
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
        val relationship: Relationship?,
        val sort: HomeSort,
    )
}
