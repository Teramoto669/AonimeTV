package com.example.aonime.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aonime.data.AonimeRepository
import com.example.aonime.data.FavoriteAnime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: AonimeRepository) : ViewModel() {

    val favorites: StateFlow<List<FavoriteAnime>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(slug: String) {
        viewModelScope.launch {
            repository.removeFavorite(slug)
        }
    }
}
