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
import androidx.compose.runtime.saveable.rememberSaveable
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
    val shadowingScores = progress.map { it.shadowingBestSimilarity }.filter { it > 0 }
    val shadowingAverage = if (shadowingScores.isEmpty()) null else shadowingScores.average().toInt()

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
                Text("Shadowing metin benzerliği: " + (shadowingAverage?.let { "%$it" } ?: "Henüz veri yok"))
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
fun SceneListScreen(
    level: String,
    unlockedSceneIds: Set<String>,
    availableSceneIds: Set<String>,
    sceneTitles: Map<String, String>,
    onOpen: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(level, style = MaterialTheme.typography.headlineMedium)
        (1..50).forEach { n ->
            val number3 = n.toString().padStart(3, '0')
            val number2 = n.toString().padStart(2, '0')
            val sceneId = "${level}_SC$number3"
            val unlocked = sceneId in unlockedSceneIds
            val available = sceneId in availableSceneIds
            val open = unlocked && available
            val title = sceneTitles[sceneId]

            OutlinedButton(
                onClick = { if (open) onOpen(sceneId) },
                enabled = open,
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusText = when {
                    open && !title.isNullOrBlank() -> title
                    unlocked && !available -> "Hazırlanıyor"
                    available && !title.isNullOrBlank() -> "$title · Kilitli"
                    else -> "Kilitli"
                }
                Text("Sahne $number2 · $statusText")
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
    var index by rememberSaveable(scene.sceneId) {
        mutableIntStateOf(initialLineIndex.coerceIn(0, scene.lines.lastIndex))
    }
    var autoPlay by rememberSaveable(scene.sceneId) { mutableStateOf(true) }
    var audioMissing by remember { mutableStateOf(false) }
    var showPinyin by rememberSaveable { mutableStateOf(showPinyinDefault) }
    var showTurkish by rememberSaveable { mutableStateOf(showTurkishDefault) }

    val line = scene.lines[index]
    val speaker = if (line.speakerId.contains("LI_NA")) "李娜" else "张伟"
    val context = LocalContext.current
    val audioPlayer = remember { AssetAudioPlayer(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    LaunchedEffect(index, autoPlay, playbackSpeed) {
        audioMissing = false
        if (autoPlay) {
            val assetPath = "${sceneIdToAssetBase(scene.sceneId)}/${line.audioFile}"
            val ok = audioPlayer.play(assetPath, playbackSpeed) {
                if (index < scene.lines.lastIndex) {
                    index++
                    onPositionChanged(index, 0L)
                } else {
                    autoPlay = false
                    onPositionChanged(index, 0L)
                }
            }
            audioMissing = !ok
            if (!ok) autoPlay = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                val position = audioPlayer.currentPositionMs()
                audioPlayer.pause()
                autoPlay = false
                onPositionChanged(index, position)
                onStudy()
            }) { Text("Çalışma") }

            Text(
                "${index + 1} / ${scene.lines.size}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(scene.titleZh, style = MaterialTheme.typography.headlineSmall)
            Text(scene.titleTr, style = MaterialTheme.typography.titleMedium)
        }

        AssetSceneStage(
            sceneAssetBase = sceneIdToAssetBase(scene.sceneId),
            background = scene.visualAssets.background,
            characterA = scene.visualAssets.characterA,
            characterB = scene.visualAssets.characterB,
            foreground = scene.visualAssets.foreground,
            activeSpeakerId = line.speakerId,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 180.dp, max = 245.dp)
        )

        Card(
            Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    speaker,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    line.chinese,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (showPinyin) {
                    Text(
                        line.pinyin,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showTurkish) {
                    Text(
                        line.turkish,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (audioMissing) {
                    Text(
                        "Bu satırın Mandarin sesi bulunamadı.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val position = audioPlayer.currentPositionMs()
                        audioPlayer.pause()
                        autoPlay = false
                        onPositionChanged(index, position)
                        onBack()
                    },
                    modifier = Modifier.weight(1.1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) { Text("Geri") }

                Button(
                    onClick = {
                        audioPlayer.pause()
                        autoPlay = false
                        if (index > 0) {
                            index--
                            onPositionChanged(index, 0L)
                        }
                    },
                    modifier = Modifier.weight(0.8f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) { Text("←") }

                Button(
                    onClick = {
                        if (autoPlay) {
                            onPositionChanged(index, audioPlayer.currentPositionMs())
                            audioPlayer.pause()
                            autoPlay = false
                        } else {
                            autoPlay = true
                        }
                    },
                    modifier = Modifier.weight(1.55f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(if (autoPlay) "Duraklat" else "Oynat")
                }

                Button(
                    onClick = {
                        audioPlayer.pause()
                        autoPlay = false
                        if (index < scene.lines.lastIndex) {
                            index++
                            onPositionChanged(index, 0L)
                        }
                    },
                    modifier = Modifier.weight(0.8f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                ) { Text("→") }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showPinyin,
                    onClick = { showPinyin = !showPinyin },
                    label = { Text(if (showPinyin) "Pinyin ✓" else "Pinyin") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = showTurkish,
                    onClick = { showTurkish = !showTurkish },
                    label = { Text(if (showTurkish) "Türkçe ✓" else "Türkçe") },
                    modifier = Modifier.weight(1f)
                )
            }
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
    onAdminMode: (Boolean) -> Unit,
    onSaveReminder: (ReminderSpec) -> Unit,
    onDeleteReminder: (Int) -> Unit
) {
    var name by remember(settings.userName) { mutableStateOf(settings.userName) }
    var hourText by remember { mutableStateOf("19") }
    var minuteText by remember { mutableStateOf("00") }
    var reminderMessage by remember { mutableStateOf("Bugünkü Çince çalışmanı unutma.") }
    var adminPassword by remember { mutableStateOf("") }
    var adminMessage by remember { mutableStateOf<String?>(null) }
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
        listOf(
            listOf("PURPLE", "BLUE"),
            listOf("GREEN", "PEACH")
        ).forEach { rowPalettes ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPalettes.forEach { p ->
                    FilterChip(
                        selected = settings.palette == p,
                        onClick = { onPalette(p) },
                        label = { Text(p) },
                        modifier = Modifier.weight(1f)
                    )
                }
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
        listOf(
            listOf(0.75f, 0.85f, 1.0f),
            listOf(1.15f, 1.25f)
        ).forEach { speedRow ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                speedRow.forEach { speed ->
                    FilterChip(
                        selected = settings.playbackSpeed == speed,
                        onClick = { onPlaybackSpeed(speed) },
                        label = { Text("${speed}x") },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (speedRow.size == 2) Spacer(Modifier.weight(1f))
            }
        }

        HorizontalDivider()
        Text("Admin erişimi", style = MaterialTheme.typography.titleLarge)
        Text(
            if (settings.adminMode) {
                "Admin modu açık. Mevcut tüm sahneler kilitsiz görüntülenir."
            } else {
                "Admin modu kapalı. HSK2026 parolasını girerek tüm mevcut içeriğe erişebilirsin."
            }
        )
        if (!settings.adminMode) {
            OutlinedTextField(
                value = adminPassword,
                onValueChange = {
                    adminPassword = it
                    adminMessage = null
                },
                label = { Text("Admin şifresi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (adminPassword == "HSK2026") {
                        onAdminMode(true)
                        adminPassword = ""
                        adminMessage = "Admin modu açıldı."
                    } else {
                        adminMessage = "Şifre hatalı."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Admin Modunu Aç") }
        } else {
            OutlinedButton(
                onClick = {
                    onAdminMode(false)
                    adminMessage = "Admin modu kapatıldı."
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Admin Modunu Kapat") }
        }
        adminMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

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
