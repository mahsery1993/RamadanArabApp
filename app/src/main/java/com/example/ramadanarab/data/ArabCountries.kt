package com.example.ramadanarab.data

import com.batoulapps.adhan.CalculationMethod

data class ArabCountry(
    val nameAr: String,
    val capitalAr: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val calculationMethod: CalculationMethod,
    val currencyCode: String,
    val isRamadanIshaPlus30: Boolean = false
)

object ArabCountriesData {
    val countries = listOf(
        ArabCountry("المملكة العربية السعودية", "الرياض", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA, "ر.س", isRamadanIshaPlus30 = true),
        ArabCountry("الأردن", "عمّان", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.MUSLIM_WORLD_LEAGUE, "د.أ"),
        ArabCountry("مصر", "القاهرة", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPTIAN, "ج.م"),
        ArabCountry("الإمارات العربية المتحدة", "أبوظبي", 24.4539, 54.3773, "Asia/Dubai", CalculationMethod.DUBAI, "د.إ"),
        ArabCountry("قطر", "الدوحة", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.QATAR, "ر.ق", isRamadanIshaPlus30 = true),
        ArabCountry("الكويت", "الكويت", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.KUWAIT, "د.ك"),
        ArabCountry("البحرين", "المنامة", 26.2285, 50.5860, "Asia/Bahrain", CalculationMethod.UMM_AL_QURA, "د.ب"),
        ArabCountry("سلطنة عُمان", "مسقط", 23.5880, 58.3829, "Asia/Muscat", CalculationMethod.UMM_AL_QURA, "ر.ع"),
        ArabCountry("فلسطين", "القدس", 31.7683, 35.2137, "Asia/Jerusalem", CalculationMethod.MUSLIM_WORLD_LEAGUE, "ش.ج"),
        ArabCountry("سوريا", "دمشق", 33.5138, 36.2765, "Asia/Damascus", CalculationMethod.EGYPTIAN, "ل.س"),
        ArabCountry("لبنان", "بيروت", 33.8938, 35.5018, "Asia/Beirut", CalculationMethod.EGYPTIAN, "ل.ل"),
        ArabCountry("العراق", "بغداد", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.EGYPTIAN, "د.ع"),
        ArabCountry("اليمن", "صنعاء", 15.3694, 44.1910, "Asia/Aden", CalculationMethod.UMM_AL_QURA, "ر.ي"),
        ArabCountry("السودان", "الخرطوم", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.EGYPTIAN, "ج.س"),
        ArabCountry("ليبيا", "طرابلس", 32.8872, 13.1913, "Africa/Tripoli", CalculationMethod.EGYPTIAN, "د.ل"),
        ArabCountry("تونس", "تونس", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.MUSLIM_WORLD_LEAGUE, "د.ت"),
        ArabCountry("الجزائر", "الجزائر", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.MUSLIM_WORLD_LEAGUE, "د.ج"),
        ArabCountry("المغرب", "الرباط", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.MUSLIM_WORLD_LEAGUE, "د.م"),
        ArabCountry("موريتانيا", "نواكشوط", 18.0735, -15.9582, "Africa/Nouakchott", CalculationMethod.MUSLIM_WORLD_LEAGUE, "أ.م"),
        ArabCountry("الصومال", "مقديشو", 2.0469, 45.3182, "Africa/Mogadishu", CalculationMethod.MUSLIM_WORLD_LEAGUE, "ش.ص"),
        ArabCountry("جيبوتي", "جيبوتي", 11.5721, 43.1456, "Africa/Djibouti", CalculationMethod.MUSLIM_WORLD_LEAGUE, "ف.ج"),
        ArabCountry("جزر القمر", "موروني", -11.7172, 43.2473, "Indian/Comoro", CalculationMethod.MUSLIM_WORLD_LEAGUE, "ف.ق")
    )
}
