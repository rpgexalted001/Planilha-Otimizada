package com.example.data

import java.util.Base64
import java.util.zip.CRC32
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericShareCodecHardeningTest {
    private val dictionary = "Exalted:attributes:charms".toByteArray()
    private fun decode(code: String) =
        GenericShareCodec.decodificar(code, "TEST", 1, dictionary, 100_000)

    @Test
    fun codigoIntegroMantemCompatibilidade() {
        val content = """{"nome":"Solar","charms":["Um","Dois"]}"""
        val code = GenericShareCodec.codificar("TEST", 1, content, dictionary, 100_000).getOrThrow()
        val result = decode(code)
        assertTrue(result is GenericShareCodec.Resultado.Sucesso)
        assertEquals(content, (result as GenericShareCodec.Resultado.Sucesso).payload)
    }

    @Test
    fun rejeitaFluxoTruncadoMesmoComCrcRecalculado() {
        val original = GenericShareCodec.codificar("TEST", 1, "A".repeat(2000), dictionary, 100_000).getOrThrow()
        val zipped = Base64.getUrlDecoder().decode(original.substringAfterLast('.'))
        val truncated = zipped.copyOf(zipped.size - 1)
        val crc = CRC32().apply { update(truncated) }.value.toString(16)
        val manipulated = "TEST-V1.$crc." + Base64.getUrlEncoder().withoutPadding().encodeToString(truncated)
        assertTrue(decode(manipulated) is GenericShareCodec.Resultado.Erro)
    }

    @Test
    fun rejeitaPayloadComExpansaoExcessiva() {
        val giant = "x".repeat(9 * 1024 * 1024)
        val code = GenericShareCodec.codificar("TEST", 1, giant, dictionary, 100_000).getOrThrow()
        assertTrue(decode(code) is GenericShareCodec.Resultado.Erro)
    }

    @Test
    fun rejeitaLixoAposDadosComprimidosMesmoComCrcRecalculado() {
        val original = GenericShareCodec.codificar("TEST", 1, "Solar", dictionary, 100_000).getOrThrow()
        val zipped = Base64.getUrlDecoder().decode(original.substringAfterLast('.')) + byteArrayOf(7, 8)
        val crc = CRC32().apply { update(zipped) }.value.toString(16)
        val manipulated = "TEST-V1.$crc." + Base64.getUrlEncoder().withoutPadding().encodeToString(zipped)
        assertTrue(decode(manipulated) is GenericShareCodec.Resultado.Erro)
    }
}
