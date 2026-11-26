package br.app.rebobina.config

/**
 * Endereco do catalogo guardado no aparelho, ja migrado para o dominio proprio.
 *
 * As versoes anteriores nasceram apontando para o nome da tailnet, e a tela de Ajustes gravou
 * esse mesmo texto em quem abriu as configuracoes. Os dois textos antigos conhecidos passam a
 * valer o dominio; endereco digitado a mao continua valendo, porque pode ser um servidor
 * proprio de quem usa.
 */
object EnderecoDoCatalogo {
    private val ANTIGOS: Set<String> = setOf(
        RebobinaConfig.ENDERECO_DO_TAILNET,
        RebobinaConfig.ENDERECO_ANTIGO,
    )

    fun migrar(guardado: String?): String? {
        val texto = guardado?.trim()?.trimEnd('/').orEmpty()
        if (texto.isEmpty()) return null
        return if (texto in ANTIGOS) RebobinaConfig.CATALOGO_BASE_URL else texto
    }
}