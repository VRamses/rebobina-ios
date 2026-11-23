package net.dynv6.rebobina.dados

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import net.dynv6.rebobina.config.RebobinaConfig
import platform.Foundation.NSUserDefaults

/**
 * Motor HTTP do iOS: NSURLSession. Toda chamada passa pelo ATS do Info.plist, inclusive o
 * download das capas do Jellyfin em HTTP puro na rede local.
 */
internal actual fun criarClienteHttp(): HttpClient = HttpClient(Darwin) {
    configurarRebobina()
}

private const val CHAVE_DO_ENDERECO: String = "rebobina.endereco"
private const val CHAVE_DA_API: String = "rebobina.chave"

internal actual fun lerAjustesSalvos(): AjustesSalvos {
    val padroes = NSUserDefaults.standardUserDefaults
    return AjustesSalvos(
        endereco = padroes.stringForKey(CHAVE_DO_ENDERECO) ?: RebobinaConfig.JELLYFIN_BASE_URL,
        chave = padroes.stringForKey(CHAVE_DA_API) ?: RebobinaConfig.JELLYFIN_CHAVE_PADRAO,
    )
}

internal actual fun gravarAjustesSalvos(ajustes: AjustesSalvos) {
    val padroes = NSUserDefaults.standardUserDefaults
    padroes.setObject(ajustes.endereco, forKey = CHAVE_DO_ENDERECO)
    padroes.setObject(ajustes.chave, forKey = CHAVE_DA_API)
}
