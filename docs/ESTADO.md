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
`v0.1.0`, com o arquivo renomeado para baixar e instalar. Os tokens de desenho na versão 1.1.0
estão no repositório, com o JSON copiado byte a byte em `design/tokens` e o Kotlin gerado em
`commonMain`, travados no build e no CI. A rodada de 23/11/2026 apagou o `TemaPlaceholder.kt`:
as cinco telas do Android mais Ajustes estão escritas só com os tokens, com as duas faixas de
layout e um cliente Ktor que lê o Jellyfin de verdade. O run #10 compilou as telas; o run #11
reimportou a 1.1.0, apagou 16 constantes locais que viraram canônicas, rodou as duas travas
dentro do CI e publicou o IPA de 10.634.122 bytes com o texto gerado dentro do binário. O que
falta agora não é build: é abrir o app num iPhone de verdade, que não dá para testar desta
máquina, e é isso que separa "compila e entra no IPA" de "desenha e lê o acervo".

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
| 11 | Os tokens de desenho são cópia byte a byte da origem 1.1.0 | `design/tokens/rebobina-tokens.json` com 34.081 bytes e sha256 `622f1495f8767b4bf3e6b7bbf6a0fcc0e784d9feca6c9452d6926190cf1ae10c`, igual ao primeiro campo do `rebobina-tokens.sha256` (86 bytes sem newline final, sha256 `fcd97b5bce49394d413ba76239c32f1458e0629b7dd7de5ee134a0d2d522a41e`), copiado do commit `7ed7dcfc392cd77f7e5c2eb7a0ed6562a7edb34c` do `rebobina-servidor`, com o clone limpo naquele commit e `Get-FileHash` nos dois lados; os geradores continuam iguais ao canônico (`gerar.mjs` 15.585 bytes `7e603e91...`, `gerar-kotlin.mjs` 18.694 bytes `3fdb48f0...` e `conferir.mjs` 9.879 bytes `755d7acd...`), e o `ORIGEM.json` registra repositório, branch, commit, caminho, sha256, versão 1.1.0 e data da cópia. A 1.1.0 tem 420 folhas contra 403 da 1.0.8: entraram as 17 chaves de `interface` e mudaram oito chaves de texto de documentação e geração (`meta.versao`, `meta.historico`, `meta.regras`, `geracao.kotlin.convencao`, `geracao.css.naoEmitido`, `geracao.css.motivoNaoEmitido`, `geracao.textos.fonte` e `geracao.textos.regra`), nenhuma de desenho (folhas planas comparadas com `git show fb9eb94^:design/tokens/rebobina-tokens.json` contra o commitado). Até 23/11/2026 esta linha dizia 1.0.8; a prova daquela versão fica nos itens 12 e 14 a 16 |
| 12 | O Kotlin gerado é reprodutível e está travado | `node design/tokens/gerar-kotlin.mjs` com o pacote padrão do JSON reproduz byte a byte os arquivos de referência do commit `8367859`: `ReboTokensGerado.kt` com 12.467 bytes e sha256 `23ed806352f78ff4889c8ae678cb5dda9990524cc42cf876ebacb84f1595b4e0`, `ReboTextos.kt` com 1.976 bytes e sha256 `f30e599fb0890fb5fa5c598958ad53da4d9fbb4ada86b503c6258d77860ca9c6`; com `--pacote net.dynv6.rebobina.ui.tokens` saem 12.466 B `1ce69352...` e 1.975 B `999e520f...`, com só a linha do pacote diferente. `node scripts/conferir-tokens.mjs` sai 0 com `trava dos tokens: ok, versao 1.0.7`, e renomear o `object ReboTextos` para `ReboTextosQuebrado` derruba a tarefa `:composeApp:conferirTokens` do Gradle apontando a primeira linha diferente (9) |
| 13 | O IPA do run #7 leva o `commonMain` com os dois gerados | run #7 (id 45107159058), commit `61ddda1cc2671c0ad5195c21f691db8e85c0f4c3`, `success` em 3m36s, artefato `rebobina-ios-ipa-7` com 9.121.993 bytes e digest `sha256:bd5d00744d2af8b3ad1c72db8563a849a23b70044436576aba956df505cffc15`, `Rebobina.ipa` de 9.169.810 bytes e sha256 `ff3f748a43b4b25d54a3a4cd7192c358416968900b839d9a3846d4ce35d54a5e`, binário `Payload/Rebobina.app/Rebobina` de 24.882.120 bytes, Mach-O 64 bits arm64; passos "Compilar Kotlin (iOS arm64)" 12 s e "Xcode archive sem assinatura" 157 s, ambos `success`, e o passo "Prova extra do commonMain no alvo Android" com `ANDROID_COMPILOU=sim` |
| 14 | A 1.0.7 não mexeu em valor de desenho | comparadas as 397 folhas do JSON anterior com as 397 da 1.0.7: 0 removidas, 0 novas e 4 alteradas, todas de prosa e meta (`meta.versao`, `meta.historico`, `canonico.conferidor.oQueConfere` e `geracao.kotlin.convencao`); os dois `.kt` gerados diferem do commit anterior só nas duas linhas de cabeçalho com a versão e o sha256 do JSON, e com a linha do pacote normalizada o corpo é idêntico ao arquivo de referência do canônico |
| 15 | O IPA do run #8 leva os tokens 1.0.7 e o conferidor novo | run #8 (id 45112444059), commit `c6252aaf3b3d3c159e7cbedbdd16befb14f9c2aa`, `success` em 4m01s, artefato `rebobina-ios-ipa-8` com 9.121.987 bytes e digest `sha256:4bea81d045dd947d5c7d7d22a12575611a7a75d9697b7a362592b2f0b09c5afc`, `Rebobina.ipa` de 9.169.810 bytes e sha256 `3d11d6e861942998242743d50518a96628d4beb32dfc622204b885c2f7b4da83`, binário `Payload/Rebobina.app/Rebobina` de 24.882.120 bytes sha256 `42ab179bbb500dfdf577e7c857a8804023068cf0fd869fb229139b7651e55bb6`; mesmo tamanho de IPA e de binário do run #7, com sha diferente porque o binário carrega a data do build; passos "Trava dos tokens (sha256, regeneração e diff)" 1 s, "Compilar Kotlin (iOS arm64)" 12 s e "Xcode archive sem assinatura" 174 s, todos `success`, e "Prova extra do commonMain no alvo Android" com `success` |
| 16 | O conferidor do canônico roda dentro de um cliente Kotlin sem rebobina-textos.json | `node design/tokens/conferir.mjs` dentro deste repositório imprime `aviso: sem rebobina-textos.json ao lado deste script; a conferencia de texto remoto fica de fora nesta rodada`, segue com `canonico: design\tokens\rebobina-tokens.json versao 1.0.7, sha256 93a648069591...` e lista `ok rebobina-ios main em dia`, `ok rebobina-android main em dia` e `ok rebobina-webos main em dia` com exit 0 depois do push; antes do push os três clientes apareciam como `ATRASADO`, com a versão antiga da cópia contra o canônico 1.0.7, e exit 1, que era o esperado. Com o canônico na 1.0.8, o iOS voltou a `ATRASADO` até a reimportação do item 18, como esperado |
| 17 | O alvo mínimo do app é iOS 17.0, provado pelo IPA e não pelo pbxproj | decisão do Ramses em 23/11/2026; `IPHONEOS_DEPLOYMENT_TARGET = 17.0` nos quatro blocos do `iosApp/iosApp.xcodeproj/project.pbxproj` (era 16.0, não 26.0), commit `4e4cbe2`; run #9 (id 45166068336), `success` em 4m32s (272 s), artefato `rebobina-ios-ipa-9` com 9.121.686 bytes e digest `sha256:82fc0c20bd4ad157b0db7780c25a4b91b4bdeef82315d2968079d24f314b07f5`; baixado e aberto: `Payload/Rebobina.app/Info.plist` com `MinimumOSVersion 17.0`, lido com `plistlib` no arquivo do artefato, e o log do passo "Empacotar IPA" mostra a mesma linha; `LC_BUILD_VERSION` do binário com `minos 17.0` e `sdk 26.5`, e `_OBJC_CLASS_$_UIViewLayoutRegion` com `weak_import = 1` na tabela de chained fixups; `Rebobina.ipa` de 9.170.102 bytes e sha256 `bda93e37ce16644688dc7339eddd4e7a88fbbc5227c1b73082080a2dfc35e7fb`, binário de 24.882.120 bytes e sha256 `42510f37c66773fca443374e209b8e6777d7b4a3e53751b605a6d606d060902f`; passos "Trava dos tokens" 1 s, "Compilar Kotlin (iOS arm64)" 10 s e "Xcode archive sem assinatura" 204 s, todos `success` |
| 18 | A reimportação da 1.0.8 no iOS foi provada sem gastar runner macOS | decisão do Lead: o iOS ainda não consome token nenhum, então a prova possível sem cota é a trava local e o conferidor cross-repo; `node scripts/conferir-tokens.mjs` sai 0 com `trava dos tokens: ok, versao 1.0.8, sha256 c39b8fe77bfe..., ReboTokensGerado.kt com 12682 bytes e ReboTextos.kt com 1975 bytes iguais ao commitado`; `node design/tokens/gerar-kotlin.mjs --pacote net.dynv6.rebobina.ui.tokens` gera 12.682 B `37f78ecb...` e 1.975 B `2703fe6a...`, idênticos aos arquivos de referência do canônico (12.683 B e 1.976 B, 1 byte mais longos porque `rebobina.android.ui.base.rebo` tem um caractere a mais que `net.dynv6.rebobina.ui.tokens`) depois de normalizar a linha do pacote; `ReboGrade.Celular` e `ReboGrade.Ios` ganharam `LarguraCartao = 140`, `ColunasCanalVertical = 2` e `ColunasCanalHorizontal = 4`, e nenhum valor existente mudou; commit com `[skip ci]`, nenhum `workflow_dispatch` disparado e nenhum minuto de macOS gasto. Depois do push, `node design/tokens/conferir.mjs` imprime `ok rebobina-ios main em dia`, `ok rebobina-webos main em dia` e só `FALHA rebobina-android main ATRASADO (copia 1.0.7, canonico 1.0.8)` com exit 1, que é a divergência do Android, não do iOS |
| 19 | As cinco telas e o cliente Jellyfin compilam, entram no IPA e o CI roda a trava do desenho | run #10 (id 45177622503), commit `2b99e44da3a7b285dcf36f847ee597fbf4e7510c`, `success` em 4m29s (269 s pelo cronômetro do workflow, 310 s de job de 12:47:28 a 12:52:38 UTC), artefato `rebobina-ios-ipa-10` com 10.586.276 bytes e digest `sha256:6aae99e63a73aa1c77ece88068c156ac69cf8bae3df6ece39b62ca5ed322d4a0`; baixado e aberto: `Rebobina.ipa` de 10.635.214 bytes (10,14 MiB) e sha256 `12c27b48b3119eb2e991b8b6dc5fd6f5f13e7cf075d659b4cd548f83864a529d`, binário `Payload/Rebobina.app/Rebobina` de 29.581.704 bytes (+4.699.584 sobre o run #9), `Info.plist` de 1.487 bytes com `MinimumOSVersion 17.0`, `CFBundleIdentifier net.dynv6.rebobina.ios`, `DTSDKName iphoneos26.5` e o ATS com `NSAllowsArbitraryLoads true`; o log mostra `> Task :composeApp:compileKotlinIosArm64` executado (não `UP-TO-DATE`, não `FROM-CACHE`) e `BUILD SUCCESSFUL`, `desenho: ok, 6 telas, nenhum literal de cor/tamanho em ui/telas` duas vezes (no passo "Compilar Kotlin" e no archive, porque as tarefas `conferirTokens` e `conferirDesenho` rodam antes de cada `compileKotlin`), e `ANDROID_COMPILOU=sim (tarefa :composeApp:compileAndroidMain)` em 12 s, o que prova o mesmo `commonMain` compilando para Android; no binário estão as strings das telas e do cliente em UTF-16 (`Continuar assistindo`, `Adicionados recentemente`, `Nada baixado ainda`, `Fila deste aparelho`, `Digite o nome de um filme`, `Resultados`, `Sinopse`, `Temporada`, `Chave de API`, `Endereço do servidor`, `X-Emby-Token`, `/Users`, `LiveTv/Channels`, `192.168.18.9:8096`), o que prova que `commonMain`, `ui/base`, `ui/telas` e `dados` foram linkados; passos: "Trava dos tokens" 1 s, "Compilar Kotlin (iOS arm64)" 23 s, "Xcode archive sem assinatura" 215 s, "Empacotar IPA" 2 s, "Publicar IPA" 2 s, "Prova extra do commonMain no alvo Android" 13 s, todos `success` |
| 20 | O conferidor cross-repo passou a fechar verde nos cinco clientes | `node design/tokens/conferir.mjs` dentro deste repositório em 23/11/2026 12:59 UTC, lendo pela API do GitHub: `canonico: design\tokens\rebobina-tokens.json versao 1.0.8, sha256 c39b8fe77bfe...`, e a lista `ok rebobina-tv`, `ok rebobina-desktop`, `ok rebobina-ios main em dia`, `ok rebobina-android main em dia`, `ok rebobina-webos main em dia`, `ok rebobina-webos textos nao conferido (sem copia local do canonico)`, com `todas as copias em dia com o canonico` e exit 0. Nesta rodada o iOS não mudou nada de token: o que mudou foi o Android, que reimportou a 1.0.8 em paralelo e deixou de aparecer como `ATRASADO`; a leitura é do conferidor, não de inspeção minha no repositório do Android |
| 21 | A 1.1.0 entrou no app, o build compila e o IPA do run #11 contém o texto gerado | commit `fb9eb94` com 17 arquivos e a reimportação dos tokens; run #11 (id 45206725111) `success` em 23/11/2026, job de 275 s (4m35s) e run de 282 s (4m42s), 45,8 minutos cobrados pelo piso (50 se o GitHub arredondar o job para cima), passo "Trava dos tokens" com `trava dos tokens: ok, versao 1.1.0, sha256 622f1495f876..., ReboTokensGerado.kt com 12682 bytes e ReboTextos.kt com 2996 bytes iguais ao commitado` e passo "Compilar Kotlin" com `desenho: ok, 6 telas, nenhum literal de cor/tamanho em ui/telas`; artefato `rebobina-ios-ipa-11` com 10.583.573 bytes (zip, sha256 `1de85628ed19e5653631cfa59e5fcf519e5f1886e80212944dd8285cdef783ca`), `Rebobina.ipa` de 10.634.122 bytes e sha256 `8d08d080eda6e2acd372ef2b72ae4eba07575f33a2b4dc9068155e5503c0634c`, binário `Rebobina` de 29.581.904 bytes com a string `Séries` em UTF-16LE no deslocamento 28749106, e o passo Android com `ANDROID_COMPILOU=sim (tarefa :composeApp:compileAndroidMain)` |

