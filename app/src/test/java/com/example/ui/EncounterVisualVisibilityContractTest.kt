package com.example.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Source-level regression checks; real device visual testing remains necessary. */
class EncounterVisualVisibilityContractTest {
    private val generator = File("src/main/java/com/example/ui/tabs/EncounterGeneratorTab.kt").readText()
    private val npcCard = File("src/main/java/com/example/ui/tabs/EncounterNpcCard.kt").readText()
    private val tabs = File("src/main/java/com/example/ui/SheetTabs.kt").readText()

    @Test
    fun `generate npc labels remain present in both layouts`() {
        assertEquals(2, generator.split("label = \"Gerar NPC\"").size - 1)
        assertEquals(2, generator.split("onClick = gerarNpcDaInterface").size - 1)
        assertEquals(2, generator.split("enabled = !gerando").size - 1)
        assertTrue(generator.contains("val gerarNpcDaInterface: () -> Unit"))
        assertTrue(!generator.contains(".background(ExaltedAccentBright, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))"))
    }

    @Test
    fun `charm drawer heading remains visible and interactive`() {
        assertTrue(npcCard.contains("text = \"\$habilidade (\${encantosDaGaveta.size})\""))
        val drawer = npcCard.substringAfter("val encantosPorHabilidade")
            .substringBefore("// Gaveta exclusiva de Feitiços da Aba 11.")
        // A seleção pertence ao cabeçalho: borda reforçada e semântica,
        // mantendo o título legível nas três paletas.
        assertTrue(drawer.contains("val selecionada = expandida && gavetaSelecionada == habilidade"))
        assertTrue(drawer.contains("if (selecionada) 3.dp else 1.5.dp"))
        assertTrue(drawer.contains("if (selecionada) visualTemplate.accentBright else visualTemplate.outline"))
        assertTrue(drawer.contains(".semantics { selected = selecionada }"))
        assertTrue(drawer.contains("color = visualTemplate.accentBright"))
        assertTrue(drawer.contains("gavetasExpandidas = gavetasExpandidas + habilidade"))
    }

    @Test
    fun `fifteen tab slots remain defined`() {
        for (number in 1..15) {
            assertTrue(tabs.contains("\"$number. ") || (number == 2 && tabs.contains("2. Casta")), "Missing tab $number")
        }
    }
}
