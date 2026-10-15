package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.NpcEncontro
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EncounterNpcStoreRecoveryTest {
    @Test
    fun entradaCorrompidaNaoApagaOutrosNpcs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("encounter-recovery-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val correto = NpcEncontro(nome = "Solar intacto")
        val corrompido = JSONObject().put("schemaVersion", NpcEncontroSchema.CURRENT_VERSION + 1)
        val raw = JSONArray().put(NpcEncontroJsonCodec.encodeObject(correto)).put(corrompido).toString()
        prefs.edit().putString("exalted_npcs_encontro", raw).commit()
        val erros = mutableListOf<Exception>()
        val restaurados = EncounterNpcStore(prefs).load { erros += it }
        assertEquals(listOf(correto), restaurados)
        assertEquals(1, erros.size)
        assertTrue(prefs.contains("exalted_npcs_encontro_quarantine"))
        assertEquals(raw, prefs.getString("exalted_npcs_encontro_quarantine", null))
    }
}
