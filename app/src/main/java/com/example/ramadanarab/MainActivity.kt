package com.example.ramadanarab

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ramadanarab.alarms.AdhanAlarmManager
import com.example.ramadanarab.data.ArabCountriesData
import com.example.ramadanarab.data.ArabCountry
import com.example.ramadanarab.data.RamadanPrefs
import com.example.ramadanarab.domain.RamadanEngine
import com.example.ramadanarab.ui.*
import com.example.ramadanarab.widget.RamadanWidgetProvider
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = EmeraldDark,
                        secondary = RamadanGold,
                        background = CreamBg
                    )
                ) {
                    RamadanApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamadanApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCountry by remember { mutableStateOf(RamadanPrefs.getCountry(context)) }
    var hijriOffset by remember { mutableIntStateOf(RamadanPrefs.getHijriOffset(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رمضان في الوطن العربي", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val tabs = listOf(
                    Triple("الإمساكية", Icons.Default.AccessTime, 0),
                    Triple("المصحف", Icons.Default.AutoStories, 1),
                    Triple("الختمة", Icons.Default.MenuBook, 2),
                    Triple("الأذكار", Icons.Default.TouchApp, 3),
                    Triple("القبلة والضبط", Icons.Default.Explore, 4)
                )
                tabs.forEach { (title, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBg)
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> ImsakiyaScreen(selectedCountry, hijriOffset)
                1 -> OfflineQuranScreen()
                2 -> KhatmaScreen()
                3 -> AzkarTasbihScreen()
                4 -> ToolsAndSettingsScreen(
                    selectedCountry = selectedCountry,
                    hijriOffset = hijriOffset,
                    onCountryChange = { newCountry ->
                        selectedCountry = newCountry
                        val idx = ArabCountriesData.countries.indexOf(newCountry)
                        RamadanPrefs.saveCountryIndex(context, idx)
                        AdhanAlarmManager.scheduleDailyRamadanAlarms(context)
                        RamadanWidgetProvider.updateAllWidgets(context)
                    },
                    onOffsetChange = { newOffset ->
                        hijriOffset = newOffset
                        RamadanPrefs.saveHijriOffset(context, newOffset)
                        RamadanWidgetProvider.updateAllWidgets(context)
                    }
                )
            }
        }
    }
}

