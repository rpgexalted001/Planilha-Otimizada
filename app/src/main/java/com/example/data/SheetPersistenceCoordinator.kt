package com.example.data

import com.example.model.CharacterSheet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Persiste o snapshot mais recente em IO, sem perder uma edição pendente
 * quando o ViewModel encerra. O fechamento espera o worker parar antes de
 * gravar o último snapshot, evitando uma gravação antiga após a final.
 */
class SheetPersistenceCoordinator(
    private val write: suspend (CharacterSheet) -> Unit,
    private val onFailure: (Exception) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingSheet: CharacterSheet? = null
    private var inFlightSheet: CharacterSheet? = null
    private var lastPersistedSheet: CharacterSheet? = null
    private var writeJob: Job? = null
    private var closed = false

    @Synchronized
    fun submit(sheet: CharacterSheet) {
        if (closed) return
        if (pendingSheet == sheet) return
        if (pendingSheet == null && (inFlightSheet == sheet ||
                    (inFlightSheet == null && lastPersistedSheet == sheet))) return
        pendingSheet = sheet
        ensureWorkerLocked()
    }

    @Synchronized
    private fun ensureWorkerLocked() {
        if (closed || writeJob?.isActive == true) return
        writeJob = scope.launch { persistPending() }
    }

    private suspend fun persistPending() {
        while (true) {
            val sheet = synchronized(this) {
                if (closed) null else pendingSheet.also {
                    pendingSheet = null
                    inFlightSheet = it
                }
            }
            if (sheet == null) {
                synchronized(this) {
                    writeJob = null
                    if (!closed && pendingSheet != null) ensureWorkerLocked()
                }
                return
            }
            try {
                write(sheet)
                synchronized(this) {
                    lastPersistedSheet = sheet
                    if (inFlightSheet == sheet) inFlightSheet = null
                }
            } catch (e: CancellationException) {
                synchronized(this) { if (inFlightSheet == sheet) inFlightSheet = null }
                throw e
            } catch (e: Exception) {
                synchronized(this) { if (inFlightSheet == sheet) inFlightSheet = null }
                onFailure(e)
            }
        }
    }

    @Synchronized
    fun latestPending(): CharacterSheet? = pendingSheet ?: inFlightSheet

    fun close() {
        val snapshot: CharacterSheet?
        val job: Job?
        synchronized(this) {
            if (closed) return
            closed = true
            snapshot = pendingSheet ?: inFlightSheet
            job = writeJob
            pendingSheet = null
        }
        scope.cancel()
        // onCleared precisa concluir o último salvamento; IO executa no dispatcher
        // mas close aguarda o resultado antes de liberar o repositório.
        runBlocking(Dispatchers.IO) {
            job?.join()
            if (snapshot != null && synchronized(this@SheetPersistenceCoordinator) {
                    lastPersistedSheet != snapshot
                }) {
                try {
                    write(snapshot)
                    synchronized(this@SheetPersistenceCoordinator) { lastPersistedSheet = snapshot }
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
        }
        synchronized(this) {
            writeJob = null
            inFlightSheet = null
        }
    }
}
