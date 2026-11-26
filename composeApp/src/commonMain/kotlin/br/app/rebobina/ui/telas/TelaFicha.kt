package br.app.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import br.app.rebobina.dados.Acervo
import br.app.rebobina.dados.AlvoDoPlayer
import br.app.rebobina.dados.EpisodioDaFicha
import br.app.rebobina.dados.EstadoTela
import br.app.rebobina.dados.FichaDaMidia
import br.app.rebobina.dados.FormatoDeTexto
import br.app.rebobina.dados.ProgressoDaMidia
import br.app.rebobina.ui.base.ReboBarraProgresso
import br.app.rebobina.ui.base.ReboBotao
import br.app.rebobina.ui.base.ReboBotaoTom
import br.app.rebobina.ui.base.ReboCabecalhoTela
import br.app.rebobina.ui.base.ReboChip
import br.app.rebobina.ui.base.ReboDimensao
import br.app.rebobina.ui.base.ReboEstadoCarregando
import br.app.rebobina.ui.base.ReboEstadoFalha
import br.app.rebobina.ui.base.ReboEstadoVazio
import br.app.rebobina.ui.base.ReboEstilo
import br.app.rebobina.ui.base.ReboFaixa
import br.app.rebobina.ui.base.ReboFio
import br.app.rebobina.ui.base.ReboImagemRemota
import br.app.rebobina.ui.base.ReboRotulos
import br.app.rebobina.ui.base.ReboShape
import br.app.rebobina.ui.tokens.ReboColor
import br.app.rebobina.ui.tokens.ReboTextos

/**
 * Ficha de uma midia: capa, dados, sinopse, botao de assistir e, para serie, a lista de episodios.
 *
 * Filme toca o proprio `/preparar/<tmdb>`. Serie nao toca "a serie": toca um episodio, com
 * `/preparar/tv/<tmdb>/<temporada>/<episodio>`, entao a lista de episodios e o caminho principal
 * da ficha de serie, e nao um detalhe escondido atras de aba.
 */
@Composable
fun TelaFicha(
    acervo: Acervo,
    itemId: String,
    faixa: ReboFaixa,
    aoVoltar: () -> Unit,
    aoAssistir: (AlvoDoPlayer) -> Unit,
) {
    val celular = faixa.celular
    var ficha by remember(itemId) { mutableStateOf<EstadoTela<FichaDaMidia>?>(null) }
    var tentativa by remember(itemId) { mutableStateOf(0) }

    LaunchedEffect(acervo, itemId, tentativa) {
        ficha = EstadoTela.Carregando
        ficha = acervo.ficha(itemId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        ReboCabecalhoTela(
            titulo = ReboRotulos.FICHA,
            celular = celular,
            aoVoltar = aoVoltar,
            modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular),
        )
        when (val carregada = ficha) {
            null, EstadoTela.Carregando -> ReboEstadoCarregando(celular)
            is EstadoTela.Falha -> ReboEstadoFalha(
                detalhe = carregada.detalhe,
                semRede = carregada.semRede,
                celular = celular,
                aoTentarDeNovo = { tentativa += 1 },
            )
            is EstadoTela.Pronto -> ConteudoFicha(
                acervo = acervo,
                ficha = carregada.dado,
                celular = celular,
                aoAssistir = aoAssistir,
            )
        }
    }
}

