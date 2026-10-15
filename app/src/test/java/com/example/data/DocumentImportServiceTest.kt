package com.example.data

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentImportServiceTest {
    @Test
    fun aceitaJsonPequeno() {
        val json = """{"nome":"Solar"}"""
        assertEquals(json, DocumentImportService.readBounded(ByteArrayInputStream(json.toByteArray())))
    }

    @Test
    fun aceitaExatamenteLimite() {
        val input = ByteArrayInputStream(ByteArray(DocumentImportService.MAX_DOCUMENT_BYTES) { 'a'.code.toByte() })
        assertEquals(DocumentImportService.MAX_DOCUMENT_BYTES, DocumentImportService.readBounded(input).length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaDocumentoAcimaDoLimite() {
        val bytes = ByteArray(DocumentImportService.MAX_DOCUMENT_BYTES + 1)
        DocumentImportService.readBounded(ByteArrayInputStream(bytes))
    }
    @Test(expected = java.nio.charset.CharacterCodingException::class)
    fun rejeitaConteudoUtf8Invalido() {
        DocumentImportService.readBounded(ByteArrayInputStream(byteArrayOf(0xC3.toByte(), 0x28)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaFluxoSemProgresso() {
        val broken = object : java.io.InputStream() {
            override fun read(): Int = 0
            override fun read(b: ByteArray, off: Int, len: Int): Int = 0
        }
        DocumentImportService.readBounded(broken)
    }
}

