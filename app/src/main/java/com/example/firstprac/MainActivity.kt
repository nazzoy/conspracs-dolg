package com.example.firstprac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import com.example.firstprac.data.RepositoryDto
import com.example.firstprac.presentation.MainViewModel
import com.example.firstprac.presentation.RepoState
import androidx.compose.ui.text.style.TextAlign

// Пункты меню
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Repositories : Screen("repo_list", "Repos", Icons.Default.List)
    object Info : Screen("info", "About", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {

    // ViewModel
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            val items = listOf(Screen.Home, Screen.Repositories, Screen.Info)

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
                            Column(Modifier.fillMaxSize().padding(16.dp)) {
                                Text("Welcome to GitHub Viewer", style = MaterialTheme.typography.headlineMedium)
                            }
                        }

                        // Экран списка, для примера ставим username = google
                        composable(Screen.Repositories.route) {
                            // Запускаем загрузку, если данных еще нет
                            LaunchedEffect(Unit) {
                                if (viewModel.uiState is RepoState.Idle) {
                                    viewModel.fetchRepos("google")
                                }
                            }

                            when (val state = viewModel.uiState) {
                                is RepoState.Loading -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is RepoState.Success -> {
                                    RepositoryListScreen(
                                        items = state.repos,
                                        onItemClick = { id -> navController.navigate("details/$id") }
                                    )
                                }
                                is RepoState.Error -> {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                                    }
                                }
                                else -> {}
                            }
                        }

                        composable(Screen.Info.route) {
                            Text("Student - Anton\nPractice: Network", Modifier.padding(16.dp))
                        }

                        // Экран деталей
                        composable(
                            route = "details/{repoId}",
                            arguments = listOf(navArgument("repoId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong("repoId")
                            // Поиск репозитория с состоянием success
                            val repo = (viewModel.uiState as? RepoState.Success)?.repos?.find { it.id == id }
                            repo?.let { RepositoryDetailsScreen(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepositoryListScreen(items: List<RepositoryDto>, onItemClick: (Long) -> Unit) {
    LazyColumn {
        items(items) { repo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onItemClick(repo.id) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = repo.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = "⭐ ${repo.stargazers_count}", color = MaterialTheme.colorScheme.primary)
                    Text(text = repo.language ?: "Unknown language", style = MaterialTheme.typography.bodyMedium)
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
            InfoBlock("Stars", "⭐ ${repo.stargazers_count}")
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