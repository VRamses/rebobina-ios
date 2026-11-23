# Estado do Rebobina para iPhone

Última atualização: 23/11/2026. Repositório: <https://github.com/VRamses/rebobina-ios> (privado).

## Se o app não abrir no iPhone, leia isto primeiro

Se o iPhone estiver em iOS anterior ao 26, o app provavelmente não abre. O binário foi
linkado contra o SDK do iOS 26 e referencia a classe `UIViewLayoutRegion`, que só existe no
UIKit do iOS 26. Na hora de abrir, a mensagem é:

```
Symbol not found: _OBJC_CLASS_$_UIViewLayoutRegion
```

O que fazer, em três linhas:

1. Confira a versão em **Ajustes > Geral > Sobre > Versão** e o relatório do crash em
   **Ajustes > Privacidade e Segurança > Análises e Dados > Dados de Análises** (procure
   `Rebobina`). No Windows, o mesmo relatório sai por 3uTools ou por
   `idevicecrashreport -u <UDID> -e <pasta>`, se o libimobiledevice estiver instalado. Com Mac,
   use o Console: **Janela > Dispositivos e Simuladores > Abrir Console**.
2. Se a mensagem contiver `_OBJC_CLASS_$_UIViewLayoutRegion`, o problema não é a instalação
   nem o Sideloadly: é a versão do sistema.
3. As duas saídas: subir o mínimo do app para iOS 26 (`IPHONEOS_DEPLOYMENT_TARGET`, o que só
   deixa instalar em iPhone com iOS 26) ou fixar uma versão anterior do Compose Multiplatform
   que resolva esse símbolo em tempo de execução (a faixa 1.10.x é a candidata, não testada
   aqui).

Resumo: o build do IPA funciona no GitHub Actions e o IPA está publicado como release
`v0.1.0`, com o arquivo renomeado para baixar e instalar. O que falta é abrir o app num
iPhone de verdade, que não dá para testar desta máquina.

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

## O que NÃO está provado

- **Abrir o app num iPhone.** Não existe iPhone nem Mac nesta máquina. O IPA sai sem
  assinatura; quem assina com Apple ID gratuito é o Sideloadly ou o AltStore, no PC do
  Ramses. Se o aparelho estiver em iOS anterior ao 26, veja a seção do começo deste
  documento.
- **Instalar por Sideloadly ou AltStore.** O README descreve o caminho, mas nenhuma
  instalação foi executada por mim.
- **Rodar no simulador iOS.** Não há Mac aqui.
- **Rodar em iPhone com iOS anterior ao 26.** O binário foi linkado contra o SDK do iOS 26 e
  referencia a classe `UIViewLayoutRegion`, que só existe no UIKit do iOS 26. O Compose
  Multiplatform 1.11.1 usa essa classe em código guardado por versão, e o projeto declara
  mínimo iOS 16.0, mas o comportamento em iOS 16 a 18 não foi testado. As saídas estão na
  primeira seção.
- **Alvo Android do módulo.** `compileSdk` 36 e `minSdk` 24 configurados, nenhuma tarefa
  Android executada e não existe módulo de launcher Android nesta rodada.
- **Tela de verdade.** A atual é provisória, marcada como placeholder em
  `composeApp/src/commonMain/kotlin/net/dynv6/rebobina/ui/TemaPlaceholder.kt`.
- **Ícone do app.** Sem `Assets.xcassets` nesta rodada; o app usa o ícone padrão.
- **Chamadas de rede.** Nenhuma requisição HTTP foi feita, porque ainda não há cliente HTTP
  no código. O ATS e a permissão de rede local estão no `Info.plist`, mas não exercitados.

## Histórico das execuções

| # | id | commit | resultado | passo que falhou | duração do job | minutos cobrados (fator 10) |
|---|---|---|---|---|---|---|
| 1 | 45092332982 | `0d828ec` | failure | Xcode archive sem assinatura | 4m58s | 40 min |
| 2 | 45093782673 | `38dd978` | failure | Xcode archive sem assinatura | 3m21s | 34 min |
| 3 | 45094376845 | `8e72fa0` | success | nenhum | 3m28s | 35 min |
| 4 | 45095174609 | `8e72fa0` (tag `v0.1.0`) | success | nenhum | 3m13s | 32 min |

Total gasto até aqui: 141 minutos de runner macOS = **1.410 minutos cobrados** dos 2.000 do
mês. Sobram 590 minutos cobrados, ou seja, 59 minutos de runner macOS, cerca de 16 builds de
3m30s. O eixo da conta está no `README.md`: 2.000 dividido pelo fator 10 dá 200 minutos de
runner macOS no mês, e cada minuto de job consome 10 minutos do orçamento.

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
Correção aplicada no commit `8e72fa0`: runner `macos-26` (Xcode 26.6, SDK iOS 26.5),
mínimo do app em iOS 16.0 e `compileSdk`/`targetSdk` do módulo Android em 36, que é a
plataforma instalada na imagem do runner (a `android-37` pura não está).

## Suposições visíveis

1. Assumi que o iPhone do Ramses roda iOS 16 ou mais novo ao subir o mínimo para 16.0. Se
   for mais antigo, é preciso baixar `IPHONEOS_DEPLOYMENT_TARGET`, e o teste em iOS anterior
   ao 26 continua não provado.
2. Assumi que o download da release será feito logado na conta `VRamses`, porque o
   repositório é privado.
3. Assumi que o nome do arquivo (`Rebobina-0.1.0-unsigned.ipa`) pode ser diferente do nome
   interno (`Rebobina.ipa`) sem problema: Sideloadly e AltStore leem o arquivo, não o nome.
4. Assumi o valor de orçamento do GitHub para repositório privado: 2.000 minutos cobrados por
   mês, com fator 10 para macOS. A conta de gasto acima é soma das durações reais dos jobs
   vezes 10, sem conferir o medidor de cobrança do GitHub.
5. Assumi que o erro do iOS anterior ao 26 aparece na abertura, como símbolo não resolvido,
   porque o binário referencia a classe do UIKit novo. Não testei num aparelho antigo.

## Como reproduzir a conferência

```
gh run view 45094376845 -R VRamses/rebobina-ios --json conclusion,jobs
gh run download 45094376845 -R VRamses/rebobina-ios -n rebobina-ios-ipa-3
gh release view v0.1.0 -R VRamses/rebobina-ios --json assets
```
