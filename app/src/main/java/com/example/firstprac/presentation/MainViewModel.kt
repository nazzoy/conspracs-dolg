package com.example.firstprac.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstprac.data.GithubRepository
import com.example.firstprac.data.RepositoryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Состояния экрана
sealed class RepoState {
    object Idle : RepoState() // Ничего не происходит
    object Loading : RepoState() // Загрузки
    data class Success(val repos: List<RepositoryDto>) : RepoState() // Данные пришли
    data class Error(val message: String) : RepoState() // Ошибка
}

class MainViewModel : ViewModel() {
    private val repository = GithubRepository()

    // Состояние которое видит пользователь
    var uiState by mutableStateOf<RepoState>(RepoState.Idle)
        private set

    fun fetchRepos(username: String) {
        // Запускаем работу в фоновом потоке
        viewModelScope.launch(Dispatchers.IO) {
            uiState = RepoState.Loading
            try {
                val repos = repository.getRepositories(username)
                uiState = RepoState.Success(repos)
            } catch (e: Exception) {
                // Если нет интернета или юзер не найден — фиксируем ошибку
                uiState = RepoState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}