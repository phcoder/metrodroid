package au.id.micolous.metrodroid.test

import kotlinx.io.core.Input
import kotlinx.io.errors.IOException
import kotlin.test.assertNotNull

expect fun <T> runAsync(block: suspend () -> T)

expect abstract class BaseInstrumentedTestPlatform() {
    fun setLocale(languageTag: String)
    fun showRawStationIds(state: Boolean)
    fun showLocalAndEnglish(state: Boolean)
    fun loadAssetSafe(path: String) : Input?
    fun listAsset(path: String) : List <String>?
}

abstract class BaseInstrumentedTest : BaseInstrumentedTestPlatform() {
    fun loadSmallAssetBytesSafe(path: String): ByteArray? {
        val s = loadAssetSafe(path) ?: return null
        val out = ByteArray(MAX_SMALL_SIZE + 1)
        val length = s.readAvailable(out, 0, MAX_SMALL_SIZE + 1)
        if (length > MAX_SMALL_SIZE || length <= 0) {
            throw IOException("Expected 0 - $MAX_SMALL_SIZE bytes")
        }

        // Return truncated buffer
        return out.sliceArray(0 until length)
    }

    fun loadSmallAssetBytes(path: String): ByteArray {
        val res = loadSmallAssetBytesSafe(path)
        assertNotNull(res, "File $path not found")
        return res
    }

    fun loadAsset(path: String) : Input {
        val stream = loadAssetSafe(path)
        assertNotNull(stream, "File $path not found")
        return stream
    }

    companion object {
        const val MAX_SMALL_SIZE = 1048576
    }
}