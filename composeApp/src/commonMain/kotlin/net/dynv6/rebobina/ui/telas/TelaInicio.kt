package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.ChipDaTela
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.FileiraDaTela
import net.dynv6.rebobina.dados.MidiaCartao
import net.dynv6.rebobina.dados.PaginaInicio
import net.dynv6.rebobina.dados.RotuloDoChip
import net.dynv6.rebobina.ui.base.ReboAvatar
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboChip
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstadoCarregando
import net.dynv6.rebobina.ui.base.ReboEstadoFalha
import net.dynv6.rebobina.ui.base.ReboEstadoVazio
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFileiraDeCartoes
import net.dynv6.rebobina.ui.base.ReboGradeDeCartoes
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.base.reboColunasCatalogo
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Inicio: as fileiras que o servidor monta, mais os chips de filtro.
 *
 * O catalogo chega pronto em `/catalogo`: cada fileira ja vem com titulo e itens, e cada chip com
 * o titulo do grupo e os rotulos que funcionam como filtro em `/catalogo/itens`. Por isso esta
 * tela nao escreve nome de fileira nem de genero: se o servidor mudar de fileira, a tela muda
 * junto, sem uma linha de codigo.
 *
 * Dois passos no filtro porque o formato tem dois niveis: primeiro o grupo (Filmes, Series,
 * Novelas, Decadas...), depois o rotulo dentro dele (Acao, 1990, ...). Tocar no rotulo troca as
 * fileiras por uma grade do filtro escolhido, com um botao para voltar.
 */