@Composable
fun ImsakiyaScreen(country: ArabCountry, hijriOffset: Int) {
    var prayerInfo by remember(country, hijriOffset) {
        mutableStateOf(RamadanEngine.calculateToday(country, hijriOffset))
    }

    LaunchedEffect(country, hijriOffset) {
        while (true) {
            prayerInfo = RamadanEngine.calculateToday(country, hijriOffset)
            delay(1000L)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(EmeraldDark, EmeraldLight)))
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${country.nameAr} - ${country.capitalAr}", color = RamadanGold, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(prayerInfo.hijriDateText, color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(prayerInfo.nextEventTitle, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Text(
                        prayerInfo.countdownText,
                        color = RamadanGold,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        val prayers = listOf(
            "الفجر (الإمساك)" to prayerInfo.fajr,
            "الشروق" to prayerInfo.sunrise,
            "الظهر" to prayerInfo.dhuhr,
            "العصر" to prayerInfo.asr,
            "المغرب (الإفطار)" to prayerInfo.maghrib,
            "العشاء (التراويح)" to prayerInfo.isha
        )

        items(prayers) { (name, time) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = EmeraldDark)
                    Text(time, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun KhatmaScreen() {
    val context = LocalContext.current
    val completedJuz = remember {
        mutableStateListOf<Int>().apply { addAll(RamadanPrefs.getCompletedJuz(context)) }
    }
    val progress = completedJuz.size / 30f

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("متابعة ختمة القرآن الكريم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("أتممت ${completedJuz.size} من أصل 30 جزءاً", color = RamadanGold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = RamadanGold,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(30) { index ->
                val juz = index + 1
                val isDone = completedJuz.contains(juz)
                Card(
                    modifier = Modifier.clickable {
                        if (isDone) completedJuz.remove(juz) else completedJuz.add(juz)
                        RamadanPrefs.saveCompletedJuz(context, completedJuz.toSet())
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) EmeraldLight else Color.White
                    )
                ) {
                    Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "الجزء $juz",
                            color = if (isDone) Color.White else EmeraldDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AzkarTasbihScreen() {
    var count by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val duas = listOf(
        "دعاء الإفطار: ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
        "دعاء ليلة القدر: اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
        "سيد الاستغفار: اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("السبحة الإلكترونية", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(EmeraldDark)
                .clickable {
                    count++
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count", color = RamadanGold, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                Text("اضغط للتسبيح", color = Color.White, fontSize = 12.sp)
            }
        }
        TextButton(onClick = { count = 0 }) {
            Text("تصفير العداد", color = EmeraldDark)
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(duas) { dua ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(dua, modifier = Modifier.padding(16.dp), fontSize = 15.sp, lineHeight = 24.sp)
                }
            }
        }
    }
}

@Composable
fun ToolsAndSettingsScreen(
    selectedCountry: ArabCountry,
    hijriOffset: Int,
    onCountryChange: (ArabCountry) -> Unit,
    onOffsetChange: (Int) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var alarmsEnabled by remember { mutableStateOf(RamadanPrefs.areAlarmsEnabled(context)) }
    var familyCount by remember { mutableStateOf("4") }
    var fitrPerPerson by remember { mutableStateOf("1.5") }
    val totalFitr = (familyCount.toDoubleOrNull() ?: 0.0) * (fitrPerPerson.toDoubleOrNull() ?: 0.0)
    val qiblaAngle = remember(selectedCountry) {
        RamadanEngine.calculateToday(selectedCountry, hijriOffset).qiblaDirection
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            alarmsEnabled = true
            RamadanPrefs.setAlarmsEnabled(context, true)
            AdhanAlarmManager.scheduleDailyRamadanAlarms(context)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("اختر الدولة العربية", fontWeight = FontWeight.Bold, color = EmeraldDark)
                    Spacer(Modifier.height(8.dp))
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("${selectedCountry.nameAr} (${selectedCountry.capitalAr})")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            ArabCountriesData.countries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text("${country.nameAr} - ${country.capitalAr}") },
                                    onClick = {
                                        onCountryChange(country)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("تعديل التاريخ الهجري حسب رؤية الهلال: $hijriOffset يوم")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (-1..1).forEach { offset ->
                            FilterChip(
                                selected = hijriOffset == offset,
                                onClick = { onOffsetChange(offset) },
                                label = { Text(if (offset > 0) "+$offset" else "$offset") }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("منبّه السحور والإفطار والفجر", fontWeight = FontWeight.Bold, color = EmeraldDark)
                        Switch(
                            checked = alarmsEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        alarmsEnabled = true
                                        RamadanPrefs.setAlarmsEnabled(context, true)
                                        AdhanAlarmManager.scheduleDailyRamadanAlarms(context)
                                    }
                                } else {
                                    alarmsEnabled = false
                                    RamadanPrefs.setAlarmsEnabled(context, false)
                                    AdhanAlarmManager.cancelAllAlarms(context)
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("بوصلة القبلة الحية", fontWeight = FontWeight.Bold, color = EmeraldDark)
                    LiveQiblaCompass(qiblaAngle = qiblaAngle)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("حاسبة زكاة الفطر (${selectedCountry.currencyCode})", fontWeight = FontWeight.Bold, color = EmeraldDark)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = familyCount,
                        onValueChange = { familyCount = it },
                        label = { Text("عدد أفراد الأسرة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fitrPerPerson,
                        onValueChange = { fitrPerPerson = it },
                        label = { Text("مقدار الفطرة للفرد (${selectedCountry.currencyCode})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "الإجمالي المستحق: ${String.format(Locale.ENGLISH, "%.2f", totalFitr)} ${selectedCountry.currencyCode}",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
