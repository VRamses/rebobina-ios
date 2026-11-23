// GERADO A PARTIR DE design/tokens/rebobina-tokens.json. NAO EDITAR A MAO.
// fonte: rebobina-tokens 1.1.0, 2026-11-23, Yara Baptista.
// sha256 do JSON: 622f1495f8767b4bf3e6b7bbf6a0fcc0e784d9feca6c9452d6926190cf1ae10c
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
    object Interface {
        object Comum {
            const val Marca = "Rebobina"
            const val Inicio = "Início"
            const val Ajustes = "Ajustes"
            const val Downloads = "Downloads"
            const val Buscar = "Buscar"
            const val Filmes = "Filmes"
            const val Series = "Séries"
            const val Todos = "Todos"
            const val Assistir = "Assistir"
            const val Voltar = "Voltar"
            const val Salvar = "Salvar"
            const val Elenco = "Elenco"
            const val Sugestoes = "Sugestões"
            const val Resultados = "Resultados"
        }
        object Ficha {
            const val SemSinopse = "Sem sinopse cadastrada."
        }
        object Player {
            const val Pausado = "Pausado."
        }
        object Downloads {
            const val FilaVazia = "O que você mandar preparar na ficha de um filme ou série aparece aqui, com progresso e o botão de assistir quando terminar."
        }
    }
}
