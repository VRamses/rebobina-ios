package net.dynv6.rebobina.dados

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.dynv6.rebobina.config.RebobinaConfig
import kotlin.coroutines.cancellation.CancellationException

/** Motor HTTP de cada plataforma: NSURLSession no iOS, motor do classpath no alvo Android. */
internal expect fun criarClienteHttp(): HttpClient

/**
 * Configuracao comum do cliente Ktor, usada pelos dois motores.
 *
 * `ignoreUnknownKeys` porque o servico pode ganhar campo novo sem quebrar app velho;
 * `explicitNulls = false` para nao mandar `temporada: null` em POST de filme;
 * `expectSuccess = false` para o status ser lido por quem chamou, com o corpo do erro junto.
 */
internal fun io.ktor.client.HttpClientConfig<*>.configurarRebobina() {
    expectSuccess = false
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
            },
        )
    }
    install(HttpTimeout) {
        connectTimeoutMillis = RebobinaConfig.TIMEOUT_CONEXAO_SEGUNDOS * 1000L
        requestTimeoutMillis = RebobinaConfig.TIMEOUT_REQUISICAO_SEGUNDOS * 1000L
        socketTimeoutMillis = RebobinaConfig.TIMEOUT_REQUISICAO_SEGUNDOS * 1000L
    }
}

/**
 * Cliente do catalogo proprio do Rebobina, na 8899.
 *
 * Fino de proposito: uma funcao por rota usada pelo app, nenhuma regra de tela aqui. Nao tem
 * login, nao tem chave e nao fala com o Jellyfin: o servico e quem conversa com o acervo. A
 * imagem de capa chega pela mesma porta (`/Items/<id>/Images/...`), entao o app nunca precisa
 * saber do token do Jellyfin para desenhar poster.
 *
 * Erro: status fora de 2xx vira IllegalStateException com codigo e caminho, e quem transforma
 * isso em estado de tela e o `Acervo`. Corpo invalido tambem levanta, em vez de devolver lista
 * vazia fingindo acervo.
 */
internal class CatalogoApi(endereco: String) {
    private val base: String = endereco.trimEnd('/')
    private val cliente: HttpClient = criarClienteHttp()

    private fun url(caminho: String): String = base + caminho

    private suspend inline fun <reified T> pegar(caminho: String): T {
        val resposta: HttpResponse = cliente.get(url(caminho))
        conferirStatus(resposta, caminho)
        return resposta.body<T>()
    }

    private fun conferirStatus(resposta: HttpResponse, caminho: String) {
        val codigo = resposta.status.value
        if (codigo !in 200..299) {
            throw IllegalStateException("servidor respondeu $codigo em $caminho")
        }
    }

    suspend fun catalogo(): RespostaCatalogo = pegar("/catalogo")

    /** Um item pelo id: a ficha aberta por toque no cartao precisa do registro completo. */
    suspend fun item(id: String): ItemDoCatalogoDto =
        pegar<RespostaItemDto>("/catalogo/item/${codificar(id)}").item
            ?: throw IllegalStateException("item $id voltou vazio")

    suspend fun itens(chaveDoFiltro: String, limite: Int, inicio: Int = 0): RespostaItensDto =
        pegar("/catalogo/itens?filtro=${codificar(chaveDoFiltro)}&limite=$limite&inicio=$inicio")

    suspend fun buscar(termo: String, limite: Int = 40): RespostaItensDto =
        pegar("/catalogo/busca?termo=${codificar(termo)}&limite=$limite")

    suspend fun episodios(serieId: String): RespostaItensDto =
        pegar("/catalogo/episodios?serie=${codificar(serieId)}")

    suspend fun progresso(): RespostaProgressoDto = pegar("/progresso")

    suspend fun progressoDe(tmdb: Long, tipo: String, temporada: Int?, episodio: Int?): ItemDeProgressoDto? {
        val chave = chaveDeProgresso(tmdb, tipo, temporada, episodio)
        val resposta: HttpResponse = cliente.get(url("/progresso/item/${codificar(chave)}"))
        // 404 aqui e' o normal: titulo que ninguem comecou a assistir nao tem registro.
        if (resposta.status.value == 404) return null
        conferirStatus(resposta, "/progresso/item")
        return resposta.body<RespostaProgressoDto>().registro
    }

    suspend fun salvarProgresso(envio: EnvioDeProgressoDto): ItemDeProgressoDto? {
        val resposta: HttpResponse = cliente.post(url("/progresso")) {
            contentType(ContentType.Application.Json)
            setBody(envio)
        }
        conferirStatus(resposta, "/progresso")
        return resposta.body<RespostaProgressoDto>().registro
    }

    suspend fun preparo(caminho: String): RespostaPreparoDto {
        val resposta: HttpResponse = cliente.get(url(caminho))
        // 409 e 503 sao estados de preparo, nao falha de rede: o corpo ainda diz o motivo.
        val codigo = resposta.status.value
        if (codigo in 200..299 || codigo == 409 || codigo == 503) {
            return resposta.body<RespostaPreparoDto>().copy(ok = codigo in 200..299)
        }
        throw IllegalStateException("servidor respondeu $codigo em $caminho")
    }

    /** Byte de qualquer URL do servico: caminho relativo do catalogo ou URL inteira. */
    suspend fun bytes(alvo: String): ByteArray? = try {
        val resposta: HttpResponse = cliente.get(if (alvo.startsWith("http")) alvo else url(alvo))
        if (resposta.status.value !in 200..299) {
            null
        } else {
            resposta.body<ByteArray>()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    fun urlAbsoluta(caminho: String?): String? {
        if (caminho.isNullOrBlank()) return null
        return if (caminho.startsWith("http")) caminho else url(caminho)
    }

    fun enderecoDoMp4(chave: String): String = url("/mp4/${codificar(chave)}")

    fun urlDoStreamDoFilme(tmdb: String): String = url("/stream/$tmdb")

    private fun codificar(texto: String): String =
        buildString {
            for (c in texto) {
                when {
                    c.isLetterOrDigit() || c == '-' || c == '_' || c == '.' || c == '~' -> append(c)
                    else -> {
                        val bytes = c.toString().encodeToByteArray()
                        for (b in bytes) {
                            append('%')
                            append(HEX[(b.toInt() shr 4) and 0x0F])
                            append(HEX[b.toInt() and 0x0F])
                        }
                    }
                }
            }
        }

    private companion object {
        const val HEX = "0123456789ABCDEF"
    }
}

/** Chave de posicao combinada com o servidor: `filme:<tmdb>` e `tv:<tmdb>:<t>:<e>`. */
internal fun chaveDeProgresso(tmdb: Long, tipo: String, temporada: Int?, episodio: Int?): String =
    if (tipo == "episodio" && temporada != null && episodio != null) {
        "tv:$tmdb:$temporada:$episodio"
    } else {
        "filme:$tmdb"
    }
