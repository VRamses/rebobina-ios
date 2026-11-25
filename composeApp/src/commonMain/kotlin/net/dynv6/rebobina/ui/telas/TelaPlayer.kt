package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.AlvoDoPlayer
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.FormatoDeTexto
import net.dynv6.rebobina.dados.PreparoDaMidia
import net.dynv6.rebobina.dados.ProgressoDaMidia
import net.dynv6.rebobina.player.PlayerNativo
import net.dynv6.rebobina.ui.base.ReboBarraProgresso
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Tela do player: prepara o arquivo, retoma de onde parou, abre o player e grava a posicao.
 *
 * A ordem importa. O acervo guarda MKV, que o AVPlayer do iPhone nao toca, entao o servidor
 * converte para MP4 sob demanda em `/preparar/<tmdb>`. A primeira conversao de um filme 4K leva
 * por volta de 15 minutos; nas seguintes o arquivo esta em cache e a resposta vem pronta. Por
 * isso esta tela mostra progresso de verdade em vez de girar para sempre: cada segundo o app
 * pergunta o estado, e a resposta diz se esta aguardando o download no servidor, convertendo, ou
 * pronto com a URL do arquivo.
 *
 * Quem toca o video e o player do sistema, em tela cheia. Esta tela continua atras dele,
 * mostrando a posicao que o player informa, para o botao virar "Continuar de 12:30" quando o
 * usuario fecha o video pela alca do iOS.
 */
@Composable
fun TelaPlayer(
    acervo: Acervo,
    alvo: AlvoDoPlayer,
    faixa: ReboFaixa,
    aoVoltar: () -> Unit,
) {
    val celular = faixa.celular
    val escopo = rememberCoroutineScope()
    var preparo by remember(alvo) { mutableStateOf<EstadoTela<PreparoDaMidia>?>(null) }
    var progresso by remember(alvo) { mutableStateOf<ProgressoDaMidia?>(null) }
    var posicao by remember(alvo) { mutableStateOf(0.0) }
    var duracao by remember(alvo) { mutableStateOf(0.0) }
    var abriu by remember(alvo) { mutableStateOf(false) }
    var salvou by remember(alvo) { mutableStateOf(false) }
    var tentativa by remember(alvo) { mutableStateOf(0) }

    // Posicao salva no servidor: define por onde o video comeca.
    LaunchedEffect(alvo) {
        progresso = acervo.progressoDe(alvo.tmdb, alvo.tipoNoProgresso, alvo.temporada, alvo.numero)
        val salvo = progresso
        if (salvo != null && !salvo.visto && salvo.pos > 5.0) {
            posicao = salvo.pos
            duracao = salvo.duracao
        }
    }

    // Pede o preparo ate o servidor entregar uma URL ou dizer que nao tem arquivo.
    LaunchedEffect(alvo, tentativa) {
        while (true) {
            val pedido = if (alvo.episodio) {
                acervo.prepararEpisodio(alvo.tmdb.toString(), alvo.temporada ?: 0, alvo.numero ?: 0)
            } else {
                acervo.prepararFilme(alvo.tmdb.toString())
            }
            preparo = pedido
            val estado = (pedido as? EstadoTela.Pronto)?.dado?.estado
            if (pedido !is EstadoTela.Pronto || estado == "pronto" || estado == "ausente" || estado == "erro") return@LaunchedEffect
            delay(2_000)
        }
    }

    val pronto = (preparo as? EstadoTela.Pronto)?.dado

    fun abrir(doInicio: Boolean) {
        val arquivo = pronto?.urlDoArquivo ?: return
        val inicio = if (doInicio) 0.0 else posicao
        PlayerNativo.abrir(
            url = arquivo,
            posicaoInicial = inicio,
            aoSalvar = { novaPosicao, novaDuracao ->
                posicao = novaPosicao
                duracao = novaDuracao
                salvou = false
                escopo.launch {
                    acervo.salvarProgresso(
                        tmdb = alvo.tmdb,
                        tipo = alvo.tipoNoProgresso,
                        temporada = alvo.temporada,
                        episodio = alvo.numero,
                        pos = novaPosicao,
                        duracao = novaDuracao,
                    )
                    salvou = true
                }
            },
        )
    }

    // Abre sozinho quando o arquivo fica pronto: o usuario ja pediu para assistir na ficha.
    LaunchedEffect(pronto?.urlDoArquivo) {
        if (pronto?.pronto == true && !abriu) {
            abriu = true
            abrir(doInicio = false)
        }
    }

    // Sair da tela fecha o player, para o audio nao continuar atras de outra tela.
    DisposableEffect(alvo) {
        onDispose { PlayerNativo.fechar() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ReboDimensao.MargemCelular)
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        ReboCabecalhoTela(titulo = alvo.titulo, celular = celular, aoVoltar = aoVoltar)
        if (alvo.subtitulo.isNotBlank()) {
            Text(
                text = alvo.subtitulo,
                style = ReboEstilo.meta(celular).copy(color = ReboColor.Ink3),
            )
        }
        Spacer(Modifier.height(ReboDimensao.EspacoN3))

        when (val estadoDoPreparo = preparo) {
            null, EstadoTela.Carregando -> ReboEstadoCarregando(celular)

            is EstadoTela.Falha -> ReboEstadoFalha(
                detalhe = estadoDoPreparo.detalhe,
                semRede = estadoDoPreparo.semRede,
                celular = celular,
                aoTentarDeNovo = { tentativa += 1 },
                aoAbrirAjustes = aoVoltar,
            )

            is EstadoTela.Pronto -> if (estadoDoPreparo.dado.pronto) {
                BlocoTocando(
                    celular = celular,
                    posicao = posicao,
                    duracao = duracao,
                    salvou = salvou,
                    aoAbrir = { abrir(doInicio = false) },
                    aoAbrirDoInicio = { abrir(doInicio = true) },
                )
            } else {
                BlocoPreparando(celular = celular, dado = estadoDoPreparo.dado)
            }
        }
    }
}

