package net.dynv6.rebobina.player

/**
 * Player de video do sistema, atras de uma cara so.
 *
 * O iOS usa `AVPlayerViewController`, que ja traz controle nativo de pausa, avanco, volume,
 * legenda, AirPlay e Picture in Picture. Nao ha player desenhado em Compose: desenhar controle
 * proprio seria refazer o do sistema, pior, e sem o botao de legenda e de tela cheia que o
 * usuario de iPhone ja conhece.
 *
 * Contrato: `abrir` recebe a URL do arquivo (o `/mp4/<chave>` do servidor), a posicao em
 * segundos por onde comecar e uma funcao chamada enquanto toca, com (posicao, duracao) em
 * segundos. Quem chama e responsavel por gravar essa posicao no servidor.
 */
internal expect object PlayerNativo {
    /** Verdadeiro enquanto existir player aberto ou pronto na tela. */
    val aberto: Boolean

    /**
     * Abre o video em tela cheia. Fecha o anterior, se houver.
     *
     * `aoSalvar` e chamado a cada poucos segundos de reproducao, nunca depois de `fechar`.
     */
    fun abrir(url: String, posicaoInicial: Double, aoSalvar: (Double, Double) -> Unit)

    /** Fecha o player e para a reproducao. Seguro de chamar mais de uma vez. */
    fun fechar()
}
