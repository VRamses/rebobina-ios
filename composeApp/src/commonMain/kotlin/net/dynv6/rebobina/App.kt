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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.AjustesDoAcervo
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.Imagens
import net.dynv6.rebobina.dados.UsuarioDoAcervo
import net.dynv6.rebobina.ui.base.ReboAdaptativo
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstadoVazio
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboItemDeAba
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.ReboTema
import net.dynv6.rebobina.ui.telas.TelaAjustes
import net.dynv6.rebobina.ui.telas.TelaBusca
import net.dynv6.rebobina.ui.telas.TelaCanais
import net.dynv6.rebobina.ui.telas.TelaDownloads
import net.dynv6.rebobina.ui.telas.TelaFicha
import net.dynv6.rebobina.ui.telas.TelaInicio
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Raiz do app iOS desta rodada: tema, faixa de layout, cinco telas e a ficha.
 *
 * A resolucao do usuario acontece aqui, uma vez: `Acervo.usuarios()` decide se o app abre a
 * navegacao ou se mostra falha, e a tela de Ajustes fica a um toque do erro, porque o caso
 * mais comum de o acervo nao abrir e endereco trocado ou servidor fora do ar.
 *
 * A barra inferior some quando a ficha esta aberta: a ficha e uma tela de detalhe com "Voltar",
 * nao uma sexta aba. O player nao existe nesta rodada; a ficha diz isso em vez de fingir tocar
 * o arquivo.
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
    Inicio(ReboRotulos.ABA_INICIO),
    Canais(ReboRotulos.ABA_CANAIS),
    Downloads(ReboRotulos.ABA_DOWNLOADS),
    Busca(ReboRotulos.ABA_BUSCA),
    Ajustes(ReboRotulos.ABA_AJUSTES),
}

@Composable
private fun ReboRaiz(ajustes: AjustesDoAcervo, faixa: ReboFaixa) {
    val acervo = remember(ajustes.enderecoEfetivo, ajustes.chaveEfetiva) {
        Acervo(endereco = ajustes.enderecoEfetivo, chave = ajustes.chaveEfetiva)
    }
    SideEffect {
        Imagens.carregar = { url -> acervo.bytes(url) }
    }

    var aba by remember { mutableStateOf(Aba.Inicio) }
    var ficha by remember { mutableStateOf<String?>(null) }
    var tentativa by remember(acervo) { mutableStateOf(0) }
    var usuarios by remember(acervo) {
        mutableStateOf<EstadoTela<List<UsuarioDoAcervo>>>(EstadoTela.Carregando)
    }
    LaunchedEffect(acervo, tentativa) {
        usuarios = EstadoTela.Carregando
        usuarios = acervo.usuarios()
    }

    val usuario = (usuarios as? EstadoTela.Pronto)?.dado?.firstOrNull()
    val falha = usuarios as? EstadoTela.Falha

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val aberta = ficha
            when {
                aberta != null && usuario != null -> TelaFicha(
                    acervo = acervo,
                    usuarioId = usuario.id,
                    itemId = aberta,
                    faixa = faixa,
                    aoVoltar = { ficha = null },
                    aoAbrirFicha = { ficha = it },
                )
                aberta != null -> LaunchedEffect(aberta) { ficha = null }
                aba == Aba.Ajustes -> TelaAjustes(
                    ajustes = ajustes,
                    faixa = faixa,
                    aoVoltar = { aba = Aba.Inicio },
                )
                usuario != null -> when (aba) {
                    Aba.Inicio -> TelaInicio(
                        acervo = acervo,
                        usuarioId = usuario.id,
                        nomeDoUsuario = usuario.nome,
                        faixa = faixa,
                        aoAbrirFicha = { ficha = it },
                        aoAbrirAjustes = { aba = Aba.Ajustes },
                    )
                    Aba.Canais -> TelaCanais(
                        acervo = acervo,
                        usuarioId = usuario.id,
                        faixa = faixa,
                        aoAbrirAjustes = { aba = Aba.Ajustes },
                    )
                    Aba.Downloads -> TelaDownloads(
                        faixa = faixa,
                        aoVoltarAoCatalogo = { aba = Aba.Inicio },
                    )
                    Aba.Busca -> TelaBusca(
                        acervo = acervo,
                        usuarioId = usuario.id,
                        faixa = faixa,
                        aoAbrirFicha = { ficha = it },
                        aoAbrirAjustes = { aba = Aba.Ajustes },
                    )
                    Aba.Ajustes -> Unit
                }
                falha != null -> ReboEstadoFalha(
                    detalhe = falha.detalhe,
                    semRede = falha.semRede,
                    celular = faixa.celular,
                    aoTentarDeNovo = { tentativa += 1 },
                    aoAbrirAjustes = { aba = Aba.Ajustes },
                )
                usuarios is EstadoTela.Pronto -> ReboEstadoVazio(
                    celular = faixa.celular,
                    aoTocarAcao = { tentativa += 1 },
                    titulo = ReboRotulos.SEM_USUARIO_TITULO,
                    corpo = ReboRotulos.SEM_USUARIO_CORPO,
                    acao = ReboTextos.Estado.Erro.Acao,
                )
                else -> ReboEstadoCarregando(faixa.celular)
            }
        }
        if (ficha == null) {
            ReboBarraInferior(
                aba = aba,
                celular = faixa.celular,
                aoTrocar = { aba = it },
            )
        }
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
