package com.example.data

import com.example.model.NpcEncontro
import com.example.model.TipoExaltadoEncontro
import org.json.JSONObject

/**
 * Verifica o formato ANTES do codec tolerante usado para saves internos
 * legados. Arquivos externos nunca devem virar NPCs vazios por default.
 */
object NpcExternalImportValidator {
    fun decodeValidated(json: String): NpcEncontro {
        val root = JSONObject(json)
        require(root.optString("nome").isNotBlank()) { "NPC sem nome." }
        require(
            TipoExaltadoEncontro.entries.any { it.name == root.optString("tipoExaltado") }
        ) { "Tipo de Exaltado inválido." }
        val attributes = root.optJSONObject("attributes")
        val abilities = root.optJSONObject("abilities")
        require(attributes != null && attributes.length() <= 64) {
            "Atributos ausentes ou em quantidade inválida."
        }
        require(abilities != null && abilities.length() <= 128) {
            "Habilidades ausentes ou em quantidade inválida."
        }
        val limits = mapOf(
            "charms" to 512, "merits" to 256,
            "feiticos" to 256, "healthBoxes" to 256,
            "historicoXpBatches" to 256
        )
        for ((field, max) in limits) {
            require((root.optJSONArray(field)?.length() ?: 0) <= max) {
                "Lista de '$field' excede o limite seguro."
            }
        }
        return NpcEncontroJsonCodec.decode(json)
    }
}