@Composable
private fun ConteudoFicha(
    acervo: Acervo,
    ficha: FichaDaMidia,
    celular: Boolean,
    aoAssistir: (AlvoDoPlayer) -> Unit,
) {
    var progresso by remember(ficha.id) { mutableStateOf<ProgressoDaMidia?>(null) }
    var episodios by remember(ficha.serieId) { mutableStateOf<EstadoTela<List<EpisodioDaFicha>>?>(null) }
    var temporada by remember(ficha.serieId) { mutableStateOf<Int?>(null) }

    val alvoProprio = alvoDaFicha(ficha)
    val ehSerie = ficha.tipo == "Series" && ficha.serieId != null

    LaunchedEffect(acervo, ficha.id) {
        val tmdb = ficha.tmdb?.toLongOrNull()
        if (tmdb != null) {
            progresso = acervo.progressoDe(
                tmdb = tmdb,
                tipo = if (ficha.tipo == "Episode") "episodio" else "filme",
                temporada = ficha.temporada,
                episodio = ficha.episodio,
            )
        }
    }

    LaunchedEffect(acervo, ficha.serieId) {
        val serieId = ficha.serieId
        if (serieId != null) {
            episodios = EstadoTela.Carregando
            episodios = acervo.episodios(serieId)
        }
    }

    Column(modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .width(ReboDimensao.LarguraCartao)
                    .aspectRatio(1.0f / 1.5f)
                    .clip(ReboShape.Capa)
                    .background(ReboColor.Slot, ReboShape.Capa),
            ) {
                ReboImagemRemota(ficha.urlCapa, Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(ReboDimensao.EspacoN3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ficha.titulo,
                    style = ReboEstilo.tituloTela(celular),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(ReboDimensao.EspacoN1))
                Text(
                    text = FormatoDeTexto.juntar(
                        listOf(
                            FormatoDeTexto.tipoEmPortugues(ficha.tipo),
                            ficha.ano?.toString(),
                            FormatoDeTexto.duracao(ficha.duracaoMinutos),
                            FormatoDeTexto.episodio(ficha.temporada, ficha.episodio),
                        ),
                    ),
                    style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink3),
                )
                if (ficha.nota != null) {
                    Spacer(Modifier.height(ReboDimensao.EspacoN1))
                    Text(
                        text = ReboRotulos.NOTA + " " + ficha.nota,
                        style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink2),
                    )
                }
                val generos = ficha.generos.take(3).joinToString(FormatoDeTexto.SEPARADOR)
                if (generos.isNotBlank()) {
                    Spacer(Modifier.height(ReboDimensao.EspacoN1))
                    Text(
                        text = generos,
                        style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.height(ReboDimensao.EspacoN3))

        if (!ehSerie) {
            val alvo = alvoProprio
            if (alvo == null) {
                Text(
                    text = ReboRotulos.SEM_ARQUIVO,
                    style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Danger),
                )
            } else {
                val salvo = progresso
                val continuar = salvo != null && !salvo.visto && salvo.pos > 5.0
                ReboBotao(
                    texto = if (continuar) {
                        ReboRotulos.CONTINUAR + " " + FormatoDeTexto.relogio(salvo.pos)
                    } else {
                        ReboTextos.Interface.Comum.Assistir
                    },
                    aoTocar = { aoAssistir(alvo) },
                    tom = ReboBotaoTom.Primario,
                    celular = celular,
                )
                if (continuar && salvo != null) {
                    Spacer(Modifier.height(ReboDimensao.EspacoN2))
                    ReboBarraProgresso(fracao = salvo.fracao)
                }
            }
        }

        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        ReboFio()
        Spacer(Modifier.height(ReboDimensao.EspacoN3))

        Text(text = ReboRotulos.SINOPSE, style = ReboEstilo.tituloSecao(celular))
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        Text(
            text = ficha.sinopse ?: ReboRotulos.SEM_SINOPSE,
            style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
        )

        if (ehSerie) {
            Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
            ReboFio()
            Spacer(Modifier.height(ReboDimensao.EspacoN3))
            Text(text = ReboRotulos.EPISODIOS, style = ReboEstilo.tituloSecao(celular))
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            ListaDeEpisodios(
                estado = episodios,
                tmdbDaSerie = ficha.tmdb?.toLongOrNull(),
                tituloDaSerie = ficha.titulo,
                temporadaEscolhida = temporada,
                aoEscolherTemporada = { temporada = it },
                celular = celular,
                aoAssistir = aoAssistir,
            )
        }
    }
}

