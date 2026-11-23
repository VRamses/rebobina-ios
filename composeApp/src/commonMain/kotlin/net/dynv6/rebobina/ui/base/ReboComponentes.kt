package net.dynv6.rebobina.ui.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.dynv6.rebobina.dados.Imagens
import net.dynv6.rebobina.dados.MidiaCartao
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboSelo as ReboSeloToken
import net.dynv6.rebobina.ui.tokens.ReboMotion

/**
 * Pecas de desenho compartilhadas pelas cinco telas.
 *
 * Regra desta rodada: cor, espaco, raio, tipo e alvo saem dos objetos de `ui/base/ReboTokens.kt`
 * e de `ui/tokens/ReboTokensGerado.kt`. Nenhum `Color(0x... )`, `.dp` ou `.sp` escrito aqui:
 * o conferidor `scripts/conferir-desenho.mjs` reprova o build se aparecer.
 *
 * O que estas pecas NAO fazem: animacao de foco por D-pad (nao existe no iOS) e vibracao
 * diferente da do sistema. `ReboMotion` entra como duracao onde existe animacao; onde nao ha
 * animacao nesta rodada, o token nao e usado em vez de ser usado errado.
 */

enum class ReboBotaoTom { Primario, Contorno, Discreto }

@Composable
fun ReboBotao(
    texto: String,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    tom: ReboBotaoTom = ReboBotaoTom.Primario,
    celular: Boolean = true,
) {
    val fundo = when (tom) {
        ReboBotaoTom.Primario -> ReboColor.Brand
        ReboBotaoTom.Contorno -> Color.Transparent
        ReboBotaoTom.Discreto -> ReboColor.Surface
    }
    val tinta = when (tom) {
        ReboBotaoTom.Primario -> ReboColor.OnBrand
        ReboBotaoTom.Contorno -> ReboColor.Ink
        ReboBotaoTom.Discreto -> ReboColor.Ink2
    }
    Row(
        modifier = modifier
            .heightIn(min = ReboDimensao.AlvoMinimo)
            .clip(ReboShape.Controle)
            .background(fundo, ReboShape.Controle)
            .then(
                if (tom == ReboBotaoTom.Contorno) {
                    Modifier.border(ReboDimensao.Selecao, ReboColor.LineStrong, ReboShape.Controle)
                } else {
                    Modifier
                },
            )
            .clickable { aoTocar() }
            .padding(horizontal = ReboDimensao.EspacoN3, vertical = ReboDimensao.EspacoN1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = texto,
            style = ReboEstilo.controle(celular).copy(color = tinta),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Campo de texto com o visual de `cor.field`: fundo proprio, filete e cursor de marca. */
@Composable
fun ReboCampo(
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String? = null,
    celular: Boolean = true,
    segredo: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (rotulo != null) {
            Text(
                text = rotulo,
                style = ReboEstilo.rotulo(celular).copy(color = ReboColor.Ink3),
            )
            Spacer(Modifier.height(ReboDimensao.DentroDoTexto))
        }
        BasicTextField(
            value = valor,
            onValueChange = aoMudar,
            singleLine = true,
            textStyle = ReboEstilo.corpo(celular).copy(color = ReboColor.Ink),
            cursorBrush = SolidColor(ReboColor.Brand),
            visualTransformation = if (segredo) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ReboDimensao.AlvoControle)
                .clip(ReboShape.Controle)
                .background(ReboColor.Field, ReboShape.Controle)
                .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Controle)
                .padding(horizontal = ReboDimensao.EspacoN3, vertical = ReboDimensao.EspacoN2),
        )
    }
}

/** Selo curto de estado do item (AO VIVO, PRONTO, ATENCAO), do tamanho da plataforma iOS. */
@Composable
fun ReboSelo(texto: String, cor: Color, modifier: Modifier = Modifier, celular: Boolean = true) {
    val tinta = if (cor == ReboColor.Brand) ReboColor.OnBrand else ReboColor.Base
    Box(
        modifier = modifier
            .heightIn(min = ReboSeloToken.Altura.Ios)
            .clip(ReboShape.Selo)
            .background(cor, ReboShape.Selo)
            .padding(horizontal = ReboSeloToken.PaddingHorizontal.Ios, vertical = ReboDimensao.EspacoN0),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto,
            style = ReboEstilo.rotulo(celular).copy(color = tinta),
            maxLines = 1,
        )
    }
}

