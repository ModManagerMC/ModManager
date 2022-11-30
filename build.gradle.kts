plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("fabric-loom")
}

group = "net.modmanagermc"
version = "2.0.0"

repositories {
    maven("https://maven.fabricmc.net") {
        name = "Fabric"
    }
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    mavenCentral()
}

val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")

    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("com.terraformersmc:modmenu:3.2.1")
    includeMod("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")
    includeProject(project(":core"))
}

fun DependencyHandler.includeMod(dependencyNotation: Any) {
    modImplementation(dependencyNotation)
    include(dependencyNotation)
}

fun DependencyHandler.includeProject(dependencyNotation: Any) {
    implementation(dependencyNotation)
    include(dependencyNotation)
}

tasks.getByName<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "version" to version,
                "fabricKotlinVersion" to fabricKotlinVersion
            )
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}