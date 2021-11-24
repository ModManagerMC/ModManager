package xyz.deathsgun.modmanager.api.http

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

object HttpClient {

    fun get(url: String): ByteArray {
        return get(URI.create(url))
    }

    fun get(uri: URI): ByteArray {
        val connection = uri.toURL().openConnection() as HttpURLConnection
        connection.readTimeout = 10000
        connection.requestMethod = "GET"
        connection.connect()
        if (connection.responseCode != 200) {
            connection.disconnect()
            throw InvalidStatusCodeException(connection.responseCode)
        }
        val content = connection.inputStream.readBytes()
        connection.disconnect()
        return content
    }

    fun getInputStream(url: String): InputStream {
        return getInputStream(URI.create(url))
    }

    private fun getInputStream(uri: URI): InputStream {
        return ByteArrayInputStream(get(uri))
    }

    class InvalidStatusCodeException(val statusCode: Int) : Exception("Received invalid status code: $statusCode")
}
