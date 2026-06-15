package com.example.aonime.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aonime.data.AonimeRepository
import com.example.aonime.data.WatchSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data class Ready(
        val streamUrl: String, 
        val subtitles: List<com.example.aonime.data.SubtitleTrack> = emptyList(),
        val availableSources: List<WatchSource> = emptyList(),
        val activeSource: WatchSource? = null
    ) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}

class PlayerViewModel(private val repository: AonimeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun load(slug: String, ep: String) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            repository.watchEpisode(slug, ep).fold(
                onSuccess = { data ->
                    try {
                        val gson = Gson()
                        val sourcesJson = gson.toJson(data["sources"])
                        val listType = object : TypeToken<List<WatchSource>>() {}.type
                        val sources: List<WatchSource>? = gson.fromJson(sourcesJson, listType)
                        
                        if (!sources.isNullOrEmpty()) {
                            // Filter out duplicates based on server name and type
                            val distinctSources = sources.distinctBy { "${it.server}_${it.type}" }
                            
                            // Default to first sub source
                            val defaultSource = distinctSources.firstOrNull { it.type == "sub" } ?: distinctSources.first()
                            
                            val streamUrl = defaultSource.proxyUrl ?: defaultSource.m3u8 ?: defaultSource.url ?: ""
                            _uiState.value = PlayerUiState.Ready(
                                streamUrl = streamUrl,
                                subtitles = defaultSource.tracks ?: emptyList(),
                                availableSources = distinctSources,
                                activeSource = defaultSource
                            )
                        } else {
                            // Fallback to recursive search if sources array is not found
                            val streamUrl = extractStreamUrl(data)
                            val subtitles = extractSubtitles(data)
                            if (streamUrl != null) {
                                _uiState.value = PlayerUiState.Ready(streamUrl, subtitles)
                            } else {
                                _uiState.value = PlayerUiState.Error("No stream available")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback
                        val streamUrl = extractStreamUrl(data)
                        val subtitles = extractSubtitles(data)
                        if (streamUrl != null) {
                            _uiState.value = PlayerUiState.Ready(streamUrl, subtitles)
                        } else {
                            _uiState.value = PlayerUiState.Error("No stream available")
                        }
                    }
                },
                onFailure = {
                    _uiState.value = PlayerUiState.Error(it.message ?: "Failed to load stream")
                },
            )
        }
    }

    fun selectSource(source: WatchSource) {
        val state = _uiState.value
        if (state is PlayerUiState.Ready) {
            val streamUrl = source.proxyUrl ?: source.m3u8 ?: source.url ?: ""
            _uiState.value = state.copy(
                streamUrl = streamUrl,
                subtitles = source.tracks ?: emptyList(),
                activeSource = source
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractStreamUrl(data: Map<String, Any?>): String? {
        for (key in data.keys) {
            val serverData = data[key]
            if (serverData is Map<*, *>) {
                val url = serverData["url"] as? String
                if (!url.isNullOrBlank() && (url.contains("m3u8") || url.contains("http"))) return url
            }
            if (serverData is List<*>) {
                val first = serverData.firstOrNull()
                if (first is Map<*, *>) {
                    val url = first["url"] as? String
                    if (!url.isNullOrBlank()) return url
                }
            }
        }
        return findUrlRecursive(data)
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractSubtitles(data: Map<String, Any?>): List<com.example.aonime.data.SubtitleTrack> {
        val tracksList = mutableListOf<com.example.aonime.data.SubtitleTrack>()
        fun searchForTracks(map: Map<String, Any?>) {
            for ((k, v) in map) {
                if ((k == "tracks" || k == "subtitles") && v is List<*>) {
                    for (item in v) {
                        if (item is Map<*, *>) {
                            val file = item["file"] as? String
                            val label = item["label"] as? String
                            val kind = item["kind"] as? String
                            val default = item["default"] as? Boolean
                            val proxyUrl = item["proxyUrl"] as? String
                            if (!file.isNullOrBlank() && (kind == "captions" || kind == "subtitles" || label != null)) {
                                tracksList.add(com.example.aonime.data.SubtitleTrack(label, file, kind, default, proxyUrl))
                            }
                        }
                    }
                } else if (v is Map<*, *>) {
                    searchForTracks(v as Map<String, Any?>)
                } else if (v is List<*>) {
                    for (item in v) {
                        if (item is Map<*, *>) {
                            searchForTracks(item as Map<String, Any?>)
                        }
                    }
                }
            }
        }
        searchForTracks(data)
        return tracksList
    }

    @Suppress("UNCHECKED_CAST")
    private fun findUrlRecursive(map: Map<String, Any?>): String? {
        for ((k, v) in map) {
            if (k == "url" && v is String && v.startsWith("http")) return v
            if (v is Map<*, *>) {
                val result = findUrlRecursive(v as Map<String, Any?>)
                if (result != null) return result
            } else if (v is List<*>) {
                for (item in v) {
                    if (item is Map<*, *>) {
                        val result = findUrlRecursive(item as Map<String, Any?>)
                        if (result != null) return result
                    }
                }
            }
        }
        return null
    }
}
