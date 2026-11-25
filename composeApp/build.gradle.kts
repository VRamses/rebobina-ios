import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // iPhone de verdade e simulador em Mac com chip Apple.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "net.dynv6.rebobina"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // O plugin de biblioteca Android para KMP desliga teste de host por padrao
        // (KotlinMultiplatformAndroidLibraryExtension.withHostTest). Sem isto o
        // commonTest nao tem onde rodar: os alvos iOS precisam de macOS, e o alvo
        // Android e o unico que executa teste na JVM.
        withHostTest {}

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        // O motor HTTP do iOS e o NSURLSession, que ja respeita o ATS do Info.plist.
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

// ---------------------------------------------------------------------------
// Trava dos tokens de desenho.
//
// design/tokens/ e copia byte a byte de rebobina-servidor (branch main) e a unica
// fonte de valor de cor, tipo, espaco, raio e movimento. O Kotlin de
// commonMain/ui/tokens/ReboTokensGerado.kt e gerado por design/tokens/gerar-kotlin.mjs.
//
// A conferencia mora em scripts/conferir-tokens.mjs para ser a mesma no Gradle e no
// GitHub Actions: ela confere o sha256 do JSON contra rebobina-tokens.sha256 e regera o
// Kotlin para comparar byte a byte com o arquivo commitado. Esta tarefa roda antes de
// qualquer compileKotlin e antes de check, entao o framework iOS nao sai com token fora
// de sincronia.
//
// A segunda trava, scripts/conferir-desenho.mjs, e a do desenho desta rodada: as telas
// nao podem escrever cor, tamanho ou tipo por conta propria, e o texto de estado tem de
// sair do ReboTextos.kt gerado.
// ---------------------------------------------------------------------------

val raizDoRepositorio = rootProject.projectDir

// No Windows o node do PATH e um .cmd, que o ProcessBuilder nao executa direito; o
// cmd.exe /c resolve. No macOS e no Linux o node roda como esta no PATH.
val comandoNode: List<String> =
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        listOf("cmd.exe", "/c", "node")
    } else {
        listOf("node")
    }

val conferirTokens = tasks.register<Exec>("conferirTokens") {
    group = "verification"
    description = "Confere a copia dos tokens e o Kotlin gerado (scripts/conferir-tokens.mjs)."
    workingDir = raizDoRepositorio
    setCommandLine(comandoNode + listOf("scripts/conferir-tokens.mjs"))
}

val conferirDesenho = tasks.register<Exec>("conferirDesenho") {
    group = "verification"
    description = "Confere o desenho das telas contra os tokens (scripts/conferir-desenho.mjs)."
    workingDir = raizDoRepositorio
    setCommandLine(comandoNode + listOf("scripts/conferir-desenho.mjs"))
}

tasks.matching { it.name.startsWith("compileKotlin") || it.name == "check" }
    .configureEach { dependsOn(conferirTokens, conferirDesenho) }
