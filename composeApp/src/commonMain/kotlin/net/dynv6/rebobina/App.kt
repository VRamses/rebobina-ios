package net.dynv6.rebobina

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.AjustesDoAcervo
import net.dynv6.rebobina.dados.AlvoDoPlayer
import net.dynv6.rebobina.dados.Imagens
import net.dynv6.rebobina.player.PlayerNativo
import net.dynv6.rebobina.ui.base.ReboAdaptativo
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboItemDeAba
import net.dynv6.rebobina.ui.base.ReboTema
import net.dynv6.rebobina.ui.telas.TelaAjustes
import net.dynv6.rebobina.ui.telas.TelaBusca
import net.dynv6.rebobina.ui.telas.TelaDownloads
import net.dynv6.rebobina.ui.telas.TelaFicha
import net.dynv6.rebobina.ui.telas.TelaInicio
import net.dynv6.rebobina.ui.telas.TelaPlayer
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Raiz do app iOS: tema, faixa de layout, quatro abas, ficha e player.
 *
 * Endereco do servidor e a unica configuracao que existe, e ela vive em `AjustesDoAcervo`. O
 * app nao resolve usuario na abertura: o catalogo ja responde no primeiro `/catalogo`, e se o
 * servidor estiver fora do ar quem conta isso e a tela do Inicio, com botao para os Ajustes. Foi
 * de proposito, porque travar o app inteiro numa tela de erro deixava o usuario sem acesso ao
 * campo de endereco justamente quando precisava dele.
 *
 * A barra inferior some na ficha e no player: sao telas de detalhe com "Voltar", nao abas.
 */
@Composable
fun App() {
    val ajustes = remember { AjustesDoAcervo() }
    ReboTema {
        ReboAdaptativo { faixa ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ReboColor.Base)
                    .safeDrawingPadding(),
            ) {
                ReboRaiz(ajustes = ajustes, faixa = faixa)
            }
        }
    }
}

private enum class Aba(val rotulo: String) {
    Inicio(ReboTextos.Interface.Comum.Inicio),
    Busca(ReboTextos.Interface.Comum.Buscar),
    Downloads(ReboTextos.Interface.Comum.Downloads),
    Ajustes(ReboTextos.Interface.Comum.Ajustes),
}

@Composable
private fun ReboRaiz(ajustes: AjustesDoAcervo, faixa: ReboFaixa) {
    val acervo = remember(ajustes.enderecoEfetivo) { Acervo(ajustes.enderecoEfetivo) }
    SideEffect {
        Imagens.carregar = { url -> acervo.bytes(url) }
    }

    var aba by remember { mutableStateOf(Aba.Inicio) }
    var ficha by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<AlvoDoPlayer?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val alvo = player
            val aberta = ficha
            when {
                alvo != null -> TelaPlayer(
                    acervo = acervo,
                    alvo = alvo,
                    faixa = faixa,
                    aoVoltar = { player = null },
                )
                aberta != null -> TelaFicha(
                    acervo = acervo,
                    itemId = aberta,
                    faixa = faixa,
                    aoVoltar = { ficha = null },
                    aoAssistir = { player = it },
                )
                else -> when (aba) {
                    Aba.Inicio -> TelaInicio(
                        acervo = acervo,
                        faixa = faixa,
                        aoAbrirFicha = { ficha = it },
                        aoAbrirAjustes = { aba = Aba.Ajustes },
                    )
                    Aba.Busca -> TelaBusca(
                        acervo = acervo,
                        faixa = faixa,
                        aoAbrirFicha = { ficha = it },
                    )
                    Aba.Downloads -> TelaDownloads(
                        faixa = faixa,
                        aoVoltarAoCatalogo = { aba = Aba.Inicio },
                    )
                    Aba.Ajustes -> TelaAjustes(
                        ajustes = ajustes,
                        faixa = faixa,
                        aoSalvar = { aba = Aba.Inicio },
                    )
                }
            }
        }
        if (player == null && ficha == null) {
            ReboBarraInferior(
                aba = aba,
                celular = faixa.celular,
                aoTrocar = { aba = it },
            )
        }
    }

    // Sair do app com o video aberto fecharia o audio sem aviso; fechar aqui devolve o controle.
    DisposableEffect(Unit) {
        onDispose { PlayerNativo.fechar() }
    }
}

@Composable
private fun ReboBarraInferior(aba: Aba, celular: Boolean, aoTrocar: (Aba) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ReboColor.Surface),
    ) {
        ReboFio()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ReboDimensao.AlvoBarra),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Aba.entries.forEach { item ->
                ReboItemDeAba(
                    texto = item.rotulo,
                    ativa = item == aba,
                    aoTocar = { aoTrocar(item) },
                    celular = celular,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(ReboDimensao.EspacoN1))
    }
}
