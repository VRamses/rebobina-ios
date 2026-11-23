package net.dynv6.rebobina.dados

import kotlin.coroutines.cancellation.CancellationException

/**
 * Traducao de texto que nao e token de desenho nem texto de estado.
 *
 * Mesma falta declarada em `ReboRotulos`: o JSON 1.0.8 so publica texto de `estado.*` e de
 * `selo.variantes.*`. Unidade de duracao, separador de metadado, abreviacao de temporada e
 * nome de tipo de midia nao existem la, entao moram aqui, uma vez, e nao espalhados nas telas.
 */
internal object FormatoDeTexto {
    const val SEPARADOR: String = " · "
    const val HORA: String = "h"
    const val MINUTO: String = "min"
    const val TEMPORADA_ABREV: String = "T"
    const val EPISODIO_ABREV: String = "E"
    const val FILME: String = "Filme"
    const val SERIE: String = "Série"
    const val EPISODIO: String = "Episódio"
    const val CANAL: String = "Canal"

    fun duracao(minutos: Int?): String? {
        if (minutos == null || minutos <= 0) return null
        if (minutos < 60) return "$minutos $MINUTO"
        val horas = minutos / 60
        val resto = minutos % 60
        return if (resto == 0) "$horas $HORA" else "$horas $HORA $resto $MINUTO"
    }

    fun tipoEmPortugues(tipo: String?): String = when (tipo) {
        "Movie" -> FILME
        "Series" -> SERIE
        "Episode" -> EPISODIO
        "TvChannel" -> CANAL
        else -> tipo.orEmpty()
    }

    /** Hora do servidor, sem conversao de fuso: o Jellyfin devolve UTC e a tela mostra como veio. */
    fun hora(instante: String?): String? {
        val corte = instante?.substringAfter('T', "") ?: return null
        if (corte.length < 5) return null
        return corte.substring(0, 5)
    }

    fun juntar(partes: List<String?>): String =
        partes.filterNotNull().filter { it.isNotBlank() }.joinToString(SEPARADOR)

    fun episodio(temporada: Int?, numero: Int?): String? {
        if (temporada == null && numero == null) return null
        val t = temporada?.let { TEMPORADA_ABREV + it } ?: ""
        val e = numero?.let { EPISODIO_ABREV + it } ?: ""
        return (t + e).ifBlank { null }
    }

    fun faixaDeHorario(inicio: String?, fim: String?): String? = when {
        inicio != null && fim != null -> "$inicio - $fim"
        else -> inicio ?: fim
    }
}

/**
 * Cliente de leitura do acervo Jellyfin, com o estado de tela junto.
 *
 * Toda funcao publica devolve `EstadoTela`: ou o dado traduzido, ou a falha com detalhe e a
 * marca de "parece rede". Nenhuma funcao devolve lista vazia no lugar de erro, e nenhuma tela
 * inventa item quando o servidor nao respondeu.
 *
 * Escopo declarado: leitura. Nesta rodada o app nao escreve nada no servidor: nao marca
 * assistido, nao favorita e nao baixa arquivo. O botao de baixar da ficha e declarado como
 * pendente no README, e nao finge que baixou.
 */
