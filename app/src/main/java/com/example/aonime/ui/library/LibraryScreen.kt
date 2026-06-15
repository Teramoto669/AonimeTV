package com.example.aonime.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aonime.data.AnimeCard
import com.example.aonime.data.FavoriteAnime
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.TextMuted
import com.example.aonime.theme.TextPrimary
import com.example.aonime.ui.components.AnimeCardItem

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(24.dp),
    ) {
        Text(
            "My Library",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "${favorites.size} saved",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(20.dp))

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.VideoLibrary,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(72.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Your library is empty", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text("Save anime from the detail page to watch later", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = favorites, key = { it.slug }) { fav ->
                    AnimeCardItem(
                        anime = fav.toAnimeCard(),
                        onClick = { onAnimeClick(fav.slug) },
                    )
                }
            }
        }
    }
}

private fun FavoriteAnime.toAnimeCard() = AnimeCard(
    slug = slug,
    title = title,
    image = image,
    type = type,
    score = score.toDoubleOrNull(),
)
