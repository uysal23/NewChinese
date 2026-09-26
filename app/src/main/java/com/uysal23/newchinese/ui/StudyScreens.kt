package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.SentenceExercise
import com.uysal23.newchinese.data.VocabularyItem

@Composable
fun StudyHubScreen(
    onVocabulary: () -> Unit,
    onSentence: () -> Unit,
    onShadowing: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Çalışma", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onVocabulary, modifier = Modifier.fillMaxWidth()) { Text("Kelime Çalışması") }
        Button(onClick = onSentence, modifier = Modifier.fillMaxWidth()) { Text("Cümle Çalışması") }
        Button(onClick = onShadowing, modifier = Modifier.fillMaxWidth()) { Text("Shadowing") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Diyaloğa Dön") }
    }
}

@Composable
fun VocabularyScreen(items: List<VocabularyItem>, onBack: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var showMeaning by remember { mutableStateOf(false) }
    val favorites = remember { mutableStateListOf<String>() }
    val item = items[index]
    val favorite = favorites.contains(item.id)

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Geri") }
            TextButton(onClick = {
                if (favorite) favorites.remove(item.id) else favorites.add(item.id)
            }) {
                Text(if (favorite) "★" else "☆")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(item.hanzi, style = MaterialTheme.typography.displayMedium)
                Text(item.pinyin, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(18.dp))
                if (showMeaning) Text(item.turkish, style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { showMeaning = !showMeaning }) {
                    Text(if (showMeaning) "Anlamı Gizle" else "Anlamı Göster")
                }
                OutlinedButton(onClick = {}) { Text("🔊 Dinle") }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                if (index > 0) {
                    index--
                    showMeaning = false
                }
            }, enabled = index > 0) { Text("← Önceki") }

            Text("${index + 1} / ${items.size}")

            Button(onClick = {
                if (index < items.lastIndex) {
                    index++
                    showMeaning = false
                }
            }, enabled = index < items.lastIndex) { Text("Sonraki →") }
        }
    }
}

@Composable
fun SentencePracticeScreen(exercises: List<SentenceExercise>, onBack: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val exercise = exercises[index]
    var selectedTokens by remember(index) { mutableStateOf(emptyList<String>()) }
    var selectedOption by remember(index) { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Cümle Çalışması", style = MaterialTheme.typography.headlineMedium)
        Text("${index + 1} / ${exercises.size}")
        OutlinedButton(onClick = {}) { Text("🔊 Doğru Cümleyi Dinle") }

        when (exercise.type) {
            "reorder" -> {
                Text("Kelimeleri doğru sıraya koy.")
                Card(Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTokens.forEachIndexed { tokenIndex, token ->
                            AssistChip(
                                onClick = {
                                    selectedTokens = selectedTokens.toMutableList().also { it.removeAt(tokenIndex) }
                                },
                                label = { Text(token) }
                            )
                        }
                    }
                }
                FlowLikeRow(
                    tokens = exercise.tokens.filterIndexed { tokenIndex, _ ->
                        tokenIndex >= selectedTokens.size || !selectedTokens.contains(exercise.tokens[tokenIndex])
                    },
                    onToken = { selectedTokens = selectedTokens + it }
                )
                val answer = selectedTokens.joinToString("")
                if (selectedTokens.size == exercise.tokens.size) {
                    Text(if (answer == exercise.tokens.joinToString("")) "✓ Doğru" else "Tekrar dene")
                }
            }
            "fill_blank" -> {
                Text(exercise.sentenceZh.orEmpty().replace("___", " - - - - - - - - "))
                exercise.options.forEach { option ->
                    OutlinedButton(
                        onClick = { selectedOption = option },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(option)
                    }
                }
                if (selectedOption != null) {
                    Text(if (selectedOption == exercise.correctAnswer) "✓ Doğru" else "Tekrar dene")
                }
            }
            else -> Text("Bu alıştırma tipi sonraki içerik paketinde etkinleştirilecek.")
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { if (index > 0) index-- },
                enabled = index > 0
            ) { Text("←") }
            Button(
                onClick = { if (index < exercises.lastIndex) index++ },
                enabled = index < exercises.lastIndex
            ) { Text("→") }
        }
    }
}

@Composable
private fun FlowLikeRow(tokens: List<String>, onToken: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tokens.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { token ->
                    AssistChip(onClick = { onToken(token) }, label = { Text(token) })
                }
            }
        }
    }
}

@Composable
fun ShadowingSetupScreen(dialogueCount: Int, onBack: () -> Unit) {
    var selected by remember { mutableStateOf(6) }
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }
        Text("Shadowing", style = MaterialTheme.typography.headlineMedium)
        Text("Tekrar etmek istediğin cümle sayısını seç.")
        listOf(6, 10, 15).forEach { count ->
            OutlinedButton(
                onClick = { selected = count },
                enabled = dialogueCount >= count,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("$count cümle${if (selected == count) " ✓" else ""}")
            }
        }
        OutlinedButton(
            onClick = { selected = dialogueCount },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tüm diyalog${if (selected == dialogueCount) " ✓" else ""}")
        }
        Text("Varsayılan ve minimum seçim: 6 cümle")
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Shadowing'i Başlat")
        }
    }
}
