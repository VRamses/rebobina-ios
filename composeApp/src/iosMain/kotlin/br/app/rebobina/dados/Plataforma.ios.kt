package br.app.rebobina.dados

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import br.app.rebobina.config.RebobinaConfig
import platform.Foundation.NSUserDefaults

/**
 * Motor HTTP do iOS: NSURLSession. Toda chamada passa pelo ATS do Info.plist, inclusive o
 * poster e o video em HTTP puro na rede local, e inclusive dentro da tailnet.
 */
internal actual fun criarClienteHttp(): HttpClient = HttpClient(Darwin) {
    configurarRebobina()
}

private const val CHAVE_DO_ENDERECO: String = "rebobina.endereco"

internal actual fun lerAjustesSalvos(): AjustesSalvos {
    val padroes = NSUserDefaults.standardUserDefaults
    return AjustesSalvos(
        endereco = padroes.stringForKey(CHAVE_DO_ENDERECO) ?: RebobinaConfig.CATALOGO_BASE_URL,
    )
}

internal actual fun gravarAjustesSalvos(ajustes: AjustesSalvos) {
    NSUserDefaults.standardUserDefaults.setObject(ajustes.endereco, forKey = CHAVE_DO_ENDERECO)
}