/** Filete horizontal entre partes da tela. */
@Composable
fun ReboFio(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ReboDimensao.Fio)
            .background(ReboColor.Line),
    )
}

/**
 * Imagem remota do Jellyfin.
 *
 * O carregador real e injetado pelo `App` (`Imagens.carregar`), para as telas nao carregarem
 * cliente HTTP proprio. Enquanto o byte nao chega, fica o retangulo do token `cor.slot`: nunca
 * um icone de erro no meio do cartao.
 */
@Composable
fun ReboImagemRemota(url: String?, modifier: Modifier = Modifier) {
    var imagem by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        imagem = if (url == null) null else Imagens.obter(url)
    }
    val pronta = imagem
    if (pronta != null) {
        Image(
            bitmap = pronta,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier.background(ReboColor.Slot))
    }
}

/** Progresso de reproducao: trilho `cor.line`, preenchimento `cor.brand`. */
@Composable
fun ReboBarraProgresso(fracao: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ReboDimensao.Selecao)
            .background(ReboColor.Line, ReboShape.Fio),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fracao.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(ReboColor.Brand, ReboShape.Fio),
        )
    }
}

@Composable
fun ReboAvatar(inicial: String, modifier: Modifier = Modifier, celular: Boolean = true) {
    Box(
        modifier = modifier
            .size(ReboDimensao.AlvoControle)
            .clip(ReboShape.Pilula)
            .background(ReboColor.AvatarInicio, ReboShape.Pilula)
            .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Pilula),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = inicial, style = ReboEstilo.rotulo(celular).copy(color = ReboColor.Ink))
    }
}

@Composable
fun ReboChip(
    texto: String,
    ativa: Boolean,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
    celular: Boolean = true,
) {
    Row(
        modifier = modifier
            .heightIn(min = ReboDimensao.AlvoMinimo)
            .clip(ReboShape.Pilula)
            .background(if (ativa) ReboColor.Brand else ReboColor.Surface, ReboShape.Pilula)
            .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Pilula)
            .clickable { aoTocar() }
            .padding(horizontal = ReboDimensao.EspacoN3, vertical = ReboDimensao.EspacoN1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = texto,
            style = ReboEstilo.rotulo(celular).copy(
                color = if (ativa) ReboColor.OnBrand else ReboColor.Ink2,
            ),
            maxLines = 1,
        )
    }
}

/**
 * Cabecalho de tela: titulo a esquerda, acao a direita. No iOS nao existe botao de voltar
 * de sistema, entao toda tela empilhada desenha o proprio.
 */
@Composable
fun ReboCabecalhoTela(
    titulo: String,
    celular: Boolean,
    modifier: Modifier = Modifier,
    aoVoltar: (() -> Unit)? = null,
    celularNoTitulo: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ReboDimensao.AlvoBarra),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            style = if (celularNoTitulo) ReboEstilo.tituloTela(celular) else ReboEstilo.tituloSecao(celular),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (aoVoltar != null) {
            ReboBotao(
                texto = ReboRotulos.SETA_VOLTAR + " " + ReboRotulos.VOLTAR,
                aoTocar = aoVoltar,
                tom = ReboBotaoTom.Discreto,
                celular = celular,
            )
        }
    }
}

/**
 * Grade de capas em linhas empilhadas.
 *
 * Usa `Column` de `Row`s em vez de `LazyVerticalGrid` porque a pagina inteira ja e uma
 * `LazyColumn`: grade dentro de grade no Compose trava a altura e some com o fim da lista.
 * O numero de colunas sai de `reboColunasCatalogo`, nunca de um numero escrito na tela.
 */
@Composable
fun ReboGradeDeCartoes(
    itens: List<MidiaCartao>,
    colunas: Int,
    celular: Boolean,
    aoTocar: (MidiaCartao) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        itens.chunked(colunas.coerceAtLeast(1)).forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
            ) {
                linha.forEach { item ->
                    ReboCartaoMidia(
                        midia = item,
                        celular = celular,
                        aoTocar = { aoTocar(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(colunas - linha.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(ReboDimensao.EspacoN3))
        }
    }
}

/** Fileira horizontal de capas, a peca que o Android chama de trilho. */
@Composable
fun ReboFileiraDeCartoes(
    itens: List<MidiaCartao>,
    celular: Boolean,
    aoTocar: (MidiaCartao) -> Unit,
    modifier: Modifier = Modifier,
    recuo: Dp = ReboDimensao.EspacoN0,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = recuo),
        horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
    ) {
        items(itens, key = { it.id }) { item ->
            ReboCartaoMidia(
                midia = item,
                celular = celular,
                aoTocar = { aoTocar(item) },
                modifier = Modifier.width(ReboDimensao.LarguraCartao),
            )
        }
    }
}

