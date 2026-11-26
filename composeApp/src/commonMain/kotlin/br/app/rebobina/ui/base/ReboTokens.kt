package br.app.rebobina.ui.base

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import br.app.rebobina.ui.tokens.ReboGrade
import br.app.rebobina.ui.tokens.ReboRadius
import br.app.rebobina.ui.tokens.ReboSelo
import br.app.rebobina.ui.tokens.ReboSize
import br.app.rebobina.ui.tokens.ReboSpace
import br.app.rebobina.ui.tokens.ReboSpaceUso
import br.app.rebobina.ui.tokens.ReboType
import br.app.rebobina.ui.tokens.ReboTypeCelular
import br.app.rebobina.ui.tokens.ReboTraco

/**
 * Camada de desenho do iOS. Nenhuma tela desta rodada escreve cor, tamanho, raio ou tipo
 * por conta propria: tudo sai dos objetos gerados em `ui/tokens/ReboTokensGerado.kt`, e o
 * que nao existe no JSON canonico e declarado aqui, em um lugar so, com o motivo escrito.
 *
 * Isto espelha `ReboTokens.kt` e `ReboAdaptive.kt` do app Android (commit 9ca65241a), para
 * as duas telas olharem o mesmo desenho. O que muda e o que o iOS tem e o Android nao:
 * AreaSeguraTopo/Fonte vinda dos insets do sistema, e nenhum foco por D-pad.
 *
 * Limite declarado: o JSON 1.0.8 tem 488 folhas, e nenhuma delas e um literal de "2 por 3"
 * para capa, de alfa para scrim, nem de espessura de anel. Esses tres grupos estao em
 * ReboProporcao, ReboAlfa e ReboDimensao logo abaixo, e sao os unicos numeros de desenho
 * escritos a mao no projeto. Se a prancha um dia publicar essas folhas, estes viram apelido
 * do token e a lista encolhe.
 */
object ReboShape {
    val Controle: CornerBasedShape = RoundedCornerShape(ReboRadius.Controle)
    val Cartao: CornerBasedShape = RoundedCornerShape(ReboRadius.Cartao)
    val Capa: CornerBasedShape = RoundedCornerShape(ReboRadius.Capa)
    val Pilula: CornerBasedShape = RoundedCornerShape(ReboRadius.Pilula)
    val Fio: CornerBasedShape = RoundedCornerShape(ReboTraco.Fio)
    val Selo: CornerBasedShape = RoundedCornerShape(ReboSelo.Raio)
}

/** Apelidos dos tamanhos de alvo e de linha da plataforma iOS. */
object ReboDimensao {
    val AlvoMinimo = ReboSize.Ios.AlvoMinimo
    val AlvoLinha = ReboSize.Ios.AlvoLinha
    val AlvoControle = ReboSize.Ios.AlvoControle
    val AlvoBarra = ReboSize.Ios.AlvoBarra

    val MargemCelular = ReboSpaceUso.MargemCelular
    val AntesDeSecao = ReboSpaceUso.AntesDeSecao
    val EntreSecoes = ReboSpaceUso.EntreSecoes
    val RespiroHeroi = ReboSpaceUso.RespiroHeroi
    val PartesDoCartao = ReboSpaceUso.PartesDoCartao
    val DentroDoTexto = ReboSpaceUso.DentroDoTexto
    val EntreIrmaos = ReboSpaceUso.EntreIrmaos

    val Fio = ReboTraco.Fio
    val Selecao = ReboTraco.Selecao

    /** Largura do cartao de capa na fileira horizontal, lida da grade do iOS. */
    val LarguraCartao = ReboGrade.Ios.LarguraCartao.dp

    val EspacoN0 = ReboSpace.N0
    val EspacoN1 = ReboSpace.N1
    val EspacoN2 = ReboSpace.N2
    val EspacoN3 = ReboSpace.N3
    val EspacoN4 = ReboSpace.N4
    val EspacoN5 = ReboSpace.N5
    val EspacoN6 = ReboSpace.N6
    val EspacoN7 = ReboSpace.N7
    val EspacoN8 = ReboSpace.N8
    val EspacoN9 = ReboSpace.N9
}

/**
 * Proporcoes que a prancha nao publica como folha.
 *
 * Capa 2:3 e a proporcao de poster do TMDB, a mesma que o Android usa; heroi 16:9 e a
 * proporcao de backdrop do Jellyfin. Escritas como fracao para nao virarem numero magico.
 */
object ReboProporcao {
    const val Capa: Float = 2f / 3f
    const val Heroi: Float = 16f / 9f
    const val Quadrada: Float = 1f
}

/** Alfas de sobreposicao. O JSON publica Scrim e Overlay em cor; o alfa do veu e escolha local. */
object ReboAlfa {
    const val SobreArte: Float = 0.55f
    const val TracoForte: Float = 0.32f
    const val Apagado: Float = 0.45f
}

/**
 * Traduz o nome de cor que o JSON guarda em texto (`selo.variantes.*.cor` = "brand"/"ok"/"warn")
 * para a cor de `ReboColor`. Nome desconhecido cai em Ink3, nunca em cor nova.
 */
fun reboCorDoNome(nome: String?): androidx.compose.ui.graphics.Color = when (nome) {
    "brand" -> br.app.rebobina.ui.tokens.ReboColor.Brand
    "ok" -> br.app.rebobina.ui.tokens.ReboColor.Ok
    "warn" -> br.app.rebobina.ui.tokens.ReboColor.Warn
    "danger" -> br.app.rebobina.ui.tokens.ReboColor.Danger
    "info" -> br.app.rebobina.ui.tokens.ReboColor.Info
    else -> br.app.rebobina.ui.tokens.ReboColor.Ink3
}

/**
 * Escolha de estilo por faixa. Estes sao os mesmos nomes do gerado; existe so para a tela
 * nao repetir `if (celular)` em cada Text e para o conferidor de desenho ver que a tela nao
 * inventou tamanho de fonte.
 */
object ReboEstilo {
    fun tituloTela(celular: Boolean) = if (celular) ReboTypeCelular.H2 else ReboType.H1
    fun tituloSecao(celular: Boolean) = if (celular) ReboTypeCelular.H3 else ReboType.H2
    fun tituloItem(celular: Boolean) = if (celular) ReboTypeCelular.ItemTitle else ReboType.ItemTitle
    fun corpo(celular: Boolean) = if (celular) ReboTypeCelular.Body else ReboType.Body
    fun meta(celular: Boolean) = if (celular) ReboTypeCelular.Meta else ReboType.Meta
    fun itemMeta(celular: Boolean) = if (celular) ReboTypeCelular.ItemMeta else ReboType.ItemMeta
    fun rotulo(celular: Boolean) = if (celular) ReboTypeCelular.Label else ReboType.Label
    fun controle(celular: Boolean) = if (celular) ReboTypeCelular.Control else ReboType.Control
    fun numero(celular: Boolean) = if (celular) ReboTypeCelular.Display else ReboType.Display
}
