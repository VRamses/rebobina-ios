// Conferidor de tokens entre repositorios.
//
// Roda aqui, no repositorio canonico, e confere pela API do GitHub se cada copia
// vendorizada e byte a byte igual ao JSON desta pasta. E o unico jeito de saber
// que os clientes estao sincronizados sem clonar cada um: a trava de build de
// cada cliente prova que ele nao editou a copia que tem, mas nao prova que a
// copia e a atual.
//
// Uso:
//   node design/tokens/conferir.mjs            (falha se algum cliente estiver atras)
//   node design/tokens/conferir.mjs --tolerar  (so relata, exit 0)
//   node design/tokens/conferir.mjs --dono VRamses
//
// O token sai de GITHUB_TOKEN ou do `gh auth token`. Nunca e escrito em lugar
// nenhum: o script so o usa no cabecalho da requisicao.
//
// Desde a 1.0.6 confere tambem o rebobina-textos.json de quem e consumidor de
// CSS (web e webOS): bytes e sha256 contra o do canonico, com linha propria.
// Desde a 1.0.7 o arquivo local de textos e exigido so quando existe: um
// cliente que so recebe Kotlin (iOS e Android) vendoriza este script sem o
// textos, e nele a conferencia segue sem as linhas de texto, com aviso, em vez
// de o script morrer antes de conferir qualquer coisa. Para quem recebe Kotlin
// a checagem de texto continua sendo a versao do JSON vendorizado, porque o
// texto desses clientes sai de ReboTextos.kt gerado no pacote deles.

