package com.example.aonime.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aonime.data.AonimeRepository
import com.example.aonime.data.HomeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repository: AonimeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getHome().fold(
                onSuccess = { _uiState.value = HomeUiState.Success(it) },
                onFailure = { _uiState.value = HomeUiState.Error(it.message ?: "Unknown error") },
            )
        }
    }
}
