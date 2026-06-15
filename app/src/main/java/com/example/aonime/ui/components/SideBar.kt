package com.example.aonime.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aonime.R
import com.example.aonime.theme.BorderColor
import com.example.aonime.theme.CardSurface
import com.example.aonime.theme.DarkSurface
import com.example.aonime.theme.DeepBlack
import com.example.aonime.theme.TextMuted
import com.example.aonime.theme.TextPrimary
import com.example.aonime.theme.TextSecondary
import com.example.aonime.theme.Violet
import com.example.aonime.theme.VioletGlow
import com.example.aonime.theme.VioletLight

data class NavItem(
    val icon: ImageVector,
    val label: String,
)

val navItems = listOf(
    NavItem(Icons.Rounded.Search, "Search"),
    NavItem(Icons.Rounded.Home, "Home"),
    NavItem(Icons.Rounded.Explore, "Browse"),
    NavItem(Icons.Rounded.VideoLibrary, "Library"),
)

@Composable
fun SideBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSidebarFocused by remember { mutableIntStateOf(-1) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarFocused >= 0) 84.dp else 72.dp,
        animationSpec = tween(durationMillis = 250),
        label = "sidebarWidth",
    )

    Box(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, DarkSurface),
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, BorderColor, Color.Transparent),
                ),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = 32.dp, horizontal = 8.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo / App Name at top
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(listOf(Violet, VioletLight))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Aonime Logo",
                        modifier = Modifier.padding(8.dp)
                    )
                }
                if (isSidebarFocused >= 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "AONIME",
                        color = VioletLight,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            // Nav Items
            navItems.forEachIndexed { index, item ->
                NavRailItem(
                    item = item,
                    isSelected = selectedIndex == index,
                    showLabel = isSidebarFocused >= 0,
                    onFocusChanged = { focused ->
                        isSidebarFocused = if (focused) index else -1
                    },
                    onClick = { onItemSelected(index) },
                )
                if (index < navItems.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Bottom navigation bar for mobile/phone portrait layout.
 * Shows nav items horizontally at the bottom of the screen.
 */
@Composable
fun BottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, DarkSurface)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, BorderColor, Color.Transparent),
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navItems.forEachIndexed { index, item ->
                BottomNavItem(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Violet else Color.Transparent,
        animationSpec = tween(200),
        label = "bottomNavBg",
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextMuted,
        animationSpec = tween(200),
        label = "bottomNavIcon",
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(200),
        label = "bottomNavScale",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(bgColor)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun NavRailItem(
    item: NavItem,
    isSelected: Boolean,
    showLabel: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableIntStateOf(0) }
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> Violet
            isFocused > 0 -> VioletGlow
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "navItemBg",
    )
    val iconTint by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            isFocused > 0 -> VioletLight
            else -> TextMuted
        },
        animationSpec = tween(200),
        label = "navItemIcon",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .onFocusChanged { state ->
                isFocused = if (state.isFocused) 1 else 0
                onFocusChanged(state.isFocused)
            }
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = if (showLabel) 12.dp else 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconTint,
                modifier = Modifier.size(26.dp),
            )
            if (showLabel) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.label,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