@Composable
private fun BlocoPreparando(celular: Boolean, dado: PreparoDaMidia) {
    val titulo = when (dado.estado) {
        "aguardando" -> ReboRotulos.PREPARANDO_TITULO
        "erro" -> ReboRotulos.PREPARANDO_FALHOU
        "ausente", "sem-arquivo" -> ReboRotulos.SEM_ARQUIVO
        else -> ReboRotulos.PREPARANDO_TITULO
    }
    val corpo = when (dado.estado) {
        "aguardando" -> ReboRotulos.PREPARANDO_AGUARDANDO
        "erro" -> dado.motivo ?: ReboRotulos.PREPARANDO_FALHOU
        "ausente", "sem-arquivo" -> dado.motivo ?: ReboRotulos.SEM_ARQUIVO
        else -> ReboRotulos.PREPARANDO_CORPO
    }
    Column {
        Text(text = titulo, style = ReboEstilo.tituloSecao(celular))
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        Text(
            text = corpo,
            style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
        )
        if (dado.estado == "preparando") {
            Spacer(Modifier.height(ReboDimensao.EspacoN3))
            ReboBarraProgresso(fracao = dado.progresso)
            Spacer(Modifier.height(ReboDimensao.EspacoN1))
            Text(
                text = "${(dado.progresso * 100).toInt()}%",
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
            )
        }
        if (dado.motivo != null && dado.estado == "preparando") {
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            Text(
                text = dado.motivo,
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
            )
        }
    }
}

@Composable
private fun BlocoTocando(
    celular: Boolean,
    posicao: Double,
    duracao: Double,
    salvou: Boolean,
    aoAbrir: () -> Unit,
    aoAbrirDoInicio: () -> Unit,
) {
    Column {
        ReboFio()
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        Text(text = ReboRotulos.TOCANDO, style = ReboEstilo.tituloSecao(celular))
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        Text(
            text = ReboRotulos.TOCANDO_CORPO,
            style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink2),
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        if (duracao > 0) {
            ReboBarraProgresso(fracao = (posicao / duracao).toFloat().coerceIn(0f, 1f))
            Spacer(Modifier.height(ReboDimensao.EspacoN1))
            Text(
                text = FormatoDeTexto.juntar(
                    listOf(
                        FormatoDeTexto.relogio(posicao),
                        ReboRotulos.SALVO_AGORA,
                    ),
                ),
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
            )
            Spacer(Modifier.height(ReboDimensao.EspacoN3))
        }
        val rotulo = if (posicao > 5.0) {
            ReboRotulos.CONTINUAR_DE + " " + FormatoDeTexto.relogio(posicao)
        } else {
            ReboRotulos.ABRIR_PLAYER
        }
        ReboBotao(
            texto = rotulo,
            aoTocar = aoAbrir,
            tom = ReboBotaoTom.Primario,
            celular = celular,
        )
        if (posicao > 5.0) {
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            ReboBotao(
                texto = ReboRotulos.ASSISTIR_DO_INICIO,
                aoTocar = aoAbrirDoInicio,
                tom = ReboBotaoTom.Discreto,
                celular = celular,
            )
        }
        if (salvou) {
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            Text(
                text = ReboRotulos.SALVO_AGORA,
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ok),
            )
        }
    }
}
