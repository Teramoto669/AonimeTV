package com.example.aonime.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aonime.data.AnimeDetail
import com.example.aonime.data.AonimeRepository
import com.example.aonime.data.Episode
import com.example.aonime.data.FavoriteAnime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(
        val detail: AnimeDetail,
        val episodes: List<Episode>,
        val totalEpisodes: Int = 0
    ) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(private val repository: AonimeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _selectedRange = MutableStateFlow<Pair<String?, String?>>(null to null)
    val selectedRange: StateFlow<Pair<String?, String?>> = _selectedRange.asStateFlow()

    private val _isEpisodesLoading = MutableStateFlow(false)
    val isEpisodesLoading: StateFlow<Boolean> = _isEpisodesLoading.asStateFlow()

    fun load(slug: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            _selectedRange.value = null to null
            _isEpisodesLoading.value = false
            val detailResult = repository.getAnimeDetail(slug)
            val episodeResult = repository.getEpisodes(slug)
            detailResult.fold(
                onSuccess = { detail ->
                    val episodes = episodeResult.getOrNull()?.episodes ?: emptyList()
                    val total = detail.episodeCount ?: episodes.size
                    _uiState.value = DetailUiState.Success(detail, episodes, total)
                },
                onFailure = { _uiState.value = DetailUiState.Error(it.message ?: "Failed to load") },
            )
            // Collect favorite status
            repository.isFavorite(slug).collect { _isFavorite.value = it }
        }
    }

    fun setEpisodeRange(slug: String, start: String?, end: String?) {
        _selectedRange.value = start to end
        viewModelScope.launch {
            _isEpisodesLoading.value = true
            val episodeResult = repository.getEpisodes(slug, start, end)
            val currentState = _uiState.value
            if (currentState is DetailUiState.Success) {
                val newEpisodes = episodeResult.getOrNull()?.episodes ?: emptyList()
                _uiState.value = currentState.copy(episodes = newEpisodes)
            }
            _isEpisodesLoading.value = false
        }
    }

    fun toggleFavorite(slug: String, detail: AnimeDetail) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                repository.removeFavorite(slug)
            } else {
                repository.addFavorite(
                    FavoriteAnime(
                        slug = slug,
                        title = detail.title ?: "",
                        image = detail.image ?: "",
                        type = detail.type ?: "",
                        score = detail.malScore?.toString() ?: "",
                    )
                )
            }
        }
    }
}
