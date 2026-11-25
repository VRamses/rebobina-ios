package net.dynv6.rebobina.player

import kotlin.math.abs

/**
 * A decisao de gravar posicao, separada do player e da rede.
 *
 * Existe porque o fechamento do player precisa gravar a posicao final sem repetir o que o
 * observador de 5 s acabou de gravar, e sem gravar zero quando o video nunca tocou. Essa
 * decisao e a unica parte testavel nesta maquina (Windows, sem Mac e sem JDK): o AVPlayer do
 * iOS entrega (posicao, duracao) e quem diz se vale um POST no `/progresso` e `decidirProgresso`.
 */

/** Progresso ja gravado, em segundos. Serve para medir o quanto a posicao andou desde entao. */
internal data class ProgressoGravado(
    val posicao: Double,
    val duracao: Double,
)

/**
 * Diferenca minima de posicao, em segundos, para uma nova gravacao valer.
 *
 * O observador entrega a cada 5 s. Com 1 s de folga, fechar o player grava a posicao real
 * (o defeito que esta correcao fecha: perder ate 5 s) e, ao mesmo tempo, nao gera um segundo
 * POST quando o primeiro acabou de acontecer ou quando o video esta pausado, porque pausado a
 * posicao nao anda.
 */
internal const val AVANCO_MINIMO_PARA_GRAVAR_SEGUNDOS: Double = 1.0

/**
 * Decide se a posicao vira uma gravacao e devolve o par ja ajustado, ou null para nao gravar.
 *
 * Nao grava quando:
 * - posicao ou duracao nao sao numero finito (`NaN`, infinito), que e o que o `AVPlayer`
 *   devolve antes de o item carregar;
 * - posicao e menor ou igual a zero, ou seja, o video nunca tocou;
 * - duracao e menor ou igual a zero, duracao desconhecida: o servidor nao teria como calcular a
 *   fracao assistida;
 * - a posicao nao andou pelo menos [AVANCO_MINIMO_PARA_GRAVAR_SEGUNDOS] desde a ultima
 *   gravacao, para nao repetir o mesmo POST duas vezes.
 *
 * A posicao devolvida nunca passa da duracao: no fim do video o `AVPlayer` chega a marcar um
 * instante depois do fim.
 */
internal fun decidirProgresso(
    posicao: Double,
    duracao: Double,
    gravado: ProgressoGravado?,
): ProgressoGravado? {
    if (!posicao.isFinite() || !duracao.isFinite()) return null
    if (posicao <= 0.0 || duracao <= 0.0) return null
    val posicaoFinal = posicao.coerceAtMost(duracao)
    if (gravado != null && abs(posicaoFinal - gravado.posicao) < AVANCO_MINIMO_PARA_GRAVAR_SEGUNDOS) return null
    return ProgressoGravado(posicao = posicaoFinal, duracao = duracao)
}
