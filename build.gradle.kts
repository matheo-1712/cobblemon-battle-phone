plugins {
    base
    id("org.jetbrains.kotlin.jvm") apply false
    id("net.fabricmc.fabric-loom-remap") apply false
}

// The root coordinates all modules; loader-specific tasks stay in their modules.
tasks.named("assemble") { dependsOn(":common:assemble", ":fabric:assemble", ":neoforge:assemble") }
tasks.named("check") { dependsOn(":common:check", ":fabric:check", ":neoforge:check") }
tasks.named("build") { dependsOn(":common:build", ":fabric:build", ":neoforge:build") }
tasks.named("clean") { dependsOn(":common:clean", ":fabric:clean", ":neoforge:clean") }

for (name in listOf("runClient", "runServer", "genSources")) {
    tasks.register(name) { dependsOn(":fabric:$name") }
}