@Composable
fun ReboCartaoMidia(
    midia: MidiaCartao,
    celular: Boolean,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.clickable { aoTocar() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ReboProporcao.Capa)
                .clip(ReboShape.Capa)
                .background(ReboColor.Slot, ReboShape.Capa)
                .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Capa),
        ) {
            ReboImagemRemota(midia.urlImagem, Modifier.fillMaxSize())
            if (midia.selo != null) {
                ReboSelo(
                    texto = midia.selo,
                    cor = ReboColor.Brand,
                    celular = celular,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(ReboDimensao.EspacoN1),
                )
            }
            if (midia.progresso != null) {
                ReboBarraProgresso(
                    fracao = midia.progresso,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        Spacer(Modifier.height(ReboDimensao.PartesDoCartao))
        Text(
            text = midia.titulo,
            style = ReboEstilo.tituloItem(celular).copy(color = ReboColor.Ink),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (midia.subtitulo.isNotEmpty()) {
            Spacer(Modifier.height(ReboDimensao.DentroDoTexto))
            Text(
                text = midia.subtitulo,
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Linha de resultado de busca: capa pequena a esquerda, titulo, meta e a seta do Android. */
@Composable
fun ReboLinhaDeMidia(
    midia: MidiaCartao,
    celular: Boolean,
    aoTocar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ReboDimensao.AlvoLinha)
            .clickable { aoTocar() }
            .padding(horizontal = ReboDimensao.MargemCelular, vertical = ReboDimensao.EspacoN1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN3),
    ) {
        Box(
            modifier = Modifier
                .height(ReboDimensao.AlvoLinha)
                .aspectRatio(ReboProporcao.Capa)
                .clip(ReboShape.Controle)
                .background(ReboColor.Slot, ReboShape.Controle)
                .border(ReboDimensao.Fio, ReboColor.Line, ReboShape.Controle),
        ) {
            ReboImagemRemota(midia.urlImagem, Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = midia.titulo,
                style = ReboEstilo.tituloItem(celular).copy(color = ReboColor.Ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (midia.subtitulo.isNotEmpty()) {
                Text(
                    text = midia.subtitulo,
                    style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = ReboRotulos.SETA_AVANCAR,
            style = ReboEstilo.tituloItem(celular).copy(color = ReboColor.Ink2),
        )
    }
}

/** Aba da ficha: texto e filete embaixo quando selecionada. */
@Composable
fun ReboAba(
    texto: String,
    selecionada: Boolean,
    aoTocar: () -> Unit,
    celular: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = ReboDimensao.AlvoMinimo)
            .clickable { aoTocar() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = texto,
            style = ReboEstilo.rotulo(celular).copy(
                color = if (selecionada) ReboColor.Ink else ReboColor.Ink3,
            ),
            maxLines = 1,
        )
        Spacer(Modifier.height(ReboDimensao.DentroDoTexto))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ReboDimensao.Selecao)
                .background(if (selecionada) ReboColor.Brand else Color.Transparent),
        )
    }
}

/** Item da barra de navegacao inferior do iPhone. */
@Composable
fun ReboItemDeAba(
    texto: String,
    ativa: Boolean,
    aoTocar: () -> Unit,
    celular: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(min = ReboDimensao.AlvoBarra)
            .clickable { aoTocar() }
            .padding(horizontal = ReboDimensao.EspacoN1),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = texto,
            style = ReboEstilo.rotulo(celular).copy(
                color = if (ativa) ReboColor.Brand else ReboColor.Ink3,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Duracao de transicao do token, para quando houver animacao. Sem uso, nao aparece. */
internal fun duracaoDeEntrada(): Int = ReboMotion.Heroi

/** Espaco vertical padrao entre secoes de conteudo. */
@Composable
fun ReboEspacoDeSecao() {
    Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
}

/** Largura util de um cartao de capa dentro da grade, para o teste de faixa nao chutar. */
internal fun larguraDeCartao(larguraDp: Float, colunas: Int): Dp =
    ((larguraDp - ReboDimensao.MargemCelular.value * 2) / colunas).dp
