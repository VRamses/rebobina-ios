#!/bin/sh
# Constroi o framework Kotlin do modulo composeApp e o entrega ao Xcode.
#
# Roda dentro de uma fase de build do Xcode, entao depende das variaveis que o
# Xcode exporta: SRCROOT, CONFIGURATION, SDK_NAME.
#
# Faz o link do framework estatico com a tarefa de Gradle correspondente e copia
# o resultado para o diretorio que o FRAMEWORK_SEARCH_PATHS do projeto aponta.
# Nao assina nada: o IPA sai sem assinatura e o Sideloadly/AltStore assina depois.
set -eu

if [ "${OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED:-NO}" = "YES" ]; then
  echo "Pulando o Gradle: OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES"
  exit 0
fi

cd "$SRCROOT/.."
chmod +x gradlew

CONFIGURACAO="${CONFIGURATION:-Debug}"
case "$CONFIGURACAO" in
  Debug)
    KONFIG="Debug"
    PASTA_CONFIG="debug"
    ;;
  *)
    KONFIG="Release"
    PASTA_CONFIG="release"
    ;;
esac

case "${SDK_NAME:-iphoneos}" in
  iphonesimulator*)
    ALVO_KOTLIN="iosSimulatorArm64"
    ;;
  *)
    ALVO_KOTLIN="iosArm64"
    ;;
esac

# iosArm64 -> linkReleaseFrameworkIosArm64
TAREFA=":composeApp:link${KONFIG}FrameworkIos${ALVO_KOTLIN#ios}"
echo "Gradle: $TAREFA (CONFIGURATION=$CONFIGURACAO SDK_NAME=${SDK_NAME:-vazio})"

./gradlew "$TAREFA" --no-configuration-cache --console=plain

ORIGEM="composeApp/build/bin/${ALVO_KOTLIN}/${PASTA_CONFIG}Framework/ComposeApp.framework"
DESTINO="composeApp/build/xcode-frameworks/${CONFIGURATION}/${SDK_NAME}"

if [ ! -d "$ORIGEM" ]; then
  echo "ERRO: framework nao encontrado em $ORIGEM"
  ls -la "composeApp/build/bin/${ALVO_KOTLIN}" || true
  exit 1
fi

mkdir -p "$DESTINO"
rm -rf "${DESTINO}/ComposeApp.framework"
cp -R "$ORIGEM" "${DESTINO}/ComposeApp.framework"
echo "Framework pronto: ${DESTINO}/ComposeApp.framework"
ls -la "${DESTINO}/ComposeApp.framework"
