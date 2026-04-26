package com.example.firstprac.presentation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.firstprac.data.local.UserSettings

@Composable
fun SettingsScreen(
    currentSettings: UserSettings,
    onSave: (UserSettings) -> Unit,
    onBack: () -> Unit
) {
    // Временные состояния для полей ввода
    var username by remember { mutableStateOf(currentSettings.username) }
    var minStars by remember { mutableStateOf(currentSettings.minStars.toString()) }
    var language by remember { mutableStateOf(currentSettings.language) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки поиска",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Настройка 1: Имя пользователя
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Кого ищем (username)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Настройка 2: Минимальные звезды
        OutlinedTextField(
            value = minStars,
            onValueChange = { if (it.all { char -> char.isDigit() }) minStars = it },
            label = { Text("Минимум звезд") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Настройка 3: Язык программирования
        OutlinedTextField(
            value = language,
            onValueChange = { language = it },
            label = { Text("Язык (например, Kotlin)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // Сохраняем и выходим
                val starsInt = minStars.toIntOrNull() ?: 0
                onSave(UserSettings(username, starsInt, language))
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Сохранить и применить")
        }
    }
}