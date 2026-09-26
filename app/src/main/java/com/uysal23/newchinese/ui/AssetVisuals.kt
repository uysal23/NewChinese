package com.uysal23.newchinese.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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

@Composable
fun AssetSceneStage(
    sceneAssetBase: String,
    background: String?,
    characterA: String?,
    characterB: String?,
    foreground: String?,
    activeSpeakerId: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
) {
    val liNaActive = activeSpeakerId.contains("LI_NA")
    val zhangWeiActive = activeSpeakerId.contains("ZHANG_WEI")

    val liNaScale by animateFloatAsState(
        targetValue = if (liNaActive) 1.08f else 1.0f,
        animationSpec = tween(220),
        label = "liNaScale"
    )
    val zhangWeiScale by animateFloatAsState(
        targetValue = if (zhangWeiActive) 1.08f else 1.0f,
        animationSpec = tween(220),
        label = "zhangWeiScale"
    )
    val liNaAlpha by animateFloatAsState(
        targetValue = if (liNaActive) 1.0f else 0.82f,
        animationSpec = tween(180),
        label = "liNaAlpha"
    )
    val zhangWeiAlpha by animateFloatAsState(
        targetValue = if (zhangWeiActive) 1.0f else 0.82f,
        animationSpec = tween(180),
        label = "zhangWeiAlpha"
    )

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AssetSceneImage(
            sceneAssetBase = sceneAssetBase,
            relativePath = background,
            contentDescription = "Sahne arka planı",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // The current foreground asset is not guaranteed to contain alpha.
        // Draw it before characters so it can add scene depth without masking bodies.
        if (!foreground.isNullOrBlank()) {
            AssetSceneImage(
                sceneAssetBase = sceneAssetBase,
                relativePath = foreground,
                contentDescription = "Sahne ön planı",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        AssetSceneImage(
            sceneAssetBase = sceneAssetBase,
            relativePath = characterA,
            contentDescription = "Li Na",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.56f)
                .fillMaxHeight(0.98f)
                .graphicsLayer {
                    scaleX = liNaScale
                    scaleY = liNaScale
                    alpha = liNaAlpha
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.35f, 1f)
                },
            contentScale = ContentScale.Fit
        )

        AssetSceneImage(
            sceneAssetBase = sceneAssetBase,
            relativePath = characterB,
            contentDescription = "Zhang Wei",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.56f)
                .fillMaxHeight(0.98f)
                .graphicsLayer {
                    scaleX = zhangWeiScale
                    scaleY = zhangWeiScale
                    alpha = zhangWeiAlpha
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.65f, 1f)
                },
            contentScale = ContentScale.Fit
        )
    }
}

fun sceneIdToAssetBase(sceneId: String): String {
    val match = Regex("""HSK(\d)_SC(\d{3})""").matchEntire(sceneId)
        ?: return "hsk1/sc001"
    return "hsk${match.groupValues[1]}/sc${match.groupValues[2]}"
}
