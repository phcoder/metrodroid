package au.id.micolous.metrodroid.util

expect fun ByteArray.utf8ToString(): String

interface Input {
    fun readBytes(sz: Int): ByteArray
    fun readToString(): String
}
