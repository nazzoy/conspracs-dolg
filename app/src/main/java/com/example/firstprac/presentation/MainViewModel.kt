package com.example.firstprac.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstprac.data.AlarmReceiver
import com.example.firstprac.data.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.firstprac.data.local.SettingsManager
import com.example.firstprac.data.local.UserSettings
import com.example.firstprac.data.local.FavoriteDao
import com.example.firstprac.data.local.FavoriteEntity
import com.example.firstprac.data.RepositoryDto
import com.example.firstprac.data.local.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

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
    val userProfile: StateFlow<UserProfile> = settingsManager.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    fun saveProfile(name: String, avatar: String, resume: String, time: String) {
        viewModelScope.launch {
            settingsManager.saveProfile(UserProfile(name, avatar, resume, time))
        }
    }

    fun scheduleNotification(context: Context, time: String, userName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                setInexactAlarm(context, alarmManager, time, userName)
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("USER_NAME", userName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = getCalendarFromTime(time)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun setInexactAlarm(context: Context, alarmManager: AlarmManager, time: String, userName: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("USER_NAME", userName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = getCalendarFromTime(time)

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun getCalendarFromTime(time: String): Calendar {
        val (hour, minute) = time.split(":").map { it.toInt() }
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }
}
