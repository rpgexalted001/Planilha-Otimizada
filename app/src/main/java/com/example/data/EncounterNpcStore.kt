package com.example.data

import android.content.SharedPreferences
import com.example.model.NpcEncontro
import org.json.JSONArray

/** Persistência dos NPCs gerados, com isolamento por registro corrompido. */
class EncounterNpcStore(private val prefs: SharedPreferences) {
    fun save(npcs: List<NpcEncontro>) {
        val array = JSONArray().apply {
            npcs.forEach { put(NpcEncontroJsonCodec.encodeObject(it)) }
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun load(onError: (Exception) -> Unit): List<NpcEncontro> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        val array = try {
            JSONArray(raw)
        } catch (e: Exception) {
            onError(e)
            return emptyList()
        }
        val validos = mutableListOf<NpcEncontro>()
        var corrompidos = 0
        for (i in 0 until array.length()) {
            try {
                validos += NpcEncontroJsonCodec.decode(array.getJSONObject(i).toString())
            } catch (e: Exception) {
                corrompidos++
                onError(IllegalArgumentException("NPC de encontro corrompido no índice $i", e))
            }
        }
        if (corrompidos > 0) preservarOriginalCorrompido(raw)
        return validos
    }

    // Preserva uma cópia privada para recuperação manual se uma gravação futura
    // substituir o índice parcialmente corrompido. Não publica dados pessoais.
    private fun preservarOriginalCorrompido(raw: String) {
        if (!prefs.contains(KEY_QUARANTINE)) {
            prefs.edit().putString(KEY_QUARANTINE, raw).apply()
        }
    }

    private companion object {
        const val KEY = "exalted_npcs_encontro"
        const val KEY_QUARANTINE = "exalted_npcs_encontro_quarantine"
    }
}
