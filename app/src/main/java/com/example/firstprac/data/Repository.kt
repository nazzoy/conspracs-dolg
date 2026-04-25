package com.example.firstprac.data

class GithubRepository {

    // Функция для получения данных из сети
    suspend fun getRepositories(username: String): List<RepositoryDto> {
        return NetworkModule.githubApi.getUserRepos(username)
    }
}