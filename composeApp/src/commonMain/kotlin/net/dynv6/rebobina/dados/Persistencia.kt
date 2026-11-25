package net.dynv6.rebobina.dados

/**
 * Ajustes que sobrevivem a fechar o app.
 *
 * Agora e so o endereco do catalogo: o cliente nao tem login nem chave, entao nao ha segredo
 * para guardar no aparelho.
 *
 * iOS grava em `NSUserDefaults`, no sandbox do app. Android, nesta rodada, guarda so em memoria:
 * o alvo Android existe para o modulo multiplataforma compilar e nao e buildado nem instalado
 * nesta rodada, entao nao ha promessa de persistencia nele.
 */
data class AjustesSalvos(
    val endereco: String,
)

internal expect fun lerAjustesSalvos(): AjustesSalvos

internal expect fun gravarAjustesSalvos(ajustes: AjustesSalvos)
