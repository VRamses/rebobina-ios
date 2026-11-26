package br.app.rebobina.dados

import kotlinx.serialization.Serializable

/**
 * Formato JSON do catalogo proprio do Rebobina (porta 8899), sem autenticacao.
 *
 * Estes tipos sao so a borda: quem as telas desenham e `AcervoModelos.kt`, traduzido em
 * `Acervo.kt`. Assim campo novo no servidor nao obriga tela a mudar, e campo que sai nao
 * derruba a tela (`ignoreUnknownKeys` e valor padrao em tudo).
 */

@Serializable
internal data class RespostaCatalogo(
    val ok: Boolean = false,
    /** Numero do formato do JSON, hoje 1. A tela nao usa; fica aqui para o dia em que mudar. */
    val schema: Int? = null,
    val geradoEm: String? = null,
    val usuario: UsuarioDoCatalogoDto? = null,
    val chips: List<ChipDto> = emptyList(),
    val fileiras: List<FileiraDto> = emptyList(),
    val avisos: List<String> = emptyList(),
)

@Serializable
internal data class UsuarioDoCatalogoDto(val id: String = "", val nome: String = "")

@Serializable
internal data class ChipDto(
    val id: String = "",
    val titulo: String = "",
    val somenteTv: Boolean = false,
    val itens: List<RotuloDoChipDto> = emptyList(),
)

@Serializable
internal data class RotuloDoChipDto(
    val id: String = "",
    val rotulo: String = "",
    val ordem: String? = null,
)

@Serializable
internal data class FileiraDto(
    val id: String = "",
    val titulo: String = "",
    val tipo: String = "",
    val limite: Int? = null,
    val total: Int? = null,
    val itens: List<ItemDoCatalogoDto> = emptyList(),
)

@Serializable
internal data class ItemDoCatalogoDto(
    val id: String = "",
    val nome: String = "",
    val tituloMidia: String = "",
    val rotulo: String = "",
    val tipo: String = "",
    val serieId: String? = null,
    val ano: Int? = null,
    val nota: Double? = null,
    val generos: List<String> = emptyList(),
    val tmdb: String? = null,
    val serie: String? = null,
    val temporada: Int? = null,
    val episodio: Int? = null,
    val duracaoMin: Int? = null,
    val sinopse: String? = null,
    val imagem: String? = null,
    val fundo: String? = null,
    val progresso: Int? = null,
    val visto: Boolean? = null,
)

@Serializable
internal data class RespostaItensDto(
    val ok: Boolean = false,
    val filtro: String? = null,
    val rotulo: String? = null,
    val termo: String? = null,
    val serie: String? = null,
    val total: Int = 0,
    val itens: List<ItemDoCatalogoDto> = emptyList(),
    val erro: String? = null,
)

@Serializable
internal data class RespostaItemDto(
    val ok: Boolean = false,
    val item: ItemDoCatalogoDto? = null,
    val erro: String? = null,
)

@Serializable
internal data class RespostaProgressoDto(
    val ok: Boolean = false,
    val total: Int = 0,
    val itens: List<ItemDeProgressoDto> = emptyList(),
    val chave: String? = null,
    val registro: ItemDeProgressoDto? = null,
    val erro: String? = null,
)

@Serializable
internal data class ItemDeProgressoDto(
    val chave: String = "",
    val tmdb: Long = 0,
    val tipo: String = "",
    val temporada: Int? = null,
    val episodio: Int? = null,
    val pos: Double = 0.0,
    val duracao: Double = 0.0,
    val progresso: Int = 0,
    val visto: Boolean = false,
    val atualizadoEm: String? = null,
)

/** Corpo do POST /progresso. `pos` e `duracao` em segundos. */
@Serializable
internal data class EnvioDeProgressoDto(
    val tmdb: Long,
    val tipo: String,
    val temporada: Int? = null,
    val episodio: Int? = null,
    val pos: Double,
    val duracao: Double,
)

/** Resposta do GET /preparar/<chave>: o estado do MP4 que o AVPlayer vai tocar. */
@Serializable
internal data class RespostaPreparoDto(
    val ok: Boolean = false,
    val chave: String? = null,
    val estado: String? = null,
    val progresso: Double = 0.0,
    val motivo: String? = null,
    val duracao: Double = 0.0,
    val bytes: Long = 0,
    val url: String? = null,
    val completo: Boolean? = null,
    val baixado: Long? = null,
    val total: Long? = null,
    val retryEmSegundos: Int? = null,
    val erro: String? = null,
)
