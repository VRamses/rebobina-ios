// Gerador do Kotlin de tokens do Rebobina.
//
// Le apenas o JSON vendorizado ao lado deste arquivo e escreve o arquivo Kotlin
// da convencao `geracao.kotlin` (rebobina-tokens.json, versao 1.0.4). Vale para
// Android (Compose) e para iOS (Compose Multiplatform), que compartilham a mesma
// linguagem de desenho.
//
// Uso:
//   node design/tokens/gerar-kotlin.mjs
//   node design/tokens/gerar-kotlin.mjs --pacote rebobina.android.ui.base.rebo \
//        --saida app/src/main/java/rebobina/android/ui/base/rebo/ReboTokensGerado.kt
//   node design/tokens/gerar-kotlin.mjs --prefixo ReboGerado
//
// Opcoes:
//   --pacote <pacote>   pacote do arquivo gerado (padrao: geracao.kotlin.pacote do JSON)
//   --saida <caminho>   arquivo de saida (padrao: design/tokens/ReboTokensGerado.kt)
//   --prefixo <nome>    prefixo dos objetos (padrao: geracao.kotlin.prefixoObjeto, "Rebo")
//   --indent <texto>    "tab" para tabulacao, senao 4 espacos
//   --sem-trava         nao confere rebobina-tokens.sha256 (nao use no build)

import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const AQUI = dirname(fileURLToPath(import.meta.url));
const RAIZ = join(AQUI, '..', '..');
const ARQUIVO_JSON = join(AQUI, 'rebobina-tokens.json');
const ARQUIVO_SHA = join(AQUI, 'rebobina-tokens.sha256');
const ARQUIVO_ORIGEM = join(AQUI, 'ORIGEM.json');

function argumento(nome, padrao) {
  const i = process.argv.indexOf('--' + nome);
  return i > 0 && process.argv[i + 1] && !process.argv[i + 1].startsWith('--') ? process.argv[i + 1] : padrao;
}

function bandeira(nome) {
  return process.argv.includes('--' + nome);
}

function falhar(mensagem) {
  process.stderr.write('erro: ' + mensagem + '\n');
  process.exit(1);
}

const sha256 = (t) => createHash('sha256').update(t, 'utf8').digest('hex');

// ---- trava ----
const brutoJson = readFileSync(ARQUIVO_JSON, 'utf8');
const real = sha256(brutoJson);
if (!bandeira('sem-trava')) {
  if (!existsSync(ARQUIVO_SHA)) falhar('falta ' + relative(RAIZ, ARQUIVO_SHA) + ', copie da origem junto com o JSON');
  const esperado = readFileSync(ARQUIVO_SHA, 'utf8').trim().split(/\s+/)[0].toLowerCase();
  if (esperado !== real) {
    falhar('o token deste cliente esta atrasado: o sha256 do JSON (' + real + ') nao casa com ' +
      relative(RAIZ, ARQUIVO_SHA) + ' (' + esperado + '). Copie o JSON e o sha256 da origem.');
  }
}

const tokens = JSON.parse(brutoJson);
const meta = tokens.meta || {};
const geracao = tokens.geracao || {};
const specKotlin = geracao.kotlin || {};

const PACOTE = argumento('pacote', specKotlin.pacote);
const SAIDA = argumento('saida', join(AQUI, specKotlin.arquivo || 'ReboTokensGerado.kt'));
const PREFIXO = argumento('prefixo', specKotlin.prefixoObjeto || 'Rebo');
const IND = argumento('indent', ' ') === 'tab' ? '\t' : '    ';

if (!PACOTE) falhar('sem pacote: passe --pacote ou declare geracao.kotlin.pacote');

// ORIGEM.json, quando existir, tem de contar a mesma historia do JSON
if (existsSync(ARQUIVO_ORIGEM)) {
  let origem;
  try {
    origem = JSON.parse(readFileSync(ARQUIVO_ORIGEM, 'utf8'));
  } catch (e) {
    falhar('design/tokens/ORIGEM.json invalido: ' + e.message);
  }
  if (origem.versao && origem.versao !== meta.versao) {
    falhar('versao do JSON (' + (meta.versao || '?') + ') nao casa com ORIGEM.json (' + origem.versao + ')');
  }
  if (origem.sha256 && origem.sha256 !== real) {
    falhar('sha256 do JSON (' + real + ') nao casa com ORIGEM.json (' + origem.sha256 + ')');
  }
}

// ---- nomes ----

const pascal = (nome) =>
  String(nome)
    .replace(/[^A-Za-z0-9]+/g, ' ')
    .split(' ')
    .filter(Boolean)
    .map((p) => (/^[0-9]/.test(p) ? 'N' + p : p.charAt(0).toUpperCase() + p.slice(1)))
    .join('');

const numero = (v) => String(Math.round(v * 1e6) / 1e6);
const dp = (v) => numero(v) + '.dp';
const sp = (v) => numero(v) + '.sp';

