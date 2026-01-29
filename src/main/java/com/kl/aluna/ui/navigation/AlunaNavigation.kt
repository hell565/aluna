package com.kl.aluna.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kl.aluna.ui.screens.*
import com.kl.aluna.ui.theme.AlunaColors

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Assistant : Screen(
        route = "assistant",
        title = "Assistant",
        icon = Icons.Outlined.ChatBubbleOutline,
        selectedIcon = Icons.Filled.ChatBubble
    )
    
    object Music : Screen(
        route = "music",
        title = "Music",
        icon = Icons.Outlined.MusicNote,
        selectedIcon = Icons.Filled.MusicNote
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )
    
    object Playlists : Screen(
        route = "playlists",
        title = "Playlists",
        icon = Icons.Outlined.List,
        selectedIcon = Icons.Filled.List
    )
    
    object Favorites : Screen(
        route = "favorites",
        title = "Favorites",
        icon = Icons.Outlined.FavoriteBorder,
        selectedIcon = Icons.Filled.Favorite
    )
    
    object Recent : Screen(
        route = "recent",
        title = "Recent",
        icon = Icons.Outlined.AccessTime,
        selectedIcon = Icons.Filled.AccessTime
    )
}

val bottomNavItems = listOf(
    Screen.Assistant,
    Screen.Music,
    Screen.Settings
)

@Composable
fun AlunaNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Assistant.route,
        Screen.Music.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = modifier,
        containerColor = AlunaColors.Background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = AlunaColors.Surface,
                    contentColor = AlunaColors.TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == screen.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                if (screen == Screen.Music && selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        AlunaColors.Primary,
                                                        AlunaColors.Secondary
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = screen.selectedIcon,
                                            contentDescription = screen.title,
                                            tint = AlunaColors.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { 
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AlunaColors.Primary,
                                selectedTextColor = AlunaColors.Primary,
                                unselectedIconColor = AlunaColors.TextSecondary,
                                unselectedTextColor = AlunaColors.TextSecondary,
                                indicatorColor = AlunaColors.Surface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Assistant.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Assistant.route) {
                AssistantScreen()
            }
            composable(Screen.Music.route) {
                MusicScreen(
                    onCategoryClick = { category ->
                        when (category) {
                            "Playlists" -> navController.navigate(Screen.Playlists.route)
                            "Favorites" -> navController.navigate(Screen.Favorites.route)
                            "Recent" -> navController.navigate(Screen.Recent.route)
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Recent.route) {
                RecentScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
