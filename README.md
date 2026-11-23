# Rebobina para iPhone

App do Rebobina para iPhone, feito em Compose Multiplatform (Kotlin), com projeto
Compose Multiplatform e projeto Xcode. O IPA sai sem assinatura do GitHub Actions e
é assinado no Windows pelo Sideloadly ou pelo AltStore com a sua conta Apple gratuita.

- Bundle id: `net.dynv6.rebobina.ios`
- Nome na tela do iPhone: Rebobina
- Servidor do acervo: `http://192.168.18.9:8096` (Jellyfin, HTTP, rede local)
- Catálogo: `http://192.168.18.9:8899`
- Versão do app: arquivo `rebobina.version` na raiz

## Estado atual (leia antes de baixar)

Esta rodada entrega o encanamento: projeto Kotlin Multiplatform que compila, framework
iOS, projeto Xcode, build no GitHub Actions e IPA para instalar. A tela do app ainda é
provisória, só para provar que o pipeline desenha na tela do iPhone. A interface de
verdade entra na rodada de UX.

O build do IPA já foi provado: o run #3 gerou o artefato `rebobina-ios-ipa-3` com
`Rebobina.ipa` de 7.571.684 bytes (7,22 MiB). O que ainda **não** foi provado é abrir o
app num iPhone, porque esta rodada não tem iPhone nem Mac para testar. Detalhe por
detalhe, com o número de cada run, em [docs/ESTADO.md](docs/ESTADO.md).

**Se o iPhone estiver em iOS anterior ao 26, o app provavelmente não abre.** O motivo, o
diagnóstico e as duas saídas estão no começo do [docs/ESTADO.md](docs/ESTADO.md).

## Baixar o IPA pronto (mais fácil)

Cada versão publicada vira uma **release** no repositório, com o arquivo renomeado para
o que se entende no Explorer e no Sideloadly:

