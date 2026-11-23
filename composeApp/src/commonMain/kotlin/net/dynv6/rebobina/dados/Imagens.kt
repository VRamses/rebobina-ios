package net.dynv6.rebobina.dados

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Cache de imagem de capa em memoria.
 *
 * O carregador e injetado pelo `App` (`Imagens.carregar = { url -> acervo.bytes(url) }`), porque
 * o cliente HTTP mora em `Acervo` e a tela nao deve criar cliente proprio. Imagem que falha nao
 * vira estado de tela: fica o retangulo de `cor.slot`, que e o que o desenho preve para arte que
 * ainda nao chegou.
 *
 * Limite de 80 capas: o suficiente para rails e grades desta rodada num iPhone, e pequeno o
 * bastante para nao estourar memoria com poster de 400 px decodificado. Passou disso, limpa tudo
 * em vez de fazer LRU: a proxima rolagem recarrega, e o servidor e local.
 */
object Imagens {
    private const val LIMITE_DE_CAPAS: Int = 80

    var carregar: (suspend (String) -> ByteArray?)? = null

    private val cache = mutableMapOf<String, ImageBitmap>()

    suspend fun obter(url: String?): ImageBitmap? {
        if (url.isNullOrBlank()) return null
        cache[url]?.let { return it }
        val carregador = carregar ?: return null
        val bytes = try {
            carregador(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } ?: return null
        val bitmap: ImageBitmap = try {
            bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            return null
        }
        if (cache.size >= LIMITE_DE_CAPAS) cache.clear()
        cache[url] = bitmap
        return bitmap
    }

    fun limpar() {
        cache.clear()
    }
}
