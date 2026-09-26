package com.uysal23.newchinese.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.SceneContent
import com.uysal23.newchinese.data.UserSettings
import com.uysal23.newchinese.data.progress.SceneProgressEntity
import com.uysal23.newchinese.media.AssetAudioPlayer
import com.uysal23.newchinese.notifications.ReminderSpec
import java.util.Calendar

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
fun DashboardScreen(
    userName: String,
    progress: List<SceneProgressEntity>,
    favoriteCount: Int,
    onNavigate: (String) -> Unit
) {
    val completed = progress.count { it.sceneCompleted }
    val unlocked = progress.count { it.unlocked }
    val lastScene = progress
        .filter { it.lastStudiedAt > 0L }
        .maxByOrNull { it.lastStudiedAt }
        ?.sceneId
        ?: "Henüz yok"

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("你好，$userName！", style = MaterialTheme.typography.headlineMedium)
        Text("Bugün Çince çalışmaya devam edelim.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tamamlanan sahne: $completed / 300")
                Text("Açılmış sahne: $unlocked / 300")
                Text("Favoriler: $favoriteCount")
                Text("Son çalışma: $lastScene")
                Text("Telaffuz Doğruluk: Henüz veri yok")
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
fun LevelsScreen(
    progress: List<SceneProgressEntity>,
    onSelect: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("HSK Seviyeleri", style = MaterialTheme.typography.headlineMedium)
        (1..6).forEach { n ->
            val prefix = "HSK${n}_SC"
            val completed = progress.count { it.sceneId.startsWith(prefix) && it.sceneCompleted }
            val unlocked = progress.count { it.sceneId.startsWith(prefix) && it.unlocked }
            OutlinedButton(
                onClick = { onSelect("HSK$n") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("HSK$n · $completed / 50")
                    if (unlocked > 0) Text("Açık sahne: $unlocked", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun SceneListScreen(level: String, unlockedSceneIds: Set<String>, availableSceneIds: Set<String>, onOpen: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(level, style = MaterialTheme.typography.headlineMedium)
        (1..50).forEach { n ->
            val number3 = n.toString().padStart(3, '0')
            val sceneId = "${level}_SC$number3"
            val unlocked = sceneId in unlockedSceneIds
            val available = sceneId in availableSceneIds
            val open = unlocked && available
            OutlinedButton(
                onClick = { if (open) onOpen(sceneId) },
                enabled = open,
                modifier = Modifier.fillMaxWidth()
            ) {
                val number = n.toString().padStart(2, '0')
                val label = when {
                    sceneId == "HSK1_SC001" -> "· İlk Tanışma"
                    sceneId == "HSK1_SC002" && available -> "· İsimler"
                    unlocked && !available -> "· İçerik hazırlanıyor"
                    open -> "· Açık"
                    else -> "· Kilitli"
                }
                Text("SC$number $label")
            }
        }
    }
}

@Composable
fun DialogueScreen(
    scene: SceneContent,
    showPinyinDefault: Boolean,
    showTurkishDefault: Boolean,
    playbackSpeed: Float,
    initialLineIndex: Int,
    onPositionChanged: (Int, Long) -> Unit,
    onStudy: () -> Unit,
    onBack: () -> Unit
) {
    var index by rememberSaveable(scene.sceneId) { mutableIntStateOf(initialLineIndex.coerceIn(0, scene.lines.lastIndex)) }
    var playing by remember { mutableStateOf(false) }
    var audioMissing by remember { mutableStateOf(false) }
    var showPinyin by rememberSaveable { mutableStateOf(showPinyinDefault) }
    var showTurkish by rememberSaveable { mutableStateOf(showTurkishDefault) }
    val line = scene.lines[index]
    val speaker = if (line.speakerId.contains("LI_NA")) "李娜" else "张伟"
    val context = LocalContext.current
    val audioPlayer = remember { AssetAudioPlayer(context.applicationContext) }
    DisposableEffect(Unit) { onDispose { audioPlayer.release() } }
    LaunchedEffect(index) { audioMissing = false }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                val position = audioPlayer.currentPositionMs()
                audioPlayer.pause()
                playing = false
                onPositionChanged(index, position)
                onStudy()
            }) { Text("Çalışma") }
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
            ActiveSpeakerMarker(
                speakerName = speaker,
                isLeft = line.speakerId.contains("LI_NA")
            )
            Spacer(Modifier.height(10.dp))
            Text(line.chinese, style = MaterialTheme.typography.headlineSmall)
            if (showPinyin) Text(line.pinyin)
            if (showTurkish) Text(line.turkish)
            Spacer(Modifier.height(12.dp))
            Text("Ses: ${line.voiceId}", style = MaterialTheme.typography.labelSmall)
            if (audioMissing) {
                Text("Doğal Mandarin ses asset’i henüz eklenmedi.", color = MaterialTheme.colorScheme.error)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = {
                val position = audioPlayer.currentPositionMs()
                audioPlayer.pause()
                playing = false
                onPositionChanged(index, position)
                onBack()
            }) { Text("Geri") }
            Button(onClick = { if (index > 0) { audioPlayer.pause(); index--; playing = false; onPositionChanged(index, 0L) } }) { Text("←") }
            Button(onClick = {
                if (playing) {
                    audioPlayer.pause()
                    playing = false
                } else {
                    val scenePath = scene.sceneId.lowercase().replace("_", "/").replace("sc/", "sc")
                    val ok = audioPlayer.play("$scenePath/${line.audioFile}", playbackSpeed) {
                        playing = false
                        if (index < scene.lines.lastIndex) {
                            index++
                            onPositionChanged(index, 0L)
                        }
                    }
                    audioMissing = !ok
                    playing = ok
                }
            }) { Text(if (playing) "Pause" else "Start") }
            Button(onClick = { if (index < scene.lines.lastIndex) { audioPlayer.pause(); index++; playing = false; onPositionChanged(index, 0L) } }) { Text("→") }
            TextButton(onClick = { showTurkish = !showTurkish }) { Text("TR") }
        }
    }
}

@Composable
private fun ActiveSpeakerMarker(speakerName: String, isLeft: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isLeft) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SpeechBubble(width = 54.dp, height = 34.dp)
            Spacer(Modifier.height(4.dp))
            Text(speakerName, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SpeechBubble(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.78f))
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.85f),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f))
                )
            )
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
    onTurkish: (Boolean) -> Unit,
    onPlaybackSpeed: (Float) -> Unit,
    onSaveReminder: (ReminderSpec) -> Unit,
    onDeleteReminder: (Int) -> Unit
) {
    var name by remember(settings.userName) { mutableStateOf(settings.userName) }
    var hourText by remember { mutableStateOf("19") }
    var minuteText by remember { mutableStateOf("00") }
    var reminderMessage by remember { mutableStateOf("Bugünkü Çince çalışmanı unutma.") }
    var selectedDays by remember { mutableStateOf(setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

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
                Text("Konuşma hızı")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.75f, 0.85f, 1.0f, 1.15f, 1.25f).forEach { speed ->
                        FilterChip(
                            selected = settings.playbackSpeed == speed,
                            onClick = { onPlaybackSpeed(speed) },
                            label = { Text("${speed}x") }
                        )
                    }
                }

        HorizontalDivider()
        Text("Hatırlatıcılar", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                label = { Text("Saat") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                label = { Text("Dakika") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = reminderMessage,
            onValueChange = { reminderMessage = it },
            label = { Text("Hatırlatma mesajı") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Günler")
        val days = listOf(
            Calendar.MONDAY to "Pzt",
            Calendar.TUESDAY to "Sal",
            Calendar.WEDNESDAY to "Çar",
            Calendar.THURSDAY to "Per",
            Calendar.FRIDAY to "Cum",
            Calendar.SATURDAY to "Cmt",
            Calendar.SUNDAY to "Paz"
        )
        days.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (day, label) ->
                    FilterChip(
                        selected = day in selectedDays,
                        onClick = {
                            selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
        Button(
            onClick = {
                val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 19
                val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                if (Build.VERSION.SDK_INT >= 33) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (selectedDays.isNotEmpty()) {
                    onSaveReminder(
                        ReminderSpec(
                            id = (System.currentTimeMillis() % 1000000000L).toInt(),
                            hour = hour,
                            minute = minute,
                            days = selectedDays,
                            message = reminderMessage.ifBlank { "Bugünkü Çince çalışmanı unutma." },
                            enabled = true
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Hatırlatıcı Ekle") }

        settings.reminders.forEach { reminder ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("%02d:%02d".format(reminder.hour, reminder.minute), style = MaterialTheme.typography.titleMedium)
                    Text(reminder.message)
                    Text(reminder.days.sorted().joinToString(" · ") { day -> days.firstOrNull { it.first == day }?.second ?: day.toString() })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = reminder.enabled,
                            onCheckedChange = { onSaveReminder(reminder.copy(enabled = it)) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (reminder.enabled) "Açık" else "Kapalı")
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { onDeleteReminder(reminder.id) }) { Text("Sil") }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
