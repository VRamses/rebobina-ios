package br.app.rebobina.config

/**
 * Unico lugar do projeto onde o endereco do servidor Rebobina mora.
 *
 * Depois da migracao o app fala so com o catalogo proprio, na 8899: nao fala com o Jellyfin e
 * nao guarda chave nenhuma. O Jellyfin continua existindo atras do servico, para o servico, e
 * nunca aparece no aparelho de quem assiste.
 *
 * Se o servidor mudar de IP, muda aqui e em mais lugar nenhum. O Info.plist do iosApp tem a
 * liberacao de HTTP local em NSAppTransportSecurity.
 */
object RebobinaConfig {
    /**
     * Catalogo proprio do Rebobina. Nome do servidor na tailnet, que funciona tanto na rede de
     * casa quanto fora dela; o IP fixo da rede local era o que quebrava ao sair de casa.
     */
    const val CATALOGO_BASE_URL: String = "http://servidor.tail056d02.ts.net:8899"

    /** Endereco que o app usava antes, mantido para a tela de Ajustes explicar a troca. */
    const val ENDERECO_ANTIGO: String = "http://192.168.18.99:8899"

    /** Tempo maximo, em segundos, para abrir conexao com o servidor. */
    const val TIMEOUT_CONEXAO_SEGUNDOS: Int = 15

    /**
     * Tempo maximo, em segundos, para uma resposta inteira chegar. Vinte segundos nao servem
     * para o primeiro `/catalogo` de um servidor recem-reiniciado, que monta o acervo inteiro.
     */
    const val TIMEOUT_REQUISICAO_SEGUNDOS: Int = 60

    /** Identificador do app usado em cabecalho de cliente. */
    const val CLIENTE: String = "Rebobina iOS"
}
