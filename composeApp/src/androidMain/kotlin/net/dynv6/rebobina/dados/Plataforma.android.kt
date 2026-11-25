package net.dynv6.rebobina.dados

import io.ktor.client.HttpClient
import net.dynv6.rebobina.config.RebobinaConfig

/**
 * Alvo Android fora do escopo desta rodada: existe para o modulo multiplataforma compilar e
 * para o framework iOS sair.
 *
 * Sem motor HTTP declarado de proposito: `HttpClient { }` resolve o motor do classpath em tempo
 * de execucao, e o Android nao e executado nesta rodada. O ajuste fica so em memoria, entao nao
 * ha promessa de persistencia aqui. Declarado no README.
 */
internal actual fun criarClienteHttp(): HttpClient = HttpClient {
    configurarRebobina()
}

private var enderecoSalvo: String? = null

internal actual fun lerAjustesSalvos(): AjustesSalvos = AjustesSalvos(
    endereco = enderecoSalvo ?: RebobinaConfig.CATALOGO_BASE_URL,
)

internal actual fun gravarAjustesSalvos(ajustes: AjustesSalvos) {
    enderecoSalvo = ajustes.endereco
}
