package com.example.aonime.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aonime.data.FilterState
import com.example.aonime.theme.*

@Composable
fun FilterDialog(
    initialState: FilterState,
    onDismissRequest: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filters",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        FilterSection(
                            title = "Type",
                            options = FILTER_TYPES.map { it to it.uppercase() },
                            selected = listOfNotNull(state.termType),
                            onSelect = { v -> state = state.copy(termType = if (state.termType == v) null else v) }
                        )
                    }
                    item {
                        FilterSection(
                            title = "Status",
                            options = FILTER_STATUSES,
                            selected = listOfNotNull(state.status),
                            onSelect = { v -> state = state.copy(status = if (state.status == v) null else v) }
                        )
                    }
                    item {
                        FilterSection(
                            title = "Sort",
                            options = FILTER_SORTS,
                            selected = listOfNotNull(state.sort),
                            onSelect = { v -> state = state.copy(sort = if (state.sort == v) null else v) }
                        )
                    }
                    item {
                        FilterSection(
                            title = "Genre",
                            options = FILTER_GENRES,
                            selected = state.genre,
                            onSelect = { v ->
                                val current = state.genre.toMutableList()
                                if (current.contains(v)) current.remove(v) else current.add(v)
                                state = state.copy(genre = current)
                            }
                        )
                    }
                    item {
                        FilterSection(
                            title = "Season",
                            options = FILTER_SEASONS.map { it to it.replaceFirstChar { c -> c.uppercase() } },
                            selected = listOfNotNull(state.season),
                            onSelect = { v -> state = state.copy(season = if (state.season == v) null else v) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { state = FilterState(keyword = state.keyword) }) {
                        Text("Reset", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onApply(state) },
                        colors = ButtonDefaults.buttonColors(containerColor = Violet)
                    ) {
                        Text("Apply Filters", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<Pair<String, String>>, // value to label
    selected: List<String>,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, label) ->
                FilterOptionItem(
                    value = value,
                    label = label,
                    isSelected = selected.contains(value),
                    onSelect = onSelect
                )
            }
        }
    }
}

@Composable
private fun FilterOptionItem(
    value: String,
    label: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(150),
        label = "optionScale"
    )
    
    val bgColor = if (isSelected) Violet else DeepBlack
    val textColor = if (isSelected) Color.White else TextPrimary
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                if (isFocused) 2.dp else 1.dp,
                if (isFocused) FocusedBorder else if (isSelected) Color.Transparent else BorderColor,
                RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelect(value) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp
        )
    }
}

val FILTER_GENRES = listOf(
    "1" to "Action", "2" to "Adventure", "4" to "Comedy", "8" to "Drama",
    "9" to "Ecchi", "10" to "Fantasy", "14" to "Horror", "16" to "Magic",
    "18" to "Mecha", "7" to "Mystery", "40" to "Psychological", "22" to "Romance",
    "24" to "Sci-Fi", "42" to "Seinen", "27" to "Shounen", "36" to "Slice of Life",
    "30" to "Sports", "37" to "Supernatural", "41" to "Thriller"
)

val FILTER_TYPES = listOf("tv", "movie", "ova", "ona", "special", "music")

val FILTER_STATUSES = listOf(
    "currently-airing" to "Airing",
    "finished-airing" to "Finished",
    "not-yet-aired" to "Upcoming",
)

val FILTER_SORTS = listOf(
    "latest-updated" to "Latest Updated",
    "score" to "Score",
    "name-az" to "Name A-Z",
    "release-date" to "Release Date"
)

val FILTER_SEASONS = listOf("fall", "summer", "spring", "winter")
