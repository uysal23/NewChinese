package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.SceneContent
import com.uysal23.newchinese.data.UserSettings

@Composable
fun WelcomeScreen(onContinue: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("欢迎", style = MaterialTheme.typography.headlineLarge)
        Text("Adın nedir?")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Kullanıcı adı") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { if (name.isNotBlank()) onContinue(name) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Devam Et")
        }
    }
}

@Composable
fun DashboardScreen(userName: String, onNavigate: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("你好，$userName！", style = MaterialTheme.typography.headlineMedium)
        Text("Bugün Çince çalışmaya devam edelim.")
        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Telaffuz Doğruluk")
                Text("Henüz veri yok")
            }
        }
        DashboardButton("HSK Seviyeleri") { onNavigate("levels") }
        DashboardButton("Seviye Tespit Sınavı") { onNavigate("placement") }
        DashboardButton("Serbest Çalışma") { onNavigate("freeStudy") }
        DashboardButton("Favoriler") { onNavigate("favorites") }
        DashboardButton("İlerlemem") { onNavigate("progress") }
        DashboardButton("Ayarlar") { onNavigate("settings") }
    }
}

@Composable
private fun DashboardButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
fun LevelsScreen(onSelect: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("HSK Seviyeleri", style = MaterialTheme.typography.headlineMedium)
        (1..6).forEach { n ->
            OutlinedButton(
                onClick = { onSelect("HSK$n") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("HSK$n · 0 / 50")
            }
        }
    }
}

@Composable
fun SceneListScreen(level: String, onOpen: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(level, style = MaterialTheme.typography.headlineMedium)
        (1..50).forEach { n ->
            val open = level == "HSK1" && n == 1
            OutlinedButton(
                onClick = { if (open) onOpen() },
                enabled = open,
                modifier = Modifier.fillMaxWidth()
            ) {
                val number = n.toString().padStart(2, '0')
                Text("SC$number ${if (open) "· İlk Tanışma" else "· Kilitli"}")
            }
        }
    }
}

@Composable
fun DialogueScreen(
    scene: SceneContent,
    showPinyinDefault: Boolean,
    showTurkishDefault: Boolean,
    onStudy: () -> Unit,
    onBack: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var showPinyin by remember { mutableStateOf(showPinyinDefault) }
    var showTurkish by remember { mutableStateOf(showTurkishDefault) }
    val line = scene.lines[index]
    val speaker = if (line.speakerId.contains("LI_NA")) "李娜" else "张伟"

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onStudy) { Text("Çalışma") }
            TextButton(onClick = { showPinyin = !showPinyin }) {
                Text(if (showPinyin) "Pinyin ✓" else "Pinyin")
            }
        }

        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(scene.titleZh, style = MaterialTheme.typography.titleLarge)
            Text(scene.titleTr)
            Spacer(Modifier.height(32.dp))
            Text(speaker, style = MaterialTheme.typography.titleMedium)
            Text("◌", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Text(line.chinese, style = MaterialTheme.typography.headlineSmall)
            if (showPinyin) Text(line.pinyin)
            if (showTurkish) Text(line.turkish)
            Spacer(Modifier.height(12.dp))
            Text("Ses: ${line.voiceId}", style = MaterialTheme.typography.labelSmall)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onBack) { Text("Geri") }
            Button(onClick = { if (index > 0) { index--; playing = false } }) { Text("←") }
            Button(onClick = { playing = !playing }) { Text(if (playing) "Pause" else "Start") }
            Button(onClick = { if (index < scene.lines.lastIndex) { index++; playing = false } }) { Text("→") }
            TextButton(onClick = { showTurkish = !showTurkish }) { Text("TR") }
        }
    }
}

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onName: (String) -> Unit,
    onDark: (Boolean) -> Unit,
    onPalette: (String) -> Unit,
    onPinyin: (Boolean) -> Unit,
    onTurkish: (Boolean) -> Unit
) {
    var name by remember(settings.userName) { mutableStateOf(settings.userName) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(name, { name = it }, label = { Text("Kullanıcı adı") }, modifier = Modifier.fillMaxWidth())
        Button({ onName(name) }, Modifier.fillMaxWidth()) { Text("Adı Kaydet") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(settings.darkMode, onDark)
            Spacer(Modifier.width(8.dp))
            Text("Koyu tema")
        }
        Text("Pastel tema")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("PURPLE", "BLUE", "GREEN", "PEACH").forEach { p ->
                AssistChip(onClick = { onPalette(p) }, label = { Text(p) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(settings.showPinyin, onPinyin)
            Spacer(Modifier.width(8.dp))
            Text("Pinyin varsayılan açık")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(settings.showTurkish, onTurkish)
            Spacer(Modifier.width(8.dp))
            Text("Türkçe varsayılan açık")
        }
        Text("Hatırlatıcı altyapısı sonraki entegrasyon paketinde bu ekrana bağlanacak.")
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
