package net.dynv6.rebobina.ui.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// GERADO A PARTIR DE design/tokens/rebobina-tokens.json. NAO EDITAR A MAO.
// fonte: rebobina-tokens 1.0.4, 2026-11-22, Yara Baptista.
// sha256 do JSON: 67fe6750b56c3fd84587792b9b54d4996164966dbe4968c20b5459d7dfca91f3
// canonico: rebobina-servidor main design/tokens/rebobina-tokens.json.
// familia: system-ui, Roboto, -apple-system, Segoe UI, sans-serif.
// Sem fonte propria no repositorio (tipo.familia.propria = false): em Compose a pilha do
// sistema e FontFamily.Default, entao nao existe constante de familia aqui.
// Gerado por design/tokens/gerar-kotlin.mjs. Para mudar valor, mude o JSON na origem.

// cor, tema escuro (cor.escuro)
object ReboColor {
    val Base = Color(0xFF000000)
    val Surface = Color(0xFF141414)
    val Elevated = Color(0xFF181818)
    val Hover = Color(0xFF232323)
    val Line = Color(0xFF262A31)
    val LineStrong = Color(0xFF343A43)
    val Ink = Color(0xFFF2F3F5)
    val Ink2 = Color(0xFFA7ADB8)
    val Ink3 = Color(0xFF858C99)
    val Brand = Color(0xFFE50914)
    val BrandPressed = Color(0xFFB00710)
    val OnBrand = Color(0xFFFFFFFF)
    val Focus = Color(0xFFFFFFFF)
    val Info = Color(0xFF4C8DFF)
    val Ok = Color(0xFF3DD68C)
    val Warn = Color(0xFFFFB020)
    val Danger = Color(0xFFFF5A5F)
    val Field = Color(0xFF0F1115)
    val FieldFocused = Color(0xFF12151A)
    val Slot = Color(0xFF22262D)
    val Scrim = Color(0xB8000000)
    val Overlay = Color(0x8C000000)
}

// cor, tema claro (cor.claro)
object ReboColorClaro {
    val Base = Color(0xFFF7F7F8)
    val Surface = Color(0xFFFFFFFF)
    val Elevated = Color(0xFFFFFFFF)
    val Hover = Color(0xFFEDEEF0)
    val Line = Color(0xFFDADCE0)
    val LineStrong = Color(0xFFBFC3C9)
    val Ink = Color(0xFF14161A)
    val Ink2 = Color(0xFF4A515C)
    val Ink3 = Color(0xFF5A6270)
    val Brand = Color(0xFFC0070F)
    val BrandPressed = Color(0xFF8E050C)
    val OnBrand = Color(0xFFFFFFFF)
    val Focus = Color(0xFF0B0C0E)
    val Info = Color(0xFF2563EB)
    val Ok = Color(0xFF157F4E)
    val Warn = Color(0xFF8A5A00)
    val Danger = Color(0xFFC1121F)
    val Field = Color(0xFFFFFFFF)
    val FieldFocused = Color(0xFFF2F3F5)
    val Slot = Color(0xFFE8E9EC)
    val Scrim = Color(0x73000000)
    val Overlay = Color(0x59000000)
}

// tipo.escala, na densidade de referencia
object ReboType {
    val Display = TextStyle(
        fontSize = 44.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp,
    )
    val H1 = TextStyle(
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp,
    )
    val H2 = TextStyle(
        fontSize = 22.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val H3 = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val Body = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp,
    )
    val Meta = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp,
    )
    val Label = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 1.2.sp,
    )
    val Control = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val ItemTitle = TextStyle(
        fontSize = 15.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val ItemMeta = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp,
    )
}

// tipo.escalaCelular, o mesmo papel com medida de celular
object ReboTypeCelular {
    val Display = TextStyle(
        fontSize = 40.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp,
    )
    val H1 = TextStyle(
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp,
    )
    val H2 = TextStyle(
        fontSize = 22.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val H3 = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val Body = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp,
    )
    val Meta = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp,
    )
    val Label = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 1.2.sp,
    )
    val Control = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val ItemTitle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp,
    )
    val ItemMeta = TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp,
    )
}

// tipo.peso
object ReboPeso {
    val Leitura = FontWeight(400)
    val Destaque = FontWeight(600)
    val Titulo = FontWeight(700)
}

// tipo.pisoLeitura: menor corpo de texto legivel por plataforma
object ReboTypePiso {
    val Tv = 12.sp
    val Webos = 12.sp
    val Web = 12.sp
    val Desktop = 12.sp
    val Celular = 13.sp
}

