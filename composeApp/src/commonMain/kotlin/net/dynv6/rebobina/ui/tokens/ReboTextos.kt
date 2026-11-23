// GERADO A PARTIR DE design/tokens/rebobina-tokens.json. NAO EDITAR A MAO.
// fonte: rebobina-tokens 1.0.5, 2026-11-22, Yara Baptista.
// sha256 do JSON: a043d42591c4e91dc947054bbc43a1e5152c423423802f17d798710d0522e7b5
// Gerado por design/tokens/gerar-kotlin.mjs. Para mudar palavra, mude o JSON na origem.
// A folha de estilo nao carrega texto: no web e no webOS o mesmo conteudo sai em rebobina-textos.json.

package net.dynv6.rebobina.ui.tokens

object ReboTextos {
    object Estado {
        object Carregando {
            const val TituloDemora = "Ainda carregando"
            const val CorpoDemora = "A rede está lenta. Já já aparece."
        }
        object SemRede {
            const val Titulo = "Sem conexão"
            const val Corpo = "Verifique a rede do aparelho e tente de novo."
            const val Acao = "Tentar de novo"
            const val DetalheRotulo = "Ver detalhes"
            const val Icone = "nuvemCortada"
        }
        object Erro {
            const val Titulo = "Algo deu errado"
            const val Corpo = "Não foi possível carregar agora."
            const val Acao = "Tentar de novo"
            const val DetalheRotulo = "Ver detalhes"
            const val Icone = "alerta"
        }
        object Vazio {
            const val Titulo = "Nada por aqui"
            const val Corpo = "Esta lista está vazia."
            const val Acao = "Voltar"
        }
        object SemPasse {
            const val Titulo = "Sem passe ativo"
            const val Corpo = "Esta área pede um passe ativo."
            const val Acao = "Ver passes"
        }
    }
    object Selo {
        object AoVivo {
            const val Texto = "AO VIVO"
            const val Cor = "brand"
        }
        object Pronto {
            const val Texto = "PRONTO"
            const val Cor = "ok"
        }
        object Atencao {
            const val Texto = "ATENÇÃO"
            const val Cor = "warn"
        }
    }
}
