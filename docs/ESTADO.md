# Estado do Rebobina para iPhone

Ultima atualizacao: 23/11/2026. Repositorio: <https://github.com/VRamses/rebobina-ios> (privado).

Resumo: o build do IPA funciona no GitHub Actions e o IPA esta publicado como release
`v0.1.0`, com o arquivo renomeado para baixar e instalar. O que falta e abrir o app num
iPhone de verdade, que nao da para testar desta maquina.

## O que esta provado, com evidencia

| # | o que | evidencia |
|---|---|---|
| 1 | O Kotlin compila para iOS arm64 | run #3 (id 45094376845), passo "Compilar Kotlin (iOS arm64)" com conclusao `success`, 01:24:45 a 01:25:07, lido em `gh run view 45094376845 --json jobs` |
| 2 | O app linka e arquiva sem assinatura | mesmo run, passo "Xcode archive sem assinatura" `success`, 01:25:07 a 01:27:32 (2m25s), com Xcode 26.6 e SDK iOS 26.5 |
| 3 | O IPA e empacotado e publicado | passo "Publicar IPA" `success`, artefato `rebobina-ios-ipa-3` com 7.527.066 bytes (zip), retencao 14 dias, lido em `gh api repos/VRamses/rebobina-ios/actions/runs/45094376845/artifacts` |
| 4 | O IPA e um app iOS de verdade | baixei o artefato com `gh run download`, abri e conferi: `Payload/Rebobina.app/` com `Info.plist` (1487 bytes), `PkgInfo` e o binario `Rebobina` de 20.486.752 bytes, Mach-O 64 bits arm64 (primeiros bytes `CF FA ED FE`, cputype `0x0100000C`) |
| 5 | O plist do app esta correto | do `Info.plist` dentro do IPA: CFBundleDisplayName `Rebobina`, CFBundleIdentifier `net.dynv6.rebobina.ios`, CFBundleShortVersionString `0.1.0`, CFBundleVersion `202611230124`, MinimumOSVersion `16.0`, UIDeviceFamily `[1]`, UIRequiredDeviceCapabilities `[arm64]`, NSAllowsArbitraryLoads `true`, NSAllowsLocalNetworking `true`, NSExceptionDomains `192.168.18.9`, NSBonjourServices `_http._tcp`, DTXcode `2660`, DTSDKName `iphoneos26.5`, DTPlatformVersion `26.5`, BuildMachineOSBuild `25G332` |
| 6 | O codigo compartilhado entrou no app | as strings `http://192.168.18.9:8096` e `http://192.168.18.9:8899` e os textos da tela provisoria aparecem em UTF-16 dentro do binario, ou seja, o `commonMain` e o `RebobinaConfig` foram linkados |
| 7 | O binario nao depende de framework privado | 0 ocorrencias de `PrivateFrameworks` e de `UIUtilities` no binario; as dylibs citadas sao CoreFoundation, CoreGraphics, CoreText, Foundation, IOSurface, Metal, QuartzCore, SwiftUI e UIKit. `UIViewLayoutRegion` aparece como referencia de simbolo e vem do UIKit do SDK do iOS 26 |
| 8 | O IPA tem download facil, fora do Actions | release `v0.1.0` no commit `8e72fa0` (o mesmo do run #3), titulo "Rebobina iOS 0.1.0 (IPA sem assinatura)", asset `Rebobina-0.1.0-unsigned.ipa` com 7.571.684 bytes (7,22 MiB), sha256 `6fa5f17ff398e5575a735d8ba0270b3e59f7543cd6640fc9231077944ba42ee6`, estado `uploaded`, lido em `gh release view v0.1.0 --json assets` |
| 9 | O asset publicado e exatamente o IPA do run #3 | baixei o artefato do run 45094376845, calculei o sha256 do `Rebobina.ipa` de dentro dele (6fa5f17f...) e publiquei esse arquivo sem recompilar e sem reempacotar |
| 10 | O gatilho por tag tambem funciona | o push da tag `v0.1.0`, feito pela criacao da release, disparou o run #4 (id 45095174609), que terminou `success` em 3m13s e publicou o artefato `rebobina-ios-ipa-4` |

## O que NAO esta provado

- **Abrir o app num iPhone.** Nao existe iPhone nem Mac nesta maquina. O IPA sai sem
  assinatura; quem assina com Apple ID gratuito e o Sideloadly ou o AltStore, no PC do
  Ramses.
- **Instalar por Sideloadly ou AltStore.** O README descreve o caminho, mas nenhuma
  instalacao foi executada por mim.
- **Rodar no simulador iOS.** Nao ha Mac aqui.
- **Rodar em iPhone com iOS anterior ao 26.** O binario foi linkado contra o SDK do iOS 26
  e referencia a classe `UIViewLayoutRegion`, que so existe no UIKit do iOS 26. O Compose
  Multiplatform 1.11.1 usa essa classe em codigo guardado por versao, e o projeto declara
  minimo iOS 16.0, mas o comportamento em iOS 16 a 18 nao foi testado. Se o app fechar na
  abertura de um iPhone antigo, o primeiro suspeito e esse simbolo (no aparelho apareceria
  "Symbol not found: _OBJC_CLASS_$_UIViewLayoutRegion"); as saidas sao subir o minimo para
  iOS 26 ou trocar o Compose Multiplatform por uma versao que resolve o simbolo em tempo
  de execucao.
- **Alvo Android do modulo.** `compileSdk` 36 e `minSdk` 24 configurados, nenhuma tarefa
  Android executada e nao existe modulo de launcher Android nesta rodada.
- **Tela de verdade.** A atual e provisoria, marcada como placeholder em
  `composeApp/src/commonMain/kotlin/net/dynv6/rebobina/ui/TemaPlaceholder.kt`.
- **Icone do app.** Sem `Assets.xcassets` nesta rodada; o app usa o icone padrao.
- **Chamadas de rede.** Nenhuma requisicao HTTP foi feita, porque ainda nao ha cliente HTTP
  no codigo. O ATS e a permissao de rede local estao no `Info.plist`, mas nao exercitados.

## Historico das execucoes

| # | id | commit | resultado | passo que falhou | duracao do job | cota (fator 10) |
|---|---|---|---|---|---|---|
| 1 | 45092332982 | `0d828ec` | failure | Xcode archive sem assinatura | 4m58s | 40 min |
| 2 | 45093782673 | `38dd978` | failure | Xcode archive sem assinatura | 3m21s | 34 min |
| 3 | 45094376845 | `8e72fa0` | success | nenhum | 3m28s | 35 min |
| 4 | 45095174609 | `8e72fa0` (tag `v0.1.0`) | success | nenhum | 3m13s | 32 min |

Total gasto ate aqui: 141 minutos de cota dos 2000 mensais (7%). O eixo e o mesmo do
`README.md`: 2000 minutos divididos pelo fator 10 de runner macOS, e cada minuto de job
conta 10.

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

### Run 2: tentativa de link fraco com o framework privado, tambem falhou

```
ld: warning: search path '.../iPhoneOS18.5.sdk/System/Library/PrivateFrameworks' not found
ld: framework 'UIUtilities' not found
clang: error: linker command failed with exit code 1 (use -v to see invocation)
** ARCHIVE FAILED **
```

Motivo: o SDK publico do Xcode 16.4 nao traz stub de framework privado nenhum, entao nao
havia o que linkar. A tentativa foi revertida no commit `8e72fa0`.

### Causa raiz e correcao

O Compose Multiplatform 1.11.1 (`ui-uikit`, objeto `CMPLayoutRegion.o`) chama
`UIViewLayoutRegion`, API de layout do iOS 26. Com SDK anterior ao do iOS 26 o simbolo nao
existe no SDK publico e o link do app quebra, mesmo com o framework Kotlin compilando bem.
Correcao aplicada no commit `8e72fa0`: runner `macos-26` (Xcode 26.6, SDK iOS 26.5),
minimo do app em iOS 16.0 e `compileSdk`/`targetSdk` do modulo Android em 36, que e a
plataforma instalada na imagem do runner (a `android-37` pura nao esta).

## Suposicoes visiveis

1. Assumi que o iPhone do Ramses roda iOS 16 ou mais novo ao subir o minimo para 16.0. Se
   for mais antigo, e preciso baixar `IPHONEOS_DEPLOYMENT_TARGET`, e o teste em iOS anterior
   ao 26 continua nao provado.
2. Assumi que o download da release sera feito logado na conta `VRamses`, porque o
   repositorio e privado.
3. Assumi que o nome do arquivo (`Rebobina-0.1.0-unsigned.ipa`) pode ser diferente do nome
   interno (`Rebobina.ipa`) sem problema: Sideloadly e AltStore leem o arquivo, nao o nome.
4. Assumi o valor de cota do GitHub para repositorio privado (2000 minutos por mes, fator 10
   para macOS). A conta de gasto acima e soma das duracoes reais dos jobs, sem conferir o
   medidor de cobranca do GitHub.

## Como reproduzir a conferencia

```
gh run view 45094376845 -R VRamses/rebobina-ios --json conclusion,jobs
gh run download 45094376845 -R VRamses/rebobina-ios -n rebobina-ios-ipa-3
gh release view v0.1.0 -R VRamses/rebobina-ios --json assets
```
