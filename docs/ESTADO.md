# Estado do Rebobina para iPhone

Última atualização: 23/11/2026. Repositório: <https://github.com/VRamses/rebobina-ios> (privado).

## Se o app não abrir no iPhone, leia isto primeiro

O alvo mínimo do app é **iOS 17.0**, decisão do Ramses em 23/11/2026. O binário é linkado
contra o SDK do iOS 26 (Xcode 26.6 no runner) e referencia a classe `UIViewLayoutRegion`, que
só existe no UIKit do iOS 26, mas essa referência é fraca: no Mach-O do IPA a importação
`_OBJC_CLASS_$_UIViewLayoutRegion` tem `weak_import = 1` e o `LC_BUILD_VERSION` do binário
declara `minos 17.0`. Por isso o dyld de um iPhone com iOS 17 a 25 não falha por esse
símbolo; o Compose 1.11.1 só usa a classe no caminho do iOS 26.

A narração anterior, de que o app provavelmente não abriria em iOS anterior ao 26, era
hipótese minha, não medição: o projeto declarava mínimo 16.0 e o símbolo já saía fraco. Ela
foi substituída pela prova acima, feita no IPA do run #9 (a mesma leitura, no #8, com
`minos 16.0`, deu o mesmo `weak_import = 1`). O que quebrava de verdade era o
link no runner `macos-15` (SDK iOS 18.5), que não tem o stub da classe: erro de build, não
de aparelho.

O que fazer, em três linhas:

1. Confira a versão em **Ajustes > Geral > Sobre > Versão** e o relatório do crash em
   **Ajustes > Privacidade e Segurança > Análises e Dados > Dados de Análises** (procure
   `Rebobina`). No Windows, o mesmo relatório sai por 3uTools ou por
   `idevicecrashreport -u <UDID> -e <pasta>`, se o libimobiledevice estiver instalado. Com Mac,
   use o Console: **Janela > Dispositivos e Simuladores > Abrir Console**.
2. Se a versão do sistema for anterior ao iOS 17, o Sideloadly não instala: o mínimo é 17.0.
   Em iOS 17 a 25 a instalação vale, e o comportamento não foi medido num aparelho.
3. Se a mensagem citar `_OBJC_CLASS_$_UIViewLayoutRegion` como símbolo obrigatório, seria
   contradição com o weak import provado no IPA: me avise em vez de reinstalar.

