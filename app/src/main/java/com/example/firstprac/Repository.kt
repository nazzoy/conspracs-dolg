package com.example.firstprac

data class Repository(
    val id: Int,
    val name: String,
    val owner: String,
    val stars: Int,
    val description: String,
    val language: String,
    val lastUpdate: String,
    val forks: Int,        // Новое поле
    val openIssues: Int,   // Новое поле
    val size: String       // Новое поле
)

val mockRepositories = listOf(
    Repository(1, "Compose-Samples", "android", 15400, "Official samples for Jetpack Compose components and layouts.", "Kotlin", "2024-04-20", 1200, 45, "12 MB"),
    Repository(2, "Linux-Kernel", "torvalds", 120500, "The Linux kernel source tree. The foundation of modern computing.", "C", "2024-04-24", 45000, 1200, "1.2 GB"),
    Repository(3, "Cyber-Scanner", "anton-dev", 42, "OSINT tool for scanning open ports and vulnerabilities.", "Python", "2024-03-15", 5, 2, "150 KB"),
    Repository(4, "TensorFlow", "tensorflow", 182000, "An Open Source Machine Learning Framework for Everyone.", "C++", "2024-04-25", 89000, 3400, "450 MB"),
    Repository(5, "Flutter", "flutter", 162000, "Google's UI toolkit for building beautiful apps.", "Dart", "2024-04-26", 26000, 5600, "210 MB"),
    Repository(6, "Architecture-Samples", "android", 42000, "A collection of samples to discuss Android architecture.", "Kotlin", "2024-04-10", 11000, 15, "5 MB"),
    Repository(7, "React-Native", "facebook", 115000, "A framework for building native apps using React.", "JavaScript", "2024-04-22", 24000, 2100, "180 MB"),
    Repository(8, "VLC-Android", "videolan", 1200, "VLC for Android official repository.", "Java", "2024-04-01", 450, 89, "85 MB")
)