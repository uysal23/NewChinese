package com.uysal23.newchinese.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val CHARACTER_MIN_TRANSPARENT_FRACTION = 0.35f

private data class LoadedVisual(
    val image: ImageBitmap,
    val transparentFraction: Float
)

private fun decodeAssetBitmap(
    context: android.content.Context,
    sceneAssetBase: String,
    relativePath: String?
): Bitmap? {
    if (relativePath.isNullOrBlank()) return null
    return runCatching {
        context.assets.open("$sceneAssetBase/$relativePath").use {
            BitmapFactory.decodeStream(it)
        }
    }.getOrNull()
}

private fun transparentFraction(bitmap: Bitmap): Float {
    if (!bitmap.hasAlpha() || bitmap.width <= 0 || bitmap.height <= 0) return 0f

    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    var transparent = 0
    pixels.forEach { pixel ->
        if (AndroidColor.alpha(pixel) < 245) transparent++
    }
    return transparent.toFloat() / pixels.size.toFloat()
}

private fun trimTransparentPadding(bitmap: Bitmap): Bitmap {
    if (!bitmap.hasAlpha() || bitmap.width <= 0 || bitmap.height <= 0) return bitmap

    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    var left = width
    var top = height
    var right = -1
    var bottom = -1

    for (y in 0 until height) {
        val row = y * width
        for (x in 0 until width) {
            if (AndroidColor.alpha(pixels[row + x]) > 16) {
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
            }
        }
    }

    if (right < left || bottom < top) return bitmap

    val padX = maxOf(2, ((right - left + 1) * 0.025f).toInt())
    val padY = maxOf(2, ((bottom - top + 1) * 0.025f).toInt())
    val cropLeft = maxOf(0, left - padX)
    val cropTop = maxOf(0, top - padY)
    val cropRight = minOf(width - 1, right + padX)
    val cropBottom = minOf(height - 1, bottom + padY)

    val cropWidth = cropRight - cropLeft + 1
    val cropHeight = cropBottom - cropTop + 1

    if (cropWidth == width && cropHeight == height) return bitmap

    return Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropWidth, cropHeight)
}

