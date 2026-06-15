package com.example.aonime.ui.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.aonime.data.AnimeDetail
import com.example.aonime.data.Episode
import com.example.aonime.theme.CardSurface
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.FocusedBorder
import com.example.aonime.theme.TextMuted
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    slug: String,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onPlayEpisode: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    LaunchedEffect(slug) { viewModel.load(slug) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack),
    ) {
        when (val state = uiState) {
            DetailUiState.Loading -> {
                CircularProgressIndicator(
                    color = Violet,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            is DetailUiState.Error -> {
                Text(
                    "Error: ${state.message}",
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            is DetailUiState.Success -> {
                DetailContent(
                    detail = state.detail,
                    episodes = state.episodes,
                    isFavorite = isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite(slug, state.detail) },
                    onPlayEpisode = { ep -> onPlayEpisode(slug, ep) },
                )
            }
        }

        // Back button overlay
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    detail: AnimeDetail,
    episodes: List<Episode>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPlayEpisode: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Hero banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            ) {
                AsyncImage(
                    model = detail.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DeepBlack),
                                startY = 100f,
                            )
                        )
                )
            }
        }

        // Info section
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Title + Bookmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = detail.title ?: "",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp,
                        modifier = Modifier.weight(1f),
                    )
                    var isBookmarkFocused by remember { mutableStateOf(false) }
                    val bookmarkScale by animateFloatAsState(
                        targetValue = if (isBookmarkFocused) 1.2f else 1f,
                        animationSpec = tween(150),
                        label = "bookmarkScale"
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .scale(bookmarkScale)
                            .onFocusChanged { isBookmarkFocused = it.isFocused }
                            .background(
                                if (isBookmarkFocused) Violet.copy(alpha = 0.2f) else Color.Transparent, 
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isFavorite || isBookmarkFocused) VioletLight else TextSecondary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Metadata row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (detail.malScore != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = VioletLight, modifier = Modifier.size(14.dp))
                            Text(detail.malScore.toString(), color = VioletLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (!detail.type.isNullOrBlank()) Text("•  ${detail.type}", color = TextSecondary, fontSize = 13.sp)
                    if (!detail.status.isNullOrBlank()) Text("•  ${detail.status}", color = TextSecondary, fontSize = 13.sp)
                    if (!detail.duration.isNullOrBlank()) {
                        val cleanDur = detail.duration.replace(Regex("(?i)\\bmin\\b"), "").trim()
                        Text("•  $cleanDur", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Genre chips
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    detail.genres?.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Violet.copy(alpha = 0.2f))
                                .border(1.dp, Violet.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(genre, color = VioletLight, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Synopsis
                if (!detail.synopsis.isNullOrBlank()) {
                    Text("Synopsis", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = detail.synopsis,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Studios
                if (!detail.studios.isNullOrEmpty()) {
                    Text("Studios", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(detail.studios.joinToString(", "), color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                }

                // Episode section header
                Text(
                    "Episodes (${episodes.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        // Episode list
        items(episodes) { episode ->
            EpisodeItem(
                episode = episode,
                onClick = { episode.number?.let { onPlayEpisode(it) } },
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun EpisodeItem(
    episode: Episode,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(150),
        label = "episodeScale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isFocused) Violet.copy(alpha = 0.2f) else CardSurface)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) FocusedBorder else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Episode number box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Violet.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = episode.number ?: "?",
                color = VioletLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episode.title?.takeIf { it.isNotBlank() } ?: "Episode ${episode.number}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (episode.hasSub == true) {
                    Text("SUB", color = VioletLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (episode.hasDub == true) {
                    Text("DUB", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}
