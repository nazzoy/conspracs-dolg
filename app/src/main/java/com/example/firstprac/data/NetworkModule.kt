package com.example.firstprac.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://api.github.com/"

    // Инициализация Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Конвертер JSON в объекты RepositoryDto
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Экземпляр интерфейса
    val githubApi: GithubApi by lazy {
        retrofit.create(GithubApi::class.java)
    }
}