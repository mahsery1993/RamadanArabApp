package com.example.ramadanarab.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ramadanarab.data.RamadanPrefs
import org.json.JSONArray

data class Surah(val id: Int, val name: String, val type: String, val verses: List<String>)

fun loadQuranFromAssets(context: Context): List<Surah> {
    return try {
        val jsonText = context.assets.open("quran.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            val versesArr = obj.getJSONArray("verses")
            val verses = List(versesArr.length()) { v -> versesArr.getString(v) }
            Surah(obj.getInt("id"), obj.getString("name"), obj.getString("type"), verses)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun OfflineQuranScreen() {
    val context = LocalContext.current
    val surahs = remember { loadQuranFromAssets(context) }
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var fontSizeSp by remember { mutableFloatStateOf(22f) }
    var lastRead by remember { mutableStateOf(RamadanPrefs.getLastReadSurah(context)) }

    if (selectedSurah != null) {
        val surah = selectedSurah!!
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedSurah = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text("سورة ${surah.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
                Row {
                    TextButton(onClick = { if (fontSizeSp > 16f) fontSizeSp -= 2f }) { Text("A-") }
                    TextButton(onClick = { if (fontSizeSp < 34f) fontSizeSp += 2f }) { Text("A+") }
                }
            }
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (surah.id != 1 && surah.id != 9) {
                    Text(
                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                }
                val combinedText = surah.verses.mapIndexed { idx, ayah ->
                    "$ayah ﴿${idx + 1}﴾"
                }.joinToString(" ")
                Text(
                    text = combinedText,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.8f).sp,
                    textAlign = TextAlign.Justify,
                    color = Color(0xFF1E1E1E)
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            lastRead?.let { (id, name) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                        selectedSurah = surahs.find { it.id == id }
                    }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = RamadanGold)
                        Spacer(Modifier.width(8.dp))
                        Text("متابعة القراءة: سورة $name", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحث عن سورة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            val filtered = surahs.filter { it.name.contains(searchQuery.trim()) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { surah ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().clickable {
                            RamadanPrefs.saveLastReadSurah(context, surah.id, surah.name)
                            lastRead = surah.id to surah.name
                            selectedSurah = surah
                        }
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${surah.id}. سورة ${surah.name}", fontWeight = FontWeight.Bold, color = EmeraldDark)
                            Text("${surah.verses.size} آية • ${surah.type}", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
