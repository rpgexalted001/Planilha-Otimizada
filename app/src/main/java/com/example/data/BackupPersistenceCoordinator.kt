package com.example.data

import com.example.model.CharacterSheet

/**
 * Backups periódicos compartilham a mesma fila segura da planilha ativa.
 * Evita descartes de snapshots pendentes quando o ViewModel é encerrado.
 */
class BackupPersistenceCoordinator(
    write: suspend (CharacterSheet) -> Unit,
    onFailure: (Exception) -> Unit
) {
    private val persistence = SheetPersistenceCoordinator(write, onFailure)

    fun submit(sheet: CharacterSheet) = persistence.submit(sheet)

    fun close() = persistence.close()
}
