package com.example.aonime.data

import kotlinx.coroutines.flow.Flow

class AonimeRepository(
    private val api: ApiService = ApiClient.service,
    private val favoriteDao: FavoriteDao,
) {

    // ─── Home ────────────────────────────────────────────────────────────────
    suspend fun getHome(): Result<HomeData> = runCatching {
        api.getHome().data ?: error("No data")
    }

    // ─── Search ──────────────────────────────────────────────────────────────
    suspend fun searchAnime(keyword: String): Result<SearchResult> = runCatching {
        api.searchAnime(keyword).data ?: error("No data")
    }

    // ─── Filter ──────────────────────────────────────────────────────────────
    suspend fun filterAnime(
        keyword: String? = null,
        genre: List<String>? = null,
        season: String? = null,
        year: String? = null,
        termType: String? = null,
        status: String? = null,
        language: String? = null,
        rating: List<String>? = null,
        sort: String? = null,
        page: Int? = null,
    ): Result<FilterResult> = runCatching {
        api.filterAnime(keyword, genre, season, year, termType, status, language, rating, sort, page).data ?: error("No data")
    }

    // ─── Latest ──────────────────────────────────────────────────────────────
    suspend fun getLatest(type: String? = null, page: Int? = null): Result<List<AnimeCard>> = runCatching {
        api.getLatest(type, page).data ?: error("No data")
    }

    // ─── Status ──────────────────────────────────────────────────────────────
    suspend fun getByStatus(type: String? = null, page: Int? = null): Result<List<AnimeCard>> = runCatching {
        api.getByStatus(type, page).data ?: error("No data")
    }

    // ─── Genre ───────────────────────────────────────────────────────────────
    suspend fun getByGenre(genre: String, page: Int? = null): Result<List<AnimeCard>> = runCatching {
        api.getByGenre(genre, page).data ?: error("No data")
    }

    // ─── Type ────────────────────────────────────────────────────────────────
    suspend fun getByType(type: String, page: Int? = null): Result<List<AnimeCard>> = runCatching {
        api.getByType(type, page).data ?: error("No data")
    }

    // ─── Anime Detail ────────────────────────────────────────────────────────
    suspend fun getAnimeDetail(slug: String): Result<AnimeDetail> = runCatching {
        api.getAnimeDetail(slug).data ?: error("No data")
    }

    // ─── Episode List ────────────────────────────────────────────────────────
    suspend fun getEpisodes(slug: String, start: String? = null, end: String? = null): Result<EpisodeListData> = runCatching {
        api.getAnimeEpisodes(slug, start, end).data ?: error("No data")
    }

    // ─── Watch ───────────────────────────────────────────────────────────────
    suspend fun watchEpisode(slug: String, ep: String): Result<Map<String, Any?>> = runCatching {
        api.watchEpisode(slug, ep).data ?: error("No data")
    }

    // ─── Favorites (Room) ────────────────────────────────────────────────────
    fun getAllFavorites(): Flow<List<FavoriteAnime>> = favoriteDao.getAllFavorites()
    fun isFavorite(slug: String): Flow<Boolean> = favoriteDao.isFavorite(slug)
    suspend fun addFavorite(anime: FavoriteAnime) = favoriteDao.addFavorite(anime)
    suspend fun removeFavorite(slug: String) = favoriteDao.removeBySlug(slug)
}
