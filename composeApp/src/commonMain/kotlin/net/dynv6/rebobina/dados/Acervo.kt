package net.dynv6.rebobina.dados

import kotlin.coroutines.cancellation.CancellationException

/**
 * Traducao de texto que nao e token de desenho nem texto de estado.
 *
 * Mesma falta declarada em `ReboRotulos`: o JSON 1.0.8 so publica texto de `estado.*` e de
 * `selo.variantes.*`. Unidade de duracao, separador de metadado, abreviacao de temporada e
 * nome de tipo de midia nao existem la, entao moram aqui, uma vez, e nao espalhadas nas telas.
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
        else -> tipo.orEmpty()
    }

    fun juntar(partes: List<String?>): String =
        partes.filterNotNull().filter { it.isNotBlank() }.joinToString(SEPARADOR)

    fun episodio(temporada: Int?, numero: Int?): String? {
        if (temporada == null && numero == null) return null
        val t = temporada?.let { TEMPORADA_ABREV + it } ?: ""
        val e = numero?.let { EPISODIO_ABREV + it } ?: ""
        return (t + e).ifBlank { null }
    }

    /** Tempo de video em texto de gente: 2h 46min, 46min, 3min. Segundos abaixo de 1 minuto somem. */
    fun tempo(segundos: Double): String? {
        if (segundos <= 0 || segundos.isNaN()) return null
        val minutos = (segundos / 60.0).toInt()
        if (minutos < 1) return null
        if (minutos < 60) return "$minutos $MINUTO"
        val horas = minutos / 60
        val resto = minutos % 60
        return if (resto == 0) "$horas $HORA" else "$horas $HORA $resto $MINUTO"
    }

    /** Posicao no player com dois digitos: 1:07:32 ou 7:32. */
    fun relogio(segundos: Double): String {
        if (segundos <= 0 || segundos.isNaN()) return "0:00"
        val total = segundos.toLong()
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        val dois = if (s < 10) "0$s" else "$s"
        return if (h > 0) {
            val mm = if (m < 10) "0$m" else "$m"
            "$h:$mm:$dois"
        } else {
            "$m:$dois"
        }
    }
}

/**
 * Leitura e escrita do acervo pelo catalogo proprio do Rebobina, na 8899.
 *
 * Toda funcao publica devolve `EstadoTela` quando desenha tela: ou o dado traduzido, ou a falha
 * com detalhe e a marca de "parece rede". Nenhuma funcao devolve lista vazia no lugar de erro.
 *
 * Escopo: o app le o catalogo, le e grava posicao de reproducao e pede o MP4 que o AVPlayer
 * toca. Nao existe login, nao existe chave e nao existe Jellyfin neste cliente.
 */
class Acervo(endereco: String) {
    private val api = CatalogoApi(endereco)

    suspend fun inicio(): EstadoTela<PaginaInicio> = tentar {
        val dto = api.catalogo()
        val fileiras = dto.fileiras.mapNotNull { fileira ->
            val itens = fileira.itens.mapNotNull { it.virarCartao() }
            if (itens.isEmpty()) null else FileiraDaTela(id = fileira.id, titulo = fileira.titulo, itens = itens)
        }
        // Chip marcado como `somenteTv` e do layout de TV: no celular ele nao entra.
        val chips = dto.chips
            .filterNot { it.somenteTv }
            .mapNotNull { chip ->
                val rotulos = chip.itens
                    .filter { it.id.isNotBlank() && it.rotulo.isNotBlank() }
                    .map { RotuloDoChip(id = it.id, rotulo = it.rotulo) }
                if (rotulos.isEmpty()) null else ChipDaTela(id = chip.id, titulo = chip.titulo, rotulos = rotulos)
            }
        PaginaInicio(
            fileiras = fileiras,
            chips = chips,
            nomeDoUsuario = dto.usuario?.nome?.takeIf { it.isNotBlank() }.orEmpty(),
        )
    }

    suspend fun ficha(itemId: String): EstadoTela<FichaDaMidia> = tentar {
        val dto = api.item(itemId)
        FichaDaMidia(
            id = dto.id,
            titulo = dto.nome.ifBlank { dto.tituloMidia },
            tipo = dto.tipo,
            tmdb = dto.tmdb,
            ano = dto.ano,
            duracaoMinutos = dto.duracaoMin,
            sinopse = dto.sinopse?.takeIf { it.isNotBlank() },
            nota = dto.nota,
            generos = dto.generos,
            urlCapa = api.urlAbsoluta(dto.imagem),
            urlFundo = api.urlAbsoluta(dto.fundo),
            temporada = dto.temporada,
            episodio = dto.episodio,
            serieId = dto.serieId,
            percentualAssistido = dto.progresso?.let { (it / 100.0).toFloat() },
        )
    }

