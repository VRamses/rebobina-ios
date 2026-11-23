package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.MidiaCartao
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.base.ReboCampo
import net.dynv6.rebobina.ui.base.ReboChip
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboLinhaDeMidia
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.tokens.ReboColor

/**
 * Busca: campo, sugestoes e resultados do `/Items?searchTerm=`.
 *
 * O atraso de 350 ms existe para nao disparar uma requisicao por tecla; e o mesmo tipo de
 * espera que o Android faz com `debounce`. A espera e cancelada quando o termo muda, porque
 * `LaunchedEffect(termo)` reinicia o bloco.
 *
 * Sem rede a tela mostra o estado de falha de `ReboTextos.Estado.SemRede`; sem resultado,
 * mostra a frase de vazio da propria tela, nunca uma lista vazia silenciosa.
 */
@Composable
fun TelaBusca(
    acervo: Acervo,
    usuarioId: String,
    faixa: ReboFaixa,
    aoAbrirFicha: (String) -> Unit,
    aoAbrirAjustes: () -> Unit,
) {
    val celular = faixa.celular
    var termo by remember { mutableStateOf("") }
    var estado by remember(acervo) { mutableStateOf<EstadoTela<List<MidiaCartao>>?>(null) }
    var tentativa by remember(acervo) { mutableStateOf(0) }

    LaunchedEffect(acervo, termo, tentativa) {
        val limpo = termo.trim()
        if (limpo.length < 2) {
            estado = null
            return@LaunchedEffect
        }
        estado = EstadoTela.Carregando
        delay(350)
        estado = acervo.buscar(usuarioId, limpo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .padding(horizontal = ReboDimensao.MargemCelular)
            .padding(top = ReboDimensao.EspacoN3),
    ) {
        ReboCabecalhoTela(titulo = ReboRotulos.BUSCAR, celular = celular)
        ReboCampo(
            valor = termo,
            aoMudar = { termo = it },
            rotulo = ReboRotulos.CAMPO_BUSCA,
            celular = celular,
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        Row(horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2)) {
            ReboChip(
                texto = ReboRotulos.SUGESTAO_FILMES,
                ativa = false,
                aoTocar = { termo = ReboRotulos.SUGESTAO_FILMES },
                celular = celular,
            )
            ReboChip(
                texto = ReboRotulos.SUGESTAO_SERIES,
                ativa = false,
                aoTocar = { termo = ReboRotulos.SUGESTAO_SERIES },
                celular = celular,
            )
            ReboChip(
                texto = ReboRotulos.SUGESTAO_ANIMES,
                ativa = false,
                aoTocar = { termo = ReboRotulos.SUGESTAO_ANIMES },
                celular = celular,
            )
        }
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        ReboFio()

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val atual = estado) {
                null -> MensagemDeBusca(
                    texto = ReboRotulos.BUSCA_VAZIA,
                    celular = celular,
                )
                EstadoTela.Carregando -> ReboEstadoCarregando(celular)
                is EstadoTela.Falha -> ReboEstadoFalha(
                    detalhe = atual.detalhe,
                    semRede = atual.semRede,
                    celular = celular,
                    aoTentarDeNovo = { tentativa += 1 },
                    aoAbrirAjustes = aoAbrirAjustes,
                )
                is EstadoTela.Pronto -> ResultadosDaBusca(
                    resultados = atual.dado,
                    celular = celular,
                    aoAbrirFicha = aoAbrirFicha,
                )
            }
        }
    }
}

@Composable
private fun MensagemDeBusca(texto: String, celular: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ReboDimensao.AntesDeSecao),
    ) {
        Text(
            text = texto,
            style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
        )
    }
}

@Composable
private fun ResultadosDaBusca(
    resultados: List<MidiaCartao>,
    celular: Boolean,
    aoAbrirFicha: (String) -> Unit,
) {
    if (resultados.isEmpty()) {
        MensagemDeBusca(texto = ReboRotulos.BUSCA_VAZIA, celular = celular)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = ReboDimensao.EspacoN3,
            bottom = ReboDimensao.EntreSecoes,
        ),
        verticalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
    ) {
        item {
            Text(
                text = ReboRotulos.RESULTADOS,
                style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
            )
        }
        items(resultados, key = { it.id }) { midia ->
            ReboLinhaDeMidia(
                midia = midia,
                celular = celular,
                aoTocar = { aoAbrirFicha(midia.id) },
            )
        }
    }
}
