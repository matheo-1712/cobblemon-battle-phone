import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("net.fabricmc.fabric-loom-remap")
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
}

// Common is compiled against the Fabric builds of its dependencies, remapped to Mojang names by
// Loom, and never calls Fabric itself - `verifyLoaderIndependence` holds that line. NeoForge
// compiles these same sources again against its own jars.
dependencies {
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings(loom.officialMojangMappings())
    modCompileOnly("maven.modrinth:cobblemon:${providers.gradleProperty("cobblemon_version").get()}")
    // Same reason as in Cobblemon Trainers: Cobblemon's class hierarchy names a Fabric API
    // interface, and the remapper needs it on the classpath to see through it.
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    modCompileOnly("maven.modrinth:${providers.gradleProperty("cobblemon_trainers_project").get()}:${providers.gradleProperty("cobblemon_trainers_version").get()}-fabric") {
        isTransitive = false
    }
    compileOnly("net.fabricmc:sponge-mixin:${providers.gradleProperty("mixin_version").get()}")
    // A `modCompileOnly` never reaches the test runtime, and the payload tests encode Cobblemon
    // Trainers' own types: its remapped jar is taken from the compile classpath, where Loom put it.
    testRuntimeOnly(files(configurations.compileClasspath.map { classpath ->
        classpath.filter { "cobblemon-trainers" in it.name }
    }))
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:${providers.gradleProperty("junit_version").get()}")
}

configurations.testImplementation { extendsFrom(configurations.compileOnly.get()) }

tasks.test {
    val runDirectory = layout.buildDirectory.dir("test-run")
    workingDir(runDirectory)
    doFirst { runDirectory.get().asFile.mkdirs() }
}

loom {
    runs.clear()
}

// Only loader modules are distributed. They consume and remap the named common jar.
tasks.named("remapJar") { enabled = false }
tasks.named("remapSourcesJar") { enabled = false }

val verifyLoaderIndependence = tasks.register("verifyLoaderIndependence") {
    group = "verification"
    description = "Rejects loader-specific imports in shared sources."
    val sources = fileTree("src/main") { include("**/*.kt", "**/*.java") }
    inputs.files(sources)
    doLast {
        val forbidden = Regex("(?m)^\\s*(?:import|package)\\s+(?:net\\.fabricmc|net\\.neoforged|matheo1712\\.(?:cobbletrainers|battlephone)\\.(?:fabric|neoforge))\\b")
        val violations = sources.files.filter { forbidden.containsMatchIn(it.readText()) }
        check(violations.isEmpty()) { "Loader-specific sources in common: ${violations.joinToString()}" }
    }
}
tasks.named("check") { dependsOn(verifyLoaderIndependence) }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}
