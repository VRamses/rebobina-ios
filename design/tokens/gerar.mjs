// Gerador do CSS de tokens do Rebobina.
//
// Le apenas o JSON vendorizado ao lado deste arquivo e escreve
// rebobina-tokens.css na mesma pasta. Regras da receita em
// TOKENS-COMO-GERAR.md: prefixo --rebo-, grupo "cor" omitido do nome final,
// tema claro em [data-rebo-tema="claro"] com os mesmos nomes, cor com alfa em
// rgba(), tipo em rem com base 16, densidade multiplicada por
// plataforma.<id>.fator e a trava de sha256 conferida antes de escrever.
//
// A folha e so estilo. Texto de estado e de selo sai em rebobina-textos.json, na
// mesma pasta, gerado por este script com o mesmo sha256 na cabeca: a fala das
// seis plataformas vem do JSON unico, nunca do cliente.
//
// Uso: node design/tokens/gerar.mjs [--plataforma webos|tv|web|desktop|celular|ios]
//        [--saida rebobina-tokens.css] [--textos rebobina-textos.json]

import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const AQUI = dirname(fileURLToPath(import.meta.url));
const RAIZ = join(AQUI, '..', '..');
const ARQUIVO_JSON = join(AQUI, 'rebobina-tokens.json');
const ARQUIVO_SHA = join(AQUI, 'rebobina-tokens.sha256');
const ARQUIVO_ORIGEM = join(AQUI, 'ORIGEM.json');
// a saida e o nome do arquivo ao lado deste script por padrao; --saida troca,
// porque este repositorio guarda a folha da web e a do webOS lado a lado
const ARQUIVO_CSS = join(AQUI, argumento('saida', 'rebobina-tokens.css'));
const ARQUIVO_TEXTOS = join(AQUI, argumento('textos', 'rebobina-textos.json'));
const BASE_REM = 16;

function argumento(nome, padrao) {
  var i = process.argv.indexOf('--' + nome);
  return i > 0 && process.argv[i + 1] ? process.argv[i + 1] : padrao;
}

const PLATAFORMA = argumento('plataforma', 'webos');

function falhar(mensagem) {
  process.stderr.write('erro: ' + mensagem + '\n');
  process.exit(1);
}

function texto(caminho) {
  return readFileSync(caminho, 'utf8');
}

function sha256(t) {
  return createHash('sha256').update(t, 'utf8').digest('hex');
}

// ---- trava de hash ----

const brutoJson = texto(ARQUIVO_JSON);
let brutoSha;
try {
  brutoSha = texto(ARQUIVO_SHA);
} catch (e) {
  falhar('falta ' + relative(RAIZ, ARQUIVO_SHA) + ', copie da origem junto com o JSON');
}
const esperado = brutoSha.trim().split(/\s+/)[0].toLowerCase();
const real = sha256(brutoJson);
if (esperado !== real) {
  falhar('o token deste cliente esta atrasado, copie o JSON e o sha256 da origem (sha256 do arquivo: ' + real + ')');
}

const tokens = JSON.parse(brutoJson);
const meta = tokens.meta || {};

// ---- trava de versao e de origem (receita, item 15) ----
// O JSON vendorizado e o ORIGEM.json tem de contar a mesma historia: mesma
// versao e mesmo sha256. Sem isso a copia e de uma versao e a origem de outra.
if (existsSync(ARQUIVO_ORIGEM)) {
  let origem;
  try {
    origem = JSON.parse(texto(ARQUIVO_ORIGEM));
  } catch (e) {
    falhar('design/tokens/ORIGEM.json invalido: ' + e.message);
  }
  if (origem.versao !== meta.versao) {
    falhar('versao do JSON (' + (meta.versao || '?') + ') nao casa com design/tokens/ORIGEM.json (' +
      (origem.versao || '?') + '), atualize a origem junto com a copia');
  }
  if (origem.sha256 !== real) {
    falhar('sha256 do JSON (' + real + ') nao casa com design/tokens/ORIGEM.json (' +
      (origem.sha256 || '?') + ')');
  }
}

const plataformas = tokens.plataforma || {};
const plataforma = plataformas[PLATAFORMA];
if (!plataforma) falhar('plataforma desconhecida: ' + PLATAFORMA);
const fator = typeof plataforma.fator === 'number' ? plataforma.fator : 1;
// plataforma.<nome>.escalaTipo aponta a escala: principal usa tipo.escala, celular
// usa tipo.escalaCelular. O nome das variaveis nao muda com a escolha.
const NOME_ESCALA = plataforma.escalaTipo === 'celular' ? 'escalaCelular' : 'escala';

