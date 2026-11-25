package net.dynv6.rebobina.ui.base

/**
 * Rotulos de interface que o JSON canonico ainda NAO publica.
 *
 * Escopo verificado em 23/11/2026 no `design/tokens/rebobina-tokens.json` 1.1.0 (sha256
 * 622f1495f8767b4bf3e6b7bbf6a0fcc0e784d9feca6c9452d6926190cf1ae10c, 34.081 bytes): o grupo
 * `interface` publica 17 chaves em `ReboTextos.Interface.Comum`, `.Ficha`, `.Player` e
 * `.Downloads`. Fora dessas tres familias (`estado.*`, `selo.*`, `interface.*`) o arquivo nao
 * tem folha para nome de secao, rotulo de campo nem frase de player.
 *
 * Depois da migracao para o catalogo proprio, os titulos das fileiras do Inicio e os rotulos
 * dos chips vem do servidor (`titulo` de cada fileira e de cada chip): nao sao constantes daqui.
 * O que sobrou neste arquivo e o que o servidor nao manda: nome de campo, frase de estado do
 * player e texto declarado de pendencia.
 */
object ReboRotulos {
    // Navegacao
    const val SETA_VOLTAR: String = "‹"

    /** Seta de "abre outra tela" no fim de uma linha da lista. */
    const val SETA_AVANCAR: String = "›"

    // Ficha
    const val FICHA: String = "Ficha"
    const val SINOPSE: String = "Sinopse"
    const val EPISODIOS: String = "Episódios"
    const val CONTINUAR: String = "Continuar"
    const val TEMPORADA: String = "Temporada"
    const val EPISODIO: String = "Episódio"
    const val SEM_SINOPSE: String = "Sem sinopse cadastrada no acervo."
    const val SEM_EPISODIOS: String = "Sem episódios cadastrados no acervo."
    const val NOTA: String = "Nota"

    // Player
    const val PREPARANDO_TITULO: String = "Preparando o vídeo"
    const val PREPARANDO_CORPO: String =
        "O servidor está convertendo o arquivo para o formato que o iPhone toca. Isso acontece uma vez por título."
    const val PREPARANDO_AGUARDANDO: String =
        "O arquivo ainda está sendo baixado no servidor. Assim que chegar, a conversão começa sozinha."
    const val PREPARANDO_FALHOU: String =
        "A conversão não terminou. Tente de novo em alguns instantes."
    const val SEM_ARQUIVO: String =
        "Este título não tem arquivo no acervo: nem o servidor encontrou o vídeo para converter."
    const val ASSISTIR_DO_INICIO: String = "Assistir do início"
    const val CONTINUAR_DE: String = "Continuar de"
    const val TOCANDO: String = "Tocando no player do sistema"
    const val TOCANDO_CORPO: String =
        "O vídeo abre em tela cheia. Ao fechar, a posição fica salva aqui e continua de onde parou."
    const val ABRIR_PLAYER: String = "Abrir o player"
    const val SALVO_AGORA: String = "Posição salva"
    const val SALVANDO: String = "Salvando a posição"

    // Busca
    const val CAMPO_BUSCA: String = "Filme, série, anime"
    const val LIMPAR: String = "Limpar"
    const val BUSCA_VAZIA: String = "Digite o nome de um filme, série ou anime."
    const val BUSCA_SEM_RESULTADO: String = "Nenhum título encontrado com esse nome."

    // Downloads
    const val FILA_VAZIA_TITULO: String = "Nada baixado ainda"
    const val FILA_VAZIA_ACAO: String = "Voltar ao catálogo"
    const val DOWNLOADS_RESUMO: String = "Fila deste aparelho"
    const val DOWNLOADS_PENDENTE: String =
        "O download para assistir sem rede entra na próxima rodada: esta tela mostra a fila vazia, sem inventar item."

    // Ajustes
    const val ENDERECO_DO_SERVIDOR: String = "Endereço do servidor"
    const val SALVO: String = "Salvo neste aparelho"
    const val TESTAR_CONEXAO: String = "Testar conexão"
    const val CONECTADO_COMO: String = "Conectado como"
    const val ENDERECO_VAZIO: String = "Endereço vazio: informe o servidor para carregar o acervo."
    const val DICA_ENDERECO: String =
        "Só o endereço do catálogo, na porta 8899. Não tem login nem chave: quem está na rede vê o acervo."
}
