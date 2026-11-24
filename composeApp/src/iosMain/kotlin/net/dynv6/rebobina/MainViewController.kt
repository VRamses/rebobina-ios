@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package net.dynv6.rebobina

import androidx.compose.ui.window.ComposeUIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIFont
import platform.UIKit.UIScreen
import platform.UIKit.UITextView
import platform.UIKit.UIViewController

/**
 * Ponto de entrada chamado pelo Swift (MainViewControllerKt.MainViewController()).
 *
 * Este build e de diagnostico. O app fechava sozinho 0,6 s depois de abrir, com excecao nao
 * tratada numa fila de segundo plano, e o .ipa sai sem simbolo de depuracao, entao o texto da
 * excecao nao aparecia em lugar nenhum: nem no relatorio de crash, nem no log do Sideloadly.
 *
 * O gancho abaixo grava o texto da excecao antes de o processo abortar, e a abertura seguinte
 * mostra esse texto numa tela de UIKit puro, para o texto aparecer mesmo que a falha esteja na
 * camada de desenho do Compose. O texto e consumido uma vez: na abertura depois dessa, o app
 * volta ao normal e o gancho fica pronto para a proxima falha.
 */
private const val CHAVE_DO_ERRO: String = "rebobina.ultimoErro"

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    instalarGanchoDeErro()
    val erro = consumirErroGravado()
    if (erro != null) return telaDeErro(erro)
    return ComposeUIViewController { App() }
}

@OptIn(ExperimentalNativeApi::class)
private fun instalarGanchoDeErro() {
    setUnhandledExceptionHook { erro ->
        val padroes = NSUserDefaults.standardUserDefaults
        padroes.setObject(erro.stackTraceToString(), forKey = CHAVE_DO_ERRO)
        padroes.synchronize()
    }
}

/** Le o texto gravado na falha anterior e apaga a chave, para a tela nao ficar presa. */
private fun consumirErroGravado(): String? {
    val padroes = NSUserDefaults.standardUserDefaults
    val texto = padroes.stringForKey(CHAVE_DO_ERRO)
    if (texto != null) padroes.removeObjectForKey(CHAVE_DO_ERRO)
    return texto
}

/**
 * Tela de leitura do erro, em UIKit e nao em Compose de proposito: se a excecao vier da camada
 * de desenho, uma tela Compose quebraria do mesmo jeito e nao mostraria nada.
 */
private fun telaDeErro(texto: String): UIViewController {
    val controlador = UIViewController(nibName = null, bundle = null)
    val campo = UITextView(frame = UIScreen.mainScreen.bounds)
    campo.font = UIFont.systemFontOfSize(11.0)
    campo.text = "\n\nRebobina: erro na abertura anterior\n\n" + texto
    controlador.view = campo
    return controlador
}
