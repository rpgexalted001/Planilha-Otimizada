package com.example.data

import com.example.model.NpcEncontro
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NpcExternalImportValidatorTest {
    @Test
    fun arquivoLegitimoEImportadoSemAlterarRegras() {
        val original = NpcEncontro(nome = "Lunar válido")
        assertEquals(original, NpcExternalImportValidator.decodeValidated(NpcEncontroJsonCodec.encode(original)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaJsonVazioQueCriariaNpcPadrao() {
        NpcExternalImportValidator.decodeValidated("{}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaTipoDeExaltadoInvalido() {
        val root = NpcEncontroJsonCodec.encodeObject(NpcEncontro(nome = "Teste"))
        root.put("tipoExaltado", "DESCONHECIDO")
        NpcExternalImportValidator.decodeValidated(root.toString())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejeitaMapaDeAtributosArtificialmenteGrande() {
        val root = NpcEncontroJsonCodec.encodeObject(NpcEncontro(nome = "Teste"))
        val mapa = JSONObject()
        repeat(65) { mapa.put("atributo-$it", 1) }
        root.put("attributes", mapa)
        NpcExternalImportValidator.decodeValidated(root.toString())
    }
}
