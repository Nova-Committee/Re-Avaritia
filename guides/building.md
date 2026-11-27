# Build and test Re:Avaritia

[简体中文](building_CN.md) · [Project home](../README.md)

Run `./gradlew build` from the repository root to build and test all seven targets. The root Wrapper uses Gradle 9.7.1 on Java 25. Each Stonecutter node keeps its original Forge, NeoForge or Fabric build plugin and Java toolchain.

| Game and loader | Target | Java toolchain | Main JAR task | Data entry |
| --- | --- | --- | --- | --- |
| Forge 1.20.1 | [`forge`](../targets/forge/) | 17 | `jar` | `runData` |
| Fabric 1.20.1 | [`fabric`](../targets/fabric/) | 17 | `remapJar` | [Retained resource exporter](#fabric-data-export) |
| NeoForge 1.21.1 | [`neoforge-1.21.1`](../targets/neoforge-1.21.1/) | 21 | `jar` | `runData` |
| NeoForge 1.21.11 | [`neoforge-1.21.11`](../targets/neoforge-1.21.11/) | 21 | `jar` | `runClientData`, `runServerData` |
| NeoForge 26.1.2 | [`neoforge-26.1.2`](../targets/neoforge-26.1.2/) | 25 | `jar` | `runClientData`, `runServerData` |
| Fabric 26.3 | [`fabric-26.3`](../targets/fabric-26.3/) | 25 | `jar` | [Retained resource exporter](#fabric-data-export) |
| NeoForge 26.3 | [`neoforge-26.3`](../targets/neoforge-26.3/) | 25 | `jar` | `runClientData`, `runServerData` |

## Common commands

Set `JAVA_HOME` to a JDK 25 installation. Gradle discovers JDKs 17 and 21 or reads their paths from the variables below. Legacy Forge ModDevGradle also uses JDK 21 for NeoForm Runtime. The root `build` runs Fabric 1.20.1 in a separate Gradle build scope because Loom 1.10.5 cannot remap with Loom 1.18 loaded as a sibling.

```bash
export AVARITIA_JAVA17_HOME=/path/to/jdk-17
export AVARITIA_JAVA21_HOME=/path/to/jdk-21
export JAVA_HOME=/path/to/jdk-25
./gradlew build
./gradlew -Ptarget=neoforge-1.21.11 :neoforge-1.21.11:test
./gradlew -Ptarget=fabric :fabric:build --offline
./gradlew -Ptarget=fabric-26.3 :fabric-26.3:runClient
./gradlew -Ptarget=forge :forge:runServer
```

Each target writes its main JAR to `targets/<target>/build/libs/`. The main JAR follows `Re-Avaritia-<loader>-<game>-<mod-version>-<channel>.jar`. `-sources`, `-javadoc` and `-api` identify supplementary artifacts when the target produces them. Fabric 26.3 uses unobfuscated Minecraft and builds its main JAR with `jar`. Use `remapJar` on Fabric 1.20.1.

The [build workflow](../.github/workflows/build.yml) reads the same [target list](../gradle/targets.tsv) as the root settings. Each job selects one node and uploads its JARs and JUnit XML. Earlier release workflows are in the [maintenance branch history](#maintenance-branches).

## Source ownership

- `targets/<target>/src/main` holds loader- and version-specific code, resources, metadata and templates. `src/generated/resources` holds checked-in generator output or retained resources used by that target. `src/test` holds unit tests. Some targets also provide `src/gameTest`.
- [Shared Java](../shared/src/main/java/) holds `PortableLayout`. [Shared resources](../shared/src/main/resources/) hold the main compressed chest texture. Target-specific atlases, models and embedded resource packs stay with their target. [Shared source checks](../gradle/shared-sources.gradle) reject copies that would shadow either shared file.
- [Stonecutter sources](../src/main/java/) hold the versioned `ResourceUtil` shared by Forge and Fabric. The [controller](../stonecutter.gradle.kts) generates the `Identifier` variant for Fabric 26.3.

## Data generation

Forge 1.20.1 and NeoForge 1.21.1 use `runData`. NeoForge 1.21.11, 26.1.2 and 26.3 register their providers in the client data run, so `runClientData` writes the files. Use `runServerData` to exercise the server data entry. By default these Forge and NeoForge tasks write under the chosen target's `src/generated/resources/`. Review the changes before committing generated files.

```bash
./gradlew -Ptarget=forge :forge:runData
./gradlew -Ptarget=neoforge-1.21.1 :neoforge-1.21.1:runData
./gradlew -Ptarget=neoforge-1.21.11 :neoforge-1.21.11:runClientData
```

### Fabric data export

The two Fabric targets keep their source data under `src/main/resources/` and `src/generated/resources/`. [`ModDataGen` (1.20.1)](../targets/fabric/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) and [`ModDataGen` (26.3)](../targets/fabric-26.3/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) export those resources and the shared chest texture. To use either entry point, run its `main` on the target's Gradle `main` runtime classpath with two arguments: the absolute target directory and a separate output directory. For example, save this Gradle init script outside the source directories as `export-fabric.gradle`.

```groovy
gradle.projectsEvaluated {
    def target = gradle.rootProject.project(":" + gradle.startParameter.projectProperties.target)
    target.tasks.register('exportRetainedResources', JavaExec) {
        dependsOn target.tasks.named('classes')
        classpath = target.sourceSets.main.runtimeClasspath
        javaLauncher = target.javaToolchains.launcherFor(target.java.toolchain)
        mainClass.set('committee.nova.mods.avaritia.init.data.ModDataGen')
        args target.projectDir.absolutePath,
                target.layout.buildDirectory.dir('retained-resources').get().asFile.absolutePath
    }
}
```

```bash
./gradlew -Ptarget=fabric :fabric:exportRetainedResources -I /path/to/export-fabric.gradle
./gradlew -Ptarget=fabric-26.3 :fabric-26.3:exportRetainedResources -I /path/to/export-fabric.gradle
```

## Forge addon dependencies

For a Forge 1.20.1 addon built with ForgeGradle, set `avaritia_version` to the full artifact version, for example `1.20.1-1.4.4-release`.

```groovy
repositories { maven { url 'https://maven.nova-committee.cn/releases' } }
dependencies { implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}") }
```

For a local build, run `./gradlew -Ptarget=forge :forge:publishToMavenLocal` and add `mavenLocal()` to the addon's repositories.

## Maintenance branches

These are the revisions originally imported into the combined tree. They contain the standalone targets' original CI and release workflows under `.github/`.

- [Forge 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb/.github)
- [Fabric 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5/.github)
- [NeoForge 1.21.1](https://github.com/Nova-Committee/Re-Avaritia/tree/5543b95c138b2cd692f90764122a50652a5d3e8c/.github)
- [NeoForge 1.21.11](https://github.com/Nova-Committee/Re-Avaritia/tree/dbd9e5b60524e97dacfc6160ad87ea7f8cf3dccf/.github)
- [NeoForge 26.1.2](https://github.com/Nova-Committee/Re-Avaritia/tree/86c930fc47a386159afcb870830f4634927a6f04/.github)
- [Fabric 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/a44c1aae9558aa6b8b24c7a869b42c9716607711/.github)
- [NeoForge 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/71763cb63e3e1558d0e3bf3b1f122bcf704af96c/.github)
