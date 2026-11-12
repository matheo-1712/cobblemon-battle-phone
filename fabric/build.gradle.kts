import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	id("org.jetbrains.kotlin.jvm")
}

repositories {
	mavenLocal { content { includeGroup("matheo1712.cobbletrainers") } }
	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
		content { includeGroup("maven.modrinth") }
	}
}

/**
 * The Cobblemon archive on its own, so the libraries nested inside it can be unpacked for the
 * dev game - see `cobblemonLibs` below, and the same task in Cobblemon Trainers.
 */
val cobblemonArchive: Configuration by configurations.creating { isTransitive = false }

// Compile against common in development and merge its classes into the Fabric jar.
val commonBundle: Configuration by configurations.creating {
	isCanBeConsumed = false
	isTransitive = false
}

group = rootProject.group
version = rootProject.version

base {
	archivesName.set(rootProject.name)
}

evaluationDependsOn(":common")
val commonSources = project(":common").extensions.getByType<SourceSetContainer>()
loom {
	runs.configureEach {
		generateRunConfig.set(true)
		preferGradleTask.set(true)
		appendProjectPathToDisplayName.set(false)
		displayName.set(if (name == "client") "Minecraft Client (Fabric)" else "Minecraft Server (Fabric)")
	}
	mods {
		create("cobblemon_battlephone") {
			sourceSet(sourceSets.main.get())
			sourceSet(commonSources.named("main").get())
		}
	}
}
configurations.named("compileOnly") { extendsFrom(commonBundle) }

val cobblemonTrainersVersion = providers.gradleProperty("cobblemon_trainers_version").get()

dependencies {
	commonBundle(project(path = ":common", configuration = "namedElements"))
	runtimeOnly(files(commonSources.named("main").map { it.output }))
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
	modImplementation("maven.modrinth:cobblemon:${project.property("cobblemon_version")}")
	// The mod this one is built on. Also what the dev game runs it next to.
	modImplementation("matheo1712.cobbletrainers:cobblemon-trainers-fabric:$cobblemonTrainersVersion") {
		isTransitive = false
	}
	cobblemonArchive("maven.modrinth:cobblemon:${project.property("cobblemon_version")}")
}

/**
 * Cobblemon's nested libraries (GraalJS and friends), unpacked onto the dev run classpath. Loom
 * does not put a mod's JiJ jars there, and without them the Showdown thread dies on the first
 * battle. Same task, same reasons as in Cobblemon Trainers.
 */
val cobblemonLibsDir = layout.buildDirectory.dir("cobblemon-libs")

val cobblemonLibs = tasks.register<Sync>("cobblemonLibs") {
	description = "Unpacks the libraries nested in the Cobblemon jar, for the dev run classpath."
	group = "fabric"

	from(cobblemonArchive.elements.map { archives -> archives.map { zipTree(it) } }) {
		include("META-INF/jars/*.jar")
		exclude("**/fabric-language-kotlin-*.jar")
		eachFile { relativePath = RelativePath(true, name) }
	}
	into(cobblemonLibsDir)
	includeEmptyDirs = false
}

dependencies {
	localRuntime(fileTree(cobblemonLibsDir) {
		include("*.jar")
		builtBy(cobblemonLibs)
	})
}

tasks.processResources {
	val version = version
	inputs.property("version", version)
	inputs.property("cobblemonTrainersVersion", cobblemonTrainersVersion)

	filesMatching("fabric.mod.json") {
		expand("version" to version, "cobblemon_trainers_version" to cobblemonTrainersVersion)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
	}
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	val projectName = rootProject.name
	inputs.property("projectName", projectName)

	from(commonBundle.elements.map { files -> files.map { zipTree(it.asFile) } }) {
		exclude("META-INF/MANIFEST.MF")
	}

	from(rootProject.layout.projectDirectory.file("LICENSE")) {
		rename { "${it}_$projectName" }
	}
}

tasks.named<Jar>("sourcesJar") {
	from(commonSources.named("main").map { it.allSource })
}
