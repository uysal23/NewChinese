package com.uysal23.newchinese.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uysal23.newchinese.data.SceneContent

@Composable
fun SceneIntroScreen(
    scene: SceneContent,
    onStartDialogue: () -> Unit,
    onStudy: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("Geri") }

        Text(scene.titleZh, style = MaterialTheme.typography.headlineLarge)
        Text(scene.titleTr, style = MaterialTheme.typography.titleLarge)

        AssetSceneImage(
            sceneAssetBase = sceneIdToAssetBase(scene.sceneId),
            relativePath = scene.visualAssets.preview,
            contentDescription = scene.titleTr
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(scene.locationTr)
                Text("Karakterler: ${scene.characterIds.joinToString(" · ") { characterDisplayName(it) }}")
                if (scene.estimatedMinutes > 0) {
                    Text("Tahmini süre: ${scene.estimatedMinutes} dk")
                }
            }
        }

        if (scene.summaryTr.isNotBlank()) {
            Text(scene.summaryTr)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStartDialogue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Diyaloğu Başlat")
        }

        OutlinedButton(
            onClick = onStudy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çalışma")
        }
    }
}

private fun characterDisplayName(characterId: String): String = when (characterId) {
    "CHAR_ZHANG_WEI_001" -> "张伟 · Zhang Wei"
    "CHAR_LI_NA_001" -> "李娜 · Li Na"
    "CHAR_WANG_MING_001" -> "王明 · Wang Ming"
    "CHAR_CHEN_YU_001" -> "陈雨 · Chen Yu"
    "CHAR_LIU_MEI_001" -> "刘梅 · Liu Mei"
    "CHAR_ZHAO_QIANG_001" -> "赵强 · Zhao Qiang"
    "CHAR_SUN_LIN_001" -> "孙琳 · Sun Lin"
    "CHAR_GAO_JIE_001" -> "高杰 · Gao Jie"
    else -> characterId
}