@Composable
fun TelaInicio(
    acervo: Acervo,
    faixa: ReboFaixa,
    aoAbrirFicha: (String) -> Unit,
    aoAbrirAjustes: () -> Unit,
) {
    val celular = faixa.celular
    var pagina by remember { mutableStateOf<EstadoTela<PaginaInicio>?>(null) }
    var tentativa by remember { mutableStateOf(0) }
    var grupoAberto by remember { mutableStateOf<ChipDaTela?>(null) }
    var filtro by remember { mutableStateOf<RotuloDoChip?>(null) }
    var itensDoFiltro by remember { mutableStateOf<EstadoTela<List<MidiaCartao>>?>(null) }

    LaunchedEffect(acervo, tentativa) {
        pagina = EstadoTela.Carregando
        pagina = acervo.inicio()
    }

    LaunchedEffect(acervo, filtro) {
        val escolhido = filtro
        if (escolhido == null) {
            itensDoFiltro = null
        } else {
            itensDoFiltro = EstadoTela.Carregando
            itensDoFiltro = acervo.itensDoChip(escolhido.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        CabecalhoInicio(
            celular = celular,
            nomeDoUsuario = (pagina as? EstadoTela.Pronto)?.dado?.nomeDoUsuario.orEmpty(),
            aoAbrirAjustes = aoAbrirAjustes,
        )

        when (val carregada = pagina) {
            null, EstadoTela.Carregando -> ReboEstadoCarregando(celular)
            is EstadoTela.Falha -> ReboEstadoFalha(
                detalhe = carregada.detalhe,
                semRede = carregada.semRede,
                celular = celular,
                aoTentarDeNovo = { tentativa += 1 },
                aoAbrirAjustes = aoAbrirAjustes,
            )
            is EstadoTela.Pronto -> {
                val dado = carregada.dado
                if (dado.fileiras.isEmpty() && dado.chips.isEmpty()) {
                    ReboEstadoVazio(
                        celular = celular,
                        aoTocarAcao = { tentativa += 1 },
                        titulo = ReboTextos.Estado.Vazio.Titulo,
                        corpo = ReboTextos.Estado.Vazio.Corpo,
                        acao = ReboTextos.Estado.Erro.Acao,
                    )
                } else {
                    LinhaDeGrupos(
                        chips = dado.chips,
                        grupoAberto = grupoAberto,
                        aoTocarGrupo = { grupo ->
                            grupoAberto = if (grupoAberto?.id == grupo.id) null else grupo
                            filtro = null
                        },
                    )
                    val grupo = grupoAberto
                    if (grupo != null) {
                        LinhaDeRotulos(
                            grupo = grupo,
                            filtro = filtro,
                            aoTocarRotulo = { rotulo ->
                                filtro = if (filtro?.id == rotulo.id) null else rotulo
                            },
                        )
                    }
                    val escolhido = filtro
                    if (escolhido == null) {
                        dado.fileiras.forEach { fileira ->
                            FileiraDaInicio(
                                fileira = fileira,
                                celular = celular,
                                aoAbrirFicha = aoAbrirFicha,
                            )
                        }
                    } else {
                        GradeDoFiltro(
                            titulo = escolhido.rotulo,
                            estado = itensDoFiltro,
                            celular = celular,
                            larguraDp = faixa.larguraDp,
                            alturaDp = faixa.alturaDp,
                            aoAbrirFicha = aoAbrirFicha,
                            aoFechar = { filtro = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CabecalhoInicio(
    celular: Boolean,
    nomeDoUsuario: String,
    aoAbrirAjustes: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReboDimensao.MargemCelular)
            .heightIn(min = ReboDimensao.AlvoBarra),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ReboTextos.Interface.Comum.Marca,
            style = ReboEstilo.tituloTela(celular),
            modifier = Modifier.weight(1f),
        )
        if (nomeDoUsuario.isNotBlank()) {
            ReboAvatar(
                inicial = nomeDoUsuario.take(1).uppercase(),
                celular = celular,
            )
            Spacer(Modifier.width(ReboDimensao.EspacoN2))
        }
        ReboBotao(
            texto = ReboTextos.Interface.Comum.Ajustes,
            aoTocar = aoAbrirAjustes,
            tom = ReboBotaoTom.Discreto,
            celular = celular,
        )
    }
}

@Composable
private fun LinhaDeGrupos(
    chips: List<ChipDaTela>,
    grupoAberto: ChipDaTela?,
    aoTocarGrupo: (ChipDaTela) -> Unit,
) {
    if (chips.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ReboDimensao.EspacoN2),
        contentPadding = PaddingValues(horizontal = ReboDimensao.MargemCelular),
        horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
    ) {
        items(chips, key = { it.id }) { grupo ->
            ReboChip(
                texto = grupo.titulo,
                ativa = grupoAberto?.id == grupo.id,
                aoTocar = { aoTocarGrupo(grupo) },
            )
        }
    }
}

@Composable
private fun LinhaDeRotulos(
    grupo: ChipDaTela,
    filtro: RotuloDoChip?,
    aoTocarRotulo: (RotuloDoChip) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = ReboDimensao.EspacoN2),
        contentPadding = PaddingValues(horizontal = ReboDimensao.MargemCelular),
        horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2),
    ) {
        items(grupo.rotulos, key = { it.id }) { rotulo ->
            ReboChip(
                texto = rotulo.rotulo,
                ativa = filtro?.id == rotulo.id,
                aoTocar = { aoTocarRotulo(rotulo) },
            )
        }
    }
}

@Composable
private fun FileiraDaInicio(
    fileira: FileiraDaTela,
    celular: Boolean,
    aoAbrirFicha: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = ReboDimensao.AntesDeSecao)) {
        Text(
            text = fileira.titulo,
            style = ReboEstilo.tituloSecao(celular),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular),
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        ReboFileiraDeCartoes(
            itens = fileira.itens,
            celular = celular,
            aoTocar = { midia -> aoAbrirFicha(midia.id) },
            recuo = ReboDimensao.MargemCelular,
        )
    }
}

@Composable
private fun GradeDoFiltro(
    titulo: String,
    estado: EstadoTela<List<MidiaCartao>>?,
    celular: Boolean,
    larguraDp: Float,
    alturaDp: Float,
    aoAbrirFicha: (String) -> Unit,
    aoFechar: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = ReboDimensao.MargemCelular)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ReboDimensao.AlvoBarra),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titulo,
                style = ReboEstilo.tituloSecao(celular),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            ReboBotao(
                texto = ReboRotulos.LIMPAR,
                aoTocar = aoFechar,
                tom = ReboBotaoTom.Discreto,
                celular = celular,
            )
        }
        when (val carregado = estado) {
            null, EstadoTela.Carregando -> ReboEstadoCarregando(celular)
            is EstadoTela.Falha -> ReboEstadoFalha(
                detalhe = carregado.detalhe,
                semRede = carregado.semRede,
                celular = celular,
                aoTentarDeNovo = aoFechar,
            )
            is EstadoTela.Pronto -> if (carregado.dado.isEmpty()) {
                ReboEstadoVazio(celular = celular, aoTocarAcao = aoFechar)
            } else {
                ReboGradeDeCartoes(
                    itens = carregado.dado,
                    colunas = reboColunasCatalogo(larguraDp, alturaDp),
                    celular = celular,
                    aoTocar = { midia -> aoAbrirFicha(midia.id) },
                )
            }
        }
    }
}
