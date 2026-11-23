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

A rodada de 23/11/2026 saiu do placeholder: o app agora tem as cinco telas do Android
(Início, Ficha, Canais, Downloads e Busca) mais Ajustes, desenhadas só com os tokens
gerados, navegáveis pela barra inferior, com as duas faixas de layout do Android (celular
quando `largura < 600 dp` ou `altura < 480 dp`) e dados reais do Jellyfin por um cliente
Ktor mínimo. O player fica para a próxima rodada, e os botões de assistir e de baixar
declaram isso em vez de fingir reprodução. `ui/TemaPlaceholder.kt` foi apagado.

Os tokens de desenho (cor, tipo, espaço, raio, movimento e os textos de estado) continuam
copiados do `rebobina-servidor` na versão 1.1.0, com o Kotlin deles gerado e travado no
build e no CI. A camada que liga tela e token mora em `ui/base/`, e uma trava local
(`scripts/conferir-desenho.mjs`) reprova cor ou tamanho escrito à mão em tela. Detalhe nas
seções [Os tokens de desenho](#os-tokens-de-desenho) e
[As cinco telas e a camada de desenho](#as-cinco-telas-e-a-camada-de-desenho).

O build do IPA já foi provado: o run #9 (id 45166068336) gerou o artefato
`rebobina-ios-ipa-9` com `Rebobina.ipa` de 9.170.102 bytes (8,75 MiB), sha256
`bda93e37ce16644688dc7339eddd4e7a88fbbc5227c1b73082080a2dfc35e7fb`, e o `Info.plist` de
dentro dele diz `MinimumOSVersion 17.0`. O que ainda **não** foi provado é abrir o app num
iPhone, porque esta rodada não tem iPhone nem Mac para testar.
Detalhe por detalhe, com o número de cada run, em [docs/ESTADO.md](docs/ESTADO.md).

O último IPA medido em detalhe é o do run #11 (id 45206725111, 23/11/2026), o primeiro com a
reimportação do canônico 1.1.0: artefato `rebobina-ios-ipa-11` com 10.583.573 bytes (zip),
`Rebobina.ipa` de 10.634.122 bytes (10,14 MiB), sha256
`8d08d080eda6e2acd372ef2b72ae4eba07575f33a2b4dc9068155e5503c0634c`, binário `Rebobina` de
29.581.904 bytes, job verde em 4m42s, cerca de 46 minutos cobrados. O `Info.plist` de dentro
dele não foi aberto nesta rodada; o do run #10 dizia `MinimumOSVersion 17.0`. O passo extra do
CI provou que o mesmo `commonMain` compila também para Android: o run #11 mostra
`ANDROID_COMPILOU=sim (tarefa :composeApp:compileAndroidMain)` e `BUILD SUCCESSFUL in 9s`. O que
ele **não** prova é a tela desenhada nem a chamada ao Jellyfin em execução: só um iPhone
abrindo o app prova isso.

O IPA cresceu de 7.526.950 bytes (artefato do run #4) para 9.106.918 no run #5 e 9.121.985 no
run #6. A explicação provável do salto do run #4 para o #5 é a entrada do
`ReboTokensGerado.kt` no `commonMain`: ele referencia `TextStyle`, `FontWeight`, `dp` e `sp`,
o que puxa o stack de texto do Compose para dentro do link estático do framework. O segundo
salto, de uns 15 KB, acompanha o `ReboTextos.kt` e os objetos novos daquela versão. É medida e
hipótese, não causa provada por contagem de símbolo. O run #7, que só mudou prosa e ferramenta,
manteve o tamanho: 9.169.810 bytes, 1 byte acima do run #6. O run #8, que reimportou a 1.0.7,
repetiu o tamanho do IPA (9.169.810 bytes) e do binário (24.882.120 bytes) e só trocou o sha256,
porque o binário carrega a data do build. É o comportamento esperado quando nenhum valor de
desenho muda, e confirma a leitura do salto. O run #9, que baixou o mínimo para iOS 17.0 e não
tocou em desenho, manteve o binário em 24.882.120 bytes e deixou o IPA 292 bytes maior
(9.170.102): a diferença é do zip e dos poucos bytes do cabeçalho Mach-O que declaram o
`minos`, não de código.

O run #10, primeiro com as telas, o cliente Ktor e os DTOs, levou o binário de 24.882.120 para
29.581.704 bytes (+4.699.584, uns 4,5 MiB) e o IPA de 9.170.102 para 10.635.214 (+1.465.112).
A leitura é medida, não contagem de símbolo: o salto acompanha a entrada de
`ktor-client-core`, `ktor-client-darwin`, `ktor-client-content-negotiation`,
`kotlinx-serialization-json`, `kotlinx-coroutines-core`, do cache de imagem e das seis telas
com os componentes de `ui/base`, que puxam `TextField`, `LazyColumn` e `ScrollState` do
Compose. Nenhum valor de token mudou nesta rodada, que é o que o `sha256` da trava confirma.

O run #11, que só trocou texto de interface pelo gerado, deixou o binário 200 bytes maior
(29.581.904) e o IPA 1.092 bytes menor (10.634.122): a diferença é de string e de zip, não de
código novo, e nenhum valor de desenho mudou.

**O alvo mínimo do app é iOS 17.** O binário linka contra o SDK do iOS 26, mas a classe
`UIViewLayoutRegion` sai como weak import, então o dyld de um iPhone com iOS 17 a 25 não
falha por ela. O histórico e o que não está provado estão no começo do
[docs/ESTADO.md](docs/ESTADO.md).

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
   baixando; de 3m13s a 4m21s quando o cache do toolchain já existe).
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
mês, e runner macOS consome **10 vezes** a duração do job: o teto real é de **200 minutos de
runner macOS por mês**, e não 2.000. São duas contas diferentes, e confundir uma com a outra
é o erro que faz parecer que ainda sobra Mac para o mês inteiro.

Conta feita, com os onze builds que já rodaram:

```
orçamento: 2.000 minutos cobrados por mês
macOS: cada minuto de runner consome 10 minutos do orçamento
teto real: 2.000 / 10 = 200 minutos de runner macOS por mês

duração real de cada job, lida em gh run view <id> --json jobs:
run 1 (falhou, 4m58s)  238 s
run 2 (falhou, 3m21s)  201 s
run 3 (verde, 3m28s)   208 s
run 4 (verde, 3m13s)   193 s
run 5 (verde, 4m21s)   261 s
run 6 (verde, 3m47s)   227 s
run 7 (verde, 3m36s)   216 s
run 8 (verde, 4m01s)   241 s
run 9 (verde, 4m32s)   272 s
run 10 (verde, 4m29s)  269 s
run 11 (verde, 4m35s)  275 s
soma:                  2.601 s = 43 minutos e 21 segundos de runner macOS

cobrado até agora: 43,35 x 10 = 433,5 minutos dos 2.000
sobrou: 2.000 - 433,5 = 1.566,5 minutos cobrados
1.566,5 / 10 = 156,65 minutos de runner macOS disponíveis
156,65 / 3,5 minutos por build = cerca de 44 builds de 3m30s neste mês
```

Ou seja: **sobram cerca de 156 minutos de runner macOS**, que dão cerca de 44 builds de 3m30s.
A conta acima trunca a duração de cada job ao minuto, então o número é o piso do gasto: o
GitHub arredonda cada job para cima ao faturar, e o total real pode ficar alguns minutos
acima. Não reconferi essa regra na documentação do GitHub nesta rodada.
Quando os 200 minutos de runner macOS do mês acabarem, os builds ficam
bloqueados até a virada do ciclo de cobrança. Por isso o gatilho é manual: cada disparo é
decisão consciente, não efeito colateral de commit. O resumo de cada run mostra a duração e a
conta de minutos cobrados, truncando para baixo run a run, então a soma dos resumos pode
ficar alguns minutos abaixo do total cobrado.

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
    App.kt                                navegação: barra inferior, ficha e Ajustes
    config/RebobinaConfig.kt              único lugar com o endereço do servidor
    dados/Acervo.kt                       leitura do Jellyfin traduzida para a tela
    dados/AcervoModelos.kt                modelos que as telas desenham
    dados/JellyfinApi.kt                  cliente Ktor, um endpoint por função
    dados/JellyfinDtos.kt                 DTOs em PascalCase, como o servidor responde
    dados/Imagens.kt                      cache de capa em memória (80 imagens)
    dados/AjustesDoAcervo.kt              endereço e chave em memória, com salvar
    dados/Persistencia.kt                 expect/actual dos ajustes salvos
    Plataforma.kt                         expect do nome da plataforma
    ui/base/ReboTokens.kt                 apelidos dos tokens e tradução de cor por nome
    ui/base/ReboAdaptativo.kt             faixa celular/tablet e colunas da grade
    ui/base/ReboComponentes.kt            botão, campo, cartão, grade, abas, imagem
    ui/base/ReboEstado.kt                 carregando, falha e vazio com ReboTextos
    ui/base/ReboTema.kt                   MaterialTheme escuro mapeado nos tokens
    ui/base/ReboRotulos.kt                rótulos que o JSON canônico não publica
    ui/telas/TelaInicio.kt                trilho e grade do acervo
    ui/telas/TelaFicha.kt                 ficha com abas de episódios, elenco e similares
    ui/telas/TelaCanais.kt                grade de canais ao vivo
    ui/telas/TelaBusca.kt                 busca com atraso de 350 ms e 2 caracteres
    ui/telas/TelaDownloads.kt             fila vazia declarada, sem baixar de verdade
    ui/telas/TelaAjustes.kt               endereço, chave e teste de conexão
    ui/tokens/ReboTokensGerado.kt         tokens de desenho, gerado (não editar à mão)
    ui/tokens/ReboTextos.kt               textos de estado e selo, gerado (não editar à mão)
  src/commonTest/kotlin/net/dynv6/rebobina/ui/base/
    ReboAdaptativoTest.kt                 7 testes da faixa e das colunas
  src/androidMain/kotlin/net/dynv6/rebobina/
    Plataforma.android.kt                 nome da plataforma
    dados/Plataforma.android.kt           cliente sem motor e ajustes em memória
  src/iosMain/kotlin/net/dynv6/rebobina/
    MainViewController.kt                 ponte Kotlin para o Swift
    Plataforma.ios.kt                     nome da plataforma
    dados/Plataforma.ios.kt               cliente Darwin e ajustes no NSUserDefaults
design/tokens/                            cópia da fonte da verdade do desenho
  rebobina-tokens.json                    o JSON, byte a byte do rebobina-servidor
  rebobina-tokens.sha256                  sha256 do JSON, a trava da cópia
  ORIGEM.json                             de onde veio: commit, versão e sha256
  gerar.mjs                               gerador das folhas CSS de referência
  gerar-kotlin.mjs                        gerador do Kotlin de commonMain
  conferir.mjs                            confere pela API do GitHub se a cópia está em dia
iosApp/                                   projeto Xcode
  iosApp.xcodeproj                        projeto, sem dependência de CocoaPods
  iosApp/Info.plist                       permissão de HTTP local e nome Rebobina
  iosApp/iOSApp.swift, ContentView.swift  casca SwiftUI em volta do Compose
scripts/build-kotlin-framework.sh         compila o framework Kotlin chamado pelo Xcode
scripts/conferir-tokens.mjs               sha256 e regeração, rodado no Gradle e no CI
scripts/conferir-desenho.mjs              trava do desenho: nada de cor/dp literal em tela
.github/workflows/ios.yml                 build do IPA no GitHub Actions
docs/ESTADO.md                            o que está provado e o que não está
```

## Os tokens de desenho

Cor, tipo, espaço, raio, movimento e os textos de estado vêm de um JSON só, o
`design/tokens/rebobina-tokens.json` do repositório `rebobina-servidor`. Este repositório tem
uma cópia byte a byte em `design/tokens/`, versão **1.1.0**, 34.081 bytes, sha256
`622f1495f8767b4bf3e6b7bbf6a0fcc0e784d9feca6c9452d6926190cf1ae10c`, copiada do commit
`7ed7dcfc392cd77f7e5c2eb7a0ed6562a7edb34c`. A 1.1.0 não mexeu em nenhum valor de desenho:
acrescentou o grupo `interface` com 17 textos em quatro assuntos (`comum` 14,
`ficha.semSinopse`, `player.pausado` e `downloads.filaVazia`), com as frases que já coincidiam
entre plataformas. A folha foi de 403 para 420 folhas, e só mudou texto de documentação e de
geração: nenhum valor de cor, tipo, espaço, raio, movimento, ícone, traço, selo, estado ou
plataforma mudou. Antes dela, a 1.0.8 tinha acrescentado a grade do celular:
`LarguraCartao = 140`, `ColunasCanalVertical = 2` e `ColunasCanalHorizontal = 4` em
`ReboGrade.Ios` e no espelho `ReboGrade.Celular`. A 1.0.7, antes dessa, tinha corrigido a
convenção do espaço no Kotlin, que dizia `ReboSpace.x<n>` quando o gerador sempre emitiu
`ReboSpace.N<indice>` de `N0` a `N9`, e fez o `conferir.mjs` rodar dentro de um cliente que só
recebe Kotlin, sem o `rebobina-textos.json`.

O Kotlin que o app usa é gerado, nunca escrito à mão, e são dois arquivos:

- `ReboTokensGerado.kt`, 12.682 bytes, 20 objetos com prefixo `Rebo`: `ReboColor`,
  `ReboColorClaro`, `ReboType`, `ReboTypeCelular`, `ReboPeso`, `ReboTypePiso`,
  `ReboTypePisos`, `ReboTypeNumeros`, `ReboSpace`, `ReboSpaceUso`, `ReboRadius`, `ReboMotion`,
  `ReboMotionReducao`, `ReboSelo`, `ReboIconeTamanho`, `ReboTraco`, `ReboEstadoApos`,
  `ReboEstadoCarregando`, `ReboSize` e `ReboGrade`;
- `ReboTextos.kt`, 2.996 bytes, os 5 estados (`carregando`, `semRede`, `erro`, `vazio`,
  `semPasse`), 3 selos e o grupo `interface` em `ReboTextos.Interface` (`Comum`, `Ficha`,
  `Player` e `Downloads`), do mesmo JSON.

Comando que gera os dois, e é o mesmo que o CI roda:

```
node design/tokens/gerar-kotlin.mjs \
  --pacote net.dynv6.rebobina.ui.tokens \
  --saida composeApp/src/commonMain/kotlin/net/dynv6/rebobina/ui/tokens/ReboTokensGerado.kt \
  --saida-textos composeApp/src/commonMain/kotlin/net/dynv6/rebobina/ui/tokens/ReboTextos.kt
```

Com a linha do pacote normalizada, os dois saem idênticos aos arquivos de referência do
canônico, que têm 12.683 e 2.997 bytes porque o pacote de lá é `rebobina.android.ui.base.rebo`,
um caractere mais longo que `net.dynv6.rebobina.ui.tokens`.

A trava roda em dois lugares com a mesma implementação, `scripts/conferir-tokens.mjs`:

```
node scripts/conferir-tokens.mjs      # local
./gradlew :composeApp:conferirTokens  # roda antes de todo compileKotlin e de check
```

Ela confere três coisas: o sha256 do JSON contra `rebobina-tokens.sha256`; que o
`ORIGEM.json` conta a mesma história do JSON (versão e sha256); e que os dois `.kt`
commitados são exatamente o que o gerador produz agora, comparados byte a byte. No GitHub
Actions um passo faz a mesma conferência, regera os dois arquivos no lugar e exige
`git diff --exit-code`, antes do Xcode, que é a parte cara da execução.

O `design/tokens/conferir.mjs` é o conferidor do repositório canônico copiado verbatim, não a
trava: ele fala com a API do GitHub, lista as cópias de `rebobina-ios`, `rebobina-android` e
`rebobina-webos` e, num cliente que só recebe Kotlin como este, avisa que não há
`rebobina-textos.json` ao lado dele e confere o resto sem as linhas de texto.

Para atualizar um valor de desenho: mudar o JSON na origem, subir a versão lá, copiar JSON,
sha256 e `gerar-kotlin.mjs` para cá, atualizar o `ORIGEM.json`, regerar os dois `.kt` e
commitar tudo junto. Valor novo não entra por edição no Kotlin gerado nem no JSON daqui: o
gerador recusa quando a versão do `ORIGEM.json` não casa com a do JSON.

A tela agora consome esses objetos: nenhuma tela de `ui/telas` escreve cor, tamanho de fonte
ou `.dp`, e o que o JSON não publica (proporção de capa, alfa de véu, espessura de anel,
texto de rótulo) está declarado em um lugar só, `ui/base/ReboTokens.kt` e
`ui/base/ReboRotulos.kt`, com o motivo escrito. Detalhe na seção
[As cinco telas e a camada de desenho](#as-cinco-telas-e-a-camada-de-desenho).

## As cinco telas e a camada de desenho

A estrutura é a mesma do Android (commit `9ca65241a`), para as duas telas olharem o mesmo
desenho:

| tela | arquivo | o que desenha | dado |
|---|---|---|---|
| Início | `ui/telas/TelaInicio.kt` | palavra do app, usuário logado, trilho de continuar assistindo, grade de adicionados recentemente | `/Users/{id}/Items` com `Filters=IsResumable` e a mesma rota com `SortBy=DateCreated` |
| Ficha | `ui/telas/TelaFicha.kt` | fundo, capa, título, meta, sinopse, abas Episódios / Elenco / Similares | `/Users/{id}/Items/{itemId}`, `/Episodes` e `/Similar` |
| Canais | `ui/telas/TelaCanais.kt` | grade de canais ao vivo com selo e programa atual | `/LiveTv/Channels` |
| Busca | `ui/telas/TelaBusca.kt` | campo, sugestões e resultados em linha | `/Users/{id}/Items` com `SearchTerm`, depois de 350 ms e 2 caracteres |
| Ajustes | `ui/telas/TelaAjustes.kt` | endereço e chave do servidor, salvar e testar conexão | `/Users`, só no botão de testar |
| Downloads | `ui/telas/TelaDownloads.kt` | fila vazia declarada, sem inventar download | nenhum: o app não baixa nesta rodada |

A camada de desenho, toda em `commonMain/ui/base/`:

- `ReboTokens.kt`: apelidos dos tokens gerados (`ReboDimensao`, `ReboShape`, `ReboEstilo`,
  `ReboProporcao`, `ReboAlfa`) e `reboCorDoNome`, que traduz o nome de cor que o JSON guarda
  em texto para `ReboColor`;
- `ReboAdaptativo.kt`: a faixa de layout, `largura < 600 dp` ou `altura < 480 dp` vira
  celular, mais as colunas de catálogo e de canal lidas de `ReboGrade.Ios`;
- `ReboComponentes.kt`: botão, campo, selo, fio, imagem remota, barra de progresso, avatar,
  chip, cabeçalho, grade, fileira, cartão, linha, abas e item de aba;
- `ReboEstado.kt`: carregando, falha e vazio, com o texto de `ReboTextos.kt` gerado;
- `ReboTema.kt`: `MaterialTheme` escuro mapeado nos tokens, só como casca: as telas leem
  `ReboColor`, não `MaterialTheme`;
- `ReboRotulos.kt`: os rótulos de navegação, campo e seção que o JSON canônico ainda não
  publica: 43 constantes depois da 1.1.0, contra 59 antes dela.

Limites declarados desta rodada, sem invenção de dado:

- o player e o download não existem: os botões mostram `ReboRotulos.PENDENTE_REPRODUTOR`;
- `ReboRotulos` e `FormatoDeTexto` (duração, temporada, episódio, tipo de mídia e separador)
  continuam texto escrito à mão porque o JSON 1.1.0 não tem frase de tela fora de `estado.*`,
  `selo.variantes.*.texto` e `interface.*`: nas outras chaves com texto o conteúdo é
  documentação da folha (`meta`, `canonico`, `consumidores`, `geracao`, `tipo`, `movimento`) ou
  valor (`cor` em hexadecimal, `plataforma` em unidade e escala). A 1.1.0 permitiu apagar 16
  constantes locais e ler o gerado; `SEM_SINOPSE` ficou porque o valor local diz "no acervo" e
  o canônico não, e trocar a frase é decisão de produto;
- o tema claro (`ReboColorClaro`) existe no gerado e não é usado: esta rodada é escura só;
- as imagens de capa são buscadas em 300 px e as de fundo em 1200 px, valores escolhidos
  aqui e não vindos do JSON: o token de tamanho de arte não existe;
- o ATS do `Info.plist` libera `NSAllowsArbitraryLoads` para o Jellyfin em HTTP na rede
  local. É risco aceito de sideload, não recomendação: o IPA é sem assinatura e a chave do
  servidor fica no `NSUserDefaults` do aparelho, nunca no repositório;
- os 7 testes de `ReboAdaptativoTest` existem e nunca rodaram: o CI compila
  `:composeApp:compileKotlinIosArm64` e `:composeApp:compileAndroidMain`, não a suíte, e esta
  máquina não tem JDK nem Gradle para rodar `iosSimulatorArm64Test`;
- nenhuma tela foi vista em aparelho nesta rodada: o binário do run #11 tem 29.581.904 bytes e
  contém, em UTF-16LE, as strings `Séries` (deslocamento 28749106), `Ainda carregando`,
  `Tentar de novo`, `Sem conexão` e, em UTF-8 e UTF-16LE, `Ajustes`, `Downloads` e `Assistir`,
  o que prova que o texto do canônico 1.1.0 entrou no binário, não que a tela desenha ou que o
  servidor responde.

A trava do desenho roda local e no CI, antes do build:

```
node scripts/conferir-desenho.mjs
```

Ela reprova `Color(0x...)`, `Color.<nome>`, hexadecimal e `.dp`/`.sp` literal em
`ui/telas`, exige `ReboRotulos` e um componente de estado em cada tela (Ajustes de fora,
porque o estado dela é o resultado de "Testar conexão") e, no fim, chama a trava dos tokens.
Saída desta rodada: `desenho: ok, 6 telas, nenhum literal de cor/tamanho em ui/telas` e
`trava dos tokens: ok, versao 1.1.0`. No CI ela é dependência das tarefas `compileKotlin*` e
`check`, então o log do run #11 mostra `> Task :composeApp:conferirDesenho` e a mesma linha
duas vezes, no passo "Compilar Kotlin (iOS arm64)" e dentro do archive. A trava é textual:
ela não prova pixel nem beleza, e a comparação com o Android só valeria com captura de tela
dos dois lados, que esta rodada não tem.

## Trocar o endereço do servidor

Um lugar só: `composeApp/src/commonMain/kotlin/net/dynv6/rebobina/config/RebobinaConfig.kt`.
O `Info.plist` tem a liberação de HTTP para rede local, que não precisa mudar se o IP mudar,
porque `NSAllowsLocalNetworking` cobre a rede local inteira.

## Rodar no Mac, se precisar

Precisa de **Xcode 26 ou mais novo**, porque o Compose Multiplatform 1.11.1 chama uma
classe que só existe no SDK do iOS 26 (com Xcode 16.4 o link do app falha em
`UIViewLayoutRegion`), e de JDK 21. No binário a referência a essa classe sai como weak
import, então o SDK novo é exigência do link no Mac, não do aparelho.

O app roda em iOS 17 ou mais novo: o alvo mínimo do projeto Xcode é 17.0 desde 23/11/2026,
quando o Ramses decidiu o mínimo 17 (antes era 16.0).

```
cd iosApp
open iosApp.xcodeproj
```

Escolha o simulador e rode. O Xcode chama o Gradle sozinho pela fase de build
`Compilar framework Kotlin`. Para aparelho de verdade, escolha o time de assinatura em
**Signing & Capabilities**.
