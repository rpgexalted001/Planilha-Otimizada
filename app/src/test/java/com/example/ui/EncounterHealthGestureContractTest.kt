package com.example.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Contrato estático da trilha de vitalidade de NPCs.
 * A propagação de gestos e a área de toque exigem validação de interface.
 */
class EncounterHealthGestureContractTest {
    private val source = File("src/main/java/com/example/ui/tabs/EncounterNpcCard.kt").readText()

    @Test
    fun `health box keeps cycling damage on tap and extra box removal on long press`() {
        val healthBox = source.substringAfter("rowBoxes.forEach { box ->")
            .substringBefore("contentAlignment = Alignment.Center")
        assertTrue(healthBox.contains(".feedbackCombinedClickable("))
        assertTrue(healthBox.contains("onClick = { onCiclarDano(box.id) }"))
        assertTrue(healthBox.contains("onLongClick = if (ehExtra) ({ boxParaRemover = box }) else null"))
        assertTrue(!healthBox.contains(".feedbackOnPress("))
        assertTrue(!healthBox.contains(".pointerInput("))
    }
    @Test
    fun `encounter charm and spell details remain long press only`() {
        val charm = source.substringAfter("groupAccumulatedEncounterCharms(encantosDaGaveta)")
            .substringBefore("if (mostrarDetalhes)")
        // Toque curto agora marca/desmarca visualmente a caixa;
        // somente o toque prolongado abre o diálogo.
        assertTrue(charm.contains(".feedbackCombinedClickable("))
        assertTrue(charm.contains("encantoSelecionado = if (selecionado) null else chaveDaCaixa"))
        assertTrue(charm.contains("onLongClick = {"))
        assertTrue(charm.contains("mostrarDetalhes = true"))
        assertTrue(!charm.substringBefore("onLongClick = {").contains("mostrarDetalhes = true"))
        assertTrue(!charm.contains("detectTapGestures("))
        val spell = source.substringAfter("npc.feiticos.forEach { feitico ->")
            .substringBefore("if (mostrarDetalhesFeitico)")
        assertTrue(spell.contains("detectTapGestures(onLongPress = { mostrarDetalhesFeitico = true })"))
        // A tap must not open these dialogs. Gesture/haptic timing needs device validation.
    }

    @Test
    fun `health track keeps clear button right aligned and rows separated`() {
        val health = source.substringAfter("val ordemPenalidadesNpc = NPC_HEALTH_PENALTY_ORDER")
            .substringBefore("Spacer(modifier = Modifier.height(18.dp))")
        assertTrue(health.contains("verticalArrangement = Arrangement.spacedBy(14.dp)"))
        assertTrue(health.contains("Column(verticalArrangement = Arrangement.spacedBy(10.dp))"))
        val clear = health.substringAfter("// Sempre encostada à DIREITA da trilha")
        assertTrue(clear.contains("modifier = Modifier.fillMaxWidth().padding(top = 8.dp)"))
        assertTrue(clear.contains("horizontalArrangement = Arrangement.End"))
        assertTrue(clear.indexOf("horizontalArrangement = Arrangement.End") < clear.indexOf("label = \"Limpar\""))
        assertTrue(clear.contains("enabled = npc.healthBoxes.any { it.tipoDano != 0 }"))
        assertTrue(clear.contains("onClick = onLimparDano"))
    }

    @Test
    fun `health clear action does not steal width from damage rows`() {
        val health = source.substringAfter("val ordemPenalidadesNpc = NPC_HEALTH_PENALTY_ORDER")
            .substringBefore("Spacer(modifier = Modifier.height(18.dp))")
        val clear = health.indexOf("label = \"Limpar\"")
        val groupEnd = health.indexOf("// A ação não compete com a largura")
        assertTrue(groupEnd >= 0 && clear > groupEnd)
        assertTrue(health.contains("enabled = npc.healthBoxes.any { it.tipoDano != 0 }"))
        assertTrue(health.contains("onClick = onLimparDano"))
    }

    @Test
    fun `npc header actions use card constraints and remain accessible on narrow widths`() {
        assertTrue(source.contains("BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(16.dp))"))
        assertTrue(source.contains("val compactPhone = maxWidth < 480.dp"))
        assertTrue(source.contains("val compactActions = compactPhone"))
        assertTrue(source.contains("Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())"))
        assertTrue(source.contains("horizontalArrangement = Arrangement.spacedBy(8.dp)"))
        listOf("label = \"Exportar\"", "label = \"Salvar\"", "label = \"Carregar\"")
            .forEach { assertTrue(source.contains(it), "Missing NPC action: $it") }
    }

    @Test
    fun `xp actions preserve both controls without compressing narrow rows`() {
        val anchor = "// Três linhas de respiro entre Encantos/Feitiços e os controles de XP."
        assertTrue(source.contains(anchor))
        val xp = source.substringAfter(anchor).substringBefore("// XP movido pra cá")
        assertTrue(xp.contains("Spacer(modifier = Modifier.height(36.dp))"))
        assertTrue(xp.contains("val xpActionsScrollable = maxWidth < 320.dp"))
        assertTrue(xp.contains("Modifier.horizontalScroll(rememberScrollState())"))
        assertTrue(xp.contains("Modifier.width(156.dp) else Modifier.weight(1f)"))
        assertTrue(xp.contains("label = \"+ Aumentar XP\""))
        assertTrue(xp.contains("label = \"− Diminuir XP\""))
        assertTrue(xp.contains("enabled = !gerando && npc.historicoXpBatches.isNotEmpty()"))
    }

}
