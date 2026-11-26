#!/usr/bin/env node
/**
 * Trava do desenho.
 *
 * Conferido nesta rodada (23/11/2026) contra o codigo de `composeApp/src`:
 *  - nenhum `Color(0x...)`, `Color.Red` ou hexadecimal escrito a mao em tela;
 *  - nenhum `.dp` ou `.sp` literal em tela: espaco, alvo, raio e tipo vem de `ReboTokensGerado.kt`
 *    por `ReboDimensao`, `ReboShape`, `ReboEstilo` e `ReboProporcao`;
 *  - toda tela de `ui/telas` usa `ReboRotulos` (rotulo) e um componente de estado, para nao
 *    inventar texto de estado.
 *
 * Limite declarado: a trava e textual. Ela reprova literal de cor e de tamanho, nao prova que o
 * desenho ficou bonito, nem que o pixel bate com o Android: isso so uma captura de tela prova.
 *
 * Uso: node scripts/conferir-desenho.mjs
 * Sai 0 quando tudo passa, 1 com a lista de arquivo:linha quando algo reprova.
 */
import { readFileSync, readdirSync, statSync } from "node:fs";
import { spawnSync } from "node:child_process";
import { dirname, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const fonte = join(raiz, "composeApp", "src");

const PASTAS_DE_TELA = ["ui/telas"];
const ARQUIVOS_COM_COR_PERMITIDA = new Set([
  "ui/tokens/ReboTokensGerado.kt",
  "ui/base/ReboTokens.kt",
  "ui/base/ReboTema.kt",
  "ui/base/ReboComponentes.kt",
  "ui/base/ReboEstado.kt",
]);
const ARQUIVOS_COM_DP_PERMITIDO = new Set([
  "ui/tokens/ReboTokensGerado.kt",
  "ui/base/ReboTokens.kt",
]);

function arquivosKotlin(pasta) {
  const saida = [];
  for (const nome of readdirSync(pasta)) {
    const caminho = join(pasta, nome);
    const info = statSync(caminho);
    if (info.isDirectory()) saida.push(...arquivosKotlin(caminho));
    else if (nome.endsWith(".kt")) saida.push(caminho);
  }
  return saida;
}

const problemas = [];
const todasAsTelas = [];

for (const arquivo of arquivosKotlin(fonte)) {
  const completo = relative(fonte, arquivo).replaceAll("\\", "/");
  const corte = completo.indexOf("br/app/rebobina/");
  if (corte < 0) continue;
  const relativo = completo.slice(corte + "br/app/rebobina/".length);
  const linhas = readFileSync(arquivo, "utf8").split(/\r?\n/);
  const emTela = PASTAS_DE_TELA.some((p) => relativo.startsWith(p + "/"));
  if (emTela) todasAsTelas.push({ relativo, texto: linhas.join("\n") });

  const gerado = relativo === "ui/tokens/ReboTokensGerado.kt";
  linhas.forEach((linha, indice) => {
    const numero = indice + 1;
    const onde = `${relativo}:${numero}`;
    // Comentario nao desenha: o texto de KDoc pode citar `Color(0x...)` sem ser literal.
    const codigo = linha.replace(/\/\/.*$/, "").trim();
    if (codigo === "" || codigo.startsWith("*") || codigo.startsWith("/*")) return;

    if (!gerado && /Color\(0x/i.test(codigo)) {
      problemas.push(`${onde} usa Color(0x...): cor vem dos tokens gerados.`);
    }
    if (/(?<![A-Za-z0-9_])Color\.(?!Unspecified)/.test(codigo) && !ARQUIVOS_COM_COR_PERMITIDA.has(relativo)) {
      problemas.push(`${onde} usa Color.<nome> fora dos arquivos de token.`);
    }
    if (emTela && /(?<![A-Za-z0-9_])Color\b/.test(codigo.replaceAll(/ReboColor/g, ""))) {
      problemas.push(`${onde} cita Color em tela: use ReboColor/reboCorDoNome.`);
    }
    if (!gerado && /\b0x[0-9A-Fa-f]{6,8}\b/.test(codigo)) {
      problemas.push(`${onde} tem hexadecimal literal de cor.`);
    }
    if (!gerado && /\b\d+(?:\.\d+)?\.(?:dp|sp)\b/.test(codigo) && !ARQUIVOS_COM_DP_PERMITIDO.has(relativo)) {
      problemas.push(`${onde} tem tamanho literal em dp/sp: use ReboDimensao/ReboEstilo.`);
    }
    if (emTela && /\bfontSize\s*=/.test(codigo)) {
      problemas.push(`${onde} escreve fontSize: o tipo vem de ReboEstilo.`);
    }
    if (emTela && /\bMaterialTheme\.(typography|colorScheme)/.test(codigo)) {
      problemas.push(`${onde} le MaterialTheme em vez dos tokens Rebo*.`);
    }
  });
}

if (todasAsTelas.length === 0) {
  problemas.push("ui/telas esta vazio: as cinco telas nao foram encontradas.");
}

for (const tela of todasAsTelas) {
  if (!/ReboRotulos\./.test(tela.texto)) {
    problemas.push(`${tela.relativo} nao usa ReboRotulos: rotulo de tela nao pode ser inventado ali.`);
  }
  // Ajustes nao busca o acervo na abertura: o estado dela e o resultado do "Testar conexao",
  // que mostra o detalhe do servidor em vez de uma frase de estado generica.
  if (!tela.relativo.endsWith("TelaAjustes.kt") && !/ReboEstado(Carregando|Falha|Vazio)/.test(tela.texto)) {
    problemas.push(`${tela.relativo} nao usa componente de estado: texto de estado nao pode ser inventado ali.`);
  }
}

if (problemas.length > 0) {
  console.error("desenho reprovado:");
  for (const problema of problemas) console.error("  " + problema);
  process.exit(1);
}

console.log(`desenho: ok, ${todasAsTelas.length} telas, nenhum literal de cor/tamanho em ui/telas`);

const tokens = spawnSync(process.execPath, [join(raiz, "scripts", "conferir-tokens.mjs")], {
  stdio: "inherit",
  cwd: raiz,
});
if (tokens.status !== 0) {
  console.error("desenho: a trava dos tokens falhou junto.");
  process.exit(tokens.status ?? 1);
}
