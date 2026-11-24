package net.dynv6.rebobina.dados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.dynv6.rebobina.config.RebobinaConfig

/**
 * Ajustes do acervo, editaveis na tela de Ajustes.
 *
 * O endereco e a chave comecam no que esta guardado no aparelho, senao no padrao de
 * `RebobinaConfig`. A chave nunca e escrita no codigo nem no repositorio: ela existe no
 * aparelho depois que alguem digita na tela de Ajustes.
 *
 * `mutableStateOf` porque a tela de Ajustes edita e o `App` recria o `Acervo` quando estes
 * valores mudam.
 */
class AjustesDoAcervo {
    var endereco by mutableStateOf(RebobinaConfig.JELLYFIN_BASE_URL)
    var chave by mutableStateOf(RebobinaConfig.JELLYFIN_CHAVE_PADRAO)

    init {
        val salvo = lerAjustesSalvos()
        endereco = salvo.endereco
        chave = salvo.chave
    }

    /**
     * Endereco pronto para uso. Quem digita so o IP e a porta nao deveria levar erro de URL
     * nem precisar lembrar do esquema: completa com `http`, que e o que o servidor usa na
     * rede local. Barra sobrando no fim tambem sai.
     */
    val enderecoEfetivo: String
        get() {
            val bruto = endereco.trim().trimEnd('/')
            if (bruto.isEmpty()) return ""
            return if (bruto.startsWith("http://") || bruto.startsWith("https://")) bruto else "http://$bruto"
        }
    val chaveEfetiva: String get() = chave.trim()

    fun salvar() {
        gravarAjustesSalvos(AjustesSalvos(endereco = enderecoEfetivo, chave = chaveEfetiva))
    }
}
