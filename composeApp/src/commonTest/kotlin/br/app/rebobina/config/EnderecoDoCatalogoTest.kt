package br.app.rebobina.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * O endereco guardado no aparelho: os textos antigos viram o dominio proprio, o resto fica.
 */
class EnderecoDoCatalogoTest {

    @Test
    fun padraoEhODominioProprioComTls() {
        assertEquals("https://rebobina.app.br", RebobinaConfig.CATALOGO_BASE_URL)
    }

    @Test
    fun textoDaTailnetViraODominio() {
        assertEquals(
            RebobinaConfig.CATALOGO_BASE_URL,
            EnderecoDoCatalogo.migrar("http://servidor.tail056d02.ts.net:8899"),
        )
    }

    @Test
    fun enderecoAntigoDaRedeLocalViraODominio() {
        assertEquals(
            RebobinaConfig.CATALOGO_BASE_URL,
            EnderecoDoCatalogo.migrar("http://192.168.18.99:8899/"),
        )
    }

    @Test
    fun enderecoDigitadoAMaoEhPreservado() {
        assertEquals(
            "http://192.168.18.99:8899",
            EnderecoDoCatalogo.migrar("http://192.168.18.99:8899"),
        )
        assertEquals(
            "https://meu.servidor:8899",
            EnderecoDoCatalogo.migrar("  https://meu.servidor:8899/  "),
        )
    }

    @Test
    fun vazioEhNulo() {
        assertNull(EnderecoDoCatalogo.migrar(null))
        assertNull(EnderecoDoCatalogo.migrar("   "))
    }
}