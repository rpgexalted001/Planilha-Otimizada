package com.example.data

import com.example.model.CharacterSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Collections

class SheetPersistenceCoordinatorTest {
    @Test
    fun serializaESempreTerminaComOEstadoMaisRecente() = runBlocking {
        val escritos = Collections.synchronizedList(mutableListOf<CharacterSheet>())
        val coordinator = SheetPersistenceCoordinator(
            write = { sheet ->
                delay(10)
                escritos += sheet
            },
            onFailure = { throw it }
        )
        val primeiro = CharacterSheet()
        val segundo = primeiro.copy(id = "mais-recente")
        coordinator.submit(primeiro)
        coordinator.submit(segundo)
        delay(100)
        coordinator.close()
        assertEquals("mais-recente", escritos.last().id)
    }

    @Test
    fun ignoraSubmitDuplicadoEnquantoOEstadoEstaPendente() = runBlocking {
        var quantidade = 0
        val coordinator = SheetPersistenceCoordinator(
            write = { quantidade++ },
            onFailure = { throw it }
        )
        val sheet = CharacterSheet()
        coordinator.submit(sheet)
        coordinator.submit(sheet)
        delay(50)
        coordinator.close()
        assertEquals(1, quantidade)
    }
    @Test
    fun closeSalvaSnapshotPendenteSemAguardarDebounce() = runBlocking {
        val escritos = Collections.synchronizedList(mutableListOf<CharacterSheet>())
        val coordinator = SheetPersistenceCoordinator(
            write = { sheet -> delay(50); escritos += sheet },
            onFailure = { throw it }
        )
        coordinator.submit(CharacterSheet(id = "antigo"))
        coordinator.submit(CharacterSheet(id = "final"))
        coordinator.close()
        assertEquals("final", escritos.last().id)
    }

    @Test
    fun closeCancelaGravacaoEmVooEGravaSnapshotMaisRecente() = runBlocking {
        val escritos = Collections.synchronizedList(mutableListOf<String>())
        val iniciou = kotlinx.coroutines.CompletableDeferred<Unit>()
        val coordinator = SheetPersistenceCoordinator(
            write = { sheet ->
                if (sheet.id == "antigo") {
                    iniciou.complete(Unit)
                    delay(5_000)
                }
                escritos += sheet.id
            },
            onFailure = { throw it }
        )
        coordinator.submit(CharacterSheet(id = "antigo"))
        iniciou.await()
        coordinator.submit(CharacterSheet(id = "novo"))
        coordinator.close()
        assertEquals("novo", escritos.last())
        assertEquals(1, escritos.count { it == "novo" })
    }

    @Test
    fun closeEIdempotenteERejeitaNovasAlteracoes() = runBlocking {
        val escritos = Collections.synchronizedList(mutableListOf<String>())
        val coordinator = SheetPersistenceCoordinator(
            write = { escritos += it.id }, onFailure = { throw it }
        )
        coordinator.submit(CharacterSheet(id = "antes"))
        coordinator.close()
        coordinator.close()
        coordinator.submit(CharacterSheet(id = "depois"))
        assertEquals("antes", escritos.last())
        assertEquals(null, coordinator.latestPending())
    }
}
