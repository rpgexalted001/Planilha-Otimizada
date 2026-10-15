package com.example.data

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Imports documents explicitly selected by the user through Android's
 * Storage Access Framework. Never resolves a content URI to a filesystem path.
 */
object DocumentImportService {
    const val MAX_DOCUMENT_BYTES = 4 * 1024 * 1024

    fun readBounded(input: InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            if (count == 0) continue
            if (count > MAX_DOCUMENT_BYTES - total) {
                throw IllegalArgumentException("Arquivo excede o limite de 4 MB.")
            }
            total += count
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    fun read(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use(::readBounded)
            ?: throw IllegalArgumentException("Não foi possível abrir o arquivo selecionado.")
    }
}
