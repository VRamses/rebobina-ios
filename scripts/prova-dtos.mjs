/**
 * Confere os tipos que o servico de catalogo devolve contra os DTOs do app iOS.
 *
 * Nao precisa do aparelho: pega amostra real de cada rota que o app chama e diz, campo por
 * campo, se o JSON traz texto, numero, booleano ou nulo onde o Kotlin espera outra coisa.
 * Campo ausente nao quebra porque todo DTO tem valor padrao; tipo trocado quebra.
 */
const BASE = process.env.REB_BASE ?? 'http://servidor.tail056d02.ts.net:8899'

// Campo -> tipo Kotlin, lido de CatalogoDtos.kt: os nomes aqui sao os mesmos.
const DTO = {
  RespostaCatalogo: { ok: 'Boolean', geradoEm: 'String', chips: 'List', fileiras: 'List', avisos: 'List' },
  UsuarioDoCatalogoDto: { id: 'String', nome: 'String' },
  ChipDto: { id: 'String', titulo: 'String', somenteTv: 'Boolean', itens: 'List' },
  RotuloDoChipDto: { id: 'String', rotulo: 'String', ordem: 'String' },
  FileiraDto: { id: 'String', titulo: 'String', tipo: 'String', limite: 'Int', total: 'Int', itens: 'List' },
  ItemDoCatalogoDto: {
    id: 'String', nome: 'String', tituloMidia: 'String', rotulo: 'String', tipo: 'String',
    serieId: 'String', ano: 'Int', nota: 'Double', generos: 'List', tmdb: 'String', serie: 'String',
    temporada: 'Int', episodio: 'Int', duracaoMin: 'Int', sinopse: 'String', imagem: 'String',
    fundo: 'String', progresso: 'Int', visto: 'Boolean',
  },
  RespostaItensDto: {
    ok: 'Boolean', filtro: 'String', rotulo: 'String', termo: 'String', serie: 'String',
    total: 'Int', itens: 'List', erro: 'String',
  },
  RespostaItemDto: { ok: 'Boolean', item: 'Object', erro: 'String' },
  RespostaProgressoDto: { ok: 'Boolean', total: 'Int', itens: 'List', chave: 'String' },
  ItemDeProgressoDto: {
    chave: 'String', tmdb: 'Long', tipo: 'String', temporada: 'Int', episodio: 'Int',
    pos: 'Double', duracao: 'Double', progresso: 'Int', visto: 'Boolean', atualizadoEm: 'String',
  },
  RespostaPreparoDto: {
    ok: 'Boolean', chave: 'String', estado: 'String', progresso: 'Double', motivo: 'String',
    duracao: 'Double', bytes: 'Long', url: 'String', completo: 'Boolean',
  },
}

const tipoNoJson = (v) => {
  if (v === null) return 'nulo'
  if (Array.isArray(v)) return 'lista'
  if (typeof v === 'object') return 'objeto'
  if (typeof v === 'number') return Number.isInteger(v) ? 'inteiro' : 'decimal'
  if (typeof v === 'string') return 'texto'
  if (typeof v === 'boolean') return 'booleano'
  return typeof v
}

const aceita = (esperado, valor) => {
  if (valor === null || valor === undefined) return true // nulo cabe em campo opcional
  const t = tipoNoJson(valor)
  if (esperado === 'String') return t === 'texto'
  if (esperado === 'Int' || esperado === 'Long') return t === 'inteiro'
  if (esperado === 'Double') return t === 'inteiro' || t === 'decimal'
  if (esperado === 'Boolean') return t === 'booleano'
  if (esperado === 'List') return t === 'lista'
  if (esperado === 'Object') return t === 'objeto'
  return false
}

const problemas = []
const vistos = {}

function conferir(nomeDto, objeto, onde) {
  const campos = DTO[nomeDto]
  if (!campos || !objeto) return
  for (const [campo, esperado] of Object.entries(campos)) {
    if (!(campo in objeto)) continue
    vistos[`${nomeDto}.${campo}`] = vistos[`${nomeDto}.${campo}`] ?? new Set()
    vistos[`${nomeDto}.${campo}`].add(tipoNoJson(objeto[campo]))
    if (!aceita(esperado, objeto[campo])) {
      problemas.push(
        `${onde}: ${nomeDto}.${campo} espera ${esperado} e veio ${tipoNoJson(objeto[campo])} ` +
          `(${JSON.stringify(objeto[campo]).slice(0, 40)})`,
      )
    }
  }
}

const pegar = async (rota) => {
  const resposta = await fetch(BASE + rota)
  const texto = await resposta.text()
  let dados = null
  try { dados = JSON.parse(texto) } catch { dados = null }
  return { status: resposta.status, dados, texto }
}

