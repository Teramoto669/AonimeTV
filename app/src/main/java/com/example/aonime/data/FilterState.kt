package com.example.aonime.data

data class FilterState(
    val keyword: String? = null,
    val genre: List<String> = emptyList(),
    val termType: String? = null,
    val status: String? = null,
    val sort: String? = null,
    val season: String? = null,
    val year: String? = null,
    val language: String? = null,
    val rating: List<String> = emptyList()
) {
    fun toQueryMap(): Map<String, String?> {
        return mapOf(
            "keyword" to keyword?.takeIf { it.isNotBlank() },
            "genre" to genre.joinToString(",").takeIf { it.isNotBlank() },
            "termType" to termType,
            "status" to status,
            "sort" to sort,
            "season" to season,
            "year" to year,
            "language" to language,
            "rating" to rating.joinToString(",").takeIf { it.isNotBlank() }
        )
    }
}
