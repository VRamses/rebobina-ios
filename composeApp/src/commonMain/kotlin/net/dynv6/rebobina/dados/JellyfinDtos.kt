package net.dynv6.rebobina.dados

import kotlinx.serialization.Serializable

/**
 * DTOs do Jellyfin.
 *
 * O Jellyfin responde em PascalCase (`Id`, `Name`, `ImageTags`), entao os nomes das
 * propriedades aqui sao PascalCase de proposito e nao levam `@SerialName`. Todos os campos
 * opcionais tem valor padrao, porque o mesmo endpoint devolve formas diferentes conforme a
 * versao e conforme a biblioteca: `ignoreUnknownKeys` cobre o que vem a mais, e o padrao cobre
 * o que vem a menos.
 *
 * Escopo declarado: estes DTOs cobrem o que as cinco telas leem. Campo que nao esta aqui nao
 * esta sendo lido, e nao ha valor inventado no lugar dele.
 */
@Serializable
internal data class RespostaUsuarios(
    val Items: List<UsuarioDto> = emptyList(),
)

@Serializable
internal data class UsuarioDto(
    val Id: String = "",
    val Name: String? = null,
)

@Serializable
internal data class RespostaItens(
    val Items: List<ItemDto> = emptyList(),
    val TotalRecordCount: Int = 0,
)

@Serializable
internal data class RespostaCanais(
    val Items: List<CanalDto> = emptyList(),
    val TotalRecordCount: Int = 0,
)

@Serializable
internal data class ItemDto(
    val Id: String = "",
    val Name: String? = null,
    val Type: String? = null,
    val ProductionYear: Int? = null,
    val RunTimeTicks: Long? = null,
    val Overview: String? = null,
    val Genres: List<String> = emptyList(),
    val OfficialRating: String? = null,
    val CommunityRating: Double? = null,
    val People: List<PessoaDto> = emptyList(),
    val ImageTags: Map<String, String> = emptyMap(),
    val BackdropImageTags: List<String> = emptyList(),
    val UserData: DadosDoUsuarioDto? = null,
    val RecursiveItemCount: Int? = null,
    val SeriesName: String? = null,
    val IndexNumber: Int? = null,
    val ParentIndexNumber: Int? = null,
)

@Serializable
internal data class DadosDoUsuarioDto(
    val PlaybackPositionTicks: Long? = null,
    val PlayedPercentage: Double? = null,
    val Played: Boolean = false,
)

@Serializable
internal data class PessoaDto(
    val Name: String? = null,
    val Role: String? = null,
    val Type: String? = null,
)

@Serializable
internal data class CanalDto(
    val Id: String = "",
    val Name: String? = null,
    val Number: String? = null,
    val ImageTags: Map<String, String> = emptyMap(),
    val CurrentProgram: ProgramaDto? = null,
)

@Serializable
internal data class ProgramaDto(
    val Name: String? = null,
    val StartDate: String? = null,
    val EndDate: String? = null,
)
