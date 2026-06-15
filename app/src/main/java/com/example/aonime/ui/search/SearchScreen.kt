package com.example.aonime.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aonime.theme.BorderColor
import com.example.aonime.theme.CardSurface
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.FocusedBorder
import com.example.aonime.theme.TextMuted
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletLight
import com.example.aonime.ui.components.AnimeCardItem

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onAnimeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isMobileLayout = LocalConfiguration.current.screenWidthDp < 600

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
            .padding(24.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Search Anime",
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
                        color = if (isFilterFocused) FocusedBorder else Color.Transparent, 
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
                    tint = if (filterState != com.example.aonime.data.FilterState(keyword = filterState.keyword)) VioletLight else TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Filter",
                    color = if (filterState != com.example.aonime.data.FilterState(keyword = filterState.keyword)) VioletLight else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardSurface)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (query.isNotBlank()) VioletLight else TextMuted,
                    modifier = Modifier.size(20.dp),
                )
                BasicTextField(
                    value = query,
                    onValueChange = { 
                        viewModel.onQueryChange(it)
                        viewModel.setFilterState(filterState.copy(keyword = it))
                    },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    cursorBrush = SolidColor(VioletLight),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier.weight(1f)
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down)
                                        true
                                    }
                                    android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                        focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Up)
                                        true
                                    }
                                    android.view.KeyEvent.KEYCODE_BACK, android.view.KeyEvent.KEYCODE_ESCAPE -> {
                                        focusManager.clearFocus()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        },
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("Search for anime titles...", color = TextMuted, fontSize = 16.sp)
                        }
                        inner()
                    },
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        // Results
        when (val state = uiState) {
            SearchUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Type to search anime", color = TextMuted, fontSize = 15.sp)
                    }
                }
            }
            SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Violet)
                }
            }
            is SearchUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = TextSecondary)
                }
            }
            is SearchUiState.Success -> {
                if (state.results.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found.", color = TextMuted, fontSize = 16.sp)
                    }
                } else {
                    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

                    androidx.compose.runtime.LaunchedEffect(gridState, state.results.size) {
                        androidx.compose.runtime.snapshotFlow {
                            gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        }.collect { lastIndex ->
                            if (lastIndex != null && lastIndex >= state.results.size - 4) {
                                viewModel.loadNextPage()
                            }
                        }
                    }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = if (isMobileLayout) GridCells.Fixed(2) else GridCells.Adaptive(140.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items = state.results, key = { it.slug ?: it.hashCode() }) { anime ->
                            com.example.aonime.ui.components.AnimeCardItem(
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
}
