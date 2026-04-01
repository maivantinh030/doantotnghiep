package com.example.appcongvien

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appcongvien.navigation.NotificationNavigationExtras

object PushNotificationHelper {
    const val CHANNEL_ID = "park_adventure_general"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Park Adventure",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "General push notifications"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationNavigationExtras.EXTRA_OPEN_FROM_PUSH, true)
            putExtra(NotificationNavigationExtras.EXTRA_NOTIFICATION_ID, data["notificationId"])
            putExtra(NotificationNavigationExtras.EXTRA_NOTIFICATION_TYPE, data["type"])
            putExtra(NotificationNavigationExtras.EXTRA_NOTIFICATION_DATA, data["data"])
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            data["notificationId"]?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            data["notificationId"]?.hashCode() ?: (System.currentTimeMillis() and 0x7FFFFFFF).toInt(),
            notification
        )
    }
}
