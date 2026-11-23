package net.dynv6.rebobina.ui.base

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import net.dynv6.rebobina.ui.tokens.ReboGrade

/**
 * As duas faixas de layout do Rebobina, com o mesmo predicado do Android.
 *
 * Regra unica desta rodada, copiada de `ReboAdaptive.kt` do app Android (commit 9ca65241a):
 * e celular quando a largura util e menor que 600 dp OU quando a altura util e menor que
 * 480 dp. A segunda metade existe porque iPhone deitado tem 390 dp de altura: sem ela, o
 * layout de tablete entraria em uma tela de 390 dp de altura e cortaria o conteudo.
 *
 * Todo lugar do app que precisa escolher entre faixa chama `reboFaixaCelular`, e o teste em
 * commonTest fixa a tabela de fronteira (599/600/601 de largura e 479/480/481 de altura).
 * Nao existe outro `if (largura <` neste projeto: o conferidor de desenho reprova.
 */
const val ReboLarguraCelularDp: Float = 600f
const val ReboAlturaCelularDp: Float = 480f

fun reboFaixaCelular(larguraDp: Float, alturaDp: Float): Boolean =
    larguraDp < ReboLarguraCelularDp || alturaDp < ReboAlturaCelularDp

/** Colunas da grade de capas: 3 em pe no celular, 4 deitado, e a grade do iOS fora do celular. */
fun reboColunasCatalogo(larguraDp: Float, alturaDp: Float): Int =
    if (reboFaixaCelular(larguraDp, alturaDp)) {
        ReboGrade.Ios.ColunasVertical
    } else {
        ReboGrade.Ios.ColunasHorizontal
    }

/** Colunas da grade de canais: 2 em pe no celular, 4 deitado, e a mesma do iOS fora do celular. */
fun reboColunasCanal(larguraDp: Float, alturaDp: Float): Int =
    if (reboFaixaCelular(larguraDp, alturaDp)) {
        ReboGrade.Ios.ColunasCanalVertical
    } else {
        ReboGrade.Ios.ColunasCanalHorizontal
    }

/** Faixa resolvida, para a tela nao repetir a conta. */
data class ReboFaixa(
    val celular: Boolean,
    val larguraDp: Float,
    val alturaDp: Float,
)

/**
 * Resolve a faixa uma vez por tela e entrega o resultado para o conteudo.
 *
 * `maxWidth`/`maxHeight` sao a area util depois dos insets do sistema, que no iOS e o que
 * sobra entre o topo e a barra de gestos.
 */
@Composable
fun ReboAdaptativo(conteudo: @Composable (ReboFaixa) -> Unit) {
    BoxWithConstraints {
        val faixa = ReboFaixa(
            celular = reboFaixaCelular(maxWidth.value, maxHeight.value),
            larguraDp = maxWidth.value,
            alturaDp = maxHeight.value,
        )
        conteudo(faixa)
    }
}
