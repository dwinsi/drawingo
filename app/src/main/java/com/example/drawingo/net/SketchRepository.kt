package com.example.drawingo.net

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.drawingo.model.SketchCategory
import com.example.drawingo.model.StockSketch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object SketchRepository {

    private const val TAG = "SketchRepository"
    private const val CACHE_FILE_NAME = "cached_sketches.json"
    private val memoryBitmapCache = mutableMapOf<String, Bitmap>()

    /**
     * Loads the available stock sketches (merging bundled offline assets with backend database).
     */
    suspend fun getSketches(context: Context, forceRefresh: Boolean = false): List<StockSketch> = withContext(Dispatchers.IO) {
        val bundled = loadBundledSketches(context)

        // 1. Try remote fetch if online / forceRefresh
        try {
            val remote = AdcBackendClient.getStockSketches()
            if (!remote.isNullOrEmpty()) {
                saveCachedSketches(context, remote)
                return@withContext mergeSketches(bundled, remote)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Remote sketch sync failed: ${e.message}")
        }

        // 2. Fall back to cached sketches on disk
        val cached = loadCachedSketches(context)
        if (cached.isNotEmpty()) {
            return@withContext mergeSketches(bundled, cached)
        }

        return@withContext bundled
    }

    /**
     * Loads the high-resolution bitmap outline for a given sketch.
     */
    suspend fun loadSketchBitmap(context: Context, sketch: StockSketch): Bitmap? = withContext(Dispatchers.IO) {
        // 1. In-memory cache
        memoryBitmapCache[sketch.id]?.let { return@withContext it }

        // 2. Check disk cache
        val cacheDir = File(context.cacheDir, "sketches").apply { mkdirs() }
        val diskFile = File(cacheDir, "${sketch.id}.png")
        if (diskFile.exists() && diskFile.length() > 0) {
            try {
                val bmp = BitmapFactory.decodeFile(diskFile.absolutePath)
                if (bmp != null) {
                    memoryBitmapCache[sketch.id] = bmp
                    return@withContext bmp
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error decoding disk cached sketch: ${e.message}")
            }
        }

        // 3. Check bundled assets
        val assetPath = sketch.assetPath ?: "sketches/${sketch.id}.png"
        try {
            context.assets.open(assetPath).use { input ->
                val bmp = BitmapFactory.decodeStream(input)
                if (bmp != null) {
                    memoryBitmapCache[sketch.id] = bmp
                    return@withContext bmp
                }
            }
        } catch (_: Exception) {
            // Not bundled or filename mismatch, proceed to download
        }

        // 4. Download from remote Cloud Storage URL
        val imageUri = Uri.parse(sketch.imageUrl)
        if (imageUri.scheme.equals("https", ignoreCase = true) &&
            imageUri.host == "storage.googleapis.com") {
            try {
                val url = URL(sketch.imageUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 10000
                conn.readTimeout = 15000
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    conn.inputStream.use { input ->
                        if (conn.contentLengthLong > 8L * 1024L * 1024L) return@withContext null
                        val bytes = input.readBytes()
                        if (bytes.size > 8 * 1024 * 1024) return@withContext null
                        // Save to disk cache
                        FileOutputStream(diskFile).use { it.write(bytes) }
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) {
                            memoryBitmapCache[sketch.id] = bmp
                            return@withContext bmp
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading sketch image: ${e.message}")
            }
        }

        return@withContext null
    }

    private fun loadBundledSketches(context: Context): List<StockSketch> {
        try {
            val jsonStr = context.assets.open("sketches/catalog.json").bufferedReader().use { it.readText() }
            return parseSketchesJson(jsonStr)
        } catch (e: Exception) {
            Log.w(TAG, "Could not load bundled sketches: ${e.message}")
            return emptyList()
        }
    }

    private fun loadCachedSketches(context: Context): List<StockSketch> {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            parseSketchesJson(file.readText())
        } catch (e: Exception) {
            Log.w(TAG, "Could not read cached sketches: ${e.message}")
            emptyList()
        }
    }

    private fun saveCachedSketches(context: Context, sketches: List<StockSketch>) {
        try {
            val array = JSONArray()
            for (s in sketches) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("category", s.category.name)
                    put("emoji", s.emoji)
                    put("difficulty", s.difficulty)
                    put("tags", JSONArray(s.tags))
                    put("imageUrl", s.imageUrl)
                    s.thumbnailUrl?.let { put("thumbnailUrl", it) }
                    s.assetPath?.let { put("assetPath", it) }
                    s.createdAt?.let { put("createdAt", it) }
                }
                array.put(obj)
            }
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(array.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Could not write cached sketches: ${e.message}")
        }
    }

    private fun parseSketchesJson(jsonStr: String): List<StockSketch> {
        val list = mutableListOf<StockSketch>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val tagsList = mutableListOf<String>()
            val tagsArray = item.optJSONArray("tags")
            if (tagsArray != null) {
                for (t in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(t))
                }
            }
            list.add(
                StockSketch(
                    id = item.optString("id", "sketch_$i"),
                    title = item.optString("title", "Sketch"),
                    category = SketchCategory.fromString(item.optString("category")),
                    emoji = item.optString("emoji", "🎨"),
                    difficulty = item.optString("difficulty", "EASY"),
                    tags = tagsList,
                    imageUrl = item.optString("imageUrl", ""),
                    thumbnailUrl = if (item.has("thumbnailUrl") && !item.isNull("thumbnailUrl")) item.getString("thumbnailUrl") else null,
                    assetPath = if (item.has("assetPath") && !item.isNull("assetPath")) item.getString("assetPath") else null,
                    createdAt = if (item.has("createdAt") && !item.isNull("createdAt")) item.getString("createdAt") else null
                )
            )
        }
        return list
    }

    private fun mergeSketches(bundled: List<StockSketch>, remote: List<StockSketch>): List<StockSketch> {
        val map = linkedMapOf<String, StockSketch>()
        for (b in bundled) {
            map[b.id] = b
        }
        for (r in remote) {
            val existing = map[r.id]
            if (existing != null) {
                map[r.id] = r.copy(assetPath = existing.assetPath ?: r.assetPath)
            } else {
                map[r.id] = r
            }
        }
        return map.values.toList()
    }
}
