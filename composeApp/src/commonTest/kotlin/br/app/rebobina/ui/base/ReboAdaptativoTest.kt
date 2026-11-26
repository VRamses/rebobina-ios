package br.app.rebobina.ui.base

import br.app.rebobina.ui.tokens.ReboGrade
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Congela a fronteira das duas faixas de layout.
 *
 * O predicado e o mesmo do Android (`largura < 600 || altura < 480`), e este teste existe
 * porque mudar um dos dois numeros muda o desenho inteiro sem mudar nenhuma tela: o erro
 * passaria despercebido em revisao de codigo e apareceria so no iPhone deitado.
 *
 * Cobre tres pontos por eixo (abaixo, exatamente na fronteira, acima), o caso de iPhone
 * deitado (844 x 390) e a coluna que cada faixa escolhe na grade do iOS 1.0.8.
 */
class ReboAdaptativoTest {

    @Test
    fun larguraAbaixoDaFronteiraEhCelular() {
        assertTrue(reboFaixaCelular(larguraDp = 599f, alturaDp = 800f))
    }

    @Test
    fun larguraNaFronteiraNaoEhCelular() {
        assertFalse(reboFaixaCelular(larguraDp = 600f, alturaDp = 800f))
        assertFalse(reboFaixaCelular(larguraDp = 601f, alturaDp = 800f))
    }

    @Test
    fun alturaAbaixoDaFronteiraEhCelular() {
        assertTrue(reboFaixaCelular(larguraDp = 800f, alturaDp = 479f))
    }

    @Test
    fun alturaNaFronteiraNaoEhCelular() {
        assertFalse(reboFaixaCelular(larguraDp = 800f, alturaDp = 480f))
        assertFalse(reboFaixaCelular(larguraDp = 800f, alturaDp = 481f))
    }

    @Test
    fun iphoneDeitadoEntraNaFaixaCelularPelaAltura() {
        assertTrue(reboFaixaCelular(larguraDp = 844f, alturaDp = 390f))
    }

    @Test
    fun colunasDoCatalogoSeguemAFaixa() {
        assertEquals(ReboGrade.Ios.ColunasVertical, reboColunasCatalogo(599f, 800f))
        assertEquals(3, reboColunasCatalogo(599f, 800f))
        assertEquals(ReboGrade.Ios.ColunasHorizontal, reboColunasCatalogo(600f, 800f))
        assertEquals(4, reboColunasCatalogo(600f, 800f))
    }
}
