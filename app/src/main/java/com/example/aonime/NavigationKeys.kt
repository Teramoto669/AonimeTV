package com.example.aonime

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SplashNav : NavKey
@Serializable data object HomeNav : NavKey
@Serializable data object SearchNav : NavKey
@Serializable data object BrowseNav : NavKey
@Serializable data object LibraryNav : NavKey

@Serializable data class DetailNav(val slug: String, val title: String = "") : NavKey
@Serializable data class PlayerNav(val slug: String, val ep: String, val title: String = "") : NavKey

// Keep Main for template compatibility
@Serializable data object Main : NavKey
