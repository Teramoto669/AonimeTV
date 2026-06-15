package com.example.aonime.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aonime.theme.CardSurface
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.TextMuted
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletLight
import com.example.aonime.ui.components.AnimeCardItem

@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showFilterDialog) {
        com.example.aonime.ui.components.FilterDialog(
            initialState = filterState,
            onDismissRequest = { showFilterDialog = false },
            onApply = { newState ->
                viewModel.setFilterState(newState)
                showFilterDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(top = 24.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Browse",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            // Filter Button
            var isFilterFocused by remember { androidx.compose.runtime.mutableStateOf(false) }
            val filterScale by animateFloatAsState(targetValue = if (isFilterFocused) 1.05f else 1f)
            
            Row(
                modifier = Modifier
                    .scale(filterScale)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFilterFocused) Violet.copy(alpha=0.2f) else CardSurface)
                    .border(
                        width = if (isFilterFocused) 2.dp else 0.dp, 
                        color = if (isFilterFocused) com.example.aonime.theme.FocusedBorder else Color.Transparent, 
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { isFilterFocused = it.isFocused }
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        onClick = { showFilterDialog = true }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.Icon(
                    Icons.Rounded.FilterList, 
                    contentDescription = "Filter",
                    tint = if (filterState != com.example.aonime.data.FilterState()) VioletLight else TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Filter",
                    color = if (filterState != com.example.aonime.data.FilterState()) VioletLight else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Results
        when (val state = uiState) {
            BrowseUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Violet)
                }
            }
            is BrowseUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = TextSecondary)
                }
            }
            is BrowseUiState.Success -> {
                val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

                androidx.compose.runtime.LaunchedEffect(gridState, state.items.size) {
                    androidx.compose.runtime.snapshotFlow {
                        gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    }.collect { lastIndex ->
                        if (lastIndex != null && lastIndex >= state.items.size - 4) {
                            viewModel.loadNextPage()
                        }
                    }
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = state.items, key = { it.slug ?: it.hashCode() }) { anime ->
                        AnimeCardItem(
                            anime = anime,
                            onClick = { anime.slug?.let { onAnimeClick(it) } },
                        )
                    }
                    if (state.hasNextPage) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(this.maxLineSpan) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Violet)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    small: Boolean = false,
) {
    val bgColor = if (selected) Violet else CardSurface
    val textColor = if (selected) Color.White else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .then(
                if (selected) Modifier else Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            )
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(
                horizontal = if (small) 12.dp else 16.dp,
                vertical = if (small) 6.dp else 8.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (small) 12.sp else 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
