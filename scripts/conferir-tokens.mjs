#!/usr/bin/env node
// Trava dos tokens de desenho do rebobina-ios.
//
// Confere tres coisas e so tres:
//   1. o sha256 de design/tokens/rebobina-tokens.json casa com rebobina-tokens.sha256;
//   2. o Kotlin commitado em commonMain/ui/tokens/ReboTokensGerado.kt e exatamente o
//      que design/tokens/gerar-kotlin.mjs produz agora;
//   3. o mesmo vale para ReboTextos.kt, que o gerador tambem escreve.
//
// A copia em design/tokens e byte a byte do rebobina-servidor. Se a origem mudar,
// nao adianta editar o Kotlin aqui: copie JSON e sha256 novos, regere e commite.
//
// Uso: node scripts/conferir-tokens.mjs
// Sai 0 quando esta tudo em dia e 1 quando algo divergiu (com o motivo e o comando).

import { createHash } from 'node:crypto'
import { existsSync, mkdirSync, readFileSync } from 'node:fs'
import { dirname, join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'
import { spawnSync } from 'node:child_process'

const AQUI = dirname(fileURLToPath(import.meta.url))
const RAIZ = join(AQUI, '..')
const PASTA = join(RAIZ, 'design', 'tokens')
const PACOTE = 'net.dynv6.rebobina.ui.tokens'
const PACOTE_EM_PASTA = PACOTE.split('.').join('/')

const JSON_DE_TOKENS = join(PASTA, 'rebobina-tokens.json')
const SHA_DE_TOKENS = join(PASTA, 'rebobina-tokens.sha256')
const ORIGEM = join(PASTA, 'ORIGEM.json')
const GERADOR = join(PASTA, 'gerar-kotlin.mjs')

const PASTA_DO_KOTLIN = join(RAIZ, 'composeApp', 'src', 'commonMain', 'kotlin', PACOTE_EM_PASTA)
const CONFERIDOS = [
  { nome: 'ReboTokensGerado.kt', linhaDoGerador: '--saida' },
  { nome: 'ReboTextos.kt', linhaDoGerador: '--saida-textos' },
]
const PASTA_TEMP = join(RAIZ, 'build', 'tokens')

function erro(mensagem) {
  console.error('trava dos tokens: ' + mensagem)
  process.exitCode = 1
}

const obrigatorios = [JSON_DE_TOKENS, SHA_DE_TOKENS, ORIGEM, GERADOR]
for (const arquivo of obrigatorios) {
  if (!existsSync(arquivo)) {
    erro('falta ' + relative(RAIZ, arquivo) + '. A copia de design/tokens esta incompleta.')
  }
}
for (const { nome } of CONFERIDOS) {
  if (!existsSync(join(PASTA_DO_KOTLIN, nome))) {
    erro('falta ' + relative(RAIZ, join(PASTA_DO_KOTLIN, nome)) + '. Gere de novo e commite.')
  }
}
if (process.exitCode) process.exit()

const bytesDoJson = readFileSync(JSON_DE_TOKENS)
const shaDoJson = createHash('sha256').update(bytesDoJson).digest('hex')
const shaEsperado = readFileSync(SHA_DE_TOKENS, 'utf8').trim().split(/\s+/)[0].toLowerCase()
if (shaDoJson !== shaEsperado) {
  erro(
    'o sha256 de design/tokens/rebobina-tokens.json (' +
      shaDoJson +
      ') nao casa com rebobina-tokens.sha256 (' +
      shaEsperado +
      '). A copia esta atrasada ou foi editada a mao: copie o JSON e o sha256 da origem ' +
      '(rebobina-servidor, design/tokens) e regere o Kotlin.',
  )
  process.exit()
}

const versao = (JSON.parse(bytesDoJson.toString('utf8')).meta || {}).versao || '?'
const origem = JSON.parse(readFileSync(ORIGEM, 'utf8'))
if (origem.versao !== versao || origem.sha256 !== shaDoJson) {
  erro(
    'ORIGEM.json nao conta a mesma historia do JSON: versao ' +
      origem.versao +
      ' e sha256 ' +
      String(origem.sha256).slice(0, 12) +
      '..., JSON diz ' +
      versao +
      ' e ' +
      shaDoJson.slice(0, 12) +
      '...',
  )
  process.exit()
}

mkdirSync(PASTA_TEMP, { recursive: true })
const argumentos = ['--pacote', PACOTE]
for (const { nome, linhaDoGerador } of CONFERIDOS) {
  argumentos.push(linhaDoGerador, join(PASTA_TEMP, nome.replace(/\.kt$/, '.conferido.kt')))
}
const geracao = spawnSync(process.execPath, [GERADOR, ...argumentos], {
  cwd: RAIZ,
  encoding: 'utf8',
})
if (geracao.status !== 0) {
  erro(
    'o gerador design/tokens/gerar-kotlin.mjs saiu com codigo ' +
      geracao.status +
      ':\n' +
      ((geracao.stdout || '') + (geracao.stderr || '')).trim(),
  )
  process.exit()
}

const tamanhos = []
for (const { nome } of CONFERIDOS) {
  const geradoAgoraPath = join(PASTA_TEMP, nome.replace(/\.kt$/, '.conferido.kt'))
  const commitadoPath = join(PASTA_DO_KOTLIN, nome)
  const geradoAgora = readFileSync(geradoAgoraPath)
  const geradoCommitado = readFileSync(commitadoPath)
  if (!geradoAgora.equals(geradoCommitado)) {
    const linhasCommitadas = geradoCommitado.toString('utf8').split('\n')
    const linhasNovas = geradoAgora.toString('utf8').split('\n')
    let linha = 0
    while (
      linha < linhasCommitadas.length &&
      linha < linhasNovas.length &&
      linhasCommitadas[linha] === linhasNovas[linha]
    ) {
      linha++
    }
    erro(
      nome +
        ' nao e o que o gerador produz (primeira linha diferente: ' +
        (linha + 1) +
        ').\n  commitado: ' +
        JSON.stringify(linhasCommitadas[linha] ?? '(fim do arquivo)') +
        '\n  gerado:    ' +
        JSON.stringify(linhasNovas[linha] ?? '(fim do arquivo)') +
        '\n  Regere e commite: node design/tokens/gerar-kotlin.mjs --pacote ' +
        PACOTE +
        ' --saida ' +
        relative(RAIZ, join(PASTA_DO_KOTLIN, 'ReboTokensGerado.kt')) +
        ' --saida-textos ' +
        relative(RAIZ, join(PASTA_DO_KOTLIN, 'ReboTextos.kt')),
    )
    process.exit()
  }
  tamanhos.push(nome + ' com ' + geradoAgora.length + ' bytes')
}

console.log(
  'trava dos tokens: ok, versao ' +
    versao +
    ', sha256 ' +
    shaDoJson.slice(0, 12) +
    '..., ' +
    tamanhos.join(' e ') +
    ' iguais ao commitado',
)
