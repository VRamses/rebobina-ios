package net.dynv6.rebobina.dados

/**
 * Modelos que as telas desenham.
 *
 * Nada aqui sabe de Jellyfin: os DTOs de `JellyfinDtos.kt` sao traduzidos para estes tipos em
 * `Acervo.kt`. Assim a tela nao depende de nome de campo do servidor e o conferidor de desenho
 * nao confunde dado com token.
 */
data class MidiaCartao(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val urlImagem: String?,
    val progresso: Float?,
    val selo: String?,
)

data class PaginaInicio(
    val retomar: List<MidiaCartao>,
    val recentes: List<MidiaCartao>,
)

data class FichaDaMidia(
    val id: String,
    val titulo: String,
    val tipo: String,
    val ano: Int?,
    val duracaoMinutos: Int?,
    val sinopse: String?,
    val nota: Double?,
    val classificacao: String?,
    val generos: List<String>,
    val urlCapa: String?,
    val urlFundo: String?,
    val urlHeroi: String?,
    val numeroEpisodios: Int?,
    val pessoas: List<PessoaDaFicha>,
    val percentualAssistido: Float?,
)

data class PessoaDaFicha(val nome: String, val papel: String?)

data class EpisodioDaFicha(
    val id: String,
    val titulo: String,
    val temporada: Int?,
    val numero: Int?,
    val duracaoMinutos: Int?,
    val urlImagem: String?,
    val sinopse: String?,
)

data class CanalDaGrade(
    val id: String,
    val nome: String,
    val numero: String?,
    val urlImagem: String?,
    val programaAtual: String?,
    val comecaEm: String?,
    val terminaEm: String?,
)

data class UsuarioDoAcervo(val id: String, val nome: String)

/** Estado de uma tela que depende da rede. Um dos tres, sempre. */
sealed interface EstadoTela<out T> {
    data object Carregando : EstadoTela<Nothing>
    data class Pronto<out T>(val dado: T) : EstadoTela<T>
    data class Falha(val detalhe: String, val semRede: Boolean) : EstadoTela<Nothing>
}
