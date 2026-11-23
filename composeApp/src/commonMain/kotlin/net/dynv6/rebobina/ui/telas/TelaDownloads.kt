package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoVazio
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.tokens.ReboColor

/**
 * Downloads: a fila deste aparelho.
 *
 * Esta tela nao inventa item. O download de verdade (arquivo em disco, progresso, assistir
 * offline) entra na proxima rodada, junto com o player: o que existe hoje e a tela, o estado
 * vazio vindo de `ReboTextos.Estado.Vazio` e a frase declarada de pendencias.
 */
@Composable
fun TelaDownloads(
    faixa: ReboFaixa,
    aoVoltarAoCatalogo: () -> Unit,
) {
    val celular = faixa.celular
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ReboDimensao.MargemCelular)
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        ReboCabecalhoTela(titulo = ReboRotulos.DOWNLOADS_CABECALHO, celular = celular)
        Text(
            text = ReboRotulos.DOWNLOADS_RESUMO,
            style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink3),
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        ReboFio()
        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        ReboEstadoVazio(
            celular = celular,
            aoTocarAcao = aoVoltarAoCatalogo,
            titulo = ReboRotulos.FILA_VAZIA_TITULO,
            corpo = ReboRotulos.FILA_VAZIA_CORPO,
            acao = ReboRotulos.FILA_VAZIA_ACAO,
        )
        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        Text(
            text = ReboRotulos.DOWNLOADS_PENDENTE,
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
        )
    }
}