// alfa de 0 a 1 vira o byte AA de Color(0xAARRGGBB)
function corComAlfa(cor, alfa) {
  const hex = String(cor).replace('#', '').toUpperCase();
  if (hex.length !== 6) falhar('cor nao suportada: ' + cor);
  const a = Math.round(Math.min(Math.max(alfa, 0), 1) * 255)
    .toString(16)
    .toUpperCase()
    .padStart(2, '0');
  return 'Color(0x' + a + hex + ')';
}

// um grupo de cor vira object com uma val por chave; chave que tem {cor, alfa} viranuma cor com alfa
function grupoDeCor(nomeObjeto, grupo, comentario) {
  const linhas = ['object ' + nomeObjeto + ' {'];
  for (const chave of Object.keys(grupo)) {
    const v = grupo[chave];
    if (v && typeof v === 'object') {
      linhas.push(IND + 'val ' + pascal(chave) + ' = ' + corComAlfa(v.cor, typeof v.alfa === 'number' ? v.alfa : 1));
    } else if (typeof v === 'string') {
      linhas.push(IND + 'val ' + pascal(chave) + ' = Color(0xFF' + v.replace('#', '').toUpperCase() + ')');
    }
  }
  linhas.push('}');
  return [comentario, ...linhas].join('\n');
}

// escala tipografica: TextStyle com tamanho, entrelinha, peso e espacamento
function grupoDeTipo(nomeObjeto, escala, comentario) {
  const linhas = ['object ' + nomeObjeto + ' {'];
  for (const papel of Object.keys(escala)) {
    const t = escala[papel];
    linhas.push(IND + 'val ' + pascal(papel) + ' = TextStyle(');
    linhas.push(IND + IND + 'fontSize = ' + sp(t.tamanho) + ',');
    linhas.push(IND + IND + 'lineHeight = ' + sp(t.entrelinha) + ',');
    linhas.push(IND + IND + 'fontWeight = FontWeight(' + t.peso + '),');
    linhas.push(IND + IND + 'letterSpacing = ' + sp(t.espacamento || 0) + ',');
    linhas.push(IND + ')');
  }
  linhas.push('}');
  return [comentario, ...linhas].join('\n');
}

function grupoSimples(nomeObjeto, objeto, valor, comentario) {
  const linhas = ['object ' + nomeObjeto + ' {'];
  for (const chave of Object.keys(objeto)) {
    if (/^motivo$/.test(chave)) continue;
    const v = objeto[chave];
    if (v === null || typeof v === 'object') continue;
    linhas.push(IND + 'val ' + pascal(chave) + ' = ' + valor(v));
  }
  linhas.push('}');
  return [comentario, ...linhas].join('\n');
}

const blocos = [];

// cor
blocos.push(grupoDeCor(PREFIXO + 'Color', tokens.cor.escuro, '// cor, tema escuro (cor.escuro)'));
if (tokens.cor.claro) blocos.push(grupoDeCor(PREFIXO + 'ColorClaro', tokens.cor.claro, '// cor, tema claro (cor.claro)'));

// tipo
blocos.push(grupoDeTipo(PREFIXO + 'Type', tokens.tipo.escala, '// tipo.escala, na densidade de referencia'));
if (tokens.tipo.escalaCelular) {
  blocos.push(grupoDeTipo(PREFIXO + 'TypeCelular', tokens.tipo.escalaCelular, '// tipo.escalaCelular, o mesmo papel com medida de celular'));
}
if (tokens.tipo.peso) {
  const p = ['object ' + PREFIXO + 'Peso {'];
  for (const chave of Object.keys(tokens.tipo.peso)) {
    p.push(IND + 'val ' + pascal(chave) + ' = FontWeight(' + tokens.tipo.peso[chave] + ')');
  }
  p.push('}');
  blocos.push('// tipo.peso\n' + p.join('\n'));
}
if (tokens.tipo.pisoLeitura) {
  const p = ['object ' + PREFIXO + 'TypePiso {'];
  for (const chave of Object.keys(tokens.tipo.pisoLeitura)) {
    p.push(IND + 'val ' + pascal(chave) + ' = ' + sp(tokens.tipo.pisoLeitura[chave]));
  }
  p.push('}');
  blocos.push('// tipo.pisoLeitura: menor corpo de texto legivel por plataforma\n' + p.join('\n'));
}
if (tokens.tipo.numeros) {
  const n = tokens.tipo.numeros;
  blocos.push([
    '// tipo.numeros: algarismo tabular da sans, sem segunda familia',
    'object ' + PREFIXO + 'TypeNumeros {',
    IND + 'val Estilo = TextStyle(fontFeatureSettings = "' + (n.configuracao || 'tnum') + '")',
    '}'
  ].join('\n'));
}

