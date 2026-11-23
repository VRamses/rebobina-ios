package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.EpisodioDaFicha
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.FichaDaMidia
import net.dynv6.rebobina.dados.FormatoDeTexto
import net.dynv6.rebobina.dados.MidiaCartao
import net.dynv6.rebobina.ui.base.ReboAba
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboGradeDeCartoes
import net.dynv6.rebobina.ui.base.ReboImagemRemota
import net.dynv6.rebobina.ui.base.ReboProporcao
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.ReboShape
import net.dynv6.rebobina.ui.base.reboColunasCatalogo
import net.dynv6.rebobina.ui.tokens.ReboColor

/**
 * Ficha da midia: fundo, capa, titulo, meta, sinopse e as abas Episodios / Elenco / Similares,
 * na mesma estrutura do `FichaScreen` do Android.
 *
 * O que esta tela NAO faz, de proposito: tocar o arquivo. O botao de assistir e o de baixar
 * existem para a ficha ficar completa, e os dois mostram a frase declarada
 * `ReboRotulos.PENDENTE_REPRODUTOR` em vez de fingir reproducao. O player e a proxima rodada.
 */
@Composable
fun TelaFicha(
    acervo: Acervo,
    usuarioId: String,
    itemId: String,
    faixa: ReboFaixa,
    aoVoltar: () -> Unit,
    aoAbrirFicha: (String) -> Unit,
) {
    var estado by remember(acervo, itemId) {
        mutableStateOf<EstadoTela<FichaDaMidia>>(EstadoTela.Carregando)
    }
    var tentativa by remember(acervo, itemId) { mutableStateOf(0) }
    LaunchedEffect(acervo, itemId, tentativa) {
        estado = EstadoTela.Carregando
        estado = acervo.ficha(usuarioId, itemId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base),
    ) {
        Box(modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular)) {
            ReboCabecalhoTela(
                titulo = ReboRotulos.FICHA,
                celular = faixa.celular,
                aoVoltar = aoVoltar,
            )
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val atual = estado) {
                EstadoTela.Carregando -> ReboEstadoCarregando(faixa.celular)
                is EstadoTela.Falha -> ReboEstadoFalha(
                    detalhe = atual.detalhe,
                    semRede = atual.semRede,
                    celular = faixa.celular,
                    aoTentarDeNovo = { tentativa += 1 },
                )
                is EstadoTela.Pronto -> ConteudoFicha(
                    ficha = atual.dado,
                    acervo = acervo,
                    usuarioId = usuarioId,
                    faixa = faixa,
                    aoAbrirFicha = aoAbrirFicha,
                )
            }
        }
    }
}

private enum class AbaDaFicha(val rotulo: String) {
    Episodios(ReboRotulos.EPISODIOS),
    Elenco(ReboRotulos.ELENCO),
    Similares(ReboRotulos.SIMILARES),
}

@Composable
private fun ConteudoFicha(
    ficha: FichaDaMidia,
    acervo: Acervo,
    usuarioId: String,
    faixa: ReboFaixa,
    aoAbrirFicha: (String) -> Unit,
) {
    val celular = faixa.celular
    var aba by remember(ficha.id) { mutableStateOf(AbaDaFicha.Elenco) }
    var episodios by remember(ficha.id) { mutableStateOf<EstadoTela<List<EpisodioDaFicha>>?>(null) }
    var similares by remember(ficha.id) { mutableStateOf<EstadoTela<List<MidiaCartao>>?>(null) }
    var tentativa by remember(ficha.id) { mutableStateOf(0) }
    var mostrarPendencia by remember(ficha.id) { mutableStateOf(false) }

    LaunchedEffect(aba, ficha.id, tentativa) {
        when (aba) {
            AbaDaFicha.Episodios -> {
                episodios = EstadoTela.Carregando
                episodios = acervo.episodios(usuarioId, ficha.id)
            }
            AbaDaFicha.Similares -> {
                similares = EstadoTela.Carregando
                similares = acervo.similares(usuarioId, ficha.id)
            }
            AbaDaFicha.Elenco -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (ficha.urlHeroi != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ReboProporcao.Heroi)
                    .background(ReboColor.Slot),
            ) {
                ReboImagemRemota(ficha.urlHeroi, Modifier.fillMaxSize())
            }
        }

        Column(modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular)) {
            Spacer(Modifier.height(ReboDimensao.EspacoN4))
            Row(horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN3)) {
                Box(
                    modifier = Modifier
                        .width(ReboDimensao.LarguraCartao)
                        .aspectRatio(ReboProporcao.Capa)
                        .clip(ReboShape.Capa)
                        .background(ReboColor.Slot, ReboShape.Capa)
                        .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Capa),
                ) {
                    ReboImagemRemota(ficha.urlCapa, Modifier.fillMaxSize())
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ficha.titulo,
                        style = ReboEstilo.tituloTela(celular).copy(color = ReboColor.Ink),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(ReboDimensao.EspacoN1))
                    Text(
                        text = metaDaFicha(ficha),
                        style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink2),
                    )
                    if (ficha.generos.isNotEmpty()) {
                        Spacer(Modifier.height(ReboDimensao.DentroDoTexto))
                        Text(
                            text = ficha.generos.joinToString(FormatoDeTexto.SEPARADOR),
                            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.height(ReboDimensao.EspacoN4))
            Row(
                horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReboBotao(
                    texto = if (ficha.percentualAssistido != null) ReboRotulos.CONTINUAR else ReboRotulos.ASSISTIR,
                    aoTocar = { mostrarPendencia = true },
                    tom = ReboBotaoTom.Primario,
                    celular = celular,
                )
                ReboBotao(
                    texto = ReboRotulos.BAIXAR,
                    aoTocar = { mostrarPendencia = true },
                    tom = ReboBotaoTom.Contorno,
                    celular = celular,
                )
            }
            if (mostrarPendencia) {
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                Text(
                    text = ReboRotulos.PENDENTE_REPRODUTOR,
                    style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Warn),
                )
            }

            Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
            Text(
                text = ReboRotulos.SINOPSE,
                style = ReboEstilo.tituloSecao(celular).copy(color = ReboColor.Ink),
            )
            Spacer(Modifier.height(ReboDimensao.EspacoN1))
            Text(
                text = ficha.sinopse ?: ReboRotulos.SEM_SINOPSE,
                style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
            )

            Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
            ReboFio()
            Row(modifier = Modifier.fillMaxWidth()) {
                AbaDaFicha.entries.forEach { candidata ->
                    ReboAba(
                        texto = candidata.rotulo,
                        selecionada = candidata == aba,
                        aoTocar = { aba = candidata },
                        celular = celular,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            ReboFio()
            Spacer(Modifier.height(ReboDimensao.EspacoN3))

            when (aba) {
                AbaDaFicha.Episodios -> SecaoEpisodios(
                    estado = episodios,
                    celular = celular,
                    aoTentarDeNovo = { tentativa += 1 },
                )
                AbaDaFicha.Elenco -> SecaoElenco(ficha = ficha, celular = celular)
                AbaDaFicha.Similares -> SecaoSimilares(
                    estado = similares,
                    celular = celular,
                    faixa = faixa,
                    aoTentarDeNovo = { tentativa += 1 },
                    aoAbrirFicha = aoAbrirFicha,
                )
            }
            Spacer(Modifier.height(ReboDimensao.EntreSecoes))
        }
    }
}

