package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CharacterSheet
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SheetIndexStoreRecoveryTest {
    private fun prefs(name: String) =
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences(name, Context.MODE_PRIVATE)
            .also { it.edit().clear().commit() }

    @Test
    fun preservaIndiceCorrompidoSemDescartarPlanilhaValida() {
        val p = prefs("sheet-index-partial-corruption")
        val valid = CharacterSheet(id = "jogador-seguro", nome = "Solar")
        val raw = JSONArray()
            .put(CharacterSheetJsonCodec.encode(valid))
            .put("nao-e-um-json")
            .toString()
        p.edit().putString("exalted_sheet_index", raw).commit()
        val failures = mutableListOf<Exception>()
        val recovered = SheetIndexStore(p).loadAll({ failures += it }, { failures += it })
        assertTrue(recovered.any { it.id == valid.id })
        assertEquals(1, failures.size)
        assertEquals(raw, p.getString("exalted_sheet_index_quarantine", null))
    }

    @Test
    fun preservaAtivoCorrompidoParaRecuperacao() {
        val p = prefs("sheet-active-corruption")
        val raw = "{nao-json"
        p.edit().putString("exalted_character_sheet", raw).commit()
        val recovered = SheetIndexStore(p).loadActive { }
        assertTrue(recovered.id.isNotBlank())
        assertEquals(raw, p.getString("exalted_character_sheet_quarantine", null))
    }

    @Test
    fun preservaIndiceInvalidoInteiroAoSalvarOutraPlanilha() {
        val p = prefs("sheet-index-parse-corruption")
        val raw = "{invalido"
        p.edit().putString("exalted_sheet_index", raw).commit()
        SheetIndexStore(p).saveToIndex(CharacterSheet(id = "novo"), {}, {})
        assertEquals(raw, p.getString("exalted_sheet_index_quarantine", null))
    }
}
