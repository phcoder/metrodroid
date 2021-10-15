package au.id.micolous.metrodroid.util

expect fun sendPostRequest(urlString: String, request: ByteArray): ByteArray?
expect fun randomUUID(): String