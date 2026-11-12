pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
		id("org.jetbrains.kotlin.jvm") version providers.gradleProperty("kotlin_version")
		id("net.neoforged.moddev") version providers.gradleProperty("moddev_version")
		id("me.modmuss50.mod-publish-plugin") version providers.gradleProperty("publish_plugin_version")
	}
}

rootProject.name = "cobblemon-battlephone"

include("common", "fabric", "neoforge")
