package net.dynv6.rebobina.ui.base

/**
 * Rotulos de interface que o JSON canonico ainda NAO publica.
 *
 * Escopo verificado em 23/11/2026 no `design/tokens/rebobina-tokens.json` 1.1.0 (sha256
 * 622f1495f8767b4bf3e6b7bbf6a0fcc0e784d9feca6c9452d6926190cf1ae10c, 34.081 bytes): o
 * grupo `interface` publica 17 chaves (comum 14, ficha 1, player 1, downloads 1) e o
 * gerado as expoe em `ReboTextos.Interface.Comum`, `.Ficha`, `.Player` e `.Downloads`.
 * Ele entra como terceira familia de texto do JSON, ao lado de `estado.*` e `selo.*`.
 * Fora dessas tres, o arquivo continua sem folha para nome de aba, titulo de secao,
 * rotulo de campo ou lugar-comum de botao.
 *
 * A reimportacao da 1.1.0 trocou 16 constantes locais cujo valor coincidia com o canonico
 * por leituras de `ReboTextos.Interface.*` e apagou esses constantes. Ficou aqui, de
 * proposito:
 *
 * - `SEM_SINOPSE`, porque o valor local ("Sem sinopse cadastrada no acervo.") diverge do
 *   canonico ("Sem sinopse cadastrada."): trocar a frase e decisao de produto, nao
 *   reimportacao, e a divergencia esta declarada no relatorio da rodada;
 * - `player.pausado` nao tinha constante no iOS, entao a chave entrou no gerado sem
 *   substituicao;
 * - o resto, que nao tem chave canonica (abas Canais e Busca, titulos, campos e textos
 *   declarados de rodada).
 *
 * Isto era falta de token, nao escolha de estilo: a 1.1.0 cobriu as 17 chaves e o que
 * sobra continua declarado no relatorio da rodada.
 */
object ReboRotulos {
    // Navegacao
    const val ABA_CANAIS: String = "Canais"
    const val ABA_BUSCA: String = "Busca"
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
    const val SIMILARES: String = "Similares"
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
    const val AO_VIVO: String = "AO VIVO"
    const val SEM_GRADE: String = "Sem grade de programação no momento."
    const val CANAIS_CARREGANDO: String = "Carregando os canais"
    const val TENTAR_DE_NOVO_AJUSTES: String =
        "Se o servidor mudou de endereço ou a chave venceu, ajuste em Ajustes e tente de novo."

    // Downloads
    const val FILA_VAZIA_TITULO: String = "Nada baixado ainda"
    const val FILA_VAZIA_ACAO: String = "Voltar ao catálogo"
    const val DOWNLOADS_RESUMO: String = "Fila deste aparelho"
    const val DOWNLOADS_PENDENTE: String =
        "O download em si entra na próxima rodada: esta tela mostra a fila vazia, sem inventar item."

    // Ficha
    const val PENDENTE_REPRODUTOR: String =
        "Reprodutor e download entram na próxima rodada. Esta tela lê o acervo e não toca o arquivo."

    // Busca
    const val CAMPO_BUSCA: String = "Filme, série, canal"
    const val LIMPAR: String = "Limpar"
    const val SUGESTAO_ANIMES: String = "Animes"
    const val SUGESTAO_NOVELAS: String = "Novelas"
    const val BUSCA_VAZIA: String = "Digite o nome de um filme, série ou canal."

    // Ajustes
    const val ENDERECO_DO_SERVIDOR: String = "Endereço do servidor"
    const val CHAVE_DE_API: String = "Chave de API"
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
