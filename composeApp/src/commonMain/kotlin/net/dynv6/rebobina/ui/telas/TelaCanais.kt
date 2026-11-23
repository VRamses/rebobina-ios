package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.CanalDaGrade
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.FormatoDeTexto
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboImagemRemota
import net.dynv6.rebobina.ui.base.ReboProporcao
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.ReboSelo
import net.dynv6.rebobina.ui.base.ReboShape
import net.dynv6.rebobina.ui.base.reboColunasCanal
import net.dynv6.rebobina.ui.base.reboCorDoNome
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Canais: grade de canais ao vivo do `/LiveTv/Channels`, com selo AO VIVO e o programa atual
 * quando o servidor manda `CurrentProgram`.
 *
 * O texto do selo vem de `ReboTextos.Selo.AoVivo` e a cor do selo vem de
 * `ReboTextos.Selo.AoVivo.Cor` traduzida por `reboCorDoNome`: o JSON guarda o nome da cor, nao
 * um hexadecimal, entao a tela nao escreve cor.
 */
@Composable
fun TelaCanais(
    acervo: Acervo,
    usuarioId: String,
    faixa: ReboFaixa,
    aoAbrirAjustes: () -> Unit,
) {
    var estado by remember(acervo, usuarioId) {
        mutableStateOf<EstadoTela<List<CanalDaGrade>>>(EstadoTela.Carregando)
    }
    var tentativa by remember(acervo, usuarioId) { mutableStateOf(0) }
    LaunchedEffect(acervo, usuarioId, tentativa) {
        estado = EstadoTela.Carregando
        estado = acervo.canais(usuarioId)
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
        is EstadoTela.Pronto -> ListaDeCanais(
            canais = atual.dado,
            faixa = faixa,
            aoRecarregar = { tentativa += 1 },
            aoAbrirAjustes = aoAbrirAjustes,
        )
    }
}

@Composable
private fun ListaDeCanais(
    canais: List<CanalDaGrade>,
    faixa: ReboFaixa,
    aoRecarregar: () -> Unit,
    aoAbrirAjustes: () -> Unit,
) {
    val celular = faixa.celular
    val colunas = reboColunasCanal(faixa.larguraDp, faixa.alturaDp)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base),
        contentPadding = PaddingValues(
            start = ReboDimensao.MargemCelular,
            end = ReboDimensao.MargemCelular,
            top = ReboDimensao.EspacoN3,
            bottom = ReboDimensao.EntreSecoes,
        ),
        verticalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN3),
    ) {
        item {
            Column {
                ReboCabecalhoTela(titulo = ReboRotulos.ABA_CANAIS, celular = celular)
                Text(
                    text = if (canais.isEmpty()) ReboRotulos.SEM_GRADE else canais.size.toString() + " " + ReboRotulos.ABA_CANAIS,
                    style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink3),
                )
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                ReboFio()
            }
        }
        if (canais.isEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(ReboDimensao.EntreIrmaos)) {
                    Text(
                        text = ReboRotulos.SEM_GRADE,
                        style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
                    )
                    Text(
                        text = ReboRotulos.TENTAR_DE_NOVO_AJUSTES,
                        style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                    )
                    ReboBotao(
                        texto = ReboTextos.Interface.Comum.Ajustes,
                        aoTocar = { aoRecarregar(); aoAbrirAjustes() },
                        tom = ReboBotaoTom.Contorno,
                        celular = celular,
                    )
                }
            }
        } else {
            items(canais.chunked(colunas.coerceAtLeast(1))) { linha ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
                ) {
                    linha.forEach { canal ->
                        CartaoCanal(
                            canal = canal,
                            celular = celular,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(colunas - linha.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoCanal(
    canal: CanalDaGrade,
    celular: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ReboProporcao.Heroi)
                .clip(ReboShape.Cartao)
                .background(ReboColor.Slot, ReboShape.Cartao)
                .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Cartao),
        ) {
            ReboImagemRemota(canal.urlImagem, Modifier.fillMaxSize())
            ReboSelo(
                texto = ReboTextos.Selo.AoVivo.Texto,
                cor = reboCorDoNome(ReboTextos.Selo.AoVivo.Cor),
                celular = celular,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(ReboDimensao.EspacoN1),
            )
        }
        Spacer(Modifier.height(ReboDimensao.PartesDoCartao))
        Text(
            text = canal.nome,
            style = ReboEstilo.tituloItem(celular).copy(color = ReboColor.Ink),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val programa = FormatoDeTexto.juntar(listOf(canal.programaAtual, FormatoDeTexto.faixaDeHorario(canal.comecaEm, canal.terminaEm)))
        Text(
            text = programa.ifBlank { ReboRotulos.SEM_GRADE },
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (canal.numero != null) {
            Text(
                text = canal.numero,
                style = ReboEstilo.rotulo(celular).copy(color = ReboColor.Ink3),
            )
        }
    }
}
