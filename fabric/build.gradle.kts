import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	id("org.jetbrains.kotlin.jvm")
	id("me.modmuss50.mod-publish-plugin")
}

repositories {
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
	modImplementation("maven.modrinth:${project.property("cobblemon_trainers_project")}:$cobblemonTrainersVersion-fabric") {
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

/**
 * Release type, overridable from the command line so the release workflow drives the build
 * without editing a tracked file: `./gradlew publishMods -Prelease_type=beta`.
 */
val releaseType = when (providers.gradleProperty("release_type").getOrElse("stable").lowercase()) {
	"alpha" -> ReleaseType.ALPHA
	"beta" -> ReleaseType.BETA
	else -> ReleaseType.STABLE
}

// Captured out here on purpose: inside `publishMods`, `version` is the extension's own
// property, and interpolating it yields its Gradle description rather than the number.
val modVersion = project.version.toString()
val publishedVersion = "$modVersion-fabric"

publishMods {
	// `remapJar`, not `jar`: the latter still carries named mappings and would crash outside a
	// development environment. CI hands over the jar it already built and checksummed instead.
	file = providers.gradleProperty("release_file")
		.map { rootProject.layout.projectDirectory.file(it) }
		.orElse(tasks.named<RemapJarTask>("remapJar").flatMap { it.archiveFile })
	displayName = publishedVersion
	version = publishedVersion
	type = releaseType
	modLoaders.add("fabric")

	// The release workflow passes the GitHub release body here. Without it - a local run - the
	// changelog is a pointer rather than a lie.
	changelog = providers.environmentVariable("CHANGELOG")
		.orElse("See https://github.com/matheo-1712/cobblemon-battle-phone/releases/tag/v$modVersion")

	// A missing token turns the whole thing into a rehearsal written to build/mod-publish.
	dryRun = providers.environmentVariable("MODRINTH_TOKEN").orNull == null

	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN").orElse("dry-run")
		projectId = providers.gradleProperty("modrinth_id")
		minecraftVersions.add(providers.gradleProperty("minecraft_version").get())
		// The phone has a server half (the listing, the calls) and a client half (the screen).
		environment = CLIENT_AND_SERVER

		// Pinned to the exact versions the mod is built against, like Cobblemon Trainers does.
		requires {
			slug = "cobblemon"
			version = providers.gradleProperty("cobblemon_version")
		}
		requires {
			slug = "fabric-api"
			version = providers.gradleProperty("fabric_api_version")
		}
		requires {
			slug = "fabric-language-kotlin"
			version = providers.gradleProperty("fabric_kotlin_version")
		}
		// Not pinned: a pinned dependency makes launchers install that exact version, and this
		// mod runs with any Cobblemon Trainers from 2.0.0-alpha on. The minimum is enforced by
		// fabric.mod.json, where the loader reads it.
		requires(providers.gradleProperty("cobblemon_trainers_project").get())
	}
}
