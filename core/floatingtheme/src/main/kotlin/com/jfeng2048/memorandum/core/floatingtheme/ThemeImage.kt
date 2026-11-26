package com.jfeng2048.memorandum.core.floatingtheme

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val cache = LruCache<String, ImageBitmap>(32)

private suspend fun loadThemeImage(context: Context, ref: ThemeImageRef): ImageBitmap? {
    cache.get(ref.key)?.let { return it }
    return withContext(Dispatchers.IO) {
        runCatching {
            val bmp = if (ref.assetPath != null) {
                context.assets.open(ref.assetPath).use { BitmapFactory.decodeStream(it) }
            } else {
                BitmapFactory.decodeFile(ref.filePath)
            }
            bmp?.asImageBitmap()
        }.getOrNull()
    }?.also { cache.put(ref.key, it) }
}

@Composable
internal fun ThemeImage(ref: ThemeImageRef, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(cache.get(ref.key), ref.key) {
        value = loadThemeImage(context, ref) ?: value
    }
    bitmap?.let {
        Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Fit, modifier = modifier.size(size))
    } ?: androidx.compose.foundation.layout.Spacer(modifier.size(size))
}
