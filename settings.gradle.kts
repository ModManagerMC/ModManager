pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
    val kotlinVersion: String by settings
    val loomVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("fabric-loom") version loomVersion
    }
}
rootProject.name = "modmanager"

include("core")
