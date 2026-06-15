package com.example.aonime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.aonime.data.AnimeDetail
import com.example.aonime.theme.DeepBlack
import com.example.aonime.ui.browse.BrowseScreen
import com.example.aonime.ui.browse.BrowseViewModel
import com.example.aonime.ui.components.SideBar
import com.example.aonime.ui.detail.DetailScreen
import com.example.aonime.ui.detail.DetailViewModel
import com.example.aonime.ui.home.HomeScreen
import com.example.aonime.ui.home.HomeViewModel
import com.example.aonime.ui.library.LibraryScreen
import com.example.aonime.ui.library.LibraryViewModel
import com.example.aonime.ui.player.PlayerScreen
import com.example.aonime.ui.player.PlayerViewModel
import com.example.aonime.ui.search.SearchScreen
import com.example.aonime.ui.search.SearchViewModel
import com.example.aonime.ui.splash.SplashScreen

@Composable
fun AonimeAppRoot() {
    val context = LocalContext.current
    val app = context.applicationContext as AonimeApp

    // Nav: 0=Search, 1=Home, 2=Browse, 3=Library (matches navItems order)
    var selectedNavIndex by remember { mutableIntStateOf(1) } // default Home

    val backStack = rememberNavBackStack(SplashNav)

    // ViewModels (survive recomposition)
    val homeVm: HomeViewModel = viewModel { HomeViewModel(app.repository) }
    val searchVm: SearchViewModel = viewModel { SearchViewModel(app.repository) }
    val browseVm: BrowseViewModel = viewModel { BrowseViewModel(app.repository) }
    val libraryVm: LibraryViewModel = viewModel { LibraryViewModel(app.repository) }
    val detailVm: DetailViewModel = viewModel { DetailViewModel(app.repository) }
    val playerVm: PlayerViewModel = viewModel { PlayerViewModel(app.repository) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
    ) {
        // ── Left-side Sidebar ────────────────────────────────────────────────
        val isPlayerScreen = backStack.lastOrNull() is PlayerNav
        val isSplashScreen = backStack.lastOrNull() is SplashNav
        if (!isPlayerScreen && !isSplashScreen) {
            SideBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    val navKey = when (index) {
                        0 -> SearchNav
                        1 -> HomeNav
                        2 -> BrowseNav
                        3 -> LibraryNav
                        else -> HomeNav
                    }
                    // Pop to root and navigate
                    while (backStack.size > 1) backStack.removeLastOrNull()
                    if (backStack.lastOrNull() != navKey) {
                        backStack.removeLastOrNull()
                        backStack.add(navKey)
                    }
                },
            )
        }

        // ── Main Content (takes remaining width) ──────────────────────────────
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            entryProvider = entryProvider {
                entry<SplashNav> {
                    SplashScreen(
                        onSplashComplete = {
                            while (backStack.isNotEmpty()) backStack.removeLastOrNull()
                            backStack.add(HomeNav)
                        }
                    )
                }
                entry<HomeNav> {
                    HomeScreen(
                        viewModel = homeVm,
                        onAnimeClick = { slug ->
                            backStack.add(DetailNav(slug = slug))
                        },
                        onSpotlightClick = { spotlight ->
                            spotlight.slug?.let { backStack.add(DetailNav(slug = it, title = spotlight.title ?: "")) }
                        },
                    )
                }
                entry<SearchNav> {
                    SearchScreen(
                        viewModel = searchVm,
                        onAnimeClick = { slug ->
                            backStack.add(DetailNav(slug = slug))
                        },
                    )
                }
                entry<BrowseNav> {
                    BrowseScreen(
                        viewModel = browseVm,
                        onAnimeClick = { slug ->
                            backStack.add(DetailNav(slug = slug))
                        },
                    )
                }
                entry<LibraryNav> {
                    LibraryScreen(
                        viewModel = libraryVm,
                        onAnimeClick = { slug ->
                            backStack.add(DetailNav(slug = slug))
                        },
                    )
                }
                entry<DetailNav> { key ->
                    DetailScreen(
                        slug = key.slug,
                        viewModel = detailVm,
                        onBack = { backStack.removeLastOrNull() },
                        onPlayEpisode = { slug, ep ->
                            backStack.add(PlayerNav(slug = slug, ep = ep))
                        },
                    )
                }
                entry<PlayerNav> { key ->
                    PlayerScreen(
                        slug = key.slug,
                        ep = key.ep,
                        title = key.title,
                        viewModel = playerVm,
                    )
                }
                // Template compatibility
                entry<Main> { HomeScreen(viewModel = homeVm, onAnimeClick = {}, onSpotlightClick = {}) }
            },
        )
    }
}
