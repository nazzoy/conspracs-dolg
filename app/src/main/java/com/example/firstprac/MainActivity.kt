package com.example.firstprac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

// Пункты нижнего меню
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Repositories : Screen("repo_list", "Repos", Icons.Default.List)
    object Info : Screen("info", "About", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
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
                    // Навигационный хост, который переключает содержимое
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

                        composable(Screen.Repositories.route) {
                            RepositoryListScreen(
                                items = mockRepositories,
                                onItemClick = { id -> navController.navigate("details/$id") }
                            )
                        }

                        composable(Screen.Info.route) {
                            Text("Student - Anton\nPractice: #3", Modifier.padding(16.dp))
                        }

                        // Экран деталей
                        composable(
                            route = "details/{repoId}",
                            arguments = listOf(navArgument("repoId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("repoId")
                            val repo = mockRepositories.find { it.id == id }
                            repo?.let { RepositoryDetailsScreen(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepositoryListScreen(items: List<Repository>, onItemClick: (Int) -> Unit) {
    // Используем LazyColumn для списка
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
                    Text(text = "Owner: ${repo.owner}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "⭐ ${repo.stars}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun RepositoryDetailsScreen(repo: Repository) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val (header, badge, divider, stats, description, footer) = createRefs()

        // Заголовок и автор
        Column(
            modifier = Modifier.constrainAs(header) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        ) {
            Text(text = repo.owner, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Text(text = repo.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        // Язык программирования
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
                text = repo.language,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // 3. Линия-разделитель
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .constrainAs(divider) { top.linkTo(header.bottom) }
        )

        // Сетка статистики (Stars, Forks, Issues)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(stats) { top.linkTo(divider.bottom) },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoBlock("Stars", "⭐ ${repo.stars}")
            InfoBlock("Forks", "🍴 ${repo.forks}")
            InfoBlock("Issues", "❗ ${repo.openIssues}")
        }

        // Описание
        Text(
            text = repo.description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
            modifier = Modifier.constrainAs(description) {
                top.linkTo(stats.bottom, margin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = androidx.constraintlayout.compose.Dimension.fillToConstraints
            }
        )

        // Подвал с датой обновления и размером
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(footer) { bottom.linkTo(parent.bottom) },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Size: ${repo.size}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Updated: ${repo.lastUpdate}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InfoBlock(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}