import { createHash } from 'node:crypto';
import { execFileSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';
import { dirname, join, relative } from 'node:path';
import { fileURLToPath } from 'node:url';

const AQUI = dirname(fileURLToPath(import.meta.url));
const RAIZ = join(AQUI, '..', '..');
const ARQUIVO_JSON = join(AQUI, 'rebobina-tokens.json');
const ARQUIVO_TEXTOS = join(AQUI, 'rebobina-textos.json');

const tolerar = process.argv.includes('--tolerar');
const arg = (nome, padrao) => {
  const i = process.argv.indexOf('--' + nome);
  return i > 0 && process.argv[i + 1] && !process.argv[i + 1].startsWith('--') ? process.argv[i + 1] : padrao;
};

const sha256 = (buffer) => createHash('sha256').update(buffer).digest('hex');

function token() {
  if (process.env.GITHUB_TOKEN) return process.env.GITHUB_TOKEN.trim();
  const base = process.env.LOCALAPPDATA || process.env.HOME || '';
  const candidatos = [
    join(base, 'dsh-tools', 'gh', 'bin', 'gh.exe'),
    'gh'
  ];
  for (const caminho of candidatos) {
    try {
      return execFileSync(caminho, ['auth', 'token'], { encoding: 'utf8' }).trim();
    } catch (e) {
      // tenta o proximo
    }
  }
  return null;
}

const TOKEN = token();
if (!TOKEN) {
  process.stderr.write('erro: sem token do GitHub. Rode `gh auth login` ou exporte GITHUB_TOKEN.\n');
  process.exit(2);
}

const bruto = readFileSync(ARQUIVO_JSON);
const tokens = JSON.parse(bruto.toString('utf8'));
const meta = tokens.meta || {};
const canonico = tokens.canonico || {};
const MEU_SHA = sha256(bruto);
const DONO = arg('dono', 'VRamses');

// O texto de tela de web e webOS sai daqui, do mesmo JSON, e cada consumidor
// commitou uma copia. Sem o arquivo no canonico nao ha o que comparar, mas num
// cliente que so recebe Kotlin ele nao existe de proposito: ai a conferencia de
// texto remoto fica de fora, com aviso, em vez de morrer na primeira linha.
const TEM_TEXTOS_LOCAL = existsSync(ARQUIVO_TEXTOS);
let TEXTO_SHA = null;
let TEXTO_TAMANHO = null;
if (!TEM_TEXTOS_LOCAL) {
  process.stdout.write('aviso: sem rebobina-textos.json ao lado deste script; a conferencia de ' +
    'texto remoto fica de fora nesta rodada\n');
} else {
  const TEXTO_BRUTO = readFileSync(ARQUIVO_TEXTOS);
  TEXTO_SHA = sha256(TEXTO_BRUTO);
  TEXTO_TAMANHO = TEXTO_BRUTO.length;

  // o gerado de texto carrega o sha256 do JSON na cabeca: se nao casar, o texto
  // esta velho em relacao ao desenho e o resto da conferencia seria enganoso.
  try {
    const cabeca = JSON.parse(TEXTO_BRUTO.toString('utf8'));
    if (cabeca.sha256DoJson && cabeca.sha256DoJson !== MEU_SHA) {
      process.stderr.write('erro: ' + relative(RAIZ, ARQUIVO_TEXTOS) + ' foi gerado de outro JSON (' +
        cabeca.sha256DoJson + '); regenere com o gerar.mjs antes de conferir\n');
      process.exit(2);
    }
  } catch (e) {
    process.stderr.write('erro: ' + relative(RAIZ, ARQUIVO_TEXTOS) + ' nao e JSON valido\n');
    process.exit(2);
  }
}

const CLIENTES_CSS = (tokens.consumidores && tokens.consumidores.css && tokens.consumidores.css.clientes) || [];

async function conteudo(repositorio, caminho, branch) {
  const url = 'https://api.github.com/repos/' + DONO + '/' + repositorio + '/contents/' + caminho +
    '?ref=' + encodeURIComponent(branch);
  const resposta = await fetch(url, {
    headers: {
      Authorization: 'Bearer ' + TOKEN,
      Accept: 'application/vnd.github+json',
      'X-GitHub-Api-Version': '2022-11-28',
      'User-Agent': 'rebobina-conferir-tokens'
    }
  });
  if (resposta.status === 404) return { estado: 'ausente' };
  if (!resposta.ok) {
    const corpo = await resposta.text();
    return { estado: 'erro', detalhe: resposta.status + ' ' + corpo.slice(0, 200) };
  }
  const dados = await resposta.json();
  const bytes = Buffer.from(String(dados.content || '').replace(/\n/g, ''), 'base64');
  return { estado: 'ok', bytes: bytes, gitSha: dados.sha };
}

const clientes = (canonico.clientesVendorizados || []).slice();

if (!clientes.length) {
  process.stderr.write('erro: canonico.clientesVendorizados vazio no JSON\n');
  process.exit(2);
}

const linhas = [];
let problemas = 0;

for (const cliente of clientes) {
  const caminho = cliente.caminho || canonico.arquivoDeOrigem?.nome?.replace('ORIGEM.json', 'rebobina-tokens.json');
  const copia = await conteudo(cliente.repositorio, caminho, cliente.branch);
  if (copia.estado === 'ausente') {
    if (cliente.existe === false) {
      linhas.push({ repositorio: cliente.repositorio, branch: cliente.branch, situacao: 'declarado sem copia', ok: true });
    } else {
      linhas.push({ repositorio: cliente.repositorio, branch: cliente.branch, situacao: 'AUSENTE mas declarado existente', ok: false });
      problemas++;
    }
    continue;
  }
  if (copia.estado === 'erro') {
    linhas.push({ repositorio: cliente.repositorio, branch: cliente.branch, situacao: 'ERRO ' + copia.detalhe, ok: false });
    problemas++;
    continue;
  }

  const shaCopia = sha256(copia.bytes);
  let situacao;
  let ok = true;
  if (shaCopia === MEU_SHA) {
    situacao = 'em dia';
  } else {
    let versaoCopia = '?';
    try {
      versaoCopia = JSON.parse(copia.bytes.toString('utf8')).meta?.versao || '?';
    } catch (e) {
      // copia ilegivel conta como divergencia
    }
    situacao = 'ATRASADO (copia ' + versaoCopia + ', canonico ' + (meta.versao || '?') + ')';
    ok = false;
    problemas++;
  }

  // a copia tem de saber de onde veio
  const origem = await conteudo(cliente.repositorio, 'design/tokens/ORIGEM.json', cliente.branch);
  if (origem.estado === 'ok') {
    try {
      const o = JSON.parse(origem.bytes.toString('utf8'));
      if (o.sha256 && o.sha256 !== shaCopia) {
        situacao += '; ORIGEM.json aponta outro sha256';
        ok = false;
        problemas++;
      }
      if (o.versao && o.versao !== (meta.versao || '?')) {
        situacao += '; ORIGEM.json diz versao ' + o.versao;
      }
    } catch (e) {
      situacao += '; ORIGEM.json invalido';
      ok = false;
      problemas++;
    }
  } else if (origem.estado === 'ausente') {
    situacao += '; sem ORIGEM.json';
    ok = false;
    problemas++;
  }

  linhas.push({ repositorio: cliente.repositorio, branch: cliente.branch, situacao: situacao, ok: ok });

  // quem consome CSS tem tambem a copia do texto de tela, ao lado do JSON
  const cobre = cliente.cobre || [];
  if (cobre.some((c) => CLIENTES_CSS.includes(c))) {
    if (!TEM_TEXTOS_LOCAL) {
      linhas.push({
        repositorio: cliente.repositorio,
        branch: cliente.branch,
        situacao: 'textos nao conferido (sem copia local do canonico)',
        ok: true
      });
    } else {
      const pasta = dirname(caminho);
      const caminhoTextos = (pasta === '.' ? 'design/tokens' : pasta) + '/rebobina-textos.json';
      const textosCopia = await conteudo(cliente.repositorio, caminhoTextos, cliente.branch);
      let situacaoTextos;
      let okTextos = true;
      if (textosCopia.estado === 'ausente') {
        situacaoTextos = 'sem textos';
        okTextos = false;
        problemas++;
      } else if (textosCopia.estado === 'erro') {
        situacaoTextos = 'textos ERRO ' + textosCopia.detalhe;
        okTextos = false;
        problemas++;
      } else {
        const shaTextosCopia = sha256(textosCopia.bytes);
        if (shaTextosCopia === TEXTO_SHA) {
          situacaoTextos = 'textos em dia (' + textosCopia.bytes.length + ' B)';
        } else {
          situacaoTextos = 'textos ATRASADO (' + textosCopia.bytes.length + ' B, canonico ' + TEXTO_TAMANHO + ' B)';
          okTextos = false;
          problemas++;
        }
      }
      linhas.push({ repositorio: cliente.repositorio, branch: cliente.branch, situacao: situacaoTextos, ok: okTextos });
    }
  }
}

// relatorio
const largura = Math.max(...linhas.map((l) => l.repositorio.length), 10);
process.stdout.write('canonico: ' + relative(RAIZ, ARQUIVO_JSON) + ' versao ' + (meta.versao || '?') +
  ', sha256 ' + MEU_SHA + '\n');
process.stdout.write('dono: ' + DONO + ' | conferido pela API do GitHub em ' + new Date().toISOString() + '\n\n');
for (const l of linhas) {
  process.stdout.write('  ' + (l.ok ? 'ok   ' : 'FALHA') + '  ' + l.repositorio.padEnd(largura) + '  ' +
    String(l.branch).padEnd(16) + '  ' + l.situacao + '\n');
}
process.stdout.write('\n' + (problemas === 0
  ? 'todas as copias em dia com o canonico\n'
  : problemas + ' divergencia(s)\n'));

if (problemas > 0 && !tolerar) process.exit(1);
