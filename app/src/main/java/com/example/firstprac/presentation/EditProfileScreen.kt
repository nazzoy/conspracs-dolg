package com.example.firstprac.presentation

import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.firstprac.utils.FileUtils.createImageUri
import java.util.Locale

@Composable
fun EditProfileScreen(
    viewModel: MainViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }
    var currentAvatarUri by remember { mutableStateOf("") }
    var classTime by remember { mutableStateOf("") }

    // Валидация: формат HH:mm
    val timeRegex = remember { Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$") }
    val isTimeValid = classTime.isEmpty() || timeRegex.matches(classTime)

    // Инициализация диалога выбора времени
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                classTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            },
            12, 0, true
        )
    }

    LaunchedEffect(profile) {
        name = profile.name
        resumeUrl = profile.resumeUrl
        currentAvatarUri = profile.avatarUri
        classTime = profile.classTime
    }

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { currentAvatarUri = it.toString() }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { currentAvatarUri = it.toString() }
        }
    }

    // Определяем список разрешений в зависимости от версии системы
    val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES // Для Android 13+
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE // Для Android 12 и ниже
    }

    // Обновляем лаунчер
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[storagePermission] ?: false

        if (cameraGranted && storageGranted) {
            showDialog = true // Открываем диалог, только если всё разрешено
        } else {
            Toast.makeText(context, "Необходимы разрешения для фото и памяти", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Выберите фото") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Галерея") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    val uri = createImageUri(context)
                    tempUri = uri
                    cameraLauncher.launch(uri)
                }) { Text("Камера") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Секция с аватаром
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA, storagePermission))
                }
        ) {
            if (currentAvatarUri.isNotEmpty()) {
                AsyncImage(
                    model = currentAvatarUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Поле ФИО
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле URL
        OutlinedTextField(
            value = resumeUrl,
            onValueChange = { resumeUrl = it },
            label = { Text("Ссылка на резюме (URL)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле времени пары
        OutlinedTextField(
            value = classTime,
            onValueChange = { classTime = it },
            label = { Text("Время любимой пары (HH:mm)") },
            placeholder = { Text("Например, 10:30") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isTimeValid,
            supportingText = {
                if (!isTimeValid) {
                    Text("Некорректный формат времени", color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                IconButton(onClick = { timePickerDialog.show() }) {
                    Icon(Icons.Default.Schedule, contentDescription = "Выбрать время")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка сохранения
        Button(
            onClick = {
                if (isTimeValid && name.isNotEmpty()) {
                    viewModel.saveProfile(name, currentAvatarUri, resumeUrl, classTime)
                    if (classTime.isNotEmpty()) {
                        viewModel.scheduleNotification(context, classTime, name)
                    }
                    onDone()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isTimeValid && name.isNotEmpty(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Сохранить всё")
        }
    }
}
