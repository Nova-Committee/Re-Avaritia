pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("net.neoforged.moddev.legacyforge") version "2.0.131"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.5"
}

rootProject.name = "Re-Avaritia"

val targets = file("gradle/targets.tsv").readLines().map { it.split('\t') }.associateBy { it[0] }
val requested = providers.gradleProperty("target").orNull?.split(',')?.toSet()
require(requested == null || requested.all { it in targets }) {
    "Unknown target in $requested. Available: ${targets.keys.joinToString()}"
}
// Loom 1.10 scans sibling Loom projects during remapJar and cannot use Loom 1.18's extensions.
val selected = requested ?: (targets.keys - "fabric")
require(!selected.containsAll(listOf("fabric", "fabric-26.3"))) {
    "Build the two Fabric targets separately with -Ptarget=fabric and -Ptarget=fabric-26.3."
}

stonecutter {
    kotlinController = true
    create(rootProject) {
        for (name in selected) {
            version(name, targets.getValue(name)[1]).buildscript("targets/$name/build.gradle")
        }
    }
}

for (name in selected) {
    project(":$name").apply {
        projectDir = file("targets/$name")
        buildFileName = "build.gradle"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        for (name in selected) {
            val catalog = targets.getValue(name).getOrNull(3) ?: continue
            create(catalog) { from(files("targets/$name/gradle/libs.versions.toml")) }
        }
    }
}
