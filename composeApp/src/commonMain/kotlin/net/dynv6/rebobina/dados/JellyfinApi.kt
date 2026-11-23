package net.dynv6.rebobina.dados

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.dynv6.rebobina.config.RebobinaConfig

/**
 * Cliente HTTP do Jellyfin. Fino de proposito: uma funcao por endpoint usado nesta rodada.
 *
 * Engines: no iOS o motor e o Darwin (NSURLSession), injetado por `criarClienteHttp()` em
 * iosMain, entao o ATS do Info.plist vale para todas as chamadas, inclusive as imagens. No
 * Android o alvo existe so para o modulo compilar, sem motor declarado nesta rodada.
 *
 * Autenticacao: cabecalho `X-Emby-Token`, que e o que o Jellyfin 10.9 aceita em GET. A chave
 * vem de Ajustes, nunca do repositorio. A imagem leva `api_key` na query porque quem baixa o
 * byte e este mesmo cliente, com o mesmo cabecalho; a query fica para o caso de depuracao no
 * navegador do Mac.
 *
 * Erro: status fora de 2xx vira IllegalStateException com o codigo e o caminho. Quem transforma
 * isso em estado de tela e o `Acervo`, e 401 ganha texto proprio de chave recusada.
 */
internal class JellyfinApi(
    endereco: String,
    private val chave: String,
) {
    private val base: String = endereco.trimEnd('/')
    private val cliente: HttpClient = criarClienteHttp()

    private fun url(caminho: String): String = base + caminho

    private suspend inline fun <reified T> pegar(
        caminho: String,
        parametros: List<Pair<String, Any?>> = emptyList(),
    ): T {
        val resposta: HttpResponse = cliente.get(url(caminho)) {
            if (chave.isNotEmpty()) header("X-Emby-Token", chave)
            parametros.forEach { (nome, valor) ->
                if (valor != null) parameter(nome, valor)
            }
        }
        conferirStatus(resposta, caminho)
        return resposta.body<T>()
    }

    private fun conferirStatus(resposta: HttpResponse, caminho: String) {
        val codigo = resposta.status.value
        if (codigo !in 200..299) {
            throw IllegalStateException("servidor respondeu $codigo em $caminho")
        }
    }

    // `/Users` responde uma lista direta, nao o envelope `{Items:[...]}` dos endpoints de item.
    suspend fun usuarios(): List<UsuarioDto> = pegar<List<UsuarioDto>>("/Users")

    suspend fun retomar(usuarioId: String, limite: Int = 20): List<ItemDto> = pegar<RespostaItens>(
        caminho = "/Users/$usuarioId/Items",
        parametros = listOf(
            "Filters" to "IsResumable",
            "SortBy" to "DatePlayed",
            "SortOrder" to "Descending",
            "Limit" to limite,
            "Recursive" to true,
            "Fields" to "ProductionYear,RunTimeTicks,SeriesName",
            "ImageTypeLimit" to 1,
            "EnableImageTypes" to "Primary",
        ),
    ).Items

    suspend fun recentes(usuarioId: String, limite: Int = 30): List<ItemDto> = pegar<RespostaItens>(
        caminho = "/Users/$usuarioId/Items",
        parametros = listOf(
            "SortBy" to "DateCreated",
            "SortOrder" to "Descending",
            "Limit" to limite,
            "Recursive" to true,
            "IncludeItemTypes" to "Movie,Series",
            "Fields" to "ProductionYear,RunTimeTicks",
            "ImageTypeLimit" to 1,
            "EnableImageTypes" to "Primary",
        ),
    ).Items

    suspend fun item(usuarioId: String, itemId: String): ItemDto = pegar<ItemDto>(
        caminho = "/Users/$usuarioId/Items/$itemId",
        parametros = listOf(
            "Fields" to "Overview,Genres,People,ProductionYear,RunTimeTicks,OfficialRating,CommunityRating",
        ),
    )

    suspend fun episodios(usuarioId: String, serieId: String): List<ItemDto> = pegar<RespostaItens>(
        caminho = "/Shows/$serieId/Episodes",
        parametros = listOf(
            "userId" to usuarioId,
            "Fields" to "Overview,ProductionYear,RunTimeTicks",
            "ImageTypeLimit" to 1,
            "EnableImageTypes" to "Primary",
        ),
    ).Items

    suspend fun similares(usuarioId: String, itemId: String, limite: Int = 12): List<ItemDto> =
        pegar<RespostaItens>(
            caminho = "/Items/$itemId/Similar",
            parametros = listOf(
                "userId" to usuarioId,
                "Limit" to limite,
                "Fields" to "ProductionYear,RunTimeTicks",
                "ImageTypeLimit" to 1,
                "EnableImageTypes" to "Primary",
            ),
        ).Items

    suspend fun buscar(usuarioId: String, termo: String, limite: Int = 40): List<ItemDto> =
        pegar<RespostaItens>(
            caminho = "/Users/$usuarioId/Items",
            parametros = listOf(
                "searchTerm" to termo,
                "Recursive" to true,
                "IncludeItemTypes" to "Movie,Series,Episode",
                "Limit" to limite,
                "Fields" to "ProductionYear,RunTimeTicks,SeriesName",
                "ImageTypeLimit" to 1,
                "EnableImageTypes" to "Primary",
            ),
        ).Items

    suspend fun canais(usuarioId: String): List<CanalDto> = pegar<RespostaCanais>(
        caminho = "/LiveTv/Channels",
        parametros = listOf(
            "userId" to usuarioId,
            "Limit" to 200,
            "EnableImages" to true,
            "EnableImageTypes" to "Primary",
            "Fields" to "CurrentProgram",
        ),
    ).Items

    suspend fun bytes(url: String): ByteArray {
        val resposta = cliente.get(url) {
            if (chave.isNotEmpty()) header("X-Emby-Token", chave)
        }
        conferirStatus(resposta, "imagem")
        return resposta.body<ByteArray>()
    }

    fun urlDaImagem(itemId: String, largura: Int): String =
        url("/Items/$itemId/Images/Primary?maxWidth=$largura&quality=90") + sufixoDaChave()

    fun urlDoFundo(itemId: String, indice: Int, largura: Int): String =
        url("/Items/$itemId/Images/Backdrop/$indice?maxWidth=$largura&quality=85") + sufixoDaChave()

    private fun sufixoDaChave(): String =
        if (chave.isEmpty()) "" else "&api_key=" + chave
}

/**
 * Configuracao comum do cliente, usada pelos motores das duas plataformas.
 *
 * `expectSuccess = false` de proposito: o status e conferido em `conferirStatus`, e quem
 * transforma a falha em estado de tela e o `Acervo`, para 401 virar texto de chave recusada em
 * vez de excecao crua.
 */
internal fun HttpClientConfig<*>.configurarRebobina() {
    expectSuccess = false
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            },
        )
    }
    install(HttpTimeout) {
        connectTimeoutMillis = RebobinaConfig.TIMEOUT_CONEXAO_SEGUNDOS * 1000L
        requestTimeoutMillis = RebobinaConfig.TIMEOUT_REQUISICAO_SEGUNDOS * 1000L
    }
}

/** Motor HTTP da plataforma. iOS usa NSURLSession; Android fica sem motor nesta rodada. */
internal expect fun criarClienteHttp(): HttpClient
