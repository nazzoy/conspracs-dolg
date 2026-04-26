package com.example.firstprac.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File


// Вспомогательные функции для работы с файлами, камерой и загрузками
object FileUtils {

    fun createImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, "images")
        if (!directory.exists()) directory.mkdirs()

        // Создаем файл с уникальным именем
        val file = File(
            directory,
            "avatar_${System.currentTimeMillis()}.jpg"
        )

        // Возвращаем Uri через FileProvider
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // Скачивание PDF
    fun downloadAndOpenResume(context: Context, url: String) {
        if (url.isBlank() || !url.startsWith("http")) {
            Toast.makeText(context, "Некорректная ссылка на резюме", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Резюме")
                .setDescription("Загрузка файла...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "resume_${System.currentTimeMillis()}.pdf"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "Загрузка началась. Проверьте шторку уведомлений", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}