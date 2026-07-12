import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("org.jetbrains.grammarkit") version "2023.3.0.3"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()
    }
    testImplementation("junit:junit:4.13.2")
}

kotlin { jvmToolchain(21) }

tasks {
    generateLexer {
        sourceFile.set(file("src/main/grammar/Drun.flex"))
        targetOutputDir.set(file("src/main/gen/com/phillarmonic/drun/lexer"))
        purgeOldFiles.set(true)
    }

    compileKotlin {
        dependsOn(generateLexer)
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    sourceSets.main { java.srcDir("src/main/gen") }

    patchPluginXml {
        sinceBuild.set(providers.gradleProperty("pluginSinceBuild"))
    }

    signPlugin {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin { token.set(providers.environmentVariable("PUBLISH_TOKEN")) }

    withType<VerifyPluginTask> { dependsOn(test) }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.phillarmonic.drun"
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        vendor {
            name = "Phillarmonic"
            url = "https://github.com/phillarmonic"
        }
        description = """
            Native syntax highlighting and xdrun language-server integration for the Drun automation language.
        """.trimIndent()
        ideaVersion { sinceBuild = providers.gradleProperty("pluginSinceBuild") }
    }
    pluginVerification {
        ides {
            recommended()
            select { sinceBuild = providers.gradleProperty("pluginSinceBuild") }
        }
    }
}
