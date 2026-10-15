package com.example.ui.tabs

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** Aba 11 — a gaveta, não seus Encantos, recebe o estado selecionado. */
class EncounterNpcCharmSpacingContractTest {
    private val card = File("src/main/java/com/example/ui/tabs/EncounterNpcCard.kt").readText()
    private val section = card.substringAfter("val encantosPorHabilidade")
        .substringBefore("// Gaveta exclusiva de Feitiços da Aba 11.")
    private val drawer = section.substringBefore("groupAccumulatedEncounterCharms(encantosDaGaveta)")
    private val charms = section.substringAfter("groupAccumulatedEncounterCharms(encantosDaGaveta)")
        .substringBefore("if (mostrarDetalhes)")

    @Test
    fun `gavetas expandidas mantem margem superior e inferior para encantos`() {
        assertTrue(section.contains("Spacer(modifier = Modifier.height(10.dp))"))
        assertTrue(section.contains(".padding(start = 18.dp, end = 8.dp, top = 5.dp, bottom = 5.dp)"))
        assertTrue(section.contains("Spacer(modifier = Modifier.height(14.dp))"))
        assertTrue(section.contains(".padding(horizontal = 12.dp, vertical = 10.dp)"))
    }

    @Test
    fun `somente uma gaveta recebe selecao mesmo com multiplas abertas`() {
        assertTrue(drawer.contains("var gavetaSelecionada by remember(npc.id)"))
        assertTrue(drawer.contains("val selecionada = expandida && gavetaSelecionada == habilidade"))
        assertTrue(drawer.contains("gavetaSelecionada = habilidade"))
        assertTrue(drawer.contains("if (gavetaSelecionada == habilidade) gavetaSelecionada = null"))
        assertTrue(drawer.contains(".semantics { selected = selecionada }"))
        assertTrue(drawer.contains("if (selecionada) 3.dp else 1.5.dp"))
        assertTrue(drawer.contains("if (selecionada) visualTemplate.accentBright else visualTemplate.outline"))
        assertTrue(drawer.contains(".width(4.dp)"))
    }

    @Test
    fun `fundo tematico contrastante aparece apenas com a gaveta aberta`() {
        assertTrue(drawer.contains("visualTemplate.surfaceVariant, visualTemplate.accent, 0.24f"))
        assertTrue(drawer.contains("if (expandida) {"))
        assertTrue(drawer.contains("listOf(fundoGavetaAberta, visualTemplate.surfaceVariant, fundoGavetaAberta)"))
        assertTrue(drawer.contains("listOf(visualTemplate.metalDeep, visualTemplate.surfaceVariant, visualTemplate.surface)"))
        assertTrue(drawer.contains("color = visualTemplate.accentBright"))
        assertTrue(drawer.contains("fontSize = 17.sp, lineHeight = 22.sp"))
    }

    @Test
    fun `encantos mantem caixas neutras e detalhes somente no toque longo`() {
        assertTrue(charms.contains(".background(visualTemplate.surfaceVariant)"))
        assertTrue(charms.contains("visualTemplate.outline.copy(alpha = 0.42f)"))
        assertTrue(charms.contains("detectTapGestures(onLongPress = { mostrarDetalhes = true })"))
        assertTrue(!charms.contains("if (selecionado)"))
        assertTrue(!charms.contains(".semantics { selected ="))
        assertTrue(!section.contains("var encantoSelecionado"))
        assertTrue(section.contains("groupAccumulatedEncounterCharms(encantosDaGaveta).forEachIndexed"))
    }

    @Test
    fun `ultima gaveta fica afastada dos dois botoes XP`() {
        val start = card.indexOf("// Três linhas de respiro entre Encantos/Feitiços")
        val end = card.indexOf("// XP movido pra cá", start)
        assertTrue(start >= 0 && end > start)
        val xpSection = card.substring(start, end)
        assertTrue(xpSection.contains("Spacer(modifier = Modifier.height(36.dp))"))
        assertTrue(xpSection.contains("label = \"+ Aumentar XP\""))
        assertTrue(xpSection.contains("label = \"− Diminuir XP\""))
        assertTrue(xpSection.contains("Row("))
    }
}
