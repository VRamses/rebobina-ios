package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.MidiaCartao
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.base.ReboCampo
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstadoVazio
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboGradeDeCartoes
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.reboColunasCatalogo
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Busca no acervo pelo `/catalogo/busca`.
 *
 * Espera 400 ms depois da ultima tecla antes de perguntar, para nao disparar uma chamada por
 * letra. Termo com menos de duas letras nao busca: com uma letra o servidor devolve meio acervo,
 * o que nao ajuda ninguem.
 */
@Composable
fun TelaBusca(
    acervo: Acervo,
    faixa: ReboFaixa,
    aoAbrirFicha: (String) -> Unit,
) {
    val celular = faixa.celular
    var termo by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<EstadoTela<List<MidiaCartao>>?>(null) }
    var tentativa by remember { mutableStateOf(0) }

    LaunchedEffect(acervo, termo, tentativa) {
        val limpo = termo.trim()
        if (limpo.length < 2) {
            resultado = null
            return@LaunchedEffect
        }
        delay(400)
        resultado = EstadoTela.Carregando
        resultado = acervo.buscar(limpo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ReboDimensao.MargemCelular)
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        ReboCabecalhoTela(titulo = ReboTextos.Interface.Comum.Buscar, celular = celular)
        ReboCampo(
            valor = termo,
            aoMudar = { termo = it },
            rotulo = ReboRotulos.CAMPO_BUSCA,
            celular = celular,
        )
        if (termo.isNotBlank()) {
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            ReboBotao(
                texto = ReboRotulos.LIMPAR,
                aoTocar = { termo = "" },
                tom = ReboBotaoTom.Discreto,
                celular = celular,
            )
        }
        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        when (val carregado = resultado) {
            null -> Text(
                text = ReboRotulos.BUSCA_VAZIA,
                style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
            )
            EstadoTela.Carregando -> ReboEstadoCarregando(celular)
            is EstadoTela.Falha -> ReboEstadoFalha(
                detalhe = carregado.detalhe,
                semRede = carregado.semRede,
                celular = celular,
                aoTentarDeNovo = { tentativa += 1 },
            )
            is EstadoTela.Pronto -> if (carregado.dado.isEmpty()) {
                ReboEstadoVazio(
                    celular = celular,
                    aoTocarAcao = { termo = "" },
                    titulo = ReboTextos.Estado.Vazio.Titulo,
                    corpo = ReboRotulos.BUSCA_SEM_RESULTADO,
                    acao = ReboRotulos.LIMPAR,
                )
            } else {
                Text(
                    text = ReboTextos.Interface.Comum.Resultados,
                    style = ReboEstilo.tituloSecao(celular),
                )
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                ReboGradeDeCartoes(
                    itens = carregado.dado,
                    colunas = reboColunasCatalogo(faixa.larguraDp, faixa.alturaDp),
                    celular = celular,
                    aoTocar = { midia -> aoAbrirFicha(midia.id) },
                )
            }
        }
    }
}