// tipo.numeros: algarismo tabular da sans, sem segunda familia
object ReboTypeNumeros {
    val Estilo = TextStyle(fontFeatureSettings = "tnum")
}

// espaco.degraus, em dp
object ReboSpace {
    val N0 = 2.dp
    val N1 = 4.dp
    val N2 = 8.dp
    val N3 = 12.dp
    val N4 = 16.dp
    val N5 = 24.dp
    val N6 = 32.dp
    val N7 = 48.dp
    val N8 = 64.dp
    val N9 = 96.dp
}

// espaco.uso: para que serve cada degrau
object ReboSpaceUso {
    val DentroDoTexto = 4.dp
    val EntreIrmaos = 8.dp
    val PartesDoCartao = 12.dp
    val MargemCelular = 16.dp
    val AntesDeSecao = 24.dp
    val EntreSecoes = 32.dp
    val MargemTv = 48.dp
    val RespiroHeroi = 64.dp
    val TopoDeDestaque = 96.dp
}

// raio, em dp
object ReboRadius {
    val Controle = 8.dp
    val Cartao = 12.dp
    val Capa = 16.dp
    val Pilula = 999.dp
}

// movimento, em milissegundos
object ReboMotion {
    val Anel = 90
    val Escala = 120
    val Heroi = 240
    val Folha = 300
    val ToqueLongoTv = 700
    val ToqueLongoCelular = 500
}

// estado.carregando: quando o esqueleto e o texto entram
object ReboEstadoCarregando {
    val EsqueletoAposMs = 1000
    val TextoAposMs = 8000
}

// plataforma.<nome>: alvo de toque, area segura, foco e as tres flags
object ReboSize {
    object Tv {
        val AlvoMinimo = 48.dp
        val AlvoLinha = 56.dp
        val AlvoControle = 52.dp
        val AlvoBarra = 56.dp
        val AlvoAnel = 2.dp
        val AlvoRespiro = 2.dp
        val AreaSeguraLateral = 48.dp
        val AreaSeguraTopo = 27.dp
        val FocoEscala = 1.04f
        val Relogio = true
        val Ponteiro = false
        val Hover = false
    }
    object Webos {
        val AlvoMinimo = 48.dp
        val AlvoLinha = 56.dp
        val AlvoControle = 52.dp
        val AlvoBarra = 56.dp
        val AlvoAnel = 2.dp
        val AlvoRespiro = 2.dp
        val AreaSeguraLateral = 0.dp
        val AreaSeguraTopo = 0.dp
        val FocoEscala = 1.04f
        val Relogio = true
        val Ponteiro = true
        val Hover = true
    }
    object Web {
        val AlvoMinimo = 40.dp
        val AlvoLinha = 48.dp
        val AlvoControle = 44.dp
        val AlvoBarra = 48.dp
        val AlvoAnel = 2.dp
        val AlvoRespiro = 2.dp
        val AreaSeguraLateral = 0.dp
        val AreaSeguraTopo = 0.dp
        val FocoEscala = 1.02f
        val Relogio = false
        val Ponteiro = true
        val Hover = true
    }
    object Desktop {
        val AlvoMinimo = 40.dp
        val AlvoLinha = 48.dp
        val AlvoControle = 44.dp
        val AlvoBarra = 48.dp
        val AlvoAnel = 2.dp
        val AlvoRespiro = 2.dp
        val AreaSeguraLateral = 0.dp
        val AreaSeguraTopo = 0.dp
        val FocoEscala = 1.02f
        val Relogio = false
        val Ponteiro = true
        val Hover = true
    }
    object Celular {
        val AlvoMinimo = 48.dp
        val AlvoLinha = 64.dp
        val AlvoControle = 56.dp
        val AlvoBarra = 52.dp
        val AlvoAnel = 0.dp
        val AlvoRespiro = 0.dp
        val AreaSeguraLateral = 0.dp
        val AreaSeguraTopo = 0.dp
        val FocoEscala = 1f
        val Relogio = false
        val Ponteiro = false
        val Hover = false
    }
}

// plataforma.<nome>.grade: colunas por plataforma
object ReboGrade {
    object Tv {
        val Colunas = 7
    }
    object Webos {
        val Colunas = 6
    }
    object Web {
        val Colunas = 5
    }
    object Desktop {
        val Colunas = 5
    }
    object Celular {
        val ColunasVertical = 3
        val ColunasHorizontal = 4
    }
}
