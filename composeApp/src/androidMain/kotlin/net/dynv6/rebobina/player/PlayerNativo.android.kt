package net.dynv6.rebobina.player

/**
 * Alvo Android fora do escopo desta rodada: existe para o modulo multiplataforma compilar.
 *
 * Nao abre tela nenhuma de proposito. O player de verdade do Android mora no app Android do
 * Rebobina, que e outro projeto e ja toca o mesmo `/mp4/<chave>`.
 */
internal actual object PlayerNativo {
    actual val aberto: Boolean get() = false

    actual fun abrir(url: String, posicaoInicial: Double, aoSalvar: (Double, Double) -> Unit) = Unit

    actual fun fechar() = Unit
}
