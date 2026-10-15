package com.example.data

import android.content.SharedPreferences
import com.example.model.Npc
import org.json.JSONArray
import org.json.JSONObject

/** Persistence boundary for the campaign-wide manual NPC list. */
class NpcStore(private val prefs: SharedPreferences) {
    fun save(npcs: List<Npc>) {
        val array = JSONArray().apply { npcs.forEach { put(JSONObject(ModelJsonCodecs.npcToMap(it))) } }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun load(onError: (Exception) -> Unit): List<Npc> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    try {
                        add(ModelJsonCodecs.npcFromJson(array.getJSONObject(index)))
                    } catch (e: Exception) {
                        onError(IllegalArgumentException("NPC manual corrompido no índice $index", e))
                        if (!prefs.contains(KEY_QUARANTINE)) {
                            prefs.edit().putString(KEY_QUARANTINE, raw).apply()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onError(e)
            emptyList()
        }
    }

    private companion object {
        const val KEY = "exalted_npcs"
        const val KEY_QUARANTINE = "exalted_npcs_quarantine"
    }
}