const rodar = async () => {
  const catalogo = await pegar('/catalogo')
  if (!catalogo.dados) throw new Error(`/catalogo sem JSON: ${catalogo.status} ${catalogo.texto.slice(0, 120)}`)
  conferir('RespostaCatalogo', catalogo.dados, '/catalogo')
  conferir('UsuarioDoCatalogoDto', catalogo.dados.usuario, '/catalogo usuario')

  const chip = (catalogo.dados.chips ?? [])[0]
  conferir('ChipDto', chip, '/catalogo chips[0]')
  conferir('RotuloDoChipDto', (chip?.itens ?? [])[0], '/catalogo chips[0].itens[0]')

  const fileira = (catalogo.dados.fileiras ?? [])[0]
  conferir('FileiraDto', fileira, '/catalogo fileiras[0]')
  const todos = (catalogo.dados.fileiras ?? []).flatMap((f) => f.itens ?? [])
  conferir('ItemDoCatalogoDto', todos[0], '/catalogo fileiras[0].itens[0]')
  conferir('ItemDoCatalogoDto', todos.find((i) => i.tipo === 'Episode'), '/catalogo episodio')
  conferir('ItemDoCatalogoDto', todos.find((i) => i.tipo === 'Series'), '/catalogo serie')

  const idDoItem = todos[0]?.id
  if (idDoItem) {
    const ficha = await pegar(`/catalogo/item/${idDoItem}`)
    conferir('RespostaItemDto', ficha.dados, `/catalogo/item/${idDoItem}`)
    conferir('ItemDoCatalogoDto', ficha.dados?.item, `/catalogo/item/${idDoItem}`)
  }

  const chipDoFiltro = (chip?.itens ?? [])[0]?.id
  if (chipDoFiltro) {
    const itens = await pegar(`/catalogo/itens?filtro=${encodeURIComponent(chipDoFiltro)}&limite=5&inicio=0`)
    conferir('RespostaItensDto', itens.dados, `/catalogo/itens?filtro=${chipDoFiltro}`)
    conferir('ItemDoCatalogoDto', (itens.dados?.itens ?? [])[0], `/catalogo/itens?filtro=${chipDoFiltro}`)
  }

  const busca = await pegar('/catalogo/busca?termo=duna&limite=40')
  conferir('RespostaItensDto', busca.dados, '/catalogo/busca?termo=duna')
  conferir('ItemDoCatalogoDto', (busca.dados?.itens ?? [])[0], '/catalogo/busca?termo=duna')

  const serie = todos.find((i) => i.tipo === 'Series' && i.serieId)
  if (serie) {
    const eps = await pegar(`/catalogo/episodios?serie=${encodeURIComponent(serie.serieId)}`)
    conferir('RespostaItensDto', eps.dados, '/catalogo/episodios')
    conferir('ItemDoCatalogoDto', (eps.dados?.itens ?? [])[0], `/catalogo/episodios?serie=${serie.serieId}`)
  }

  const progresso = await pegar('/progresso')
  conferir('RespostaProgressoDto', progresso.dados, '/progresso')
  conferir('ItemDeProgressoDto', (progresso.dados?.itens ?? [])[0], '/progresso itens[0]')
  const chave = (progresso.dados?.itens ?? [])[0]?.chave
  if (chave) {
    const um = await pegar(`/progresso/item/${encodeURIComponent(chave)}`)
    conferir('RespostaProgressoDto', um.dados, '/progresso/item')
    conferir('ItemDeProgressoDto', um.dados?.registro, `/progresso/item/${chave}`)
    console.log(`/progresso/item/${chave} -> ${um.status}`)
  }

  // /preparar dispara conversao de verdade no servidor, entao so roda quando pedido de proposito.
  const tmdbDoItem = todos[0]?.tmdb
  if (tmdbDoItem && process.env.REB_PREPARAR === '1') {
    const preparo = await pegar(`/preparar/${tmdbDoItem}`)
    conferir('RespostaPreparoDto', preparo.dados, `/preparar/${tmdbDoItem}`)
    console.log(`/preparar/${tmdbDoItem} -> ${preparo.status} ${preparo.dados?.estado}`)
  }
  if (process.env.REB_SEM_PREPARAR !== '1') {
    console.log('preparo pulado: use REB_PREPARAR=1 para conferir /preparar (dispara conversao)')
  }

  const resumo = Object.fromEntries(Object.entries(vistos).map(([campo, tipos]) => [campo, [...tipos].join('|')]))
  console.log(JSON.stringify({ problemas, tiposVistos: resumo }, null, 2))
}

rodar().catch((erro) => {
  console.error('falhou:', erro.message)
  process.exit(1)
})
