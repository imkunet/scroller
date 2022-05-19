import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.lwjgl.Lwjgl

plugins {
    kotlin("jvm") version "1.6.21"
    id("org.lwjgl.plugin") version "0.0.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.kunet"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val skikoVersion = "0.7.20"
val target = "${targetOs}-${targetArch}"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$skikoVersion")
    Lwjgl { implementation(Lwjgl.Preset.minimalOpenGL) }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    withType<ShadowJar> {
        exclude("META-INF/**")
        exclude("DebugProbesKt.bin")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Jar> {
        manifest {
            attributes("Main-Class" to "dev.kunet.scroller.ScrollerKt")
        }
    }
}