## O que NÃO está provado

- **Desenhar de verdade na tela.** A trava `scripts/conferir-desenho.mjs` é textual: ela
  reprova `Color(0x...)`, `Color.<nome>`, hexadecimal e `.dp`/`.sp` literal em `ui/telas`,
  exige `ReboRotulos` e componente de estado, e passa nas seis telas. Os runs #10 e #11
  mostraram que ela também roda dentro do Gradle do CI (`> Task :composeApp:conferirDesenho` e
  `desenho: ok, 6 telas`), mas continua sendo texto. Ela não prova que o desenho ficou bonito,
  nem que o pixel bate com o Android: isso só uma captura de tela prova, e não há Mac nem
  iPhone aqui.
- **Executar o app e ver a tela.** Compilar, linkar e empacotar está provado pelos runs #10 e
  #11; o que não foi provado é o app abrindo: nenhuma tela desta rodada foi desenhada em
  aparelho, nenhum toque, rolagem ou troca de aba foi exercitado, e os artefatos
  `rebobina-ios-ipa-10` e `rebobina-ios-ipa-11` nunca foram instalados. O binário crescer
  4,5 MiB e conter as strings das telas prova que o código está dentro dele, não que ele
  funciona.
- **Testes de `commonTest`.** Os 7 testes de `ReboAdaptativoTest` foram escritos e nunca
  executados: o CI roda só `:composeApp:compileKotlinIosArm64` e `:composeApp:compileAndroidMain`,
  e esta máquina não tem JDK nem Gradle para rodar `iosSimulatorArm64Test`.
