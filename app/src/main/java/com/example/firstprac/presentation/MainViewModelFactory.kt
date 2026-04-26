package com.example.firstprac.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.firstprac.data.GithubRepository
import com.example.firstprac.data.local.FavoriteDao
import com.example.firstprac.data.local.SettingsManager

class MainViewModelFactory(
    private val settingsManager: SettingsManager,
    private val repository: GithubRepository,
    private val favoriteDao: FavoriteDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(settingsManager, repository, favoriteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}