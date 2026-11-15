package com.example.ramadanarab.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.ramadanarab.MainActivity
import com.example.ramadanarab.R
import com.example.ramadanarab.data.RamadanPrefs
import com.example.ramadanarab.domain.RamadanEngine

class RamadanWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, RamadanWidgetProvider::class.java))
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val country = RamadanPrefs.getCountry(context)
            val offset = RamadanPrefs.getHijriOffset(context)
            val info = RamadanEngine.calculateToday(country, offset)

            val views = RemoteViews(context.packageName, R.layout.ramadan_widget).apply {
                setTextViewText(R.id.widget_country_date, "${country.capitalAr} • ${info.hijriDateText}")
                setTextViewText(R.id.widget_next_title, info.nextEventTitle)
                setTextViewText(R.id.widget_countdown, info.countdownText)
                setTextViewText(R.id.widget_times, "الفجر: ${info.fajr}  |  المغرب: ${info.maghrib}")

                val intent = Intent(context, MainActivity::class.java)
                val pi = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, pi)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