- **Chamadas de rede reais.** O cliente Ktor foi escrito, compilado e linkado, e nunca
  executado: nenhuma requisição ao `http://192.168.18.9:8096` saiu desta máquina, nem `/Users`,
  nem `/Items`, nem `/LiveTv/Channels`. As strings `X-Emby-Token`, `/Users` e
  `LiveTv/Channels` estão no binário, o que prova que o caminho foi linkado, não que o servidor
  responde. O ATS do `Info.plist` libera HTTP local e não foi exercitado.
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
- **Rótulos e formato fora do token.** Depois da reimportação da 1.1.0, `ReboRotulos` caiu de
  59 para 43 constantes: 16 saíram e as 20 leituras correspondentes em 9 arquivos passaram a
  ler `ReboTextos.Interface.*`. Continuam escritos à mão o que `ReboTextos` não cobre:
  `FormatoDeTexto` de `dados/Acervo.kt` (duração, temporada, episódio, tipo de mídia e
  separador) e `SEM_SINOPSE` em `ui/base/ReboRotulos.kt`, porque o valor local
  (`Sem sinopse cadastrada no acervo.`) difere do canônico (`Sem sinopse cadastrada.`) e
  trocar a frase é decisão de produto. O JSON 1.1.0 tem 420 folhas e não publica frase de
  tela fora de `estado.*`, `selo.variantes.*.texto` e `interface.*`: nas outras chaves com
  texto o conteúdo é documentação da folha ou valor (`cor` em hexadecimal, `plataforma` em
  unidade e escala). Isso é lacuna declarada do token, não escolha de desenho.
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
| 10 | 45177622503 | `2b99e44` | success | nenhum | 269 s (4m29s) | 44 min |
| 11 | 45206725111 | `fb9eb94` | success | nenhum | 275 s (4m35s) | 45 min |

