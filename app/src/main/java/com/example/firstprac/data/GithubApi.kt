package com.example.firstprac.data

import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApi {

    // Запрос списка репозиториев конкретного пользователя
    // {username} — это динамическая часть ссылки (Path параметр)
    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") user: String
    ): List<RepositoryDto>
}

// Модель данных, которую присылает GitHub (DTO)
data class RepositoryDto(
    val id: Long,
    val name: String,
    val description: String?,
    val stargazers_count: Int,
    val language: String?
)