// ---- nomes e valores ----

function kebab(nome) {
  return String(nome)
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1-$2')
    .toLowerCase();
}

function numero(valor) {
  if (!isFinite(valor)) return String(valor);
  var arredondado = Math.round(valor * 1e6) / 1e6;
  return String(arredondado);
}

function px(valor) {
  return numero(valor * fator) + 'px';
}

function rem(valor) {
  return numero((valor * fator) / BASE_REM) + 'rem';
}

function em(valor) {
  return numero((valor * fator) / BASE_REM) + 'em';
}

function ms(valor) {
  return numero(valor) + 'ms';
}

function pilha(lista) {
  return lista
    .map(function (fonte) {
      return /^[a-zA-Z-]+$/.test(fonte) ? fonte : '"' + fonte + '"';
    })
    .join(', ');
}

function corDeFolha(folha) {
  if (typeof folha === 'string') return folha;
  if (folha && typeof folha === 'object' && typeof folha.cor === 'string') {
    var m = /^#([0-9a-fA-F]{6})$/.exec(folha.cor);
    if (!m) throw new Error('cor com alfa precisa ser hexadecimal de 6 digitos: ' + folha.cor);
    var n = parseInt(m[1], 16);
    var r = (n >> 16) & 255;
    var g = (n >> 8) & 255;
    var b = n & 255;
    var alfa = typeof folha.alfa === 'number' ? folha.alfa : 1;
    return 'rgba(' + r + ', ' + g + ', ' + b + ', ' + numero(alfa) + ')';
  }
  throw new Error('folha de cor invalida: ' + JSON.stringify(folha));
}

// Nomes: caminho pontuado em kebab-case. O grupo "cor" e o tema saem do nome,
// entao o tema escuro e o claro declaram os mesmos nomes, so que em seletores
// diferentes. Fora do grupo cor, o caminho entra inteiro.
function nomeDaCor(caminho) {
  var partes = caminho.filter(function (p) {
    return p !== 'cor' && p !== 'escuro' && p !== 'claro';
  });
  return '--rebo-' + partes.map(kebab).join('-');
}

function nomeDoCaminho(caminho) {
  return '--rebo-' + caminho.map(kebab).join('-');
}

function corComAlfa(folha) {
  return folha && typeof folha === 'object';
}

function declaracoesDeCor(tema) {
  var grupo = (tokens.cor || {})[tema] || {};
  var linhas = [];
  Object.keys(grupo).forEach(function (folha) {
    linhas.push([nomeDaCor(['cor', tema, folha]), corDeFolha(grupo[folha])]);
  });
  return linhas;
}

