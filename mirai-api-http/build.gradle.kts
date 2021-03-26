plugins {
    id("kotlinx-serialization")
    kotlin("jvm")
    id("net.mamoe.mirai-console") version "2.5.0"
}

val httpVersion: String by rootProject.ext

val ktorVersion: String by rootProject.ext
val serializationVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) =
    "org.jetbrains.kotlinx:kotlinx-$id:$version"


fun ktor(id: String, version: String = this@Build_gradle.ktorVersion) = "io.ktor:ktor-$id:$version"


kotlin {
    sourceSets["test"].apply {
        dependencies {
            api("org.slf4j:slf4j-simple:1.7.26")
        }
    }

    sourceSets.all {
        languageSettings.enableLanguageFeature("InlineClasses")
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")

        dependencies {
            api(kotlinx("serialization-json", serializationVersion))
            implementation("net.mamoe:mirai-core-utils:${mirai.coreVersion}")

            api(ktor("server-cio"))
            api(ktor("http-jvm"))
            api(ktor("websockets"))
            api("org.yaml:snakeyaml:1.25")

            implementation(ktor("server-core"))
            implementation(ktor("http"))
        }
    }
}

project.version = httpVersion

description = "Mirai HTTP API plugin"

internal val EXCLUDED_FILES = listOf(
    "kotlin-stdlib-.*",
    "kotlin-reflect-.*",
    "kotlinx-serialization-json.*",
    "kotlinx-coroutines.*",
    "kotlinx-serialization-core.*",
    "slf4j-api.*"
).map { "^$it\$".toRegex() }

mirai {
    this.configureShadow {
        exclude { elm ->
            EXCLUDED_FILES.any { it.matches(elm.path) }
        }
    }
    publishing {
        repo = "mirai"
        packageName = "mirai-api-http"
        override = true
    }
}
tasks.create("buildCiJar", Jar::class) {
    dependsOn("buildPlugin")
    doLast {
        val buildPluginTask = tasks.getByName("buildPlugin", Jar::class)
        val buildPluginFile = buildPluginTask.archiveFile.get().asFile
        project.buildDir.resolve("ci").also {
            it.mkdirs()
        }.resolve("mirai-api-http.jar").let {
            buildPluginFile.copyTo(it, true)
        }
    }
}

