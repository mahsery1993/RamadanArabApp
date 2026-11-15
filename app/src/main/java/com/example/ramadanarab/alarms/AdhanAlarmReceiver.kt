package com.example.ramadanarab.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.ramadanarab.MainActivity

class AdhanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "تنبيه رمضان"
        val message = intent.getStringExtra("message") ?: "حان موعد الصلاة"
        val notifId = intent.getIntExtra("id", 1001)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ramadan_adhan_channel"

        val channel = NotificationChannel(
            channelId,
            "مواقيت الأذان والسحور",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات السحور والإفطار والصلوات"
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)

        val openAppIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        nm.notify(notifId, notification)
        AdhanAlarmManager.scheduleDailyRamadanAlarms(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AdhanAlarmManager.scheduleDailyRamadanAlarms(context)
        }
    }
}
