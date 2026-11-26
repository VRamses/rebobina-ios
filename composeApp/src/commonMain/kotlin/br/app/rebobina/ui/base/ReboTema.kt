package br.app.rebobina.ui.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import br.app.rebobina.ui.tokens.ReboColor

/**
 * Tema do app: as cores do Material vem de `ReboColor`, o mesmo objeto gerado que as telas usam.
 *
 * O app e escuro nesta rodada, porque e o que o desenho do iPhone usa (`cor` do JSON canonico).
 * `ReboColorClaro` existe no gerado e nao e usado: tema claro no iOS fica declarado como fora do
 * escopo, e nao como pendencia escondida. Nenhum widget pinta cor por conta propria: todo
 * `Text` recebe estilo com cor de token e todo `background` usa `ReboColor`.
 */
@Composable
fun ReboTema(conteudo: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ReboColor.Brand,
            onPrimary = ReboColor.OnBrand,
            secondary = ReboColor.Info,
            onSecondary = ReboColor.OnBrand,
            background = ReboColor.Base,
            onBackground = ReboColor.Ink,
            surface = ReboColor.Surface,
            onSurface = ReboColor.Ink,
            surfaceVariant = ReboColor.Elevated,
            onSurfaceVariant = ReboColor.Ink2,
            outline = ReboColor.Line,
            error = ReboColor.Danger,
            onError = ReboColor.OnBrand,
        ),
        content = conteudo,
    )
}
