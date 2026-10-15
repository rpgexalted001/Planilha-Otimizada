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
}
