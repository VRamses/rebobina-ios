package br.app.rebobina.dados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.app.rebobina.config.RebobinaConfig

/**
 * Ajustes do catalogo, editaveis na tela de Ajustes.
 *
 * So o endereco: o cliente do catalogo nao tem login nem chave. Comeca no que esta guardado no
 * aparelho, senao no padrao de `RebobinaConfig`.
 *
 * `mutableStateOf` porque a tela de Ajustes edita e o `App` recria o `Acervo` quando o valor
 * muda.
 */
class AjustesDoAcervo {
    var endereco by mutableStateOf(RebobinaConfig.CATALOGO_BASE_URL)

    init {
        endereco = lerAjustesSalvos().endereco
    }

    /**
     * Endereco pronto para uso. Quem digita so o IP e a porta nao deveria levar erro de URL
     * nem precisar lembrar do esquema: completa com `http`, que e o que o servidor usa.
     * Barra sobrando no fim tambem sai.
     */
    val enderecoEfetivo: String
        get() {
            val bruto = endereco.trim().trimEnd('/')
            if (bruto.isEmpty()) return ""
            return if (bruto.startsWith("http://") || bruto.startsWith("https://")) bruto else "http://$bruto"
        }

    fun salvar() {
        gravarAjustesSalvos(AjustesSalvos(endereco = enderecoEfetivo))
    }
}
