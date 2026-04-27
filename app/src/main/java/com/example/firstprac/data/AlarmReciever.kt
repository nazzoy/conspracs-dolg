package com.example.firstprac.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.firstprac.MainActivity
import com.example.firstprac.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userName = intent.getStringExtra("USER_NAME") ?: "Студент"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "class_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Напоминания о парах",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Интент для открытия приложения при нажатии на пуш
        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Пора на пару!")
            .setContentText("$userName, твоя любимая пара по мобильной разработке начинается!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }
}