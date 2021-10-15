package au.id.micolous.metrodroid.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import platform.Foundation.*

actual fun sendPostRequest(urlString: String, request: ByteArray): ByteArray? {
    try {
        val url = NSURL(string = urlString)
        val conn = NSURLRequest(url=url)
        val session = NSURLSession.shared
        conn.allowsConstrainedNetworkAccess = true
        conn.allowsExpensiveNetworkAccess = true
        conn.httpMethod = "POST"
        conn.setValue(null, "Content-Type")
        conn.setValue("Metrodroid/" + Preferences.metrodroidVersion, "User-Agent")
        conn.httpBody = request

        val channel = Channel<ByteArray?>()
        return runBlocking {
            val task = session.dataTaskWithURL(conn, completionHandler = { dat, response, error ->
                val respTyped = response as NSHTTPURLResponse
                if (error == null && respTyped.statusCode == 200)
                    channel.send(dat.toByteArray())
                else
                    channel.send(null)
            })
            task.resume()
            channel.receive()
        }
    } catch (e: Exception) {
        return null
    }
}

actual fun randomUUID() = NSUUID.uuidString