private fun metaDaFicha(ficha: FichaDaMidia): String = FormatoDeTexto.juntar(
    listOf(
        FormatoDeTexto.tipoEmPortugues(ficha.tipo).takeIf { it.isNotBlank() },
        ficha.ano?.toString(),
        FormatoDeTexto.duracao(ficha.duracaoMinutos),
        ficha.classificacao,
        ficha.nota?.let { ReboRotulos.NOTA + " " + it },
        ficha.numeroEpisodios?.let { "$it " + ReboRotulos.EPISODIOS },
    ),
)

@Composable
private fun SecaoEpisodios(
    estado: EstadoTela<List<EpisodioDaFicha>>?,
    celular: Boolean,
    aoTentarDeNovo: () -> Unit,
) {
    when (estado) {
        null, EstadoTela.Carregando -> ReboEstadoCarregando(celular, Modifier.height(ReboDimensao.AlvoLinha * 3))
        is EstadoTela.Falha -> ReboEstadoFalha(
            detalhe = estado.detalhe,
            semRede = estado.semRede,
            celular = celular,
            aoTentarDeNovo = aoTentarDeNovo,
            modifier = Modifier.height(ReboDimensao.AlvoLinha * 3),
        )
        is EstadoTela.Pronto -> {
            val lista = estado.dado
            if (lista.isEmpty()) {
                Text(
                    text = ReboRotulos.SEM_EPISODIOS,
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2)) {
                    lista.forEach { episodio ->
                        LinhaDeEpisodio(episodio = episodio, celular = celular)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinhaDeEpisodio(episodio: EpisodioDaFicha, celular: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ReboDimensao.AlvoLinha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN3),
    ) {
        Box(
            modifier = Modifier
                .width(ReboDimensao.EspacoN9)
                .aspectRatio(ReboProporcao.Heroi)
                .clip(ReboShape.Controle)
                .background(ReboColor.Slot, ReboShape.Controle),
        ) {
            ReboImagemRemota(episodio.urlImagem, Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episodio.titulo,
                style = ReboEstilo.tituloItem(celular).copy(color = ReboColor.Ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = FormatoDeTexto.juntar(
                    listOf(
                        FormatoDeTexto.episodio(episodio.temporada, episodio.numero),
                        FormatoDeTexto.duracao(episodio.duracaoMinutos),
                    ),
                ),
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
            )
        }
    }
}

@Composable
private fun SecaoElenco(ficha: FichaDaMidia, celular: Boolean) {
    if (ficha.pessoas.isEmpty()) {
        Text(
            text = ReboRotulos.SEM_ELENCO,
            style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2)) {
        ficha.pessoas.forEach { pessoa ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN3),
            ) {
                Text(
                    text = pessoa.nome,
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink),
                    modifier = Modifier.weight(1f),
                )
                if (!pessoa.papel.isNullOrBlank()) {
                    Text(
                        text = pessoa.papel,
                        style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SecaoSimilares(
    estado: EstadoTela<List<MidiaCartao>>?,
    celular: Boolean,
    faixa: ReboFaixa,
    aoTentarDeNovo: () -> Unit,
    aoAbrirFicha: (String) -> Unit,
) {
    when (estado) {
        null, EstadoTela.Carregando -> ReboEstadoCarregando(celular, Modifier.height(ReboDimensao.AlvoLinha * 3))
        is EstadoTela.Falha -> ReboEstadoFalha(
            detalhe = estado.detalhe,
            semRede = estado.semRede,
            celular = celular,
            aoTentarDeNovo = aoTentarDeNovo,
            modifier = Modifier.height(ReboDimensao.AlvoLinha * 3),
        )
        is EstadoTela.Pronto -> {
            if (estado.dado.isEmpty()) {
                Text(
                    text = ReboRotulos.SEM_SIMILARES,
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink3),
                )
            } else {
                ReboGradeDeCartoes(
                    itens = estado.dado,
                    colunas = reboColunasCatalogo(faixa.larguraDp, faixa.alturaDp),
                    celular = celular,
                    aoTocar = { aoAbrirFicha(it.id) },
                )
            }
        }
    }
}
