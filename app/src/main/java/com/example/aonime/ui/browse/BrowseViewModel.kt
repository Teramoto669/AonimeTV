package com.example.aonime.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aonime.data.AnimeCard
import com.example.aonime.data.AonimeRepository
import com.example.aonime.data.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BrowseUiState {
    data object Loading : BrowseUiState
    data class Success(val items: List<AnimeCard>, val hasNextPage: Boolean = false) : BrowseUiState
    data class Error(val message: String) : BrowseUiState
}

class BrowseViewModel(private val repository: AonimeRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var isFetching = false
    private var hasNext = true
    private val currentItems = mutableListOf<AnimeCard>()

    init { load(1) }

    fun setFilterState(state: FilterState) {
        _filterState.value = state
        load(1)
    }

    fun loadNextPage() {
        if (!isFetching && hasNext) {
            load(currentPage + 1)
        }
    }

    private fun load(page: Int) {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch {
            if (page == 1) {
                _uiState.value = BrowseUiState.Loading
                currentItems.clear()
            }
            
            currentPage = page
            val currentFilter = _filterState.value
            
            val result = repository.filterAnime(
                keyword = currentFilter.keyword?.takeIf { it.isNotBlank() },
                genre = currentFilter.genre.joinToString(",").takeIf { it.isNotBlank() },
                season = currentFilter.season,
                year = currentFilter.year,
                termType = currentFilter.termType,
                status = currentFilter.status,
                language = currentFilter.language,
                rating = currentFilter.rating.joinToString(",").takeIf { it.isNotBlank() },
                sort = currentFilter.sort ?: "latest-updated",
                page = page
            )
            
            result.fold(
                onSuccess = { filterResult ->
                    val newItems = filterResult.results ?: emptyList()
                    currentItems.addAll(newItems)
                    hasNext = newItems.isNotEmpty() // Rely on items presence instead of API's hasNextPage field
                    _uiState.value = BrowseUiState.Success(currentItems.toList(), hasNext)
                },
                onFailure = { 
                    if (page == 1) {
                        _uiState.value = BrowseUiState.Error(it.message ?: "Failed to load") 
                    }
                }
            )
            isFetching = false
        }
    }
}
