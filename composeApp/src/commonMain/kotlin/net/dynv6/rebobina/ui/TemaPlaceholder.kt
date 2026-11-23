package net.dynv6.rebobina.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * PLACEHOLDER DE TEMA. Nao e a identidade visual do Rebobina.
 *
 * A identidade de verdade vem da engenheira de UX em
 * _rebobina/entrega/IDENTIDADE.md e _rebobina/entrega/rebobina-tokens.json.
 * Enquanto esses arquivos nao existirem, esta tela usa cinzas neutros de proposito,
 * so para provar que o pipeline compila e desenha.
 */
object TemaPlaceholder {
    val coresEscuras = darkColorScheme(
        primary = Color(0xFF9AA4B2),
        background = Color(0xFF121212),
        surface = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE6E6E6),
        onSurface = Color(0xFFE6E6E6),
        onSurfaceVariant = Color(0xFFB0B0B0),
    )
}
