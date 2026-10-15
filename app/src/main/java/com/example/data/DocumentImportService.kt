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
        var emptyReads = 0
        while (true) {
            val count = input.read(buffer)
            if (count == -1) break
            if (count == 0) {
                if (++emptyReads >= 3) throw IllegalArgumentException("Fluxo de arquivo inválido.")
                continue
            }
            emptyReads = 0
            if (count > MAX_DOCUMENT_BYTES - total) {
                throw IllegalArgumentException("Arquivo excede o limite de 4 MB.")
            }
            total += count
            output.write(buffer, 0, count)
        }
        return Charsets.UTF_8.newDecoder()
            .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
            .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
            .decode(java.nio.ByteBuffer.wrap(output.toByteArray()))
            .toString()
    }

    fun read(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use(::readBounded)
            ?: throw IllegalArgumentException("Não foi possível abrir o arquivo selecionado.")
    }
}
