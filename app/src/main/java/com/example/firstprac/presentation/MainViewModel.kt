package com.example.firstprac.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstprac.data.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.firstprac.data.local.SettingsManager
import com.example.firstprac.data.local.UserSettings
import com.example.firstprac.data.local.FavoriteDao
import com.example.firstprac.data.local.FavoriteEntity
import com.example.firstprac.data.RepositoryDto

sealed class RepoState {
    object Idle : RepoState()
    object Loading : RepoState()
    data class Success(val repos: List<RepositoryDto>) : RepoState()
    data class Error(val message: String) : RepoState()
}
class MainViewModel(
    private val settingsManager: SettingsManager,
    private val repository: GithubRepository,
    private val favoriteDao: FavoriteDao
) : ViewModel() {

    var uiState by mutableStateOf<RepoState>(RepoState.Idle)
        private set

    var currentSettings by mutableStateOf(UserSettings("google", 0, ""))
        private set

    init {
        viewModelScope.launch {
            settingsManager.settingsFlow.collect { settings ->
                currentSettings = settings
                fetchRepos(settings.username)
            }
        }
    }

    fun fetchRepos(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            uiState = RepoState.Loading
            try {
                val allRepos = repository.getRepositories(username)

                // Фильтрация на основе сохраненных настроек
                val filteredRepos = allRepos.filter { repo ->
                    val matchStars = repo.stargazersCount >= currentSettings.minStars
                    val matchLang = if (currentSettings.language.isBlank()) true
                    else repo.language?.contains(currentSettings.language, ignoreCase = true) == true
                    matchStars && matchLang
                }

                uiState = RepoState.Success(filteredRepos)
            } catch (e: Exception) {
                uiState = RepoState.Error("Network error or a profile not found")
            }
        }
    }

    // Сохранение в избранное (Room)
    fun toggleFavorite(repo: RepositoryDto) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = FavoriteEntity(
                id = repo.id,
                name = repo.name,
                description = repo.description,
                stars = repo.stargazersCount,
                language = repo.language
            )
            favoriteDao.insertFavorite(entity)
        }
    }

    // Получение списка избранного для отдельного экрана
    val favoritesFlow = favoriteDao.getAllFavorites()

    fun saveNewSettings(settings: UserSettings) {
        viewModelScope.launch {
            settingsManager.saveSettings(settings)
        }
    }
}