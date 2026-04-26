package com.example.firstprac.presentation

import android.content.Context
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import com.example.firstprac.utils.FileUtils.createImageUri

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

    LaunchedEffect(profile) {
        name = profile.name
        resumeUrl = profile.resumeUrl
        currentAvatarUri = profile.avatarUri
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

    // 1. Определяем список разрешений в зависимости от версии системы
    val storagePermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES // Для Android 13+
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE // Для Android 12 и ниже
    }

    // 2. Обновляем лаунчер
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
        // 3. В самом UI (в блоке clickable) запускаем массив
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    // Запрашиваем сразу оба разрешения
                    permissionLauncher.launch(
                        arrayOf(android.Manifest.permission.CAMERA, storagePermission)
                    )
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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = resumeUrl,
            onValueChange = { resumeUrl = it },
            label = { Text("Ссылка на резюме (URL)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.saveProfile(name, currentAvatarUri, resumeUrl)
                onDone()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Готово")
        }
    }
}

