package net.dynv6.rebobina.ui.base

/**
 * Rotulos de interface que o JSON canonico NAO publica.
 *
 * Escopo verificado em 23/11/2026 no `design/tokens/rebobina-tokens.json` 1.0.8 (sha256
 * c39b8fe7...): 488 folhas, e as unicas que sao texto de tela estao em `estado.*` e
 * `selo.variantes.*`. `geracao.textos.fonte` diz isso com todas as letras: "as chaves de
 * texto de estado.<estado> e de selo.variantes deste arquivo". Nao existe folha para nome de
 * aba, titulo de secao, rotulo de campo ou lugar-comum de botao.
 *
 * Consequencia pratica: tudo o que e *estado* (carregando, sem rede, erro, vazio, sem passe)
 * e o texto dos selos vem de `ReboTextos.kt` gerado, sem literal. Tudo o que e *rotulo de
 * navegacao e de campo* vive neste arquivo, uma vez, e e a unica lista de texto escrita a mao
 * no projeto. Se a prancha publicar essas chaves, este objeto vira apelido de ReboTextos e a
 * lista encolhe para zero, do mesmo jeito que `ReboProporcao`.
 *
 * Isto e uma falta de token, nao uma escolha de estilo: foi pedida e esta declarada no
 * relatorio da rodada.
 */
object ReboRotulos {
    const val NOME_DO_APP: String = "Rebobina"

    // Navegacao
    const val ABA_INICIO: String = "Início"
    const val ABA_CANAIS: String = "Canais"
    const val ABA_DOWNLOADS: String = "Downloads"
    const val ABA_BUSCA: String = "Busca"
    const val ABA_AJUSTES: String = "Ajustes"
    const val VOLTAR: String = "Voltar"
    const val SETA_VOLTAR: String = "‹"
    const val SETA_AVANCAR: String = "›"

    // Inicio
    const val CONTINUAR_ASSISTINDO: String = "Continuar assistindo"
    const val ADICIONADOS_RECENTEMENTE: String = "Adicionados recentemente"
    const val SEM_HISTORICO: String = "Nada em andamento: o que você começar aparece aqui."

    // Ficha
    const val FICHA: String = "Ficha"
    const val SINOPSE: String = "Sinopse"
    const val EPISODIOS: String = "Episódios"
    const val ELENCO: String = "Elenco"
    const val SIMILARES: String = "Similares"
    const val ASSISTIR: String = "Assistir"
    const val CONTINUAR: String = "Continuar"
    const val BAIXAR: String = "Baixar"
    const val TEMPORADA: String = "Temporada"
    const val EPISODIO: String = "Episódio"
    const val SEM_SINOPSE: String = "Sem sinopse cadastrada no acervo."
    const val SEM_ELENCO: String = "Sem elenco cadastrado no acervo."
    const val SEM_SIMILARES: String = "Sem títulos parecidos no acervo."
    const val SEM_EPISODIOS: String = "Sem episódios cadastrados no acervo."
    const val NOTA: String = "Nota"

    // Canais
    const val TODOS: String = "Todos"
    const val AO_VIVO: String = "AO VIVO"
    const val SEM_GRADE: String = "Sem grade de programação no momento."
    const val CANAIS_CARREGANDO: String = "Carregando os canais"
    const val TENTAR_DE_NOVO_AJUSTES: String =
        "Se o servidor mudou de endereço ou a chave venceu, ajuste em Ajustes e tente de novo."

    // Downloads
    const val FILA_VAZIA_TITULO: String = "Nada baixado ainda"
    const val FILA_VAZIA_CORPO: String =
        "O que você mandar preparar na ficha de um filme ou série aparece aqui, com progresso e o botão de assistir quando terminar."
    const val FILA_VAZIA_ACAO: String = "Voltar ao catálogo"
    const val DOWNLOADS_CABECALHO: String = "Downloads"
    const val DOWNLOADS_RESUMO: String = "Fila deste aparelho"
    const val DOWNLOADS_PENDENTE: String =
        "O download em si entra na próxima rodada: esta tela mostra a fila vazia, sem inventar item."

    // Ficha
    const val PENDENTE_REPRODUTOR: String =
        "Reprodutor e download entram na próxima rodada. Esta tela lê o acervo e não toca o arquivo."

    // Busca
    const val BUSCAR: String = "Buscar"
    const val CAMPO_BUSCA: String = "Filme, série, canal"
    const val SUGESTOES: String = "Sugestões"
    const val RESULTADOS: String = "Resultados"
    const val LIMPAR: String = "Limpar"
    const val SUGESTAO_FILMES: String = "Filmes"
    const val SUGESTAO_SERIES: String = "Séries"
    const val SUGESTAO_ANIMES: String = "Animes"
    const val SUGESTAO_NOVELAS: String = "Novelas"
    const val BUSCA_VAZIA: String = "Digite o nome de um filme, série ou canal."

    // Ajustes
    const val ENDERECO_DO_SERVIDOR: String = "Endereço do servidor"
    const val CHAVE_DE_API: String = "Chave de API"
    const val SALVAR: String = "Salvar"
    const val SALVO: String = "Salvo neste aparelho"
    const val TESTAR_CONEXAO: String = "Testar conexão"
    const val CONECTADO_COMO: String = "Conectado como"
    const val CHAVE_FICA_NO_APARELHO: String =
        "A chave fica guardada neste aparelho e não vai para o código nem para o repositório."
    const val ENDERECO_VAZIO: String = "Endereço vazio: informe o servidor para carregar o acervo."
    const val SEM_USUARIO_TITULO: String = "Servidor sem usuário"
    const val SEM_USUARIO_CORPO: String =
        "O Jellyfin respondeu, mas não tem nenhum usuário. Crie um usuário lá e carregue de novo."
}
