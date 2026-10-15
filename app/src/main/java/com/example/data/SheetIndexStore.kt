package com.example.data

import android.content.SharedPreferences
import com.example.model.CharacterSheet
import org.json.JSONArray

/** Persistence boundary for the saved-character index and active character. */
class SheetIndexStore(private val prefs: SharedPreferences) {
    private val lock = Any()
    fun loadActive(onError: (Exception) -> Unit): CharacterSheet {
        val raw = prefs.getString(KEY_ACTIVE_SHEET, null)
        if (raw.isNullOrEmpty()) return CharacterSheet()
        return try {
            CharacterSheetJsonCodec.decode(raw)
        } catch (e: Exception) {
            quarantineCorruptCopy(KEY_ACTIVE_QUARANTINE, raw)
            onError(e)
            CharacterSheet()
        }
    }

    fun saveActive(sheet: CharacterSheet) {
        synchronized(lock) {
            prefs.edit().putString(KEY_ACTIVE_SHEET, CharacterSheetJsonCodec.encode(sheet)).apply()
        }
    }

    fun loadAll(onItemError: (Exception) -> Unit, onIndexError: (Exception) -> Unit): List<CharacterSheet> {
        val raw = prefs.getString(KEY_SHEET_INDEX, "[]") ?: "[]"
        val result = mutableListOf<CharacterSheet>()
        try {
            val array = JSONArray(raw)
            for (index in 0 until array.length()) {
                try {
                    result += CharacterSheetJsonCodec.decode(array.getString(index))
                } catch (e: Exception) {
                    quarantineCorruptCopy(KEY_INDEX_QUARANTINE, raw)
                    onItemError(e)
                }
            }
        } catch (e: Exception) {
            quarantineCorruptCopy(KEY_INDEX_QUARANTINE, raw)
            onIndexError(e)
        }
        if (result.isEmpty()) result += loadActive(onItemError)
        return result
    }

    fun saveToIndex(sheet: CharacterSheet, onItemError: (Exception) -> Unit, onIndexError: (Exception) -> Unit) {
        synchronized(lock) {
        val current = loadAll(onItemError, onIndexError).toMutableList()
        val index = current.indexOfFirst { it.id == sheet.id }
        if (index >= 0) current[index] = sheet else current += sheet
        val array = JSONArray().apply {
            current.forEach { put(CharacterSheetJsonCodec.encode(it)) }
        }
        prefs.edit().putString(KEY_SHEET_INDEX, array.toString()).apply()
        }
    }

    fun delete(sheetId: String, onItemError: (Exception) -> Unit, onIndexError: (Exception) -> Unit): List<CharacterSheet> {
        synchronized(lock) {
        val current = loadAll(onItemError, onIndexError).filter { it.id != sheetId }
        val array = JSONArray().apply {
            current.forEach { put(CharacterSheetJsonCodec.encode(it)) }
        }
        prefs.edit().putString(KEY_SHEET_INDEX, array.toString()).apply()
        return current
        }
    }

    /** Mantém o texto originalmente corrompido sem torná-lo público. */
    private fun quarantineCorruptCopy(key: String, raw: String) {
        if (!prefs.contains(key) && raw.length <= MAX_QUARANTINE_CHARS) {
            prefs.edit().putString(key, raw).apply()
        }
    }

    private companion object {
        const val MAX_QUARANTINE_CHARS = 4 * 1024 * 1024
        const val KEY_ACTIVE_QUARANTINE = "exalted_character_sheet_quarantine"
        const val KEY_INDEX_QUARANTINE = "exalted_sheet_index_quarantine"
        const val KEY_ACTIVE_SHEET = "exalted_character_sheet"
        const val KEY_SHEET_INDEX = "exalted_sheet_index"
    }
}
