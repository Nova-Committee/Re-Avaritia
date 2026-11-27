plugins {
    base
    id("dev.kikugie.stonecutter")
}

tasks.named("build") {
    dependsOn(subprojects.map { "${it.path}:build" })
}

if (!providers.gradleProperty("target").isPresent) {
    val buildLegacyFabric by tasks.registering(GradleBuild::class) {
        group = "build"
        description = "Build Fabric 1.20.1 in a separate Gradle build scope."
        buildName = "legacy-fabric"
        tasks = listOf(":fabric:build")
        startParameter.projectProperties = gradle.startParameter.projectProperties + ("target" to "fabric")
        startParameter.projectCacheDir = layout.projectDirectory.dir(".gradle/legacy-fabric").asFile
    }
    tasks.named("build") { dependsOn(buildLegacyFabric) }
}

// ResourceUtil belongs to Forge and Fabric. Stonecutter filters skip rewriting,
// so use a source-set exclusion to keep it out of NeoForge's classes and source JARs.
subprojects {
    if (name.startsWith("neoforge-")) {
        pluginManager.withPlugin("java") {
            extensions.configure<SourceSetContainer> {
                named("main") { java.exclude("committee/nova/mods/avaritia/api/util/ResourceUtil.java") }
            }
        }
    }
}

stonecutter {
    parameters {
        replacements.string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
    }
}

// Keep shared sources in their checked-in form and generate all node variants under build/.
stonecutter active null
