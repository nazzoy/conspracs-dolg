package com.example.firstprac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.firstprac.data.GithubRepository
import com.example.firstprac.data.local.AppDatabase
import com.example.firstprac.data.local.SettingsManager
import com.example.firstprac.presentation.*
import com.example.firstprac.presentation.RepoState
import com.example.firstprac.data.RepositoryDto

// Пункты меню
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Repositories : Screen("repo_list", "Repos", Icons.Default.List)
    object Favorites : Screen("favorites", "Favs", Icons.Default.Favorite)
    object Info : Screen("info", "About", Icons.Default.Info)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object EditProfile : Screen("edit_profile", "Edit", Icons.Default.Edit)
}

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext)
        val repository = GithubRepository()
        val database = AppDatabase.getDatabase(applicationContext)
        val favoriteDao = database.favoriteDao()

        val factory = MainViewModelFactory(settingsManager, repository, favoriteDao)
        val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        // Получаем текущий маршрут
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        TopAppBar(
                            title = { Text("GitHub Viewer") },
                            actions = {
                                if (currentRoute == Screen.Repositories.route) {
                                    Box(modifier = Modifier.padding(end = 8.dp)) {
                                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                                        }

                                        // Желтый бейдж
                                        if (viewModel.currentSettings.minStars > 0 || viewModel.currentSettings.language.isNotEmpty()) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Yellow,
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-4).dp, y = 4.dp)
                                            ) {}
                                        }
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            val items = listOf(Screen.Home, Screen.Repositories, Screen.Favorites, Screen.Profile, Screen.Info)

                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Welcome to GitHub Viewer", style = MaterialTheme.typography.headlineMedium)
                            }
                        }

                        composable(Screen.Repositories.route) {
                            when (val state = viewModel.uiState) {
                                is RepoState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                is RepoState.Success -> {
                                    RepositoryListScreen(
                                        items = state.repos,
                                        onItemClick = { id -> navController.navigate("details/$id") },
                                        onLongClick = { repo -> viewModel.toggleFavorite(repo) }
                                    )
                                }
                                is RepoState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) }
                                else -> {}
                            }
                        }

                        composable(Screen.Favorites.route) {
                            val favorites by viewModel.favoritesFlow.collectAsState(initial = emptyList())
                            val favDtos = favorites.map { RepositoryDto(it.id, it.name, it.description, it.stars, it.language) }

                            if (favDtos.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No favorites yet. Long press a repo to add.")
                                }
                            } else {
                                RepositoryListScreen(
                                    items = favDtos,
                                    onItemClick = { id -> navController.navigate("details/$id") }
                                )
                            }
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                currentSettings = viewModel.currentSettings,
                                onSave = { viewModel.saveNewSettings(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                viewModel = viewModel,
                                onEditClick = { navController.navigate(Screen.EditProfile.route) }
                            )
                        }

                        composable(Screen.EditProfile.route) {
                            EditProfileScreen(
                                viewModel = viewModel,
                                onDone = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Info.route) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Student: Anton", style = MaterialTheme.typography.titleLarge)
                                Text("Practice: Local Storage")
                            }
                        }

                        composable(
                            route = "details/{repoId}",
                            arguments = listOf(navArgument("repoId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong("repoId")
                            // Ищем либо в основном списке, либо в избранном
                            val repo = (viewModel.uiState as? RepoState.Success)?.repos?.find { it.id == id }
                            repo?.let { RepositoryDetailsScreen(it) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoryListScreen(
    items: List<RepositoryDto>,
    onItemClick: (Long) -> Unit,
    onLongClick: (RepositoryDto) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn {
        items(items) { repo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .combinedClickable(
                        onClick = { onItemClick(repo.id) },
                        onLongClick = {
                            onLongClick(repo)
                            android.widget.Toast.makeText(context, "Added to favorites!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = repo.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = "⭐ ${repo.stargazersCount}", color = MaterialTheme.colorScheme.primary)
                    Text(text = repo.language ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


@Composable
fun RepositoryDetailsScreen(repo: RepositoryDto) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val (header, badge, divider, stats, description) = createRefs()

        Column(
            modifier = Modifier.constrainAs(header) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        ) {
            Text(text = "GitHub Repository", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text(text = repo.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .clip(CircleShape)
                .constrainAs(badge) {
                    top.linkTo(header.top)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = repo.language ?: "N/A",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .constrainAs(divider) { top.linkTo(header.bottom) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(stats) { top.linkTo(divider.bottom) },
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoBlock("Stars", "⭐ ${repo.stargazersCount}")
        }

        Text(
            text = repo.description ?: "No description provided for this repository.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.constrainAs(description) {
                top.linkTo(stats.bottom, margin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )
    }
}

@Composable
fun InfoBlock(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
