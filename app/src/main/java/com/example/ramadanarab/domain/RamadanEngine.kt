package com.example.ramadanarab.domain

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import com.example.ramadanarab.data.ArabCountry
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.*

data class RamadanPrayerInfo(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextEventTitle: String,
    val countdownText: String,
    val hijriDateText: String,
    val qiblaDirection: Double,
    val isRamadan: Boolean
)

object RamadanEngine {

    private val hijriMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun calculateToday(country: ArabCountry, hijriDayOffset: Int): RamadanPrayerInfo {
        val tz = TimeZone.getTimeZone(country.timeZoneId)
        val now = Date()
        val coordinates = Coordinates(country.latitude, country.longitude)
        val dateComponents = DateComponents.from(now)
        val params = country.calculationMethod.parameters

        val hijriDate = HijrahDate.from(LocalDate.now().plusDays(hijriDayOffset.toLong()))
        val hDay = hijriDate.get(ChronoField.DAY_OF_MONTH)
        val hMonth = hijriDate.get(ChronoField.MONTH_OF_YEAR)
        val hYear = hijriDate.get(ChronoField.YEAR)
        val isRamadan = (hMonth == 9)

        if (isRamadan && country.isRamadanIshaPlus30) {
            params.adjustments.isha = 30
        }

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar")).apply {
            timeZone = tz
        }

        val (eventTitle, targetTime) = when {
            now.before(prayerTimes.fajr) -> "متبقي على أذان الفجر (الإمساك)" to prayerTimes.fajr
            now.before(prayerTimes.maghrib) -> "متبقي على أذان المغرب (الإفطار)" to prayerTimes.maghrib
            else -> {
                val cal = Calendar.getInstance(tz).apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                val tomorrowTimes = PrayerTimes(coordinates, DateComponents.from(cal.time), params)
                "متبقي على فجر الغد (السحور)" to tomorrowTimes.fajr
            }
        }

        val diffMillis = (targetTime.time - now.time).coerceAtLeast(0L)
        val hours = diffMillis / (1000 * 60 * 60)
        val minutes = (diffMillis / (1000 * 60)) % 60
        val seconds = (diffMillis / 1000) % 60
        val countdown = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)

        val hijriFormatted = "$hDay ${hijriMonths[hMonth - 1]} $hYear هـ"
        val qibla = Qibla(coordinates).direction

        return RamadanPrayerInfo(
            fajr = sdf.format(prayerTimes.fajr),
            sunrise = sdf.format(prayerTimes.sunrise),
            dhuhr = sdf.format(prayerTimes.dhuhr),
            asr = sdf.format(prayerTimes.asr),
            maghrib = sdf.format(prayerTimes.maghrib),
            isha = sdf.format(prayerTimes.isha),
            nextEventTitle = eventTitle,
            countdownText = countdown,
            hijriDateText = hijriFormatted,
            qiblaDirection = qibla,
            isRamadan = isRamadan
        )
    }
}
