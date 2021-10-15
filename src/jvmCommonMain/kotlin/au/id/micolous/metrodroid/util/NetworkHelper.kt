package au.id.micolous.metrodroid.util

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

actual fun sendPostRequest(urlString: String, request: ByteArray): ByteArray? {
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doInput = true
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", null)

        conn.setRequestProperty("User-Agent", "Metrodroid/" + Preferences.metrodroidVersion)
        val send = conn.outputStream

        send.write(request)
        val recv = conn.inputStream
        val recvBytes = recv.readBytes()
        conn.disconnect()
        return recvBytes
    } catch (e: IOException) {
        return null
    }
}

actual fun randomUUID() = UUID.randomUUID().toString()