package com.example.aonime.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aonime.data.AnimeCard
import com.example.aonime.data.AonimeRepository
import com.example.aonime.data.FilterState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<AnimeCard>, val total: Int, val hasNextPage: Boolean = false) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: AonimeRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var isFetching = false
    private var hasNext = true
    private val currentItems = mutableListOf<AnimeCard>()

    init {
        _query
            .debounce(400L)
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isBlank() && _filterState.value == FilterState()) {
                    _uiState.value = SearchUiState.Idle
                } else if (q.isNotBlank() && q.length < 2) {
                    if (_filterState.value != FilterState()) {
                        search(1)
                    }
                } else {
                    search(1)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun setFilterState(state: FilterState) {
        _filterState.value = state
        search(1)
    }

    fun loadNextPage() {
        if (!isFetching && hasNext && _uiState.value is SearchUiState.Success) {
            search(currentPage + 1)
        }
    }

    private fun search(page: Int) {
        val q = _query.value
        val state = _filterState.value

        if (q.isBlank() && state == FilterState()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        if (isFetching) return
        isFetching = true

        viewModelScope.launch {
            if (page == 1) {
                _uiState.value = SearchUiState.Loading
                currentItems.clear()
            }
            currentPage = page

            repository.filterAnime(
                keyword = q.takeIf { it.isNotBlank() },
                genre = state.genre.takeIf { it.isNotEmpty() },
                season = state.season,
                year = state.year,
                termType = state.termType,
                status = state.status,
                language = state.language,
                rating = state.rating.takeIf { it.isNotEmpty() },
                sort = state.sort,
                page = page
            ).fold(
                onSuccess = { result ->
                    val newItems = result.results ?: emptyList()
                    currentItems.addAll(newItems)
                    hasNext = (result.hasNextPage ?: true) && newItems.isNotEmpty()
                    _uiState.value = SearchUiState.Success(
                        results = currentItems.toList(),
                        total = 0, // Using 0 because FilterResult doesn't return totalResults
                        hasNextPage = hasNext,
                    )
                },
                onFailure = { 
                    if (page == 1) {
                        _uiState.value = SearchUiState.Error(it.message ?: "Search failed") 
                    } else {
                        hasNext = false
                        _uiState.value = SearchUiState.Success(
                            results = currentItems.toList(),
                            total = 0,
                            hasNextPage = hasNext,
                        )
                    }
                },
            )
            isFetching = false
        }
    }
}
