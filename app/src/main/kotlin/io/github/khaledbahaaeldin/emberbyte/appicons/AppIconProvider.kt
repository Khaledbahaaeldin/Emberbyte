package io.github.khaledbahaaeldin.emberbyte.appicons

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.AppShape
import io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads, scales, and caches app icons off the main thread (Contract 13.6).
 *
 * Keeps an [LruCache] of 64 entries, scaled to 96 px bitmaps via [toBitmap].
 * Pseudo-packages starting with "uid:" or failed package lookups resolve to null.
 */
class AppIconProvider(
    private val packageManager: PackageManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val iconLoader: (String) -> Drawable = { packageManager.getApplicationIcon(it) },
) {
    constructor(context: Context) : this(context.packageManager)

    private val cache = LruCache<String, ImageBitmap>(64)

    suspend fun icon(packageName: String): ImageBitmap? {
        if (packageName.startsWith("uid:") || packageName.isBlank()) {
            return null
        }
        val cached = cache.get(packageName)
        if (cached != null) {
            return cached
        }
        return withContext(ioDispatcher) {
            val existing = cache.get(packageName)
            if (existing != null) {
                return@withContext existing
            }
            try {
                val drawable = iconLoader(packageName)
                val bitmap = drawable.toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888)
                val imageBitmap = bitmap.asImageBitmap()
                cache.put(packageName, imageBitmap)
                imageBitmap
            } catch (_: Throwable) {
                null
            }
        }
    }

    fun shapeFor(packageName: String): AppShape = io.github.khaledbahaaeldin.emberbyte.ui.design.shapes.shapeFor(packageName)
}