// espaco, raio, movimento
blocos.push(grupoSimples(PREFIXO + 'Space', tokens.espaco.degraus, dp, '// espaco.degraus, em dp'));
if (tokens.espaco.uso) blocos.push(grupoSimples(PREFIXO + 'SpaceUso', tokens.espaco.uso, dp, '// espaco.uso: para que serve cada degrau'));
blocos.push(grupoSimples(PREFIXO + 'Radius', tokens.raio, dp, '// raio, em dp'));
blocos.push(grupoSimples(PREFIXO + 'Motion', tokens.movimento, (v) => String(v), '// movimento, em milissegundos'));

// estado
if (tokens.estado && tokens.estado.carregando) {
  const c = tokens.estado.carregando;
  blocos.push([
    '// estado.carregando: quando o esqueleto e o texto entram',
    'object ' + PREFIXO + 'EstadoCarregando {',
    IND + 'val EsqueletoAposMs = ' + c.esqueletoAposMs,
    IND + 'val TextoAposMs = ' + c.textoAposMs,
    '}'
  ].join('\n'));
}

// plataforma: alvo, area segura, foco e as flags; grade separada
const plataformas = tokens.plataforma || {};
const nomesPlataforma = Object.keys(plataformas);
const size = ['object ' + PREFIXO + 'Size {'];
const grade = ['object ' + PREFIXO + 'Grade {'];
for (const nome of nomesPlataforma) {
  const p = plataformas[nome];
  size.push(IND + 'object ' + pascal(nome) + ' {');
  const alvo = p.alvo || {};
  for (const chave of Object.keys(alvo)) {
    size.push(IND + IND + 'val Alvo' + pascal(chave) + ' = ' + dp(alvo[chave]));
  }
  const area = p.areaSegura || {};
  for (const chave of Object.keys(area)) {
    size.push(IND + IND + 'val AreaSegura' + pascal(chave) + ' = ' + dp(area[chave]));
  }
  const foco = p.foco || {};
  for (const chave of Object.keys(foco)) {
    size.push(IND + IND + 'val Foco' + pascal(chave) + ' = ' + numero(foco[chave]) + 'f');
  }
  for (const flag of ['relogio', 'ponteiro', 'hover']) {
    if (typeof p[flag] === 'boolean') size.push(IND + IND + 'val ' + pascal(flag) + ' = ' + p[flag]);
  }
  size.push(IND + '}');

  if (p.grade) {
    grade.push(IND + 'object ' + pascal(nome) + ' {');
    for (const chave of Object.keys(p.grade)) {
      grade.push(IND + IND + 'val ' + pascal(chave) + ' = ' + p.grade[chave]);
    }
    grade.push(IND + '}');
  }
}
size.push('}');
grade.push('}');
blocos.push('// plataforma.<nome>: alvo de toque, area segura, foco e as tres flags\n' + size.join('\n'));
blocos.push('// plataforma.<nome>.grade: colunas por plataforma\n' + grade.join('\n'));

// ---- cabecalho e escrita ----

const cabecalho = [
  '// GERADO A PARTIR DE design/tokens/rebobina-tokens.json. NAO EDITAR A MAO.',
  '// fonte: ' + (meta.nome || 'rebobina-tokens') + ' ' + (meta.versao || '?') + ', ' + (meta.data || '?') + ', ' + (meta.autor || '?') + '.',
  '// sha256 do JSON: ' + real,
  '// canonico: ' + ((tokens.canonico || {}).repositorio || 'rebobina-servidor') + ' ' +
    ((tokens.canonico || {}).branch || 'main') + ' ' + ((tokens.canonico || {}).caminho || 'design/tokens/rebobina-tokens.json') + '.',
  '// familia: ' + ((((tokens.tipo || {}).familia || {}).pilha) || []).join(', ') + '.',
  '// Sem fonte propria no repositorio (tipo.familia.propria = false): em Compose a pilha do',
  '// sistema e FontFamily.Default, entao nao existe constante de familia aqui.',
  '// Gerado por design/tokens/gerar-kotlin.mjs. Para mudar valor, mude o JSON na origem.',
  ''
].join('\n');

const corpo = [
  'package ' + PACOTE,
  '',
  'import androidx.compose.ui.graphics.Color',
  'import androidx.compose.ui.text.TextStyle',
  'import androidx.compose.ui.text.font.FontWeight',
  'import androidx.compose.ui.unit.dp',
  'import androidx.compose.ui.unit.sp',
  '',
  cabecalho,
  blocos.join('\n\n'),
  ''
].join('\n');

mkdirSync(dirname(SAIDA), { recursive: true });
writeFileSync(SAIDA, corpo, 'utf8');

const objetos = blocos.length;
process.stdout.write('kotlin: ' + relative(RAIZ, SAIDA) + ' com ' + objetos + ' objetos, pacote ' + PACOTE +
  ', prefixo ' + PREFIXO + '\n');
process.stdout.write('fonte: ' + (meta.nome || '?') + ' ' + (meta.versao || '?') + ', sha256 ' + real + '\n');