function declaracoesDeMedida() {
  var linhas = [];

  var tipo = tokens.tipo || {};
  if (tipo.familia && Array.isArray(tipo.familia.pilha)) {
    linhas.push([nomeDoCaminho(['tipo', 'familia', 'pilha']), pilha(tipo.familia.pilha)]);
  }
  Object.keys(tipo.peso || {}).forEach(function (papel) {
    linhas.push([nomeDoCaminho(['tipo', 'peso', papel]), numero(tipo.peso[papel])]);
  });
  Object.keys(tipo.pisos || {}).forEach(function (papel) {
    linhas.push([nomeDoCaminho(['tipo', 'pisos', papel]), rem(tipo.pisos[papel])]);
  });
  var escalaEscolhida = tipo[NOME_ESCALA] || tipo.escala || {};
  Object.keys(escalaEscolhida).forEach(function (papel) {
    var escala = escalaEscolhida[papel];
    Object.keys(escala).forEach(function (prop) {
      var prefixo = nomeDoCaminho(['tipo', 'escala', papel, prop]);
      var valor = escala[prop];
      if (prop === 'tamanho' || prop === 'entrelinha') linhas.push([prefixo, rem(valor)]);
      else if (prop === 'espacamento') linhas.push([prefixo, em(valor)]);
      else linhas.push([prefixo, numero(valor)]);
    });
  });

  var espaco = tokens.espaco || {};
  (espaco.degraus || []).forEach(function (valor) {
    linhas.push([nomeDoCaminho(['espaco', 'x' + valor]), px(valor)]);
  });
  Object.keys(espaco.uso || {}).forEach(function (uso) {
    linhas.push([nomeDoCaminho(['espaco', 'uso', uso]), px(espaco.uso[uso])]);
  });

  Object.keys(tokens.raio || {}).forEach(function (raio) {
    linhas.push([nomeDoCaminho(['raio', raio]), px(tokens.raio[raio])]);
  });

  Object.keys(tokens.movimento || {}).forEach(function (mov) {
    var valor = tokens.movimento[mov];
    if (typeof valor === 'number') {
      linhas.push([nomeDoCaminho(['movimento', mov]), ms(valor)]);
      return;
    }
    // movimento.reducao e objeto: duracao em ms, escala sem unidade, booleano e
    // texto nao viram variavel.
    Object.keys(valor || {}).forEach(function (prop) {
      var v = valor[prop];
      if (typeof v !== 'number') return;
      var unidade = prop === 'escala' || prop === 'fator' ? numero(v) : ms(v);
      linhas.push([nomeDoCaminho(['movimento', mov, prop]), unidade]);
    });
  });

  Object.keys(tokens.estado || {}).forEach(function (estado) {
    Object.keys(tokens.estado[estado]).forEach(function (prop) {
      var valor = tokens.estado[estado][prop];
      // chave terminada em Ms e tempo; as outras sao texto e saem em rebobina-textos.json
      if (!/Ms$/.test(prop) || typeof valor !== 'number') return;
      linhas.push([nomeDoCaminho(['estado', estado, prop]), ms(valor)]);
    });
  });

  // selo: medida por plataforma, como alvo; o gerador escolhe a linha da plataforma
  var selo = tokens.selo || {};
  ['altura', 'paddingHorizontal'].forEach(function (medida) {
    var mapa = selo[medida] || {};
    if (typeof mapa[PLATAFORMA] === 'number') {
      linhas.push([nomeDoCaminho(['selo', medida]), px(mapa[PLATAFORMA])]);
    }
  });
  if (typeof selo.raio === 'number') linhas.push([nomeDoCaminho(['selo', 'raio']), px(selo.raio)]);

  Object.keys((tokens.icone || {}).tamanho || {}).forEach(function (nome) {
    linhas.push([nomeDoCaminho(['icone', 'tamanho', nome]), px(tokens.icone.tamanho[nome])]);
  });

  Object.keys(tokens.traco || {}).forEach(function (nome) {
    linhas.push([nomeDoCaminho(['traco', nome]), px(tokens.traco[nome])]);
  });

  // A plataforma escolhida vira series medidas, ja com o fator aplicado.
  var prefixoPlataforma = ['plataforma', PLATAFORMA];
  linhas.push([nomeDoCaminho(prefixoPlataforma.concat('fator')), numero(plataforma.fator)]);
  Object.keys(plataforma).forEach(function (chave) {
    var valor = plataforma[chave];
    if (chave === 'fator' || chave === 'unidade') return;
    if (typeof valor === 'boolean' || typeof valor === 'string') return;
    if (typeof valor === 'number') {
      linhas.push([nomeDoCaminho(prefixoPlataforma.concat(chave)), px(valor)]);
      return;
    }
    Object.keys(valor).forEach(function (filho) {
      var prefixo = nomeDoCaminho(prefixoPlataforma.concat([chave, filho]));
      var v = valor[filho];
      if (chave === 'foco') {
        // foco.escala e numero puro; foco.halo e medida em dp, como alvo.anel
        if (filho === 'halo') linhas.push([prefixo, px(v)]);
        else linhas.push([prefixo, numero(v)]);
      } else if (chave === 'grade') linhas.push([prefixo, numero(v)]);
      else linhas.push([prefixo, px(v)]);
    });
  });

  return linhas;
}

// ---- textos ----
// A folha CSS e so estilo. Texto de estado, de selo e de interface e o mesmo nas
// seis plataformas, entao sai num gerado proprio, com o sha256 do JSON na cabeca.
function escreverTextos() {
  var estados = {};
  Object.keys(tokens.estado || {}).forEach(function (nome) {
    var textos = {};
    Object.keys(tokens.estado[nome]).forEach(function (chave) {
      var valor = tokens.estado[nome][chave];
      if (/Ms$/.test(chave)) return;
      if (typeof valor === 'string') textos[chave] = valor;
    });
    if (Object.keys(textos).length) estados[nome] = textos;
  });
  var selo = {};
  Object.keys((tokens.selo || {}).variantes || {}).forEach(function (nome) {
    var v = tokens.selo.variantes[nome];
    if (!v) return;
    selo[nome] = { cor: v.cor, texto: v.texto };
  });
  // interface.<assunto>.<campo> e texto de tela com chave de tres niveis; sai
  // achatado no JSON de texto, porque o cliente le por caminho pontuado.
  var interfaceTexto = {};
  Object.keys(tokens.interface || {}).forEach(function (assunto) {
    var campos = {};
    Object.keys(tokens.interface[assunto] || {}).forEach(function (campo) {
      var valor = tokens.interface[assunto][campo];
      if (typeof valor === 'string') campos[campo] = valor;
    });
    if (Object.keys(campos).length) interfaceTexto[assunto] = campos;
  });
  var conteudo = {
    nome: 'rebobina-textos',
    versao: meta.versao || '?',
    data: meta.data || '?',
    fonte: 'design/tokens/rebobina-tokens.json',
    sha256DoJson: real,
    gerado: 'design/tokens/rebobina-textos.json',
    porque: 'texto de estado, de selo e de interface e o mesmo nas seis plataformas; trocar uma palavra e versao nova no JSON, nunca patch no cliente',
    estado: estados,
    selo: selo,
    interface: interfaceTexto
  };
  mkdirSync(AQUI, { recursive: true });
  writeFileSync(ARQUIVO_TEXTOS, JSON.stringify(conteudo, null, 2) + '\n', 'utf8');  process.stdout.write('textos: ' + relative(RAIZ, ARQUIVO_TEXTOS) + ' com ' +
    Object.keys(estados).length + ' estados, ' + Object.keys(selo).length + ' selos e ' +
    Object.keys(interfaceTexto).length + ' assuntos de interface\n');
}

