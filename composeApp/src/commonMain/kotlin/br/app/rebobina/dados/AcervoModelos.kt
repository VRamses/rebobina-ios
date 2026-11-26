package br.app.rebobina.dados

/**
 * Modelos que as telas desenham.
 *
 * Nada aqui sabe o nome de campo do servidor: os DTOs de `CatalogoDtos.kt` sao traduzidos para
 * estes tipos em `Acervo.kt`. Assim a tela nao depende do formato do JSON, e o conferidor de
 * desenho nao confunde dado com token.
 */
data class MidiaCartao(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val urlImagem: String?,
    val progresso: Float?,
    val selo: String?,
    val tmdb: String? = null,
    val tipo: String = "",
    val temporada: Int? = null,
    val episodio: Int? = null,
    val serieId: String? = null,
    val visto: Boolean = false,
)

/** Uma fileira do Inicio, do jeito que o servidor monta: continuar, recomendados, recentes... */
data class FileiraDaTela(
    val id: String,
    val titulo: String,
    val itens: List<MidiaCartao>,
)

/** Um chip do topo do Inicio. Cada rotulo do chip e um filtro que abre a grade. */
data class ChipDaTela(
    val id: String,
    val titulo: String,
    val rotulos: List<RotuloDoChip>,
)

data class RotuloDoChip(val id: String, val rotulo: String)

data class PaginaInicio(
    val fileiras: List<FileiraDaTela>,
    val chips: List<ChipDaTela>,
    val nomeDoUsuario: String,
)

data class FichaDaMidia(
    val id: String,
    val titulo: String,
    val tipo: String,
    val tmdb: String?,
    val ano: Int?,
    val duracaoMinutos: Int?,
    val sinopse: String?,
    val nota: Double?,
    val generos: List<String>,
    val urlCapa: String?,
    val urlFundo: String?,
    val temporada: Int?,
    val episodio: Int?,
    val serieId: String?,
    val percentualAssistido: Float?,
)

/** Episodio na lista da ficha. `temporada` e `numero` montam a chave do player. */
data class EpisodioDaFicha(
    val id: String,
    val titulo: String,
    val temporada: Int?,
    val numero: Int?,
    val duracaoMinutos: Int?,
    val urlImagem: String?,
    val sinopse: String?,
    val progresso: Float?,
    val visto: Boolean,
)

/** Posicao salva de um titulo, em segundos. `duracao` zero significa registro incompleto. */
data class ProgressoDaMidia(
    val pos: Double,
    val duracao: Double,
    val progresso: Int,
    val visto: Boolean,
) {
    val fracao: Float get() = if (duracao > 0) (pos / duracao).toFloat().coerceIn(0f, 1f) else 0f
}

/** Estado do MP4 que o player vai tocar. `pronto` e o unico que entrega URL utilizavel. */data class PreparoDaMidia(
    val estado: String,
    val progresso: Float,
    val motivo: String?,
    val duracaoSegundos: Double,
    val chave: String,
    val urlDoArquivo: String?,
    val completo: Boolean,
) {
    val pronto: Boolean get() = estado == "pronto" && !urlDoArquivo.isNullOrBlank()
}

/**
 * O que o player precisa saber para tocar e para gravar posicao.
 *
 * `tmdb` e o identificador que o servidor usa no `/preparar` e no `/progresso`; os campos de
 * temporada e episodio so valem quando `episodio` e verdadeiro. A ficha monta este objeto ao
 * tocar em Assistir, tanto para filme quanto para um episodio da lista.
 */
data class AlvoDoPlayer(
    val titulo: String,
    val subtitulo: String,
    val tmdb: Long,
    val episodio: Boolean,
    val temporada: Int?,
    val numero: Int?,
) {
    /** Nome do tipo no `/progresso`: o servidor so conhece `filme` e `episodio`. */
    val tipoNoProgresso: String get() = if (episodio) "episodio" else "filme"
}

/** Estado de uma tela que depende da rede. Um dos tres, sempre. */
sealed interface EstadoTela<out T> {
    data object Carregando : EstadoTela<Nothing>
    data class Pronto<out T>(val dado: T) : EstadoTela<T>
    data class Falha(val detalhe: String, val semRede: Boolean) : EstadoTela<Nothing>
}
