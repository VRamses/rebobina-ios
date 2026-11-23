# Rebobina para iPhone

App do Rebobina para iPhone, feito em Compose Multiplatform (Kotlin) com projeto
Compose Multiplatform e projeto Xcode. O IPA sai sem assinatura do GitHub Actions e
e assinado no Windows pelo Sideloadly ou pelo AltStore com a sua conta Apple gratuita.

- Bundle id: `net.dynv6.rebobina.ios`
- Nome na tela do iPhone: Rebobina
- Servidor do acervo: `http://192.168.18.9:8096` (Jellyfin, HTTP, rede local)
- Catalogo: `http://192.168.18.9:8899`
- Versao do app: arquivo `rebobina.version` na raiz

## Estado atual (leia antes de baixar)

Esta rodada entrega o encanamento: projeto Kotlin Multiplatform que compila, framework
iOS, projeto Xcode, build no GitHub Actions e IPA para instalar. A tela do app ainda e
provisoria, so para provar que o pipeline desenha na tela do iPhone. A interface de
verdade entra na rodada de UX.

O que esta provado e o que nao esta fica em [docs/ESTADO.md](docs/ESTADO.md), com o
numero do run e o tamanho do artefato. Nada aqui e declarado pronto sem essa evidencia.

## Como gerar o IPA

O build nao roda sozinho a cada commit, de proposito: cada execucao gasta minutos de
runner macOS, que em repositorio privado custam 10 vezes na cota do GitHub.

1. Abra o repositorio no GitHub.
2. Aba **Actions**.
3. Na lista da esquerda, escolha **IPA iOS**.
4. Botao **Run workflow**, escolha a branch `main`, escreva o motivo (opcional) e confirme.
5. Espere o run terminar (na primeira vez, 12 a 20 minutos; depois, com cache, menos).
6. Abra o run, desca ate **Artifacts** e baixe `rebobina-ios-ipa-<numero do run>`.
7. Descompacte: dentro tem `Rebobina.ipa`.

Tambem da para gerar por tag:

```
git tag v0.1.0
git push origin v0.1.0
```

O artefato fica disponivel por 14 dias. Depois disso, rode de novo.

## Como instalar no iPhone

### Opcao 1: Sideloadly (Windows, recomendado)

1. Baixe e instale o Sideloadly: <https://sideloadly.io>.
2. Instale o iTunes pela Microsoft Store (o Sideloadly precisa dele para falar com o iPhone).
3. Conecte o iPhone no cabo e confie no computador.
4. Abra o Sideloadly, arraste o `Rebobina.ipa` para a janela.
5. Em **Apple ID**, coloque seu Apple ID comum. A senha vai direto para a Apple, nao fica salva aqui.
6. Clique em **Start** e espere. Ele assina o IPA e instala no iPhone.
7. No iPhone, va em **Ajustes > Geral > VPN e Gerenciamento de Dispositivo**, toque no seu
   Apple ID e escolha **Confiar**.

Se o iPhone for iOS 16 ou mais novo, pode aparecer o aviso de **Modo de Desenvolvedor**:
va em **Ajustes > Privacidade e Seguranca > Modo de Desenvolvedor**, ligue e reinicie o iPhone.

### Opcao 2: AltStore

1. Instale o AltServer no PC e o AltStore no iPhone: <https://altstore.io>.
2. No iPhone, abra o AltStore, aba **My Apps**, toque em **+** e escolha o `Rebobina.ipa`.
3. O AltStore assina com seu Apple ID e instala.
4. O AltStore consegue renovar sozinho pelo Wi-Fi com o AltServer aberto no PC.

## O prazo de 7 dias da conta Apple gratuita

Assinatura com Apple ID gratuito vale **7 dias**. Depois disso o app para de abrir e
aparece o aviso de desenvolvedor nao confiavel. Nao e defeito do Rebobina, e regra da Apple
para quem nao paga o Apple Developer Program (99 dolares por ano).