Resumo: o build do IPA funciona no GitHub Actions e o IPA está publicado como release
`v0.1.0`, com o arquivo renomeado para baixar e instalar. Os tokens de desenho na versão 1.0.8
estão no repositório, com o JSON copiado byte a byte em `design/tokens` e o Kotlin gerado em
`commonMain`, travados no build e no CI. A rodada de 23/11/2026 apagou o `TemaPlaceholder.kt`:
as cinco telas do Android mais Ajustes estão escritas só com os tokens, com as duas faixas de
layout e um cliente Ktor que lê o Jellyfin de verdade, e a trava local do desenho passa. O que
falta é o build desta rodada (run #10) compilar, e depois abrir o app num iPhone de verdade,
que não dá para testar desta máquina.

## O que está provado, com evidência

| # | o que | evidência |
|---|---|---|
| 1 | O Kotlin compila para iOS arm64 | run #3 (id 45094376845), passo "Compilar Kotlin (iOS arm64)" com conclusão `success`, 01:24:45 a 01:25:07, lido em `gh run view 45094376845 --json jobs` |
| 2 | O app linka e arquiva sem assinatura | mesmo run, passo "Xcode archive sem assinatura" `success`, 01:25:07 a 01:27:32 (2m25s), com Xcode 26.6 e SDK iOS 26.5 |
| 3 | O IPA é empacotado e publicado | passo "Publicar IPA" `success`, artefato `rebobina-ios-ipa-3` com 7.527.066 bytes (zip), retenção 14 dias, lido em `gh api repos/VRamses/rebobina-ios/actions/runs/45094376845/artifacts` |
| 4 | O IPA é um app iOS de verdade | baixei o artefato com `gh run download`, abri e conferi: `Payload/Rebobina.app/` com `Info.plist` (1487 bytes), `PkgInfo` e o binário `Rebobina` de 20.486.752 bytes, Mach-O 64 bits arm64 (primeiros bytes `CF FA ED FE`, cputype `0x0100000C`) |
| 5 | O plist do app está correto | do `Info.plist` dentro do IPA: CFBundleDisplayName `Rebobina`, CFBundleIdentifier `net.dynv6.rebobina.ios`, CFBundleShortVersionString `0.1.0`, CFBundleVersion `202611230124`, MinimumOSVersion `16.0`, UIDeviceFamily `[1]`, UIRequiredDeviceCapabilities `[arm64]`, NSAllowsArbitraryLoads `true`, NSAllowsLocalNetworking `true`, NSExceptionDomains `192.168.18.9`, NSBonjourServices `_http._tcp`, DTXcode `2660`, DTSDKName `iphoneos26.5`, DTPlatformVersion `26.5`, BuildMachineOSBuild `25G332` |
| 6 | O código compartilhado entrou no app | as strings `http://192.168.18.9:8096` e `http://192.168.18.9:8899` e os textos da tela provisória aparecem em UTF-16 dentro do binário, ou seja, o `commonMain` e o `RebobinaConfig` foram linkados |
| 7 | O binário não depende de framework privado | 0 ocorrências de `PrivateFrameworks` e de `UIUtilities` no binário; as dylibs citadas são CoreFoundation, CoreGraphics, CoreText, Foundation, IOSurface, Metal, QuartzCore, SwiftUI e UIKit. `UIViewLayoutRegion` aparece como referência de símbolo e vem do UIKit do SDK do iOS 26 |
| 8 | O IPA tem download fácil, fora do Actions | release `v0.1.0` no commit `8e72fa0` (o mesmo do run #3), título "Rebobina iOS 0.1.0 (IPA sem assinatura)", asset `Rebobina-0.1.0-unsigned.ipa` com 7.571.684 bytes (7,22 MiB), sha256 `6fa5f17ff398e5575a735d8ba0270b3e59f7543cd6640fc9231077944ba42ee6`, estado `uploaded`, lido em `gh release view v0.1.0 --json assets` |
| 9 | O asset publicado é exatamente o IPA do run #3 | baixei o artefato do run 45094376845, calculei o sha256 do `Rebobina.ipa` de dentro dele (6fa5f17f...) e publiquei esse arquivo sem recompilar e sem reempacotar |
| 10 | O gatilho por tag também funciona | o push da tag `v0.1.0`, feito pela criação da release, disparou o run #4 (id 45095174609), que terminou `success` em 3m13s e publicou o artefato `rebobina-ios-ipa-4` |
| 11 | Os tokens de desenho são cópia byte a byte da origem 1.0.8 | `design/tokens/rebobina-tokens.json` com 31.413 bytes e sha256 `c39b8fe77bfe82fa57c0a1c5044dc1a16636177e9ed239f34a4641f57077ca98`, igual ao primeiro campo do `rebobina-tokens.sha256` (86 bytes sem newline final, sha256 `36e887ec6de87fd8e151e1f5448d65754e0195e699190a030c660903f3b1fd7f`), copiado do commit `0e06bcea3354b010996545b9016979d012bef3f6` do `rebobina-servidor`, com o clone local naquele commit e `Get-FileHash` nos dois lados; os geradores continuam iguais ao canônico (`gerar.mjs` 14.916 bytes `692db83c...`, `gerar-kotlin.mjs` 17.796 bytes `c9aef04c...` e `conferir.mjs` 9.879 bytes `755d7acd...`), e o `ORIGEM.json` registra repositório, branch, commit, caminho, sha256, versão 1.0.8 e data da cópia. Até 23/11/2026 esta linha dizia 1.0.7; a prova daquela versão fica nos itens 12 e 14 a 16 |
| 12 | O Kotlin gerado é reprodutível e está travado | `node design/tokens/gerar-kotlin.mjs` com o pacote padrão do JSON reproduz byte a byte os arquivos de referência do commit `8367859`: `ReboTokensGerado.kt` com 12.467 bytes e sha256 `23ed806352f78ff4889c8ae678cb5dda9990524cc42cf876ebacb84f1595b4e0`, `ReboTextos.kt` com 1.976 bytes e sha256 `f30e599fb0890fb5fa5c598958ad53da4d9fbb4ada86b503c6258d77860ca9c6`; com `--pacote net.dynv6.rebobina.ui.tokens` saem 12.466 B `1ce69352...` e 1.975 B `999e520f...`, com só a linha do pacote diferente. `node scripts/conferir-tokens.mjs` sai 0 com `trava dos tokens: ok, versao 1.0.7`, e renomear o `object ReboTextos` para `ReboTextosQuebrado` derruba a tarefa `:composeApp:conferirTokens` do Gradle apontando a primeira linha diferente (9) |
| 13 | O IPA do run #7 leva o `commonMain` com os dois gerados | run #7 (id 45107159058), commit `61ddda1cc2671c0ad5195c21f691db8e85c0f4c3`, `success` em 3m36s, artefato `rebobina-ios-ipa-7` com 9.121.993 bytes e digest `sha256:bd5d00744d2af8b3ad1c72db8563a849a23b70044436576aba956df505cffc15`, `Rebobina.ipa` de 9.169.810 bytes e sha256 `ff3f748a43b4b25d54a3a4cd7192c358416968900b839d9a3846d4ce35d54a5e`, binário `Payload/Rebobina.app/Rebobina` de 24.882.120 bytes, Mach-O 64 bits arm64; passos "Compilar Kotlin (iOS arm64)" 12 s e "Xcode archive sem assinatura" 157 s, ambos `success`, e o passo "Prova extra do commonMain no alvo Android" com `ANDROID_COMPILOU=sim` |
| 14 | A 1.0.7 não mexeu em valor de desenho | comparadas as 397 folhas do JSON anterior com as 397 da 1.0.7: 0 removidas, 0 novas e 4 alteradas, todas de prosa e meta (`meta.versao`, `meta.historico`, `canonico.conferidor.oQueConfere` e `geracao.kotlin.convencao`); os dois `.kt` gerados diferem do commit anterior só nas duas linhas de cabeçalho com a versão e o sha256 do JSON, e com a linha do pacote normalizada o corpo é idêntico ao arquivo de referência do canônico |
| 15 | O IPA do run #8 leva os tokens 1.0.7 e o conferidor novo | run #8 (id 45112444059), commit `c6252aaf3b3d3c159e7cbedbdd16befb14f9c2aa`, `success` em 4m01s, artefato `rebobina-ios-ipa-8` com 9.121.987 bytes e digest `sha256:4bea81d045dd947d5c7d7d22a12575611a7a75d9697b7a362592b2f0b09c5afc`, `Rebobina.ipa` de 9.169.810 bytes e sha256 `3d11d6e861942998242743d50518a96628d4beb32dfc622204b885c2f7b4da83`, binário `Payload/Rebobina.app/Rebobina` de 24.882.120 bytes sha256 `42ab179bbb500dfdf577e7c857a8804023068cf0fd869fb229139b7651e55bb6`; mesmo tamanho de IPA e de binário do run #7, com sha diferente porque o binário carrega a data do build; passos "Trava dos tokens (sha256, regeneração e diff)" 1 s, "Compilar Kotlin (iOS arm64)" 12 s e "Xcode archive sem assinatura" 174 s, todos `success`, e "Prova extra do commonMain no alvo Android" com `success` |
| 16 | O conferidor do canônico roda dentro de um cliente Kotlin sem rebobina-textos.json | `node design/tokens/conferir.mjs` dentro deste repositório imprime `aviso: sem rebobina-textos.json ao lado deste script; a conferencia de texto remoto fica de fora nesta rodada`, segue com `canonico: design\tokens\rebobina-tokens.json versao 1.0.7, sha256 93a648069591...` e lista `ok rebobina-ios main em dia`, `ok rebobina-android main em dia` e `ok rebobina-webos main em dia` com exit 0 depois do push; antes do push os três clientes apareciam como `ATRASADO`, com a versão antiga da cópia contra o canônico 1.0.7, e exit 1, que era o esperado. Com o canônico na 1.0.8, o iOS voltou a `ATRASADO` até a reimportação do item 18, como esperado |
| 17 | O alvo mínimo do app é iOS 17.0, provado pelo IPA e não pelo pbxproj | decisão do Ramses em 23/11/2026; `IPHONEOS_DEPLOYMENT_TARGET = 17.0` nos quatro blocos do `iosApp/iosApp.xcodeproj/project.pbxproj` (era 16.0, não 26.0), commit `4e4cbe2`; run #9 (id 45166068336), `success` em 4m32s (272 s), artefato `rebobina-ios-ipa-9` com 9.121.686 bytes e digest `sha256:82fc0c20bd4ad157b0db7780c25a4b91b4bdeef82315d2968079d24f314b07f5`; baixado e aberto: `Payload/Rebobina.app/Info.plist` com `MinimumOSVersion 17.0`, lido com `plistlib` no arquivo do artefato, e o log do passo "Empacotar IPA" mostra a mesma linha; `LC_BUILD_VERSION` do binário com `minos 17.0` e `sdk 26.5`, e `_OBJC_CLASS_$_UIViewLayoutRegion` com `weak_import = 1` na tabela de chained fixups; `Rebobina.ipa` de 9.170.102 bytes e sha256 `bda93e37ce16644688dc7339eddd4e7a88fbbc5227c1b73082080a2dfc35e7fb`, binário de 24.882.120 bytes e sha256 `42510f37c66773fca443374e209b8e6777d7b4a3e53751b605a6d606d060902f`; passos "Trava dos tokens" 1 s, "Compilar Kotlin (iOS arm64)" 10 s e "Xcode archive sem assinatura" 204 s, todos `success` |
| 18 | A reimportação da 1.0.8 no iOS foi provada sem gastar runner macOS | decisão do Lead: o iOS ainda não consome token nenhum, então a prova possível sem cota é a trava local e o conferidor cross-repo; `node scripts/conferir-tokens.mjs` sai 0 com `trava dos tokens: ok, versao 1.0.8, sha256 c39b8fe77bfe..., ReboTokensGerado.kt com 12682 bytes e ReboTextos.kt com 1975 bytes iguais ao commitado`; `node design/tokens/gerar-kotlin.mjs --pacote net.dynv6.rebobina.ui.tokens` gera 12.682 B `37f78ecb...` e 1.975 B `2703fe6a...`, idênticos aos arquivos de referência do canônico (12.683 B e 1.976 B, 1 byte mais longos porque `rebobina.android.ui.base.rebo` tem um caractere a mais que `net.dynv6.rebobina.ui.tokens`) depois de normalizar a linha do pacote; `ReboGrade.Celular` e `ReboGrade.Ios` ganharam `LarguraCartao = 140`, `ColunasCanalVertical = 2` e `ColunasCanalHorizontal = 4`, e nenhum valor existente mudou; commit com `[skip ci]`, nenhum `workflow_dispatch` disparado e nenhum minuto de macOS gasto. Depois do push, `node design/tokens/conferir.mjs` imprime `ok rebobina-ios main em dia`, `ok rebobina-webos main em dia` e só `FALHA rebobina-android main ATRASADO (copia 1.0.7, canonico 1.0.8)` com exit 1, que é a divergência do Android, não do iOS |

## O que NÃO está provado

- **Compilar esta rodada no CI.** No momento em que este documento foi escrito, as cinco
  telas, a camada de desenho e o cliente Ktor ainda não tinham passado por compilação: esta
  máquina não tem JDK nem Gradle, então o `compileKotlinIosArm64` do run #10 é a primeira
  prova. Se ele falhar, o que cai é hipótese de API, não o desenho provado pela trava.
- **Desenhar de verdade na tela.** A trava `scripts/conferir-desenho.mjs` é textual: ela
  reprova `Color(0x...)`, `Color.<nome>`, hexadecimal e `.dp`/`.sp` literal em `ui/telas`,
  exige `ReboRotulos` e componente de estado, e passa nas seis telas. Ela não prova que o
  desenho ficou bonito, nem que o pixel bate com o Android: isso só uma captura de tela prova,
  e não há Mac nem iPhone aqui.
- **Chamadas de rede reais.** O cliente Ktor foi escrito e nunca executado: nenhuma
  requisição ao `http://192.168.18.9:8096` saiu desta máquina, nem `/Users`, nem `/Items`,
  nem `/LiveTv/Channels`. O ATS do `Info.plist` libera HTTP local e não foi exercitado.
- **Decodificar imagem remota.** `Imagens.obter` usa `decodeToImageBitmap` de
  `ui-graphics`; a existência da função no klib do Compose 1.11.1 foi conferida, mas nenhum
  byte de JPEG foi decodificado nesta rodada. Se falhar em execução, o cartão fica com o
  retângulo de `cor.slot`, que é o caminho previsto.
- **Abrir o app num iPhone.** Não existe iPhone nem Mac nesta máquina. O IPA sai sem
  assinatura; quem assina com Apple ID gratuito é o Sideloadly ou o AltStore, no PC do
  Ramses. Se o aparelho estiver em iOS anterior ao 17, o Sideloadly não instala; veja a
  seção do começo deste documento.
- **Instalar por Sideloadly ou AltStore.** O README descreve o caminho, mas nenhuma
  instalação foi executada por mim.
- **Rodar no simulador iOS.** Não há Mac aqui.
- **Rodar em iPhone com iOS 17 a 25.** O alvo mínimo é 17.0 e o símbolo `UIViewLayoutRegion`
  do iOS 26 sai como weak import (prova no item 17 da tabela), mas a abertura num aparelho de
  verdade não foi executada, por falta de aparelho aqui. O comportamento em iOS 17 a 25
  continua não medido.
- **Alvo Android do módulo.** `compileSdk` 36 e `minSdk` 24 configurados, nenhuma tarefa
  Android executada e não existe módulo de launcher Android nesta rodada. O actual Android de
  `criarClienteHttp` usa `HttpClient { }` sem motor declarado e os ajustes ficam em memória:
  não é executável, existe para o módulo compilar.
- **Player e download.** `TelaFicha` mostra `ReboRotulos.PENDENTE_REPRODUTOR` no lugar de
  reproduzir, e `TelaDownloads` é uma fila vazia declarada. Nenhum arquivo é baixado, nem
  tocado.
- **Rótulos e formato fora do token.** `ui/base/ReboRotulos.kt` e o `FormatoDeTexto` de
  `dados/Acervo.kt` são texto escrito à mão. O JSON 1.0.8 tem 488 folhas e só publica texto de
  `estado.*` e `selo.variantes.*`; rótulo de aba, campo, seção, unidade de duração, temporada,
  episódio e tipo de mídia não existem lá. Isso é lacuna declarada do token, não escolha de
  desenho.
- **Tema claro.** `ReboColorClaro` está no gerado e não é usado: esta rodada é escura só.
- **Ícone do app.** Sem `Assets.xcassets` nesta rodada; o app usa o ícone padrão.

## Histórico das execuções

| # | id | commit | resultado | passo que falhou | duração do job | minutos cobrados (fator 10) |
|---|---|---|---|---|---|---|
| 1 | 45092332982 | `0d828ec` | failure | Xcode archive sem assinatura | 238 s (4m58s) | 39 min |
| 2 | 45093782673 | `38dd978` | failure | Xcode archive sem assinatura | 201 s (3m21s) | 33 min |
| 3 | 45094376845 | `8e72fa0` | success | nenhum | 208 s (3m28s) | 34 min |
| 4 | 45095174609 | `8e72fa0` (tag `v0.1.0`) | success | nenhum | 193 s (3m13s) | 32 min |
| 5 | 45101733957 | `83b9db0` | success | nenhum | 261 s (4m21s) | 43 min |
| 6 | 45103365358 | `d3f6969` | success | nenhum | 227 s (3m47s) | 37 min |
| 7 | 45107159058 | `61ddda1` | success | nenhum | 216 s (3m36s) | 36 min |
| 8 | 45112444059 | `c6252aa` | success | nenhum | 241 s (4m01s) | 40 min |
| 9 | 45166068336 | `4e4cbe2` | success | nenhum | 272 s (4m32s) | 45 min |

Total gasto até aqui: a soma real dos nove jobs é 2.057 s = **34 minutos e 17 segundos de
runner macOS**, cobrados como **342,8 minutos** dos 2.000 do mês. Sobram 1.657,2 minutos
cobrados, que dão 165,72 minutos de runner macOS, cerca de 47 builds de 3m30s.

Cuidado com a leitura: os minutos da coluna acima são **minutos cobrados** (duração do job
vezes 10, truncada para baixo em cada run, o que dá 339 no total) e não minutos de runner
macOS. A soma dos resumos de run fica alguns minutos abaixo dos 342,8 cobrados de verdade,
porque cada resumo trunca o próprio número. O eixo da conta está no `README.md`: 2.000
dividido pelo fator 10 dá 200 minutos de runner macOS no mês.

### Run 1: erro exato

Runner `macos-15`, Xcode 16.4, SDK iOS 18.5:

```
ld: warning: Could not find or use auto-linked framework 'UIUtilities': framework 'UIUtilities' not found
Undefined symbols for architecture arm64:
  "_OBJC_CLASS_$_UIViewLayoutRegion", referenced from:
      in ComposeApp[20](CMPLayoutRegion.o)
ld: symbol(s) not found for architecture arm64
clang: error: linker command failed with exit code 1 (use -v to see invocation)
** ARCHIVE FAILED **
```

### Run 2: tentativa de link fraco com o framework privado, também falhou

```
ld: warning: search path '.../iPhoneOS18.5.sdk/System/Library/PrivateFrameworks' not found
ld: framework 'UIUtilities' not found
clang: error: linker command failed with exit code 1 (use -v to see invocation)
** ARCHIVE FAILED **
```

Motivo: o SDK público do Xcode 16.4 não traz stub de framework privado nenhum, então não
havia o que linkar. A tentativa foi revertida no commit `8e72fa0`.

### Causa raiz e correção

O Compose Multiplatform 1.11.1 (`ui-uikit`, objeto `CMPLayoutRegion.o`) chama
`UIViewLayoutRegion`, API de layout do iOS 26. Com SDK anterior ao do iOS 26 o símbolo não
existe no SDK público e o link do app quebra, mesmo com o framework Kotlin compilando bem.
Correção aplicada no commit `8e72fa0` (histórica: o mínimo do app depois foi para 17.0 em
23/11/2026): runner `macos-26` (Xcode 26.6, SDK iOS 26.5), mínimo do app em iOS 16.0 e
`compileSdk`/`targetSdk` do módulo Android em 36, que é a plataforma instalada na imagem do
runner (a `android-37` pura não está).

Em 23/11/2026 o Ramses decidiu o mínimo **iOS 17**, e o alvo foi de 16.0 para 17.0 nos quatro
blocos do `project.pbxproj`. O Compose Multiplatform 1.11.1 continua: o que exigiria trocar de
versão era a hipótese de que o símbolo do iOS 26 fosse obrigatório em tempo de execução, e o
weak import do IPA derrubou essa hipótese. Trocar o Compose só volta à mesa se um aparelho com
iOS 17 falhar citando a classe.

## Suposições visíveis

1. Assumi que o alvo mínimo pedido é exatamente 17.0, e não manter 16.0, porque a decisão do
   Ramses foi "alvo mínimo de iOS 17" e o aceite pede `MinimumOSVersion 17.0`. Se o iPhone
   dele for anterior ao iOS 17, é preciso baixar mais o `IPHONEOS_DEPLOYMENT_TARGET` num
   commit novo.
2. Assumi que o download da release será feito logado na conta `VRamses`, porque o
   repositório é privado.
3. Assumi que o nome do arquivo (`Rebobina-0.1.0-unsigned.ipa`) pode ser diferente do nome
   interno (`Rebobina.ipa`) sem problema: Sideloadly e AltStore leem o arquivo, não o nome.
4. Assumi o valor de orçamento do GitHub para repositório privado: 2.000 minutos cobrados por
   mês, com fator 10 para macOS. A conta de gasto acima é soma das durações reais dos jobs
   vezes 10, sem conferir o medidor de cobrança do GitHub.
5. (Histórica, refutada em 23/11/2026.) Eu tinha assumido que o erro do iOS anterior ao 26
   apareceria na abertura, como símbolo não resolvido, porque o binário referencia a classe do
   UIKit novo. O `weak_import = 1` no Mach-O do IPA mostra que o símbolo é fraco, então o dyld
   não falha; a abertura em aparelho com iOS 17 a 25 segue não testada.
6. A comparação entre versões consecutivas dos tokens foi feita por um script de folhas planas
   (cada caminho de campo virou uma linha) e pelas duas linhas de cabeçalho dos `.kt`. Não
   comparei os dois JSONs linha a linha de texto, então uma mudança de valor que preservasse a
   contagem de folhas apareceria como folha alterada, e não como valor igual.
7. Assumi que o `weak_import` do símbolo é o que o compilador emite quando a classe é mais
   nova que o alvo, e não algo configurado por mim. Li a referência fraca no binário; não li
   na fonte do Compose qual `if #available` protege a chamada.
8. Assumi que `[skip ci]` no assunto não muda nada aqui: o workflow deste repositório só tem
   `workflow_dispatch` e tag, então o push não dispararia build de qualquer forma. O prefixo
   fica como pedido do Lead e como garantia extra de que nenhum run foi gasto.
9. Assumi as versões de dependência sem compilar: Ktor 3.3.3, coroutines 1.10.2,
   serialization 1.9.0, dentro do Kotlin 2.4.10. O cache Gradle desta máquina não tem Ktor, e
   o primeiro download é no runner do run #10.
10. Assumi que `decodeToImageBitmap` de `androidx.compose.ui.graphics` existe no commonMain do
    Compose 1.11.1: conferi a função no klib de `ui-graphics-iosarm64`, não a chamei.
11. Assumi que `safeDrawingPadding()` resolve a área segura no iOS pelo próprio Compose, sem
    `WindowInsets` nativo. Se o build reclamar, o plano é tirar o modificador da raiz e
    declarar a área segura como pendente, não escrever inset à mão.
12. Assumi `PasswordVisualTransformation`, `HttpClientConfig<*>.configurarRebobina()` como
    extensão, `resposta.body<ByteArray>()` e `Aba.entries` sem compilar nenhum deles: são as
    quatro apostas de API que o run #10 testa de uma vez.
13. Assumi que o primeiro usuário de `/Users` é o que o app usa, sem tela de escolha de
    perfil. O Android desta rodada faz o mesmo.
14. Assumi o ATS com `NSAllowsArbitraryLoads` como risco aceito de sideload, porque o
    Jellyfin da casa é HTTP em `192.168.18.9:8096` e não há certificado. Não é recomendação
    para App Store: o IPA é sem assinatura e a chave fica no `NSUserDefaults` do aparelho.
15. Assumi 300 px para capa e 1200 px para fundo porque o JSON não publica tamanho de arte; o
    número está em `dados/Acervo.kt`, num lugar só.
16. Assumi que a igualdade com o Android é de estrutura, não de pixel: os dois lados têm a
    mesma faixa (600/480), as mesmas colunas e os mesmos tokens, mas nenhum dos dois foi
    capturado em tela nesta rodada.

## Como reproduzir a conferência

```
gh run view 45166068336 -R VRamses/rebobina-ios --json conclusion,jobs
gh api repos/VRamses/rebobina-ios/actions/runs/45166068336/artifacts
gh run download 45166068336 -R VRamses/rebobina-ios -n rebobina-ios-ipa-9
gh run view 45112444059 -R VRamses/rebobina-ios --json conclusion,jobs
gh api repos/VRamses/rebobina-ios/actions/runs/45112444059/artifacts
gh run download 45112444059 -R VRamses/rebobina-ios -n rebobina-ios-ipa-8
gh run view 45094376845 -R VRamses/rebobina-ios --json conclusion,jobs
gh run download 45094376845 -R VRamses/rebobina-ios -n rebobina-ios-ipa-3
gh release view v0.1.0 -R VRamses/rebobina-ios --json assets
```

E, na pasta do repositório, a trava dos tokens e a reprodução do gerado:

```
node scripts/conferir-tokens.mjs
./gradlew :composeApp:conferirTokens
node design/tokens/gerar-kotlin.mjs   # com o pacote padrão, tem de sair igual ao 8367859
```