Total gasto até aqui: a soma real dos onze jobs é 2.601 s = **43 minutos e 21 segundos de
runner macOS**, cobrados como **433,5 minutos** dos 2.000 do mês. Sobram 1.566,5 minutos
cobrados, que dão 156,65 minutos de runner macOS, cerca de 44 builds de 3m30s.

Cuidado com a leitura: os minutos da coluna acima são **minutos cobrados** (duração do job
vezes 10, truncada para baixo em cada run, o que dá 383 no total) e não minutos de runner
macOS. A soma dos resumos de run fica alguns minutos abaixo dos 387,7 cobrados de verdade,
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

### Run 10: as cinco telas compilam, e o IPA cresce 4,5 MiB

O run #10 (id 45177622503) foi o único disparo da rodada, em 23/11/2026, sobre o commit
`2b99e44`. Passou inteiro, com os passos medidos pelo `gh run view --json jobs`:

```
Trava dos tokens (sha256, regeneracao e diff)   1 s    ok, versao 1.0.8
Compilar Kotlin (iOS arm64)                    23 s    > Task :composeApp:compileKotlinIosArm64
Xcode archive sem assinatura                  215 s    Xcode 26.6, SDK iphoneos26.5
Empacotar IPA                                   2 s    plutil e lipo dentro do passo
Publicar IPA                                    2 s    artefato rebobina-ios-ipa-10
Prova extra do commonMain no alvo Android      13 s    ANDROID_COMPILOU=sim
```