Para renovar, escolha um caminho:

- **Sideloadly**: conecte o iPhone e rode o Sideloadly de novo com o mesmo IPA. Ele reassina
  em cima do app existente e os dados do app continuam la.
- **AltStore**: deixe o AltServer aberto no PC, na mesma rede Wi-Fi do iPhone. O AltStore
  renova sozinho nos dias finais, de preferencia com o app aberto.

Se o app ja venceu e nao abre mais, a reinstalacao resolve. Dados locais do app (login,
favoritos) podem ser perdidos se voce apagar antes de reassinar, entao prefira reassinar
por cima, sem desinstalar.

## Cota de minutos do GitHub Actions

O repositorio e privado. Na conta gratuita sao 2000 minutos por mes, mas runner macOS conta
**10 vezes**: na pratica, 200 minutos de Mac por mes, e nao 2000.

Conta feita:

```
2000 minutos de cota / 10 (fator macOS) = 200 minutos de runner macOS por mes
1 build de 15 minutos (com cache frio) = 15 minutos de runner = 150 minutos de cota
1 build de 10 minutos (com cache quente) = 10 minutos de runner = 100 minutos de cota
```

Ou seja: **1 build por mes sobra, 2 builds cabem apertado, 3 builds estouram a cota**.
Por isso o gatilho e manual: cada disparo e decisao consciente, nao efeito colateral de commit.
O resumo de cada run mostra a duracao e a conta de minutos cobrados.

Se a cota estourar, o build do mes seguinte fica bloqueado ate a virada do ciclo de
cobranca. Nesse caso existe a alternativa de pagar minutos avulsos, mas o combinado do
projeto e ficar na cota gratuita.

## Onde fica cada coisa

```
settings.gradle.kts, build.gradle.kts     configuracao raiz do Gradle
gradle/libs.versions.toml                 versoes de Kotlin, Compose e AGP em um lugar so
gradle.properties                         opcoes do Gradle
rebobina.version                          versao do app (uma fonte so)
composeApp/                               modulo Kotlin Multiplatform
  build.gradle.kts                        alvos iosArm64, iosSimulatorArm64 e Android
  src/commonMain/kotlin/net/dynv6/rebobina/
    App.kt                                tela provisoria
    config/RebobinaConfig.kt              unico lugar com o endereco do servidor
    ui/TemaPlaceholder.kt                 cores provisorias, marcadas como provisorias
  src/iosMain/kotlin/net/dynv6/rebobina/
    MainViewController.kt                 ponte Kotlin para o Swift
    Plataforma.ios.kt
iosApp/                                   projeto Xcode
  iosApp.xcodeproj                        projeto, sem dependencia de CocoaPods
  iosApp/Info.plist                       permissao de HTTP local e nome Rebobina
  iosApp/iOSApp.swift, ContentView.swift  casca SwiftUI em volta do Compose
scripts/build-kotlin-framework.sh         compila o framework Kotlin chamado pelo Xcode
.github/workflows/ios.yml                 build do IPA no GitHub Actions
docs/ESTADO.md                            o que esta provado e o que nao esta
```

## Trocar o endereco do servidor

Um lugar so: `composeApp/src/commonMain/kotlin/net/dynv6/rebobina/config/RebobinaConfig.kt`.
O `Info.plist` tem a liberacao de HTTP para rede local, que nao precisa mudar se o IP mudar,
porque `NSAllowsLocalNetworking` cobre a rede local inteira.

## Rodar no Mac, se precisar

Precisa de Xcode 16.4 ou mais novo e JDK 21.

```
cd iosApp
open iosApp.xcodeproj
```

Escolha o simulador e rode. O Xcode chama o Gradle sozinho pela fase de build
`Compilar framework Kotlin`. Para aparelho de verdade, escolha o time de assinatura em
**Signing & Capabilities**.
