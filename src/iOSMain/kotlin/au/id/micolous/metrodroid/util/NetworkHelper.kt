package au.id.micolous.metrodroid.util

import platform.Foundation.*

actual fun sendPostRequest(urlString: String, request: ByteArray): ByteArray? {
    try {
        val url = NSURL(string = urlString)
        val conn = NSURLRequest(url=url)
        val session = NSURLSession.shared
        conn.httpMethod = "POST"
        conn.setValue(null, "Content-Type")

        conn.setValue("Metrodroid/" + Preferences.metrodroidVersion, "User-Agent")
        conn.httpBody = request


        val recv = conn.inputStream
        val recvBytes = recv.readBytes()
        conn.disconnect()
        return recvBytes
    } catch (e: Exception) {
        return null
    }
}

actual fun randomUUID() = NSUUID.uuidString