O passo "Compilar Kotlin" não foi `UP-TO-DATE` nem `FROM-CACHE`: o log mostra a tarefa
executada, e dentro dela as duas travas do projeto rodaram de novo (`conferirTokens` e
`conferirDesenho`), com `desenho: ok, 6 telas, nenhum literal de cor/tamanho em ui/telas`. A
tarefa de 23 s é só a compilação do klib: o link do framework acontece dentro do archive, e é
por isso que esse passo leva 215 s.

Números do artefato, lidos no arquivo baixado e não no resumo do CI:

```
artefato rebobina-ios-ipa-10   10.586.276 bytes  sha256:6aae99e63a73aa1c77ece88068c156ac69cf8bae3df6ece39b62ca5ed322d4a0
Rebobina.ipa                   10.635.214 bytes  sha256 12c27b48b3119eb2e991b8b6dc5fd6f5f13e7cf075d659b4cd548f83864a529d
Payload/Rebobina.app/Rebobina  29.581.704 bytes  (+4.699.584 sobre o run #9)
Info.plist                          1.487 bytes  MinimumOSVersion 17.0, DTSDKName iphoneos26.5
```

A prova de que o código novo está dentro do binário não é o tamanho: é a presença das strings
em UTF-16 no executável, conferidas com `zipfile` e `decode("utf-16-le")` no arquivo do
artefato. Aparecem `Continuar assistindo`, `Adicionados recentemente`, `Nada baixado ainda`,
`Fila deste aparelho`, `Digite o nome de um filme`, `Resultados`, `Voltar ao catálogo`,
`Sinopse`, `Temporada`, `Chave de API`, `Endereço do servidor`, `X-Emby-Token`, `/Users`,
`LiveTv/Channels` e `192.168.18.9:8096`; a string `AO VIVO` vem do gerado e também está lá.
Isso prova que `commonMain`, `ui/base`, `ui/telas`, `dados` e o gerado de texto foram
linkados. Não prova que qualquer uma dessas telas desenha ou que o servidor responde: isso é
execução, e não houve execução.