// ---- escrita ----

function bloco(linhas, recuo) {
  return linhas
    .map(function (par) {
      return recuo + par[0] + ': ' + par[1] + ';';
    })
    .join('\n');
}

const escuras = declaracoesDeCor('escuro');
const claras = declaracoesDeCor('claro');
const medidas = declaracoesDeMedida();

function declaracaoDoNumeros() {
  // tipo.numeros nao vira variavel: vira a classe .rebo-numero, que alinha
  // algarismo na sans do sistema. O texto da declaracao vem do proprio JSON.
  var numeros = (tokens.tipo || {}).numeros;
  if (!numeros || !numeros.como || typeof numeros.como.css !== 'string') return null;
  var partes = numeros.como.css.split(':');
  if (partes.length !== 2) falhar('tipo.numeros.como.css fora do formato "propriedade: valor": ' + numeros.como.css);
  return partes[0].trim() + ': ' + partes[1].trim();
}

const numeros = declaracaoDoNumeros();

const linhas = [];
linhas.push('/* GERADO A PARTIR DE design/tokens/rebobina-tokens.json. NAO EDITAR A MAO.');
linhas.push('   fonte: ' + (meta.nome || 'rebobina-tokens') + ' ' + (meta.versao || '?') +
  ', ' + (meta.data || '?') + ', ' + (meta.autor || '?') + '.');
if (tokens.canonico && tokens.canonico.repositorio) {
  linhas.push('   canonico: ' + tokens.canonico.repositorio + ' ' +
    (tokens.canonico.branch || '?') + ' ' + (tokens.canonico.caminho || '?') + '.');
}
linhas.push('   plataforma: ' + PLATAFORMA + ' (fator ' + numero(fator) + ', unidade ' +
  (plataforma.unidade || '?') + '). Nenhum valor provisorio: se a folha nao existir');
linhas.push('   no JSON, ela nao aparece aqui. */');
linhas.push('');
linhas.push(':root {');
linhas.push('  /* cor, tema escuro */');
linhas.push(bloco(escuras, '  '));
linhas.push(bloco(medidas, '  '));
linhas.push('}');
linhas.push('');
if (claras.length) {
  linhas.push('[data-rebo-tema="claro"] {');
  linhas.push('  /* cor, tema claro, mesmos nomes do tema escuro */');
  linhas.push(bloco(claras, '  '));
  linhas.push('}');
  linhas.push('');
}
if (numeros) {
  linhas.push('/* algarismo tabular da sans, sem segunda familia (tipo.numeros) */');
  linhas.push('.rebo-numero {');
  linhas.push('  ' + numeros + ';');
  linhas.push('}');
  linhas.push('');
}
linhas.push('/* fim. gerado por design/tokens/gerar.mjs */');
linhas.push('');

writeFileSync(ARQUIVO_CSS, linhas.join('\n'), 'utf8');
escreverTextos();

const total = escuras.length + medidas.length + claras.length;
process.stdout.write('tokens: ' + relative(RAIZ, ARQUIVO_CSS) + ' com ' + total +
  ' variaveis (' + escuras.length + ' cor escura, ' + claras.length + ' cor clara, ' +
  medidas.length + ' medidas)\n');
if (numeros) process.stdout.write('algarismo: .rebo-numero com ' + numeros + '\n');
process.stdout.write('fonte: ' + (meta.nome || '?') + ' ' + (meta.versao || '?') +
  ', ' + (meta.data || '?') + ', sha256 ' + real + '\n');
