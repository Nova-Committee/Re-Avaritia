# Build and test Re:Avaritia

[简体中文](building_CN.md) · [Project home](../README.md)

Run `bash build-targets.sh <target>` from the repository root. The default Gradle task is `build`, which also runs the target's unit tests. The script accepts Gradle arguments after the target name. Every target has its own Wrapper and dependencies.

| Game and loader | Target | Gradle JDK / Java toolchain | Wrapper | Main JAR task | Data entry |
| --- | --- | --- | --- | --- | --- |
| Forge 1.20.1 | [`forge`](../targets/forge/) | 17 / 17 | 8.12 | `jar` | `runData` |
| Fabric 1.20.1 | [`fabric`](../targets/fabric/) | 17 / 17 | 8.12 | `remapJar` | [Retained resource exporter](#fabric-data-export) |
| NeoForge 1.21.1 | [`neoforge-1.21.1`](../targets/neoforge-1.21.1/) | 21 / 21 | 8.8 | `jar` | `runData` |
| NeoForge 1.21.11 | [`neoforge-1.21.11`](../targets/neoforge-1.21.11/) | 25 / 21 | 9.1.0 | `jar` | `runClientData`, `runServerData` |
| NeoForge 26.1.2 | [`neoforge-26.1.2`](../targets/neoforge-26.1.2/) | 25 / 25 | 9.1.0 | `jar` | `runClientData`, `runServerData` |
| Fabric 26.3 | [`fabric-26.3`](../targets/fabric-26.3/) | 25 / 25 | 9.7.1 | `jar` | [Retained resource exporter](#fabric-data-export) |
| NeoForge 26.3 | [`neoforge-26.3`](../targets/neoforge-26.3/) | 25 / 25 | 9.1.0 | `jar` | `runClientData`, `runServerData` |

## Common commands

Set the following variables when building all seven targets. A single target can also use the matching `JAVA_HOME` or `java` on `PATH`. NeoForge 1.21.11 needs both JDK 25 to start Gradle and JDK 21 to compile and run the game. Gradle can discover the latter when `AVARITIA_JAVA21_HOME` is unset. Set `GRADLE_USER_HOME` to reuse a cache across targets.

```bash
export AVARITIA_JAVA17_HOME=/path/to/jdk-17
export AVARITIA_JAVA21_HOME=/path/to/jdk-21
export AVARITIA_JAVA25_HOME=/path/to/jdk-25
bash build-targets.sh all
bash build-targets.sh neoforge-1.21.11 test
bash build-targets.sh fabric build --offline
bash build-targets.sh fabric-26.3 runClient
bash build-targets.sh forge runServer
```

Each target writes its main JAR to `targets/<target>/build/libs/`. The main JAR follows `Re-Avaritia-<loader>-<game>-<mod-version>-<channel>.jar`. `-sources`, `-javadoc` and `-api` identify supplementary artifacts when the target produces them. Fabric 26.3 uses unobfuscated Minecraft and builds its main JAR with `jar`. Use `remapJar` on Fabric 1.20.1.

The [build workflow](../.github/workflows/build.yml) reads the target list and JDKs from `bash build-targets.sh --matrix`. It builds and tests each target and uploads the JARs and JUnit XML. The legacy per-branch release workflows are available in the [maintenance branch history](#maintenance-branches).

## Source ownership

- `targets/<target>/src/main` holds loader- and version-specific code, resources, metadata and templates. `src/generated/resources` holds checked-in generator output or retained resources used by that target. `src/test` holds unit tests. Some targets also provide `src/gameTest`.
- [Shared Java](../shared/src/main/java/) holds `PortableLayout`. [Shared resources](../shared/src/main/resources/) hold the main compressed chest texture. Target-specific atlases, models and embedded resource packs stay with their target. [Shared source checks](../gradle/shared-sources.gradle) reject copies that would shadow either shared file.

## Data generation

Forge 1.20.1 and NeoForge 1.21.1 use `runData`. NeoForge 1.21.11, 26.1.2 and 26.3 register their providers in the client data run, so `runClientData` writes the files. Use `runServerData` to exercise the server data entry. By default these Forge and NeoForge tasks write under the chosen target's `src/generated/resources/`. Review the changes before committing generated files.

```bash
bash build-targets.sh forge runData
bash build-targets.sh neoforge-1.21.1 runData
bash build-targets.sh neoforge-1.21.11 runClientData
```

### Fabric data export

The two Fabric targets keep their source data under `src/main/resources/` and `src/generated/resources/`. [`ModDataGen` (1.20.1)](../targets/fabric/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) and [`ModDataGen` (26.3)](../targets/fabric-26.3/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) export those resources and the shared chest texture. To use either entry point, run its `main` on the target's Gradle `main` runtime classpath with two arguments: the absolute target directory and a separate output directory. For example, save this Gradle init script outside the source directories as `export-fabric.gradle`.

```groovy
gradle.projectsEvaluated {
    def target = gradle.rootProject
    target.tasks.register('exportRetainedResources', JavaExec) {
        dependsOn target.tasks.named('classes')
        classpath = target.sourceSets.main.runtimeClasspath
        mainClass.set('committee.nova.mods.avaritia.init.data.ModDataGen')
        args target.projectDir.absolutePath,
                target.layout.buildDirectory.dir('retained-resources').get().asFile.absolutePath
    }
}
```

```bash
bash build-targets.sh fabric exportRetainedResources -I /path/to/export-fabric.gradle
bash build-targets.sh fabric-26.3 exportRetainedResources -I /path/to/export-fabric.gradle
```

## Forge addon dependencies

For a Forge 1.20.1 addon built with ForgeGradle, set `avaritia_version` to the full artifact version, for example `1.20.1-1.4.4-release`.

```groovy
repositories { maven { url 'https://maven.nova-committee.cn/releases' } }
dependencies { implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}") }
```

For a local build, run `bash build-targets.sh forge publishToMavenLocal` and add `mavenLocal()` to the addon's repositories.

## Maintenance branches

These are the revisions originally imported into the combined tree. They contain the standalone targets' original CI and release workflows under `.github/`.

- [Forge 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb/.github)
- [Fabric 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5/.github)
- [NeoForge 1.21.1](https://github.com/Nova-Committee/Re-Avaritia/tree/5543b95c138b2cd692f90764122a50652a5d3e8c/.github)
- [NeoForge 1.21.11](https://github.com/Nova-Committee/Re-Avaritia/tree/dbd9e5b60524e97dacfc6160ad87ea7f8cf3dccf/.github)
- [NeoForge 26.1.2](https://github.com/Nova-Committee/Re-Avaritia/tree/86c930fc47a386159afcb870830f4634927a6f04/.github)
- [Fabric 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/a44c1aae9558aa6b8b24c7a869b42c9716607711/.github)
- [NeoForge 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/71763cb63e3e1558d0e3bf3b1f122bcf704af96c/.github)