O passo Android, que é `continue-on-error: true`, saiu `success` em 12 s de Gradle e imprimiu
`ANDROID_COMPILOU=sim (tarefa :composeApp:compileAndroidMain)`: o mesmo `commonMain` compila
para os dois alvos, então a camada de desenho e o cliente não têm `expect`/`actual` quebrado.
Esse passo não roda nada no Android, só compila.

O único aviso de Kotlin no log é anterior a esta rodada: `composeApp/build.gradle.kts:20:5`
usa `androidLibrary`, que está marcado como deprecado em favor de `android`. Não mexi nisso
nesta rodada para não trocar API de build e de desenho na mesma rodada; fica declarado.

### Run 11: a 1.1.0 entra no app e o texto de interface passa a ser lido do gerado

O run #11 (id 45206725111) foi o único disparo da rodada, sobre o commit `fb9eb94`, e saiu
`success` em 4m42s de run e 4m35s de job. O passo "Trava dos tokens" imprimiu
`trava dos tokens: ok, versao 1.1.0, sha256 622f1495f876..., ReboTokensGerado.kt com 12682
bytes e ReboTextos.kt com 2996 bytes iguais ao commitado`; o passo "Compilar Kotlin" repetiu a
linha e acrescentou `desenho: ok, 6 telas, nenhum literal de cor/tamanho em ui/telas`; o
archive levou 200 s.

Números do artefato, lidos no arquivo baixado e não no resumo do CI:

```
artefato rebobina-ios-ipa-11   10.583.573 bytes  sha256:1de85628ed19e5653631cfa59e5fcf519e5f1886e80212944dd8285cdef783ca
Rebobina.ipa                   10.634.122 bytes  sha256 8d08d080eda6e2acd372ef2b72ae4eba07575f33a2b4dc9068155e5503c0634c
Payload/Rebobina.app/Rebobina  29.581.904 bytes  (+200 sobre o run #10)
```

A presença do texto gerado no binário foi conferida por busca de bytes no executável: `Séries`
aparece em UTF-16LE no deslocamento 28749106, e `Ainda carregando`, `Tentar de novo` e
`Sem conexão` também em UTF-16LE; `Ajustes`, `Downloads` e `Assistir` aparecem em UTF-8 e em
UTF-16LE. A busca foi por igualdade exata da string, não por leitura de tabela de símbolos.

