package br.app.rebobina.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Testes da decisao pura de gravar progresso, a parte do fechamento do player que roda sem iOS.
 *
 * O observador de 5 s tambem passa por esta decisao, entao os casos abaixo valem para os dois
 * caminhos: gravacao periodica e gravacao final.
 */
class ProgressoFinalTest {

    private val duracaoDoFilme = 5_400.0

    @Test
    fun videoQueNuncaTocouNaoGrava() {
        assertNull(decidirProgresso(posicao = 0.0, duracao = duracaoDoFilme, gravado = null))
    }

    @Test
    fun posicaoNegativaNaoGrava() {
        assertNull(decidirProgresso(posicao = -3.0, duracao = duracaoDoFilme, gravado = null))
    }

    @Test
    fun duracaoDesconhecidaNaoGrava() {
        assertNull(decidirProgresso(posicao = 12.0, duracao = 0.0, gravado = null))
        assertNull(decidirProgresso(posicao = 12.0, duracao = -1.0, gravado = null))
    }

    @Test
    fun numeroQueNaoEhFinitoNaoGrava() {
        assertNull(decidirProgresso(posicao = Double.NaN, duracao = duracaoDoFilme, gravado = null))
        assertNull(decidirProgresso(posicao = 12.0, duracao = Double.NaN, gravado = null))
        assertNull(decidirProgresso(posicao = Double.POSITIVE_INFINITY, duracao = duracaoDoFilme, gravado = null))
        assertNull(decidirProgresso(posicao = 12.0, duracao = Double.POSITIVE_INFINITY, gravado = null))
    }

    @Test
    fun primeiraPosicaoValidaGrava() {
        val decisao = assertNotNull(decidirProgresso(posicao = 12.4, duracao = duracaoDoFilme, gravado = null))
        assertEquals(12.4, decisao.posicao)
        assertEquals(duracaoDoFilme, decisao.duracao)
    }

    @Test
    fun naoRepeteGravacaoQuandoAPosicaoQuaseNaoAndou() {
        val anterior = ProgressoGravado(posicao = 120.0, duracao = duracaoDoFilme)
        assertNull(decidirProgresso(posicao = 120.0, duracao = duracaoDoFilme, gravado = anterior))
        assertNull(decidirProgresso(posicao = 120.4, duracao = duracaoDoFilme, gravado = anterior))
        assertNull(decidirProgresso(posicao = 119.7, duracao = duracaoDoFilme, gravado = anterior))
    }

    @Test
    fun gravaQuandoAPosicaoAndouMaisQueOMinimo() {
        val anterior = ProgressoGravado(posicao = 120.0, duracao = duracaoDoFilme)
        val decisao = assertNotNull(decidirProgresso(posicao = 121.5, duracao = duracaoDoFilme, gravado = anterior))
        assertEquals(121.5, decisao.posicao)
    }

    @Test
    fun voltarNaBarraContaComoMudanca() {
        val anterior = ProgressoGravado(posicao = 300.0, duracao = duracaoDoFilme)
        val decisao = assertNotNull(decidirProgresso(posicao = 60.0, duracao = duracaoDoFilme, gravado = anterior))
        assertEquals(60.0, decisao.posicao)
    }

    @Test
    fun posicaoMaiorQueADuracaoEhLimitadaAoFim() {
        val decisao = assertNotNull(decidirProgresso(posicao = 5_500.0, duracao = duracaoDoFilme, gravado = null))
        assertEquals(duracaoDoFilme, decisao.posicao)
        assertEquals(duracaoDoFilme, decisao.duracao)
    }

    @Test
    fun fimDoVideoNaoGravaDuasVezesSeguidas() {
        val anterior = ProgressoGravado(posicao = duracaoDoFilme, duracao = duracaoDoFilme)
        assertNull(decidirProgresso(posicao = 5_400.3, duracao = duracaoDoFilme, gravado = anterior))
    }

    @Test
    fun oMinimoEhInclusivo() {
        val anterior = ProgressoGravado(posicao = 120.0, duracao = duracaoDoFilme)
        val decisao = assertNotNull(
            decidirProgresso(
                posicao = 120.0 + AVANCO_MINIMO_PARA_GRAVAR_SEGUNDOS,
                duracao = duracaoDoFilme,
                gravado = anterior,
            ),
        )
        assertEquals(121.0, decisao.posicao)
    }
}
