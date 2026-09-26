package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.PlacementQuestion

@Composable
fun PlacementScreen(
    questions: List<PlacementQuestion>,
    onBack: () -> Unit,
    onGoToLevels: () -> Unit
) {
    val shuffled = remember(questions) { questions.shuffled() }
    var index by remember { mutableIntStateOf(0) }
    val correctByLevel = remember {
        mutableStateMapOf<String, Int>().apply {
            (1..6).forEach { put("HSK$it", 0) }
        }
    }
    var answered by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        val recommended = (1..6)
            .filter { (correctByLevel["HSK$it"] ?: 0) >= 4 }
            .maxOrNull()
            ?: 1

        Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Seviye Tespit Sonucu", style = MaterialTheme.typography.headlineMedium)
            (1..6).forEach { level ->
                val score = correctByLevel["HSK$level"] ?: 0
                Text("HSK$level: $score / 5")
            }
            HorizontalDivider()
            Text(
                "Önerilen başlangıç seviyesi: HSK$recommended",
                style = MaterialTheme.typography.titleLarge
            )
            Text("Bu yalnızca bir öneridir. İstersen HSK1'den başlayabilirsin.")
            Button(onClick = onGoToLevels, modifier = Modifier.fillMaxWidth()) {
                Text("HSK Seviyelerine Git")
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Dashboard'a Dön")
            }
        }
        return
    }

    val q = shuffled[index]
    val displayedOptions = remember(q.id) { q.options.mapIndexed { originalIndex, text -> originalIndex to text }.shuffled() }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Çık") }
            Text("${index + 1} / ${shuffled.size}")
        }

        LinearProgressIndicator(
            progress = { (index + 1).toFloat() / shuffled.size.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )

        AssistChip(onClick = {}, label = { Text(q.level) })
        Text(q.prompt, style = MaterialTheme.typography.headlineSmall)

        displayedOptions.forEach { (originalIndex, option) ->
            OutlinedButton(
                onClick = {
                    if (!answered) {
                        selectedIndex = originalIndex
                        answered = true
                        if (originalIndex == q.answerIndex) {
                            correctByLevel[q.level] = (correctByLevel[q.level] ?: 0) + 1
                        }
                    }
                },
                enabled = !answered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(option)
            }
        }

        if (answered) {
            Text(
                if (selectedIndex == q.answerIndex) {
                    "✓ Doğru"
                } else {
                    "Doğru cevap: ${q.options[q.answerIndex]}"
                }
            )

            Button(
                onClick = {
                    if (index == shuffled.lastIndex) {
                        finished = true
                    } else {
                        index++
                        answered = false
                        selectedIndex = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (index == shuffled.lastIndex) "Sonucu Göster" else "Sonraki Soru")
            }
        }
    }
}