@Composable
private fun ListaDeEpisodios(
    estado: EstadoTela<List<EpisodioDaFicha>>?,
    tmdbDaSerie: Long?,
    tituloDaSerie: String,
    temporadaEscolhida: Int?,
    aoEscolherTemporada: (Int) -> Unit,
    celular: Boolean,
    aoAssistir: (AlvoDoPlayer) -> Unit,
) {
    when (estado) {
        null, EstadoTela.Carregando -> ReboEstadoCarregando(celular)
        is EstadoTela.Falha -> ReboEstadoFalha(
            detalhe = estado.detalhe,
            semRede = estado.semRede,
            celular = celular,
            aoTentarDeNovo = { },
        )
        is EstadoTela.Pronto -> {
            val todos = estado.dado
            if (todos.isEmpty()) {
                ReboEstadoVazio(celular = celular, aoTocarAcao = { })
                return
            }
            val temporadas = todos.mapNotNull { it.temporada }.distinct().sorted()
            val escolhida = temporadaEscolhida ?: temporadas.firstOrNull()
            if (temporadas.size > 1) {
                LazyRowDeTemporadas(
                    temporadas = temporadas,
                    escolhida = escolhida,
                    aoEscolher = aoEscolherTemporada,
                )
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
            }
            todos
                .filter { escolhida == null || it.temporada == escolhida }
                .sortedWith(compareBy({ it.temporada ?: 0 }, { it.numero ?: 0 }))
                .forEach { episodio ->
                    LinhaDeEpisodio(
                        episodio = episodio,
                        celular = celular,
                        aoTocar = {
                            val tmdb = tmdbDaSerie
                            val t = episodio.temporada
                            val e = episodio.numero
                            if (tmdb != null && t != null && e != null) {
                                aoAssistir(
                                    AlvoDoPlayer(
                                        titulo = tituloDaSerie,
                                        subtitulo = FormatoDeTexto.juntar(
                                            listOf(
                                                FormatoDeTexto.episodio(t, e),
                                                episodio.titulo,
                                            ),
                                        ),
                                        tmdb = tmdb,
                                        episodio = true,
                                        temporada = t,
                                        numero = e,
                                    ),
                                )
                            }
                        },
                    )
                }
        }
    }
}

@Composable
private fun LazyRowDeTemporadas(
    temporadas: List<Int>,
    escolhida: Int?,
    aoEscolher: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
    ) {
        temporadas.forEach { numero ->
            ReboChip(
                texto = ReboRotulos.TEMPORADA + " " + numero,
                ativa = numero == escolhida,
                aoTocar = { aoEscolher(numero) },
            )
        }
    }
}

@Composable
private fun LinhaDeEpisodio(
    episodio: EpisodioDaFicha,
    celular: Boolean,
    aoTocar: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aoTocar() }
            .padding(vertical = ReboDimensao.EspacoN2),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(ReboDimensao.LarguraCartao)
                .aspectRatio(16.0f / 9.0f)
                .clip(ReboShape.Controle)
                .background(ReboColor.Slot, ReboShape.Controle),
        ) {
            ReboImagemRemota(episodio.urlImagem, Modifier.fillMaxSize())
            if (episodio.progresso != null) {
                ReboBarraProgresso(
                    fracao = episodio.progresso,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        Spacer(Modifier.width(ReboDimensao.EspacoN2))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = FormatoDeTexto.juntar(
                    listOf(
                        FormatoDeTexto.episodio(episodio.temporada, episodio.numero),
                        episodio.titulo,
                    ),
                ),
                style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val detalhe = FormatoDeTexto.juntar(
                listOf(
                    FormatoDeTexto.duracao(episodio.duracaoMinutos),
                    if (episodio.visto) ReboRotulos.SALVO_AGORA else null,
                ),
            )
            if (detalhe.isNotBlank()) {
                Text(
                    text = detalhe,
                    style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                )
            }
        }
    }
}

/**
 * Monta o alvo do player a partir da ficha. Fora de `remember` de proposito leve: e barato.
 */
private fun alvoDaFicha(ficha: FichaDaMidia): AlvoDoPlayer? {
    val tmdb = ficha.tmdb?.toLongOrNull() ?: return null
    return AlvoDoPlayer(
        titulo = ficha.titulo,
        subtitulo = FormatoDeTexto.juntar(
            listOf(
                FormatoDeTexto.tipoEmPortugues(ficha.tipo),
                ficha.ano?.toString(),
                FormatoDeTexto.episodio(ficha.temporada, ficha.episodio),
            ),
        ),
        tmdb = tmdb,
        episodio = ficha.tipo == "Episode",
        temporada = ficha.temporada,
        numero = ficha.episodio,
    )
}
