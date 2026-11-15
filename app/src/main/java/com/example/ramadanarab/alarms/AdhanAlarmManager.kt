package com.example.ramadanarab.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.ramadanarab.data.RamadanPrefs
import java.util.*

object AdhanAlarmManager {

    private const val REQ_SUHOOR = 1001
    private const val REQ_FAJR = 1002
    private const val REQ_MAGHRIB = 1003

    fun scheduleDailyRamadanAlarms(context: Context) {
        if (!RamadanPrefs.areAlarmsEnabled(context)) return

        val country = RamadanPrefs.getCountry(context)
        val tz = TimeZone.getTimeZone(country.timeZoneId)
        val now = Calendar.getInstance(tz)
        val coords = Coordinates(country.latitude, country.longitude)
        val params = country.calculationMethod.parameters

        val todayTimes = PrayerTimes(coords, DateComponents.from(now.time), params)
        val tomorrowCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowTimes = PrayerTimes(coords, DateComponents.from(tomorrowCal.time), params)

        val fajrTarget = if (todayTimes.fajr.after(now.time)) todayTimes.fajr else tomorrowTimes.fajr
        val suhoorTimeMillis = fajrTarget.time - (45 * 60 * 1000L)
        if (suhoorTimeMillis > System.currentTimeMillis()) {
            setExactAlarm(
                context, REQ_SUHOOR, suhoorTimeMillis,
                "تنبيه السحور",
                "تبقّى 45 دقيقة على أذان الفجر في ${country.capitalAr}، تسحروا فإن في السحور بركة"
            )
        }

        setExactAlarm(
            context, REQ_FAJR, fajrTarget.time,
            "أذان الفجر",
            "حان الآن موعد أذان الفجر (الإمساك) في ${country.capitalAr}"
        )

        val maghribTarget = if (todayTimes.maghrib.after(now.time)) todayTimes.maghrib else tomorrowTimes.maghrib
        setExactAlarm(
            context, REQ_MAGHRIB, maghribTarget.time,
            "أذان المغرب (الإفطار)",
            "تقبل الله صيامكم! حان الآن موعد الإفطار في ${country.capitalAr}"
        )
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(REQ_SUHOOR, REQ_FAJR, REQ_MAGHRIB).forEach { reqCode ->
            val intent = Intent(context, AdhanAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
        }
    }

    private fun setExactAlarm(context: Context, requestCode: Int, triggerAtMillis: Long, title: String, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", requestCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}
