package com.example.aonime.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aonime.data.AnimeCard
import com.example.aonime.data.SpotlightAnime
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletLight
import com.example.aonime.ui.components.AnimeCardItem
import com.example.aonime.ui.components.SpotlightCarousel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAnimeClick: (String) -> Unit,
    onSpotlightClick: (SpotlightAnime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack),
    ) {
        when (val state = uiState) {
            HomeUiState.Loading -> {
                CircularProgressIndicator(
                    color = Violet,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            is HomeUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("⚠️ Failed to load", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = TextSecondary, fontSize = 13.sp)
                }
            }
            is HomeUiState.Success -> {
                val data = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    // Spotlight Carousel
                    item {
                        val spotlights = data.spotlight ?: emptyList()
                        if (spotlights.isNotEmpty()) {
                            SpotlightCarousel(
                                items = spotlights,
                                onItemClick = onSpotlightClick,
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // Row sections
                    val sections = listOf(
                        "Latest Episodes" to (data.latestEpisodes ?: emptyList()),
                        "New Releases" to (data.newRelease ?: emptyList()),
                        "Newly Added" to (data.newAdded ?: emptyList()),
                        "Just Completed" to (data.justCompleted ?: emptyList()),
                        "Top Today" to (data.topDay ?: emptyList()),
                        "Top This Week" to (data.topWeek ?: emptyList()),
                        "Top This Month" to (data.topMonth ?: emptyList()),
                    )

                    sections.forEach { (title, animes) ->
                        if (animes.isNotEmpty()) {
                            item { AnimeRow(title = title, animes = animes, onAnimeClick = onAnimeClick) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeRow(
    title: String,
    animes: List<AnimeCard>,
    onAnimeClick: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        // Section header
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .width(3.dp)
                    .background(VioletLight),
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = animes, key = { it.slug ?: it.hashCode() }) { anime ->
                AnimeCardItem(
                    anime = anime,
                    onClick = { anime.slug?.let { onAnimeClick(it) } },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