    suspend fun episodios(serieId: String): EstadoTela<List<EpisodioDaFicha>> = tentar {
        api.episodios(serieId).itens.mapNotNull { dto ->
            if (dto.id.isBlank()) return@mapNotNull null
            EpisodioDaFicha(
                id = dto.id,
                titulo = dto.tituloMidia.ifBlank { dto.nome },
                temporada = dto.temporada,
                numero = dto.episodio,
                duracaoMinutos = dto.duracaoMin,
                urlImagem = api.urlAbsoluta(dto.imagem),
                sinopse = dto.sinopse?.takeIf { it.isNotBlank() },
                progresso = dto.progresso?.let { (it / 100.0).toFloat() },
                visto = dto.visto == true,
            )
        }
    }

    suspend fun buscar(termo: String): EstadoTela<List<MidiaCartao>> = tentar {
        api.buscar(termo).itens.mapNotNull { it.virarCartao() }
    }

    /** Itens de um chip do Inicio, que e o filtro de `/catalogo/itens`. */
    suspend fun itensDoChip(chaveDoFiltro: String, limite: Int = 40): EstadoTela<List<MidiaCartao>> = tentar {
        api.itens(chaveDoFiltro, limite).itens.mapNotNull { it.virarCartao() }
    }

    suspend fun progressoDe(tmdb: Long, tipo: String, temporada: Int?, episodio: Int?): ProgressoDaMidia? =
        try {
            val dto = api.progressoDe(tmdb, tipo, temporada, episodio)
            dto?.let {
                ProgressoDaMidia(
                    pos = it.pos,
                    duracao = it.duracao,
                    progresso = it.progresso,
                    visto = it.visto,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    /** Grava a posicao. Falhar aqui nao pode derrubar o player: devolve falso e segue tocando. */
    suspend fun salvarProgresso(
        tmdb: Long,
        tipo: String,
        temporada: Int?,
        episodio: Int?,
        pos: Double,
        duracao: Double,
    ): Boolean = try {
        api.salvarProgresso(
            EnvioDeProgressoDto(
                tmdb = tmdb,
                tipo = tipo,
                temporada = if (tipo == "episodio") temporada else null,
                episodio = if (tipo == "episodio") episodio else null,
                pos = pos,
                duracao = duracao,
            ),
        )
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    /**
     * Pede o preparo do MP4 do filme e devolve o estado atual.
     *
     * O servidor monta o arquivo sob demanda (o MKV do acervo nao toca no AVPlayer) e responde
     * `aguardando`, `preparando`, `pronto` ou `erro`. Quem chama repete a chamada ate `pronto`.
     */
    suspend fun prepararFilme(tmdb: String): EstadoTela<PreparoDaMidia> = tentar {
        api.preparo("/preparar/$tmdb").virarPreparo()
    }

    suspend fun prepararEpisodio(tmdb: String, temporada: Int, episodio: Int): EstadoTela<PreparoDaMidia> = tentar {
        api.preparo("/preparar/tv/$tmdb/$temporada/$episodio").virarPreparo()
    }

    /** Byte da imagem, para o carregador global de `Imagens`. Falha de imagem nao vira estado de tela. */
    suspend fun bytes(url: String): ByteArray? = api.bytes(url)

    private fun RespostaPreparoDto.virarPreparo(): PreparoDaMidia = PreparoDaMidia(
        estado = estado?.takeIf { it.isNotBlank() } ?: if (ok) "preparando" else "ausente",
        progresso = progresso.toFloat().coerceIn(0f, 1f),
        motivo = motivo?.takeIf { it.isNotBlank() },
        duracaoSegundos = duracao,
        chave = chave.orEmpty(),
        urlDoArquivo = api.urlAbsoluta(url) ?: chave?.takeIf { it.isNotBlank() }?.let { api.enderecoDoMp4(it) },
        completo = completo ?: false,
    )

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

    private fun ItemDoCatalogoDto.virarCartao(): MidiaCartao? {
        if (id.isBlank()) return null
        val subtitulo = if (tipo == "Episode") {
            FormatoDeTexto.juntar(
                listOf(
                    rotulo.ifBlank { FormatoDeTexto.episodio(temporada, episodio) },
                    FormatoDeTexto.duracao(duracaoMin),
                ),
            )
        } else {
            FormatoDeTexto.juntar(
                listOf(
                    FormatoDeTexto.tipoEmPortugues(tipo),
                    ano?.toString(),
                    FormatoDeTexto.duracao(duracaoMin),
                ),
            )
        }
        return MidiaCartao(
            id = id,
            titulo = nome.ifBlank { tituloMidia },
            subtitulo = subtitulo,
            urlImagem = api.urlAbsoluta(imagem),
            progresso = progresso?.let { (it / 100.0).toFloat().coerceIn(0f, 1f) },
            selo = null,
            tmdb = tmdb,
            tipo = tipo,
            temporada = temporada,
            episodio = episodio,
            serieId = serieId,
            visto = visto == true,
        )
    }
}