O commit `fb9eb94` reimportou os tokens, apontou 20 leituras para `ReboTextos.Interface.*` em
9 arquivos, apagou 16 constantes de `ReboRotulos` (59 para 43) e deixou `SEM_SINOPSE` local,
com a divergência de valor declarada no KDoc. O passo Android saiu `success` em 10 s e
imprimiu `ANDROID_COMPILOU=sim (tarefa :composeApp:compileAndroidMain)`.

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
17. (Resolvida pelo run #10.) As suposições 9, 11 e 12 eram apostas de versão e de API sem
    compilar, e o run #10 as fechou: Ktor 3.3.3, coroutines 1.10.2 e serialization 1.9.0
    compilaram e linkaram no Kotlin 2.4.10; `safeDrawingPadding()`,
    `PasswordVisualTransformation`, `HttpClientConfig<*>` como extensão,
    `resposta.body<ByteArray>()` e `Aba.entries` compilaram. Continuam abertas a suposição 10
    (decode de imagem: compilou, não foi executado) e a 13 (primeiro usuário de `/Users`:
    compilou, não foi executado contra servidor).
18. Assumi que o passo Android do CI prova o `commonMain` nos dois alvos: ele rodou
    `:composeApp:compileAndroidMain` com `ANDROID_COMPILOU=sim`, e não executou nada em
    Android. O alvo Android segue sem teste de execução.
19. Assumi que o objeto gerado de texto se chama `ReboTextos`, e não `ReboGeradoTextos`: o
    `gerar-kotlin.mjs` emite `object ReboTextos` e o próprio JSON 1.1.0 descreve
    `interface.<assunto>.<campo> -> ReboTextos.Interface.<Assunto>.<Campo>`. O briefing desta
    rodada dizia `ReboGeradoTextos`; segui o gerador, que é a fonte.
20. Assumi que apagar as 16 constantes locais que viraram canônicas era o pedido, e mantive
    `ReboRotulos.SEM_SINOPSE` local porque o valor diverge do canônico, sem tocar no texto que
    `TelaFicha.kt` mostra. Se o produto quiser a frase curta da 1.1.0, é uma troca de uma
    linha mais o KDoc.
21. Assumi que o iOS não precisa de cópia vendorizada do `rebobina-textos.json`, apesar do
    passo da especificação que mandava copiá-lo: o cliente iOS consome só o Kotlin gerado, o
    `LEIA.md` do canônico restringe essa cópia a cliente CSS, e o `rebobina-android`, que é
    Kotlin como o iOS, também não vendoriza.
22. Assumi que `player.pausado` não pede constante local: a chave entrou no gerado como
    `ReboTextos.Interface.Player.Pausado` e nenhuma tela desta rodada mostra rótulo de
    pausado. Por isso o briefing contava 17 constantes locais para 17 chaves e a conta real
    foi 16 apagadas, 1 mantida e 1 chave sem constante de origem.

## Como reproduzir a conferência

```
gh run view 45206725111 -R VRamses/rebobina-ios --json conclusion,jobs
gh api repos/VRamses/rebobina-ios/actions/runs/45206725111/artifacts
gh run download 45206725111 -R VRamses/rebobina-ios -n rebobina-ios-ipa-11
gh run view 45177622503 -R VRamses/rebobina-ios --json conclusion,jobs
gh api repos/VRamses/rebobina-ios/actions/runs/45177622503/artifacts
gh run download 45177622503 -R VRamses/rebobina-ios -n rebobina-ios-ipa-10
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

E, na pasta do repositório, a trava dos tokens, a trava do desenho e a reprodução do gerado:

```
node scripts/conferir-tokens.mjs
node scripts/conferir-desenho.mjs
./gradlew :composeApp:conferirTokens
./gradlew :composeApp:conferirDesenho
node design/tokens/gerar-kotlin.mjs   # com o pacote padrao, tem de sair igual ao arquivo de referencia do canonico
```
