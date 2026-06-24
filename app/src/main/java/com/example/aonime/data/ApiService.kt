package com.example.aonime.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ─── Home ────────────────────────────────────────────────────────────────
    @GET("home")
    suspend fun getHome(
        @Query("refresh") refresh: String? = null,
    ): ApiResponse<HomeData>

    // ─── Search ──────────────────────────────────────────────────────────────
    @GET("search")
    suspend fun searchAnime(
        @Query("keyword") keyword: String,
        @Query("refresh") refresh: String? = null,
    ): ApiResponse<SearchResult>

    // ─── Filter ──────────────────────────────────────────────────────────────
    @GET("filter")
    suspend fun filterAnime(
        @Query("keyword") keyword: String? = null,
        @Query("genre[]") genre: List<String>? = null,
        @Query("season[]") season: String? = null,
        @Query("year[]") year: String? = null,
        @Query("term_type[]") termType: String? = null,
        @Query("status[]") status: String? = null,
        @Query("language[]") language: String? = null,
        @Query("rating[]") rating: List<String>? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = null,
    ): ApiResponse<FilterResult>

    // ─── Latest ──────────────────────────────────────────────────────────────
    @GET("latest")
    suspend fun getLatest(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
    ): ApiResponse<List<AnimeCard>>

    // ─── Browse by Status ────────────────────────────────────────────────────
    @GET("status")
    suspend fun getByStatus(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
    ): ApiResponse<List<AnimeCard>>

    // ─── Browse by Genre ─────────────────────────────────────────────────────
    @GET("genre/{genre}")
    suspend fun getByGenre(
        @Path("genre") genre: String,
        @Query("page") page: Int? = null,
    ): ApiResponse<List<AnimeCard>>

    // ─── Browse by Type ──────────────────────────────────────────────────────
    @GET("type/{type}")
    suspend fun getByType(
        @Path("type") type: String,
        @Query("page") page: Int? = null,
    ): ApiResponse<List<AnimeCard>>

    // ─── Anime Detail ────────────────────────────────────────────────────────
    @GET("anime/{slug}")
    suspend fun getAnimeDetail(
        @Path("slug") slug: String,
        @Query("start") start: Int? = null,
        @Query("end") end: Int? = null,
    ): ApiResponse<AnimeDetail>

    // ─── Episode List ────────────────────────────────────────────────────────
    @GET("anime/{slug}/episodes")
    suspend fun getAnimeEpisodes(
        @Path("slug") slug: String,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
    ): ApiResponse<EpisodeListData>

    // ─── Watch ───────────────────────────────────────────────────────────────
    @GET("watch/{slug}")
    suspend fun watchEpisode(
        @Path("slug") slug: String,
        @Query("ep") ep: String,
        @Query("stream") stream: Boolean = false,
    ): WatchApiResponse

    // ─── Schedule ────────────────────────────────────────────────────────────
    @GET("schedule")
    suspend fun getSchedule(
        @Query("refresh") refresh: String? = null,
    ): ApiResponse<List<ScheduleDay>>
}
