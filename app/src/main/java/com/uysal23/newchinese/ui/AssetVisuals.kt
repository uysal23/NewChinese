package com.uysal23.newchinese.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AssetSceneImage(
    sceneAssetBase: String,
    relativePath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier.fillMaxWidth().height(220.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmap = remember(sceneAssetBase, relativePath) {
        if (relativePath.isNullOrBlank()) null
        else runCatching {
            context.assets.open("$sceneAssetBase/$relativePath").use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("CGI görsel asset’i bekleniyor")
        }
    }
}

fun sceneIdToAssetBase(sceneId: String): String {
    val match = Regex("""HSK(\d)_SC(\d{3})""").matchEntire(sceneId)
        ?: return "hsk1/sc001"
    return "hsk${match.groupValues[1]}/sc${match.groupValues[2]}"
}
