package com.example.aonime.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil3.compose.AsyncImage
import com.example.aonime.data.SpotlightAnime
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.FocusedBorder
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletLight
import kotlinx.coroutines.delay

@Composable
fun SpotlightCarousel(
    items: List<SpotlightAnime>,
    onItemClick: (SpotlightAnime) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })

    // Auto scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000L)
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(380.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            SpotlightItem(
                anime = items[page],
                onClick = { onItemClick(items[page]) },
            )
        }

        // Pager dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(items.size) { i ->
                val isSelected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 24.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VioletLight else Color.White.copy(alpha = 0.3f)),
                )
            }
        }
    }
}

@Composable
private fun SpotlightItem(
    anime: SpotlightAnime,
    onClick: () -> Unit,
) {
    var isItemFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onFocusChanged { isItemFocused = it.isFocused }
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
    ) {
        // Background image
        AsyncImage(
            model = anime.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(DeepBlack.copy(alpha = 0.95f), Color.Transparent),
                        startX = 0f,
                        endX = 800f,
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DeepBlack),
                        startY = 200f,
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .width(480.dp),
        ) {
            // Quality/Dub/Sub chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val chips = buildList {
                    if (!anime.quality.isNullOrBlank()) add(anime.quality)
                    if (anime.hasSub == true) add("SUB")
                    if (anime.hasDub == true) add("DUB")
                }
                chips.take(3).forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Violet.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(chip, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                text = anime.title ?: "",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))

            // Score + date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!anime.rating.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = VioletLight, modifier = Modifier.size(16.dp))
                        Text(anime.rating, color = VioletLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (!anime.date.isNullOrBlank()) {
                    Text(anime.date, color = TextSecondary, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(10.dp))

            // Description
            if (!anime.synopsis.isNullOrBlank()) {
                Text(
                    text = anime.synopsis,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(16.dp))
            }

            // Watch button
            val scale by animateFloatAsState(
                targetValue = if (isItemFocused) 1.05f else 1f,
                animationSpec = tween(150),
                label = "watchScale",
            )
            
            Box(
                modifier = Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Violet, VioletLight)))
                    .border(
                        width = 2.dp,
                        color = if (isItemFocused) FocusedBorder else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("Watch Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
