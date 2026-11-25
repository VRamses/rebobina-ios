@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package net.dynv6.rebobina.player

import net.dynv6.rebobina.raizDoApp
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.darwin.dispatch_get_main_queue

/**
 * `AVPlayerViewController` em tela cheia, apresentado pela raiz do Compose.
 *
 * A raiz vem de `raizDoApp`, gravada em `MainViewController`: apresentar a partir dela evita
 * `keyWindow`, que esta fora de moda no iOS 13 em diante, e funciona igual quando o app volta ao
 * primeiro plano depois de uma chamada.
 *
 * Posicao: um observador periodico de 5 s entrega (posicao, duracao) para quem chamou. Quem
 * ouve grava no `/progresso`; o servidor marca como visto pela regra dele (mais de 92 por cento).
 *
 * Fechar e sempre explicito, por `fechar()`, sem depender de `deinit` nem de herdar de
 * `AVPlayerViewController`: o proprio observador percebe quando o usuario dispensou a tela pela
 * alca do iOS (`presentingViewController` vira nulo) e fecha, para o audio nao seguir tocando
 * atras do catalogo.
 */
internal actual object PlayerNativo {
    private var controlador: AVPlayerViewController? = null
    private var aparelho: AVPlayer? = null
    private var observador: Any? = null
    private var apresentado: Boolean = false

    actual val aberto: Boolean get() = controlador != null

    actual fun abrir(url: String, posicaoInicial: Double, aoSalvar: (Double, Double) -> Unit) {
        fechar()
        val raiz = raizDoApp ?: return
        val destino = NSURL.URLWithString(url) ?: return

        // Som tocando mesmo com o iPhone no modo silencioso, que e o esperado em video.
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)

        val player = AVPlayer(playerItem = AVPlayerItem(uRL = destino))
        val tela = AVPlayerViewController()
        tela.player = player
        tela.showsPlaybackControls = true

        controlador = tela
        aparelho = player
        observador = player.addPeriodicTimeObserverForInterval(
            interval = CMTimeMakeWithSeconds(5.0, 600),
            queue = dispatch_get_main_queue(),
            usingBlock = { tempo ->
                if (controlador === tela) {
                    if (apresentado && tela.presentingViewController == null) {
                        fechar()
                    } else {
                        val posicao = CMTimeGetSeconds(tempo)
                        val duracao = player.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: 0.0
                        if (!posicao.isNaN() && !duracao.isNaN() && posicao > 0.0 && duracao > 0.0) {
                            aoSalvar(posicao, duracao)
                        }
                    }
                }
            },
        )

        raiz.presentViewController(tela, animated = true, completion = { apresentado = true })
        if (posicaoInicial > 5.0) player.seekToTime(CMTimeMakeWithSeconds(posicaoInicial, 600))
        player.play()
    }

    actual fun fechar() {
        val player = aparelho
        val vigia = observador
        if (player != null && vigia != null) player.removeTimeObserver(vigia)
        observador = null
        player?.pause()
        aparelho = null
        val tela = controlador
        controlador = null
        val estavaNaTela = apresentado
        apresentado = false
        if (tela != null && estavaNaTela && tela.presentingViewController != null) {
            tela.dismissViewControllerAnimated(true, completion = null)
        }
    }
}