private fun loadVisual(
    context: android.content.Context,
    sceneAssetBase: String,
    relativePath: String?,
    trimAlphaPadding: Boolean
): LoadedVisual? {
    val bitmap = decodeAssetBitmap(context, sceneAssetBase, relativePath) ?: return null
    val transparency = transparentFraction(bitmap)
    val displayBitmap = if (trimAlphaPadding && transparency > 0f) {
        trimTransparentPadding(bitmap)
    } else {
        bitmap
    }
    return LoadedVisual(
        image = displayBitmap.asImageBitmap(),
        transparentFraction = transparency
    )
}

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
        decodeAssetBitmap(context, sceneAssetBase, relativePath)?.asImageBitmap()
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
    preview: String?,
    activeSpeakerId: String,
    characterAId: String? = null,
    characterBId: String? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
) {
    val context = LocalContext.current

    val backgroundVisual = remember(sceneAssetBase, background) {
        loadVisual(context, sceneAssetBase, background, trimAlphaPadding = false)
    }
    val characterAVisual = remember(sceneAssetBase, characterA) {
        loadVisual(context, sceneAssetBase, characterA, trimAlphaPadding = true)
    }
    val characterBVisual = remember(sceneAssetBase, characterB) {
        loadVisual(context, sceneAssetBase, characterB, trimAlphaPadding = true)
    }
    val previewVisual = remember(sceneAssetBase, preview) {
        loadVisual(context, sceneAssetBase, preview, trimAlphaPadding = false)
    }

    val characterASafe =
        characterAVisual != null &&
            characterAVisual.transparentFraction >= CHARACTER_MIN_TRANSPARENT_FRACTION
    val characterBSafe =
        characterBVisual != null &&
            characterBVisual.transparentFraction >= CHARACTER_MIN_TRANSPARENT_FRACTION

    val usePreviewFallback = previewVisual != null && (!characterASafe || !characterBSafe)

    val characterAActive = characterAId?.let { activeSpeakerId == it }
        ?: activeSpeakerId.contains("LI_NA")
    val characterBActive = characterBId?.let { activeSpeakerId == it }
        ?: activeSpeakerId.contains("ZHANG_WEI")

    val characterAScale by animateFloatAsState(
        targetValue = if (characterAActive) 1.06f else 1.0f,
        animationSpec = tween(220),
        label = "characterAScale"
    )
    val characterBScale by animateFloatAsState(
        targetValue = if (characterBActive) 1.06f else 1.0f,
        animationSpec = tween(220),
        label = "characterBScale"
    )
    val characterAAlpha by animateFloatAsState(
        targetValue = if (characterAActive) 1.0f else 0.84f,
        animationSpec = tween(180),
        label = "characterAAlpha"
    )
    val characterBAlpha by animateFloatAsState(
        targetValue = if (characterBActive) 1.0f else 0.84f,
        animationSpec = tween(180),
        label = "characterBAlpha"
    )

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (usePreviewFallback) {
            Image(
                bitmap = previewVisual!!.image,
                contentDescription = "Sahne önizlemesi",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            return@Box
        }

        if (backgroundVisual != null) {
            Image(
                bitmap = backgroundVisual.image,
                contentDescription = "Sahne arka planı",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Legacy foreground exports can contain blurred/opaque full-frame fragments.
        // Do not render foreground in the dialogue stage until the asset is explicitly
        // validated as a clean transparent depth-only layer. BG + character cutouts
        // provide the stable composition without fogging or masking the scene.

        fun drawCharacterA() {
            // local marker; actual composable draw happens in the branch below
        }

        if (characterAActive && characterASafe) {
            if (characterBSafe) {
                Image(
                    bitmap = characterBVisual!!.image,
                    contentDescription = "Karakter B",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxWidth(0.50f)
                        .fillMaxHeight(0.80f)
                        .graphicsLayer {
                            scaleX = characterBScale
                            scaleY = characterBScale
                            alpha = characterBAlpha
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.65f, 1f)
                        },
                    contentScale = ContentScale.Fit
                )
            }
            Image(
                bitmap = characterAVisual!!.image,
                contentDescription = "Karakter A",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.50f)
                    .fillMaxHeight(0.80f)
                    .graphicsLayer {
                        scaleX = characterAScale
                        scaleY = characterAScale
                        alpha = characterAAlpha
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.35f, 1f)
                    },
                contentScale = ContentScale.Fit
            )
        } else {
            if (characterASafe) {
                Image(
                    bitmap = characterAVisual!!.image,
                    contentDescription = "Karakter A",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.50f)
                        .fillMaxHeight(0.80f)
                        .graphicsLayer {
                            scaleX = characterAScale
                            scaleY = characterAScale
                            alpha = characterAAlpha
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.35f, 1f)
                        },
                    contentScale = ContentScale.Fit
                )
            }
            if (characterBSafe) {
                Image(
                    bitmap = characterBVisual!!.image,
                    contentDescription = "Karakter B",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxWidth(0.50f)
                        .fillMaxHeight(0.80f)
                        .graphicsLayer {
                            scaleX = characterBScale
                            scaleY = characterBScale
                            alpha = characterBAlpha
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.65f, 1f)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (backgroundVisual == null && !characterASafe && !characterBSafe) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("CGI görsel asset’i bekleniyor")
            }
        }
    }
}

fun sceneIdToAssetBase(sceneId: String): String {
    val match = Regex("""HSK(\d)_SC(\d{3})""").matchEntire(sceneId)
        ?: return "hsk1/sc001"
    return "hsk${match.groupValues[1]}/sc${match.groupValues[2]}"
}
