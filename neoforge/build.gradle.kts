import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.neoforged.moddev")
    id("org.jetbrains.kotlin.jvm")
}

group = rootProject.group
version = rootProject.version
base { archivesName.set("${rootProject.name}-neoforge") }

repositories {
    mavenCentral()
    mavenLocal { content { includeGroup("matheo1712.cobbletrainers") } }
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
}

// Compile the same sources against NeoForge's patched Minecraft and Cobblemon API.
kotlin.sourceSets.main { kotlin.srcDir(rootProject.file("common/src/main/kotlin")) }
sourceSets.main {
    resources.srcDir(rootProject.file("common/src/main/resources"))
}

neoForge {
    version = providers.gradleProperty("neoforge_version").get()
    runs {
        create("client") { client() }
        create("server") { server(); programArgument("--nogui") }
    }
    mods { create("cobblemon_battlephone") { sourceSet(sourceSets.main.get()) } }
}

val cobblemonTrainersVersion = providers.gradleProperty("cobblemon_trainers_version").get()

dependencies {
    implementation("maven.modrinth:cobblemon:${providers.gradleProperty("cobblemon_neoforge_version").get()}")
    implementation("maven.modrinth:kotlin-for-forge:${providers.gradleProperty("kotlin_neoforge_version").get()}")
    implementation("matheo1712.cobbletrainers:cobblemon-trainers-neoforge:$cobblemonTrainersVersion") {
        isTransitive = false
    }
}

java { toolchain.languageVersion = JavaLanguageVersion.of(21); withSourcesJar() }
kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }
tasks.withType<JavaCompile>().configureEach { options.release = 21 }
tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("cobblemonTrainersVersion", cobblemonTrainersVersion)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version, "cobblemon_trainers_version" to cobblemonTrainersVersion)
    }
}
tasks.jar { from(rootProject.file("LICENSE")) { rename { "${it}_${rootProject.name}" } } }
