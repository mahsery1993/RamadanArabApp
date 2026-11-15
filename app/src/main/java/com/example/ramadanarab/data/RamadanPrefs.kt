package com.example.ramadanarab.data

import android.content.Context

object RamadanPrefs {
    private const val PREFS_NAME = "ramadan_arab_prefs"
    private const val KEY_COUNTRY_INDEX = "country_index"
    private const val KEY_HIJRI_OFFSET = "hijri_offset"
    private const val KEY_ALARMS_ENABLED = "alarms_enabled"
    private const val KEY_LAST_SURAH_ID = "last_surah_id"
    private const val KEY_LAST_SURAH_NAME = "last_surah_name"
    private const val KEY_COMPLETED_JUZ = "completed_juz"

    fun saveCountryIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_COUNTRY_INDEX, index).apply()
    }

    fun getCountry(context: Context): ArabCountry {
        val idx = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_COUNTRY_INDEX, 0)
            .coerceIn(0, ArabCountriesData.countries.lastIndex)
        return ArabCountriesData.countries[idx]
    }

    fun saveHijriOffset(context: Context, offset: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_HIJRI_OFFSET, offset).apply()
    }

    fun getHijriOffset(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HIJRI_OFFSET, 0)
    }

    fun setAlarmsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ALARMS_ENABLED, enabled).apply()
    }

    fun areAlarmsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ALARMS_ENABLED, false)
    }

    fun saveLastReadSurah(context: Context, surahId: Int, surahName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LAST_SURAH_ID, surahId)
            .putString(KEY_LAST_SURAH_NAME, surahName)
            .apply()
    }

    fun getLastReadSurah(context: Context): Pair<Int, String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_LAST_SURAH_ID, -1)
        val name = prefs.getString(KEY_LAST_SURAH_NAME, null)
        return if (id != -1 && name != null) id to name else null
    }

    fun saveCompletedJuz(context: Context, juzSet: Set<Int>) {
        val stringSet = juzSet.map { it.toString() }.toSet()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_COMPLETED_JUZ, stringSet).apply()
    }

    fun getCompletedJuz(context: Context): Set<Int> {
        val stringSet = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_COMPLETED_JUZ, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }
}
