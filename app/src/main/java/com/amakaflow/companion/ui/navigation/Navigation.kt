package com.amakaflow.companion.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amakaflow.companion.ui.screens.history.HistoryScreen
import com.amakaflow.companion.ui.screens.home.HomeScreen
import com.amakaflow.companion.ui.screens.settings.SettingsScreen
import com.amakaflow.companion.ui.screens.workouts.WorkoutDetailScreen
import com.amakaflow.companion.ui.screens.workouts.WorkoutsScreen
import com.amakaflow.companion.ui.theme.AmakaColors

/**
 * Navigation destinations
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workouts : Screen("workouts")
    data object WorkoutDetail : Screen("workout/{workoutId}") {
        fun createRoute(workoutId: String) = "workout/$workoutId"
    }
    data object Calendar : Screen("calendar")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Pairing : Screen("pairing")
    data object WorkoutPlayer : Screen("player/{workoutId}") {
        fun createRoute(workoutId: String) = "player/$workoutId"
    }
}

/**
 * Bottom navigation tabs
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    data object Workouts : BottomNavItem(
        route = Screen.Workouts.route,
        title = "Workouts",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter
    )
    data object Calendar : BottomNavItem(
        route = Screen.Calendar.route,
        title = "Calendar",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
    data object History : BottomNavItem(
        route = Screen.History.route,
        title = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )
    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Workouts,
    BottomNavItem.Calendar,
    BottomNavItem.History,
    BottomNavItem.Settings
)

@Composable
fun AmakaFlowNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToWorkouts = {
                    navController.navigate(Screen.Workouts.route)
                },
                onNavigateToWorkoutDetail = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                }
            )
        }

        composable(Screen.Workouts.route) {
            WorkoutsScreen(
                onNavigateToWorkoutDetail = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                }
            )
        }

        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
            WorkoutDetailScreen(
                workoutId = workoutId,
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { /* Navigate to workout player */ }
            )
        }

        composable(Screen.Calendar.route) {
            // Placeholder for calendar screen
            HomeScreen(
                onNavigateToWorkouts = {},
                onNavigateToWorkoutDetail = {}
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToCompletionDetail = { /* Navigate to completion detail */ }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}

@Composable
fun AmakaFlowBottomNavBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = AmakaColors.surface,
        contentColor = AmakaColors.textPrimary
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AmakaColors.accentBlue,
                    selectedTextColor = AmakaColors.accentBlue,
                    unselectedIconColor = AmakaColors.textSecondary,
                    unselectedTextColor = AmakaColors.textSecondary,
                    indicatorColor = AmakaColors.accentBlue.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            AmakaFlowBottomNavBar(navController = navController)
        },
        containerColor = AmakaColors.background
    ) { innerPadding ->
        AmakaFlowNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
