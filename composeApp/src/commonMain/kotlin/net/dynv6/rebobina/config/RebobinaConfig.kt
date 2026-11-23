package net.dynv6.rebobina.config

/**
 * Unico lugar do projeto onde os enderecos do servidor Rebobina moram.
 *
 * Servidor do acervo: Jellyfin em HTTP puro na rede local.
 * Catalogo proprio: servico na porta 8899.
 *
 * Se o servidor mudar de IP, muda aqui e em mais lugar nenhum.
 * O Info.plist do iosApp tem a liberacao de HTTP local em NSAppTransportSecurity.
 */
object RebobinaConfig {
    /** Jellyfin do acervo, HTTP puro, rede local. */
    const val JELLYFIN_BASE_URL: String = "http://192.168.18.9:8096"

    /** Catalogo proprio do Rebobina (filmes, series, animes, novelas). */
    const val CATALOGO_BASE_URL: String = "http://192.168.18.9:8899"

    /**
     * Chave de API do Jellyfin, vazia de proposito.
     *
     * A chave nao mora no codigo: ela e digitada na tela de Ajustes e fica guardada no
     * aparelho (NSUserDefaults no iOS). Servidor sem chave responde 401 e a tela mostra
     * o estado de erro, nunca um acervo inventado.
     */
    const val JELLYFIN_CHAVE_PADRAO: String = ""

    /** Tempo maximo, em segundos, para abrir conexao com o servidor. */
    const val TIMEOUT_CONEXAO_SEGUNDOS: Int = 15

    /** Tempo maximo, em segundos, para uma resposta inteira chegar. */
    const val TIMEOUT_REQUISICAO_SEGUNDOS: Int = 20

    /** Identificador do app usado em cabecalho de cliente. */
    const val CLIENTE: String = "Rebobina iOS"
}
