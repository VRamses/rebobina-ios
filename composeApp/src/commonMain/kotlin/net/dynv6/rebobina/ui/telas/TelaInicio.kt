package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.PaginaInicio
import net.dynv6.rebobina.ui.base.ReboAdaptativo
import net.dynv6.rebobina.ui.base.ReboAvatar
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstadoVazio
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFileiraDeCartoes
import net.dynv6.rebobina.ui.base.ReboGradeDeCartoes
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.reboColunasCatalogo
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Inicio: palavra do app, quem esta logado, trilho de "continuar assistindo" e grade de
 * "adicionados recentemente", na mesma ordem do `HomeScreen` do Android (commit 9ca65241a).
 *
 * Dados: `Acervo.inicio`, que junta `/Users/{id}/Items` com `Filters=IsResumable` e a mesma
 * rota com `SortBy=DateCreated`. Nada de capa ou titulo inventado: se o servidor nao respondeu,
 * a tela e de falha, nunca uma tela vazia fingindo acervo.
 */
@Composable
fun TelaInicio(
    acervo: Acervo,
    usuarioId: String,
    nomeDoUsuario: String,
    faixa: ReboFaixa,
    aoAbrirFicha: (String) -> Unit,
    aoAbrirAjustes: () -> Unit,
) {
    var estado by remember(acervo, usuarioId) {
        mutableStateOf<EstadoTela<PaginaInicio>>(EstadoTela.Carregando)
    }
    var tentativa by remember(acervo, usuarioId) { mutableStateOf(0) }
    LaunchedEffect(acervo, usuarioId, tentativa) {
        estado = EstadoTela.Carregando
        estado = acervo.inicio(usuarioId)
    }
    when (val atual = estado) {
        EstadoTela.Carregando -> ReboEstadoCarregando(faixa.celular)
        is EstadoTela.Falha -> ReboEstadoFalha(
            detalhe = atual.detalhe,
            semRede = atual.semRede,
            celular = faixa.celular,
            aoTentarDeNovo = { tentativa += 1 },
            aoAbrirAjustes = aoAbrirAjustes,
        )
        is EstadoTela.Pronto -> ConteudoInicio(
            dados = atual.dado,
            nomeDoUsuario = nomeDoUsuario,
            faixa = faixa,
            aoAbrirFicha = aoAbrirFicha,
            aoAbrirAjustes = aoAbrirAjustes,
            aoRecarregar = { tentativa += 1 },
        )
    }
}

@Composable
private fun ConteudoInicio(
    dados: PaginaInicio,
    nomeDoUsuario: String,
    faixa: ReboFaixa,
    aoAbrirFicha: (String) -> Unit,
    aoAbrirAjustes: () -> Unit,
    aoRecarregar: () -> Unit,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ReboTextos.Interface.Comum.Marca,
                    style = ReboEstilo.tituloTela(celular).copy(color = ReboColor.Ink),
                )
                Text(
                    text = nomeDoUsuario,
                    style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink3),
                )
            }
            ReboAvatar(
                inicial = nomeDoUsuario.firstOrNull()?.uppercase() ?: "?",
                celular = celular,
            )
            Spacer(Modifier.width(ReboDimensao.EspacoN1))
            ReboBotao(
                texto = ReboTextos.Interface.Comum.Ajustes,
                aoTocar = aoAbrirAjustes,
                tom = ReboBotaoTom.Discreto,
                celular = celular,
            )
        }

        if (dados.retomar.isEmpty() && dados.recentes.isEmpty()) {
            ReboEstadoVazio(celular = celular, aoTocarAcao = aoRecarregar)
        } else {
            if (dados.retomar.isNotEmpty()) {
                Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
                Text(
                    text = ReboRotulos.CONTINUAR_ASSISTINDO,
                    style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
                )
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                ReboFileiraDeCartoes(
                    itens = dados.retomar,
                    celular = celular,
                    aoTocar = { aoAbrirFicha(it.id) },
                )
            } else {
                Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
                Text(
                    text = ReboRotulos.SEM_HISTORICO,
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
                )
            }

            Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
            Text(
                text = ReboRotulos.ADICIONADOS_RECENTEMENTE,
                style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
            )
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            ReboGradeDeCartoes(
                itens = dados.recentes,
                colunas = reboColunasCatalogo(faixa.larguraDp, faixa.alturaDp),
                celular = celular,
                aoTocar = { aoAbrirFicha(it.id) },
            )
        }
    }
}
