package br.app.rebobina.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import br.app.rebobina.dados.Acervo
import br.app.rebobina.dados.AjustesDoAcervo
import br.app.rebobina.dados.EstadoTela
import br.app.rebobina.ui.base.ReboBotao
import br.app.rebobina.ui.base.ReboBotaoTom
import br.app.rebobina.ui.base.ReboCabecalhoTela
import br.app.rebobina.ui.base.ReboCampo
import br.app.rebobina.ui.base.ReboDimensao
import br.app.rebobina.ui.base.ReboEstilo
import br.app.rebobina.ui.base.ReboFaixa
import br.app.rebobina.ui.base.ReboFio
import br.app.rebobina.ui.base.ReboRotulos
import br.app.rebobina.ui.tokens.ReboColor
import br.app.rebobina.ui.tokens.ReboTextos

/**
 * Ajustes: so o endereco do catalogo.
 *
 * Nao existe campo de chave porque o servico do catalogo nao pede chave. "Testar conexao" monta
 * um `Acervo` com o endereco escrito, chama `/catalogo` e mostra o nome do usuario do acervo; se
 * falhar, mostra a frase do erro, nunca um acervo inventado.
 */
@Composable
fun TelaAjustes(
    ajustes: AjustesDoAcervo,
    faixa: ReboFaixa,
    aoSalvar: () -> Unit,
) {
    val celular = faixa.celular
    val escopo = rememberCoroutineScope()
    var teste by remember { mutableStateOf<EstadoTela<String>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReboColor.Base)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ReboDimensao.MargemCelular)
            .padding(top = ReboDimensao.EspacoN3, bottom = ReboDimensao.EntreSecoes),
    ) {
        ReboCabecalhoTela(titulo = ReboTextos.Interface.Comum.Ajustes, celular = celular)
        ReboCampo(
            valor = ajustes.endereco,
            aoMudar = { ajustes.endereco = it },
            rotulo = ReboRotulos.ENDERECO_DO_SERVIDOR,
            celular = celular,
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        Text(
            text = ReboRotulos.DICA_ENDERECO,
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
        )
        Spacer(Modifier.height(ReboDimensao.EspacoN3))
        Row(horizontalArrangement = Arrangement.spacedBy(ReboDimensao.EspacoN2)) {
            ReboBotao(
                texto = ReboTextos.Interface.Comum.Salvar,
                aoTocar = {
                    ajustes.salvar()
                    aoSalvar()
                },
                tom = ReboBotaoTom.Primario,
                celular = celular,
            )
            ReboBotao(
                texto = ReboRotulos.TESTAR_CONEXAO,
                aoTocar = {
                    teste = EstadoTela.Carregando
                    escopo.launch {
                        val endereco = ajustes.enderecoEfetivo
                        teste = if (endereco.isBlank()) {
                            EstadoTela.Falha(detalhe = ReboRotulos.ENDERECO_VAZIO, semRede = false)
                        } else {
                            when (val pagina = Acervo(endereco).inicio()) {
                                is EstadoTela.Pronto -> EstadoTela.Pronto(
                                    pagina.dado.nomeDoUsuario.ifBlank { endereco },
                                )
                                is EstadoTela.Falha -> pagina
                                EstadoTela.Carregando -> EstadoTela.Carregando
                            }
                        }
                    }
                },
                tom = ReboBotaoTom.Contorno,
                celular = celular,
            )
        }
        Spacer(Modifier.height(ReboDimensao.EspacoN2))
        Text(
            text = ReboRotulos.SALVO,
            style = ReboEstilo.itemMeta(celular).copy(color = ReboColor.Ink3),
        )

        when (val resultado = teste) {
            is EstadoTela.Pronto -> {
                Spacer(Modifier.height(ReboDimensao.EspacoN2))
                Text(
                    text = ReboRotulos.CONECTADO_COMO + " " + resultado.dado,
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
            null -> Unit
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
