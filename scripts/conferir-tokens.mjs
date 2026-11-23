#!/usr/bin/env node
// Trava dos tokens de desenho do rebobina-ios.
//
// Confere duas coisas e so duas:
//   1. o sha256 de design/tokens/rebobina-tokens.json casa com rebobina-tokens.sha256;
//   2. o Kotlin commitado em commonMain/ui/tokens/ReboTokensGerado.kt e exatamente o
//      que design/tokens/gerar-kotlin.mjs produz agora.
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
const JSON_DE_TOKENS = join(PASTA, 'rebobina-tokens.json')
const SHA_DE_TOKENS = join(PASTA, 'rebobina-tokens.sha256')
const ORIGEM = join(PASTA, 'ORIGEM.json')
const GERADOR = join(PASTA, 'gerar-kotlin.mjs')
const GERADO = join(
  RAIZ,
  'composeApp',
  'src',
  'commonMain',
  'kotlin',
  'net',
  'dynv6',
  'rebobina',
  'ui',
  'tokens',
  'ReboTokensGerado.kt',
)
const TEMP = join(RAIZ, 'build', 'tokens', 'ReboTokensGerado.conferido.kt')

function erro(mensagem) {
  console.error('trava dos tokens: ' + mensagem)
  process.exitCode = 1
}

for (const arquivo of [JSON_DE_TOKENS, SHA_DE_TOKENS, ORIGEM, GERADOR, GERADO]) {
  if (!existsSync(arquivo)) {
    erro('falta ' + relative(RAIZ, arquivo) + '. A copia de design/tokens esta incompleta.')
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

mkdirSync(dirname(TEMP), { recursive: true })
const geracao = spawnSync(process.execPath, [GERADOR, '--pacote', PACOTE, '--saida', TEMP], {
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

const geradoAgora = readFileSync(TEMP)
const geradoCommitado = readFileSync(GERADO)
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
    'ReboTokensGerado.kt nao e o que o gerador produz (primeira linha diferente: ' +
      (linha + 1) +
      ').\n  commitado: ' +
      JSON.stringify(linhasCommitadas[linha] ?? '(fim do arquivo)') +
      '\n  gerado:    ' +
      JSON.stringify(linhasNovas[linha] ?? '(fim do arquivo)') +
      '\n  Regere e commite: node design/tokens/gerar-kotlin.mjs --pacote ' +
      PACOTE +
      ' --saida ' +
      relative(RAIZ, GERADO),
  )
  process.exit()
}

console.log(
  'trava dos tokens: ok, sha256 ' +
    shaDoJson.slice(0, 12) +
    '..., ' +
    geradoAgora.length +
    ' bytes gerados iguais ao commitado',
)
