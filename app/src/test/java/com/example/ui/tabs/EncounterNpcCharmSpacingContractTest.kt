package com.example.ui.tabs

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** Aba 11 — espaço, seleção e contraste sem alterações mecânicas de Encantos/XP. */
class EncounterNpcCharmSpacingContractTest {
    private val card = File("src/main/java/com/example/ui/tabs/EncounterNpcCard.kt").readText()
    private val section = card.substringAfter("val encantosPorHabilidade")
        .substringBefore("// Gaveta exclusiva de Feitiços da Aba 11.")

    @Test
    fun `gavetas expandidas deixam margem acima e abaixo das caixas de encantos`() {
        assertTrue(section.contains("Spacer(modifier = Modifier.height(10.dp))"))
        assertTrue(section.contains(".padding(start = 18.dp, end = 8.dp, top = 5.dp, bottom = 5.dp)"))
        assertTrue(section.contains("Spacer(modifier = Modifier.height(14.dp))"))
        assertTrue(section.contains(".padding(horizontal = 12.dp, vertical = 10.dp)"))
    }

    @Test
    fun `toque seleciona caixa e toque longo preserva detalhes`() {
        assertTrue(section.contains("var encantoSelecionado by remember(npc.id, npc.charms)"))
        assertTrue(section.contains("val selecionado = encantoSelecionado == chaveDaCaixa"))
        assertTrue(section.contains(".feedbackCombinedClickable("))
        assertTrue(section.contains("encantoSelecionado = if (selecionado) null else chaveDaCaixa"))
        assertTrue(section.contains("onLongClick = {"))
        assertTrue(section.contains("mostrarDetalhes = true"))
        assertTrue(section.contains("groupAccumulatedEncounterCharms(encantosDaGaveta).forEachIndexed"))
    }

    @Test
    fun `caixa selecionada usa paleta propria do NPC`() {
        assertTrue(section.contains("if (selecionado) visualTemplate.accentBright.copy(alpha = 0.25f)"))
        assertTrue(section.contains("if (selecionado) 2.dp else 1.dp"))
        assertTrue(section.contains("if (selecionado) visualTemplate.accentBright else visualTemplate.onSurface"))
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
