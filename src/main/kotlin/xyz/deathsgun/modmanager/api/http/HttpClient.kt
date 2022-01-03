package xyz.deathsgun.modmanager.api.http

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.min

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

    fun download(url: String, path: Path, listener: ((Double) -> Unit)? = null) {
        val output = Files.newOutputStream(path)
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.readTimeout = 10000
        connection.requestMethod = "GET"
        connection.connect()
        if (connection.responseCode != 200) {
            connection.disconnect()
            throw InvalidStatusCodeException(connection.responseCode)
        }
        val size = connection.contentLength
        var downloaded = 0
        while (true) {
            val buffer = ByteArray(min(1024, size - downloaded))
            val read = connection.inputStream.read(buffer)
            if (read == -1) {
                break
            }
            output.write(buffer, 0, read)
            downloaded += read
            listener?.invoke((downloaded / size).toDouble())
        }
        connection.disconnect()
        output.flush()
        output.close()
    }

    class InvalidStatusCodeException(val statusCode: Int) : Exception("Received invalid status code: $statusCode")
}
