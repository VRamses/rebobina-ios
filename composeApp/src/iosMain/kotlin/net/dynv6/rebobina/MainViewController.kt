package net.dynv6.rebobina

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Ponto de entrada chamado pelo Swift (MainViewControllerKt.MainViewController()). */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
