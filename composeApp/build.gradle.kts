import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
// ---------------------------------------------------------------------------

val raizDoRepositorio = rootProject.projectDir

// No Windows o node do PATH e um .cmd, que o ProcessBuilder nao executa direto; o
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

tasks.matching { it.name.startsWith("compileKotlin") || it.name == "check" }
    .configureEach { dependsOn(conferirTokens) }
