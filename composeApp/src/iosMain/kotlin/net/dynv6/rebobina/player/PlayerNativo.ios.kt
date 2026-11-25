@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package net.dynv6.rebobina.player

import net.dynv6.rebobina.raizDoApp
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.seekToTime
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.darwin.dispatch_get_main_queue

/**
 * `AVPlayerViewController` em tela cheia, apresentado pela raiz do Compose.
 *
 * A raiz vem de `raizDoApp`, gravada em `MainViewController`: apresentar a partir dela evita
 * `keyWindow`, que esta fora de moda no iOS 13 em diante, e funciona igual quando o app volta ao
 * primeiro plano depois de uma chamada.
 *
 * Posicao: um observador periodico de 5 s entrega (posicao, duracao) para quem chamou, e
 * `fechar()` entrega a posicao final antes de soltar o `AVPlayerItem`. A decisao de gravar ou
 * nao (posicao que nao andou, duracao desconhecida, video que nunca tocou) mora em
 * `decidirProgresso`, em commonMain, e tem teste. Quem ouve grava no `/progresso`; o servidor
 * marca como visto pela regra dele (mais de 92 por cento).
 *
 * Fechar e sempre explicito, por `fechar()`, sem depender de `deinit` nem de herdar de
 * `AVPlayerViewController`: o proprio observador percebe quando o usuario dispensou a tela pela
 * alca do iOS (`presentingViewController` vira nulo) e fecha, para o audio nao seguir tocando
 * atras do catalogo. Como a saida por Esc do teclado e a alca do iOS dispensam o mesmo
 * controlador, as duas caem nesse mesmo caminho. No fim do app, quem avisa e a notificacao de
 * segundo plano: quando o iOS suspende o app o observador de 5 s para de disparar, e essa e a
 * ultima chance de gravar.
 */
internal actual object PlayerNativo {
    private var controlador: AVPlayerViewController? = null
    private var aparelho: AVPlayer? = null
    private var observador: Any? = null
    private var observadorDeFundo: Any? = null
    private var apresentado: Boolean = false
    private var aoSalvarAtual: ((Double, Double) -> Unit)? = null
    private var ultimaGravacao: ProgressoGravado? = null

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
        aoSalvarAtual = aoSalvar
        ultimaGravacao = null
        observador = player.addPeriodicTimeObserverForInterval(
            interval = CMTimeMakeWithSeconds(5.0, 600),
            queue = dispatch_get_main_queue(),
            usingBlock = { tempo ->
                if (controlador === tela) {
                    if (apresentado && tela.presentingViewController == null) {
                        fechar()
                    } else {
                        gravar(posicao = CMTimeGetSeconds(tempo), duracao = duracaoDoItem(player))
                    }
                }
            },
        )

        // Fim do app: o iOS suspende o processo e o observador de 5 s para de disparar, entao
        // esta notificacao e a ultima chance de gravar a posicao antes de o app sumir.
        observadorDeFundo = NSNotificationCenter.defaultCenter.addObserverForName(
            UIApplicationDidEnterBackgroundNotification,
            null,
            null,
        ) { gravarPosicaoAtual() }

        raiz.presentViewController(tela, animated = true, completion = { apresentado = true })
        if (posicaoInicial > 5.0) player.seekToTime(CMTimeMakeWithSeconds(posicaoInicial, 600))
        player.play()
    }

    actual fun fechar() {
        val player = aparelho
        val vigia = observador
        // Grava a posicao final antes de soltar o AVPlayerItem: sem isso, sair do player perde
        // ate 5 s, que e o intervalo do observador.
        if (player != null) gravarPosicaoAtual()
        if (player != null && vigia != null) player.removeTimeObserver(vigia)
        observador = null
        observadorDeFundo?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        observadorDeFundo = null
        player?.pause()
        aparelho = null
        aoSalvarAtual = null
        ultimaGravacao = null
        val tela = controlador
        controlador = null
        val estavaNaTela = apresentado
        apresentado = false
        if (tela != null && estavaNaTela && tela.presentingViewController != null) {
            tela.dismissViewControllerAnimated(true, completion = null)
        }
    }

    /** Le (posicao, duracao) do player neste instante e grava, se a decisao deixar. */
    private fun gravarPosicaoAtual() {
        val player = aparelho ?: return
        gravar(posicao = CMTimeGetSeconds(player.currentTime()), duracao = duracaoDoItem(player))
    }

    /** Grava a posicao se `decidirProgresso` aceitar, e guarda o que foi gravado para nao repetir. */
    private fun gravar(posicao: Double, duracao: Double) {
        val decisao = decidirProgresso(posicao = posicao, duracao = duracao, gravado = ultimaGravacao) ?: return
        ultimaGravacao = decisao
        aoSalvarAtual?.invoke(decisao.posicao, decisao.duracao)
    }

    private fun duracaoDoItem(player: AVPlayer): Double =
        player.currentItem()?.duration()?.let { CMTimeGetSeconds(it) } ?: 0.0
}
