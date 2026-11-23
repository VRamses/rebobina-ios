package net.dynv6.rebobina.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Os estados de tela, desenhados com o texto de `ReboTextos.kt` gerado.
 *
 * Nenhum texto de estado e literal neste arquivo nem nas telas: o que sai e
 * `ReboTextos.Estado.Carregando`, `.SemRede`, `.Erro`, `.Vazio`, que o gerador tirou do JSON
 * canonico 1.0.8. Trocar a palavra e mudar o JSON na origem, nunca o Kotlin.
 *
 * Tempo: `ReboEstadoCarregando.TextoAposMs` decide quando a frase de demora entra, e
 * `ReboMotion` nao entra aqui porque nao ha transicao animada nesta rodada.
 */

@Composable
fun ReboEstadoCarregando(
    celular: Boolean,
    modifier: Modifier = Modifier,
) {
    var mostrarFrase by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(net.dynv6.rebobina.ui.tokens.ReboEstadoCarregando.TextoAposMs.toLong())
        mostrarFrase = true
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReboColor.Base),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ReboDimensao.EntreIrmaos),
            modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular),
        ) {
            CircularProgressIndicator(
                color = ReboColor.Brand,
                modifier = Modifier.size(ReboDimensao.EspacoN6),
            )
            if (mostrarFrase) {
                Text(
                    text = ReboTextos.Estado.Carregando.TituloDemora,
                    style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = ReboTextos.Estado.Carregando.CorpoDemora,
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Falha de tela inteira. `semRede` escolhe o texto de `SemRede`; o resto cai em `Erro`.
 *
 * O detalhe tecnico (status do servidor, excecao) fica escondido atras de "Ver detalhes",
 * como no Android: a tela nao mostra stack trace para quem so quer ver filme.
 */
@Composable
fun ReboEstadoFalha(
    detalhe: String,
    semRede: Boolean,
    celular: Boolean,
    aoTentarDeNovo: () -> Unit,
    modifier: Modifier = Modifier,
    aoAbrirAjustes: (() -> Unit)? = null,
) {
    val titulo = if (semRede) ReboTextos.Estado.SemRede.Titulo else ReboTextos.Estado.Erro.Titulo
    val corpo = if (semRede) ReboTextos.Estado.SemRede.Corpo else ReboTextos.Estado.Erro.Corpo
    val acao = if (semRede) ReboTextos.Estado.SemRede.Acao else ReboTextos.Estado.Erro.Acao
    val detalheRotulo =
        if (semRede) ReboTextos.Estado.SemRede.DetalheRotulo else ReboTextos.Estado.Erro.DetalheRotulo
    var aberto by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReboColor.Base),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ReboDimensao.EntreIrmaos),
            modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular),
        ) {
            Text(
                text = titulo,
                style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
                textAlign = TextAlign.Center,
            )
            Text(
                text = corpo,
                style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2)) {
                ReboBotao(
                    texto = acao,
                    aoTocar = aoTentarDeNovo,
                    tom = ReboBotaoTom.Primario,
                    celular = celular,
                )
                if (aoAbrirAjustes != null) {
                    ReboBotao(
                        texto = ReboRotulos.ABA_AJUSTES,
                        aoTocar = aoAbrirAjustes,
                        tom = ReboBotaoTom.Contorno,
                        celular = celular,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN1),
            ) {
                ReboBotao(
                    texto = detalheRotulo,
                    aoTocar = { aberto = !aberto },
                    tom = ReboBotaoTom.Discreto,
                    celular = celular,
                )
            }
            if (aberto) {
                Text(
                    text = detalhe,
                    style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink3),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Lista vazia com acao. O titulo e o corpo vem do JSON (`estado.vazio`), exceto quando a tela
 * tem algo mais especifico a dizer, e ai a tela passa o texto de `ReboRotulos` de proposito.
 */
@Composable
fun ReboEstadoVazio(
    celular: Boolean,
    aoTocarAcao: () -> Unit,
    modifier: Modifier = Modifier,
    titulo: String = ReboTextos.Estado.Vazio.Titulo,
    corpo: String = ReboTextos.Estado.Vazio.Corpo,
    acao: String = ReboTextos.Estado.Vazio.Acao,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ReboColor.Base)
            .padding(horizontal = ReboDimensao.MargemCelular, vertical = ReboDimensao.EntreSecoes),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ReboDimensao.EntreIrmaos),
        ) {
            Text(
                text = titulo,
                style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
                textAlign = TextAlign.Center,
            )
            Text(
                text = corpo,
                style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(ReboDimensao.DentroDoTexto))
            ReboBotao(
                texto = acao,
                aoTocar = aoTocarAcao,
                tom = ReboBotaoTom.Contorno,
                celular = celular,
            )
        }
    }
}