1. Abra <https://github.com/VRamses/rebobina-ios/releases>.
2. Na release mais recente (por exemplo `v0.1.0`), baixe `Rebobina-0.1.0-unsigned.ipa`.
3. Vá para [Como instalar no iPhone](#como-instalar-no-iphone).

Pelo terminal, com o `gh` instalado e logado na sua conta:

```
gh release download v0.1.0 -R VRamses/rebobina-ios
```

O repositório é privado, então o download só funciona logado como você, no site ou no
`gh`. A release fica guardada; o artefato do Actions vence em 14 dias.

## Como gerar o IPA

O build não roda sozinho a cada commit, de propósito: cada execução gasta minutos de
runner macOS, que em repositório privado custam 10 vezes no orçamento do GitHub.

1. Abra o repositório no GitHub.
2. Aba **Actions**.
3. Na lista da esquerda, escolha **IPA iOS**.
4. Botão **Run workflow**, escolha a branch `main`, escreva o motivo (opcional) e confirme.
5. Espere o run terminar (medido: 4m58s na primeira vez, com o toolchain Kotlin/Native
   baixando; 3m28s quando o cache do toolchain já existe).
6. Abra o run, desça até **Artifacts** e baixe `rebobina-ios-ipa-<número do run>`.
7. Descompacte: dentro tem `Rebobina.ipa`. Esse é o arquivo cru do build; o que você
   instala no iPhone é o da release.

Também dá para gerar por tag, e a tag vira release:

```
git tag v0.1.0
git push origin v0.1.0
```

O artefato do Actions fica disponível por 14 dias. Depois disso, rode de novo ou use a
release.

## Como instalar no iPhone

### Opção 1: Sideloadly (Windows, recomendado)

1. Baixe e instale o Sideloadly: <https://sideloadly.io>.
2. Instale o iTunes pela Microsoft Store (o Sideloadly precisa dele para falar com o iPhone).
3. Conecte o iPhone no cabo e confie no computador.
4. Abra o Sideloadly, arraste o `Rebobina.ipa` para a janela.
5. Em **Apple ID**, coloque seu Apple ID comum. A senha vai direto para a Apple, não fica
   salva aqui.
6. Clique em **Start** e espere. Ele assina o IPA e instala no iPhone.
7. No iPhone, vá em **Ajustes > Geral > VPN e Gerenciamento de Dispositivo**, toque no seu
   Apple ID e escolha **Confiar**.

Se o iPhone for iOS 16 ou mais novo, pode aparecer o aviso de **Modo de Desenvolvedor**:
vá em **Ajustes > Privacidade e Segurança > Modo de Desenvolvedor**, ligue e reinicie o iPhone.

### Opção 2: AltStore

1. Instale o AltServer no PC e o AltStore no iPhone: <https://altstore.io>.
2. No iPhone, abra o AltStore, aba **My Apps**, toque em **+** e escolha o `Rebobina.ipa`.
3. O AltStore assina com seu Apple ID e instala.
4. O AltStore consegue renovar sozinho pelo Wi-Fi com o AltServer aberto no PC.

## O prazo de 7 dias da conta Apple gratuita

Assinatura com Apple ID gratuito vale **7 dias**. Depois disso o app para de abrir e
aparece o aviso de desenvolvedor não confiável. Não é defeito do Rebobina, é regra da Apple
para quem não paga o Apple Developer Program (99 dólares por ano).

Para renovar, escolha um caminho:

- **Sideloadly**: conecte o iPhone e rode o Sideloadly de novo com o mesmo IPA. Ele reassina
  em cima do app existente e os dados do app continuam lá.
- **AltStore**: deixe o AltServer aberto no PC, na mesma rede Wi-Fi do iPhone. O AltStore
  renova sozinho nos dias finais, de preferência com o app aberto.

Se o app já venceu e não abre mais, a reinstalação resolve. Dados locais do app (login,
favoritos) podem ser perdidos se você apagar antes de reassinar, então prefira reassinar
por cima, sem desinstalar.

## Cota de minutos do GitHub Actions

O repositório é privado. O orçamento da conta gratuita é de **2.000 minutos cobrados** por
mês, e runner macOS consome **10 vezes** a duração do job: na prática, o teto é de
**200 minutos de runner macOS por mês**, e não 2.000.

Conta feita, com os quatro builds que já rodaram:

```
orçamento: 2.000 minutos cobrados por mês
macOS: cada minuto de runner consome 10 minutos do orçamento
teto real: 2.000 / 10 = 200 minutos de runner macOS por mês

1 build com cache quente (medido 3m28s = 208 s) = 35 minutos cobrados
4 builds (runs 1 a 4, dois falhos e dois verdes) = 141 minutos de runner = 1.410 minutos cobrados

sobrou: 2.000 - 1.410 = 590 minutos cobrados = 59 minutos de runner macOS
59 / 3,5 minutos por build = cerca de 16 builds neste mês
```

Ou seja: **restam cerca de 16 builds neste mês**, e não 1.859 minutos de macOS. Quando os
200 minutos de runner macOS acabarem, os builds ficam bloqueados até a virada do ciclo de
cobrança. Por isso o gatilho é manual: cada disparo é decisão consciente, não efeito
colateral de commit. O resumo de cada run mostra a duração e a conta de minutos cobrados.

Se a cota estourar, existe a alternativa de pagar minutos avulsos, mas o combinado do
projeto é ficar na cota gratuita.

## Onde fica cada coisa

```
settings.gradle.kts, build.gradle.kts     configuração raiz do Gradle
gradle/libs.versions.toml                 versões de Kotlin, Compose e AGP em um lugar só
gradle.properties                         opções do Gradle
rebobina.version                          versão do app (uma fonte só)
composeApp/                               módulo Kotlin Multiplatform
  build.gradle.kts                        alvos iosArm64, iosSimulatorArm64 e Android
  src/commonMain/kotlin/net/dynv6/rebobina/
    App.kt                                tela provisória
    config/RebobinaConfig.kt              único lugar com o endereço do servidor
    ui/TemaPlaceholder.kt                 cores provisórias, marcadas como provisórias
  src/iosMain/kotlin/net/dynv6/rebobina/
    MainViewController.kt                 ponte Kotlin para o Swift
    Plataforma.ios.kt
iosApp/                                   projeto Xcode
  iosApp.xcodeproj                        projeto, sem dependência de CocoaPods
  iosApp/Info.plist                       permissão de HTTP local e nome Rebobina
  iosApp/iOSApp.swift, ContentView.swift  casca SwiftUI em volta do Compose
scripts/build-kotlin-framework.sh         compila o framework Kotlin chamado pelo Xcode
.github/workflows/ios.yml                 build do IPA no GitHub Actions
docs/ESTADO.md                            o que está provado e o que não está
```

## Trocar o endereço do servidor

Um lugar só: `composeApp/src/commonMain/kotlin/net/dynv6/rebobina/config/RebobinaConfig.kt`.
O `Info.plist` tem a liberação de HTTP para rede local, que não precisa mudar se o IP mudar,
porque `NSAllowsLocalNetworking` cobre a rede local inteira.

## Rodar no Mac, se precisar

Precisa de **Xcode 26 ou mais novo**, porque o Compose Multiplatform 1.11.1 chama uma
classe que só existe no SDK do iOS 26 (com Xcode 16.4 o link do app falha em
`UIViewLayoutRegion`), e de JDK 21.

O app roda em iOS 16 ou mais novo: o alvo mínimo do projeto Xcode é 16.0.

```
cd iosApp
open iosApp.xcodeproj
```

Escolha o simulador e rode. O Xcode chama o Gradle sozinho pela fase de build
`Compilar framework Kotlin`. Para aparelho de verdade, escolha o time de assinatura em
**Signing & Capabilities**.