class Acervo(
    endereco: String,
    chave: String,
) {
    private val api = JellyfinApi(endereco, chave)

    suspend fun usuarios(): EstadoTela<List<UsuarioDoAcervo>> = tentar {
        api.usuarios()
            .filter { it.Id.isNotEmpty() }
            .map { UsuarioDoAcervo(id = it.Id, nome = it.Name?.takeIf { nome -> nome.isNotBlank() } ?: it.Id) }
    }

    suspend fun inicio(usuarioId: String): EstadoTela<PaginaInicio> = tentar {
        val retomar = api.retomar(usuarioId).map { it.virarCartao() }
        val recentes = api.recentes(usuarioId).map { it.virarCartao() }
        PaginaInicio(retomar = retomar, recentes = recentes)
    }

    suspend fun ficha(usuarioId: String, itemId: String): EstadoTela<FichaDaMidia> = tentar {
        val dto = api.item(usuarioId, itemId)
        FichaDaMidia(
            id = dto.Id,
            titulo = dto.Name.orEmpty(),
            tipo = dto.Type.orEmpty(),
            ano = dto.ProductionYear,
            duracaoMinutos = dto.duracaoEmMinutos(),
            sinopse = dto.Overview?.takeIf { it.isNotBlank() },
            nota = dto.CommunityRating,
            classificacao = dto.OfficialRating?.takeIf { it.isNotBlank() },
            generos = dto.Genres,
            urlCapa = dto.urlDaCapa(api, 400),
            urlFundo = dto.urlDoFundo(api, 0, 900),
            urlHeroi = dto.urlDoFundo(api, 0, 1400),
            numeroEpisodios = dto.RecursiveItemCount,
            pessoas = dto.People.take(12).map { PessoaDaFicha(it.Name.orEmpty(), it.Role) },
            percentualAssistido = dto.UserData?.PlayedPercentage?.let { (it / 100.0).toFloat() },
        )
    }

    suspend fun episodios(usuarioId: String, serieId: String): EstadoTela<List<EpisodioDaFicha>> = tentar {
        api.episodios(usuarioId, serieId).map { dto ->
            EpisodioDaFicha(
                id = dto.Id,
                titulo = dto.Name.orEmpty(),
                temporada = dto.ParentIndexNumber,
                numero = dto.IndexNumber,
                duracaoMinutos = dto.duracaoEmMinutos(),
                urlImagem = dto.urlDaCapa(api, 300),
                sinopse = dto.Overview?.takeIf { it.isNotBlank() },
            )
        }
    }

    suspend fun similares(usuarioId: String, itemId: String): EstadoTela<List<MidiaCartao>> = tentar {
        api.similares(usuarioId, itemId).map { it.virarCartao() }
    }

    suspend fun buscar(usuarioId: String, termo: String): EstadoTela<List<MidiaCartao>> = tentar {
        api.buscar(usuarioId, termo).map { it.virarCartao() }
    }

    suspend fun canais(usuarioId: String): EstadoTela<List<CanalDaGrade>> = tentar {
        api.canais(usuarioId).map { dto ->
            CanalDaGrade(
                id = dto.Id,
                nome = dto.Name.orEmpty(),
                numero = dto.Number?.takeIf { it.isNotBlank() },
                urlImagem = if (dto.ImageTags.isNotEmpty()) api.urlDaImagem(dto.Id, 240) else null,
                programaAtual = dto.CurrentProgram?.Name?.takeIf { it.isNotBlank() },
                comecaEm = FormatoDeTexto.hora(dto.CurrentProgram?.StartDate),
                terminaEm = FormatoDeTexto.hora(dto.CurrentProgram?.EndDate),
            )
        }
    }

    /** Byte da imagem, para o carregador global de `Imagens`. Falha de imagem nao vira estado de tela. */
    suspend fun bytes(url: String): ByteArray? = try {
        api.bytes(url)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    fun urlDaImagem(itemId: String, largura: Int): String = api.urlDaImagem(itemId, largura)

    private suspend fun <T> tentar(bloco: suspend () -> T): EstadoTela<T> = try {
        EstadoTela.Pronto(bloco())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val detalhe = e.message?.takeIf { it.isNotBlank() } ?: (e::class.simpleName ?: "falha sem mensagem")
        EstadoTela.Falha(detalhe = detalhe, semRede = pareceFalhaDeRede(detalhe))
    }

    private fun pareceFalhaDeRede(detalhe: String): Boolean {
        val marcas = listOf("connect", "network", "timeout", "timed out", "unreachable", "offline", "dns", "host")
        return marcas.any { detalhe.contains(it, ignoreCase = true) }
    }

    private fun ItemDto.duracaoEmMinutos(): Int? =
        RunTimeTicks?.let { (it / 600_000_000L).toInt() }?.takeIf { it > 0 }

    private fun ItemDto.urlDaCapa(api: JellyfinApi, largura: Int): String? =
        if (ImageTags.isNotEmpty()) api.urlDaImagem(Id, largura) else null

    private fun ItemDto.urlDoFundo(api: JellyfinApi, indice: Int, largura: Int): String? =
        if (BackdropImageTags.isNotEmpty()) api.urlDoFundo(Id, indice, largura) else null

    private fun ItemDto.virarCartao(): MidiaCartao {
        val partes = buildList {
            if (Type == "Episode" && !SeriesName.isNullOrBlank()) add(SeriesName)
            add(FormatoDeTexto.tipoEmPortugues(Type).takeIf { it.isNotBlank() })
            add(ProductionYear?.toString())
            add(FormatoDeTexto.duracao(duracaoEmMinutos()))
        }
        val progresso = UserData?.PlayedPercentage?.let { (it / 100.0).toFloat().coerceIn(0f, 1f) }
        return MidiaCartao(
            id = Id,
            titulo = Name.orEmpty(),
            subtitulo = FormatoDeTexto.juntar(partes),
            urlImagem = urlDaCapa(api, 300),
            progresso = progresso,
            selo = null,
        )
    }
}
