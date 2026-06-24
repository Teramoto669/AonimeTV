package com.example.aonime.data

import com.google.gson.annotations.SerializedName

// ─── Shared ─────────────────────────────────────────────────────────────────

data class EpisodeStatus(
    val sub: Int? = null,
    val dub: Int? = null,
    val total: Int? = null,
)

data class AnimeCard(
    val id: String? = null,
    val slug: String? = null,
    val title: String? = null,
    val titleJp: String? = null,
    val image: String? = null,
    val href: String? = null,
    val type: String? = null,
    val episodes: EpisodeStatus? = null,
    val date: String? = null,
    val score: Double? = null,
    val totalEpisodes: Int? = null,
    // For top day/week/month
    val rank: Int? = null,
)

// ─── Spotlight Anime (hero banner) ───────────────────────────────────────────

data class SpotlightAnime(
    val slug: String? = null,
    val title: String? = null,
    val titleJp: String? = null,
    val rating: String? = null,
    val quality: String? = null,
    val hasDub: Boolean? = null,
    val hasSub: Boolean? = null,
    val date: String? = null,
    val synopsis: String? = null,
    val watchUrl: String? = null,
    val href: String? = null,
    val image: String? = null,
)

// ─── Anime Detail ─────────────────────────────────────────────────────────────

data class AnimeDetail(
    val id: String? = null,
    val slug: String? = null,
    val title: String? = null,
    val titleJp: String? = null,
    val alternativeTitles: List<String>? = null,
    val image: String? = null,
    val rating: String? = null,
    val quality: String? = null,
    val hasDub: Boolean? = null,
    val hasSub: Boolean? = null,
    val synopsis: String? = null,
    val type: String? = null,
    val premiered: String? = null,
    val aired: String? = null,
    val status: String? = null,
    val genres: List<String>? = null,
    val malScore: Double? = null,
    val duration: String? = null,
    val episodeCount: Int? = null,
    val studios: List<String>? = null,
    val producers: List<String>? = null,
    val watchUrl: String? = null,
    val episodes: EpisodeListData? = null,
    val related: List<RelatedAnime>? = null,
)

data class RelatedAnime(
    val id: String? = null,
    val title: String? = null,
    val titleJp: String? = null,
    val image: String? = null,
    val relation: String? = null,
    val href: String? = null,
    val slug: String? = null,
)

// ─── Episode ──────────────────────────────────────────────────────────────────

data class Episode(
    val number: String? = null,
    val title: String? = null,
    val href: String? = null,
    val id: String? = null,
    val dataIds: String? = null,
    val hasDub: Boolean? = null,
    val hasSub: Boolean? = null,
)

// ─── Home Data ────────────────────────────────────────────────────────────────

data class HomeData(
    val spotlight: List<SpotlightAnime>? = null,
    val latestEpisodes: List<AnimeCard>? = null,
    val newRelease: List<AnimeCard>? = null,
    val newAdded: List<AnimeCard>? = null,
    val justCompleted: List<AnimeCard>? = null,
    val topDay: List<AnimeCard>? = null,
    val topWeek: List<AnimeCard>? = null,
    val topMonth: List<AnimeCard>? = null,
)

// ─── Search Result ────────────────────────────────────────────────────────────

data class SearchResult(
    val keyword: String? = null,
    val totalResults: Int? = null,
    val results: List<AnimeCard>? = null,
)

// ─── Filter Result ────────────────────────────────────────────────────────────

data class FilterResult(
    val results: List<AnimeCard>? = null,
    val currentPage: Int? = null,
    val hasNextPage: Boolean? = null,
)

// ─── Episode List ────────────────────────────────────────────────────────────

data class EpisodeListData(
    val animeId: String? = null,
    val slug: String? = null,
    val episodes: List<Episode>? = null,
)

// ─── Watch Data ───────────────────────────────────────────────────────────────

data class WatchSource(
    val server: String? = null,
    val type: String? = null,
    val url: String? = null,
    val m3u8: String? = null,
    val proxyUrl: String? = null,
    @SerializedName("tracks")
    val tracks: List<SubtitleTrack>? = null,
    val headers: Map<String, String>? = null,
)

data class SubtitleTrack(
    val label: String? = null,
    val file: String? = null,
    val kind: String? = null,
    val default: Boolean? = null,
    val proxyUrl: String? = null,
)

// ─── Generic API Wrappers ─────────────────────────────────────────────────────

data class ApiResponse<T>(
    val ok: Boolean? = null,
    val cached: Boolean? = null,
    val data: T? = null,
)

data class WatchApiResponse(
    val ok: Boolean? = null,
    val cached: Boolean? = null,
    val data: Map<String, Any?>? = null,
)

// ─── Schedule ─────────────────────────────────────────────────────────────────

data class ScheduleDay(
    val day: String? = null,
    val animes: List<AnimeCard>? = null,
)
