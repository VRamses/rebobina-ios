package br.app.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.app.rebobina.ui.base.ReboCabecalhoTela
import br.app.rebobina.ui.base.ReboDimensao
import br.app.rebobina.ui.base.ReboEstadoVazio
import br.app.rebobina.ui.base.ReboEstilo
import br.app.rebobina.ui.base.ReboFaixa
import br.app.rebobina.ui.base.ReboFio
import br.app.rebobina.ui.base.ReboRotulos
import br.app.rebobina.ui.tokens.ReboColor
import br.app.rebobina.ui.tokens.ReboTextos

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
        ReboCabecalhoTela(titulo = ReboTextos.Interface.Comum.Downloads, celular = celular)
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
            corpo = ReboTextos.Interface.Downloads.FilaVazia,
            acao = ReboRotulos.FILA_VAZIA_ACAO,
        )
        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        Text(
            text = ReboRotulos.DOWNLOADS_PENDENTE,
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
        )
    }
}
