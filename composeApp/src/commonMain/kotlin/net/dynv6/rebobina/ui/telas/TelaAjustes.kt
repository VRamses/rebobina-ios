package net.dynv6.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import net.dynv6.rebobina.dados.Acervo
import net.dynv6.rebobina.dados.AjustesDoAcervo
import net.dynv6.rebobina.dados.EstadoTela
import net.dynv6.rebobina.dados.UsuarioDoAcervo
import net.dynv6.rebobina.ui.base.ReboCabecalhoTela
import net.dynv6.rebobina.ui.base.ReboCampo
import net.dynv6.rebobina.ui.base.ReboBotao
import net.dynv6.rebobina.ui.base.ReboBotaoTom
import net.dynv6.rebobina.ui.base.ReboDimensao
import net.dynv6.rebobina.ui.base.ReboEstilo
import net.dynv6.rebobina.ui.base.ReboFaixa
import net.dynv6.rebobina.ui.base.ReboFio
import net.dynv6.rebobina.ui.base.ReboRotulos
import net.dynv6.rebobina.ui.tokens.ReboColor
import net.dynv6.rebobina.ui.tokens.ReboTextos

/**
 * Ajustes: endereco do servidor Jellyfin e chave de API.
 *
 * A chave fica gravada nos `NSUserDefaults` deste aparelho, nunca no repositorio. O campo da
 * chave e mascarado na tela (`PasswordVisualTransformation`), mas o valor continua em texto
 * puro no armazenamento local: isso e declarado no README como limite desta rodada, nao como
 * seguranca de cofre de chaves.
 *
 * "Testar conexao" cria um `Acervo` novo com o que esta escrito nos campos, chama `/Users` e
 * mostra o nome do primeiro usuario. Se falhar, mostra a frase de falha do `ReboTextos`, com o
 * detalhe do servidor, em vez de dizer que conectou sem ter conectado.
 */
@Composable
fun TelaAjustes(
    ajustes: AjustesDoAcervo,
    faixa: ReboFaixa,
    aoVoltar: () -> Unit,
) {
    val celular = faixa.celular
    var salvo by remember { mutableStateOf(false) }
    var teste by remember { mutableStateOf<EstadoTela<List<UsuarioDoAcervo>>?>(null) }
    val escopo = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ReboDimensao.MargemCelular)
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        ReboCabecalhoTela(titulo = ReboRotulos.ABA_AJUSTES, celular = celular, aoVoltar = aoVoltar)
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        ReboCampo(
            valor = ajustes.endereco,
            aoMudar = {
                ajustes.endereco = it
                salvo = false
                teste = null
            },
            rotulo = ReboRotulos.ENDERECO_DO_SERVIDOR,
            celular = celular,
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        ReboCampo(
            valor = ajustes.chave,
            aoMudar = {
                ajustes.chave = it
                salvo = false
                teste = null
            },
            rotulo = ReboRotulos.CHAVE_DE_API,
            celular = celular,
            segredo = true,
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN1))
        Text(
            text = ReboRotulos.CHAVE_FICA_NO_APARELHO,
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
        )

        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        Row(horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2)) {
            ReboBotao(
                texto = ReboRotulos.SALVAR,
                aoTocar = {
                    ajustes.salvar()
                    salvo = true
                },
                tom = ReboBotaoTom.Primario,
                celular = celular,
            )
            ReboBotao(
                texto = ReboRotulos.TESTAR_CONEXAO,
                aoTocar = {
                    val endereco = ajustes.enderecoEfetivo
                    if (endereco.isBlank()) {
                        teste = EstadoTela.Falha(ReboRotulos.ENDERECO_VAZIO, semRede = false)
                    } else {
                        teste = EstadoTela.Carregando
                        escopo.launch {
                            val novo = Acervo(endereco, ajustes.chaveEfetiva)
                            teste = novo.usuarios()
                        }
                    }
                },
                tom = ReboBotaoTom.Contorno,
                celular = celular,
            )
        }
        if (salvo) {
            Spacer(Modifier.height(ReboDimensao.EspacoN2))
            Text(
                text = ReboRotulos.SALVO,
                style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ok),
            )
        }
        when (val resultado = teste) {
            null -> Unit
            is EstadoTela.Pronto -> {
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                val nomes = resultado.dado.map { it.nome }.distinct()
                Text(
                    text = ReboRotulos.CONECTADO_COMO + " " + nomes.joinToString(", "),
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Ok),
                )
            }
            is EstadoTela.Falha -> {
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                Text(
                    text = resultado.detalhe,
                    style = ReboEstilo.corpo(celular).copy(color = ReboColor.Danger),
                )
            }
            EstadoTela.Carregando -> {
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                Text(
                    text = ReboTextos.Estado.Carregando.TituloDemora,
                    style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
                )
            }
        }

        Spacer(Modifier.height(ReboDimensao.AntesDeSecao))
        ReboFio()
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        Text(
            text = ajustes.enderecoEfetivo,
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
        )
    }
}
