# 构建与测试 Re:Avaritia

[English](building.md) · [返回项目首页](../README_CN.md)

在仓库根目录运行 `./gradlew build`，构建并测试七个目标。根 Wrapper 用 Java 25 运行 Gradle 9.7.1，各 Stonecutter 节点继续使用本目标的 Forge、NeoForge 或 Fabric 插件与编译工具链。

| 游戏和加载器 | 目标 | Java 工具链 | 主 JAR 任务 | 数据入口 |
| --- | --- | --- | --- | --- |
| Forge 1.20.1 | [`forge`](../targets/forge/) | 17 | `jar` | `runData` |
| Fabric 1.20.1 | [`fabric`](../targets/fabric/) | 17 | `remapJar` | [保留资源导出](#fabric-数据导出) |
| NeoForge 1.21.1 | [`neoforge-1.21.1`](../targets/neoforge-1.21.1/) | 21 | `jar` | `runData` |
| NeoForge 1.21.11 | [`neoforge-1.21.11`](../targets/neoforge-1.21.11/) | 21 | `jar` | `runClientData`、`runServerData` |
| NeoForge 26.1.2 | [`neoforge-26.1.2`](../targets/neoforge-26.1.2/) | 25 | `jar` | `runClientData`、`runServerData` |
| Fabric 26.3 | [`fabric-26.3`](../targets/fabric-26.3/) | 25 | `jar` | [保留资源导出](#fabric-数据导出) |
| NeoForge 26.3 | [`neoforge-26.3`](../targets/neoforge-26.3/) | 25 | `jar` | `runClientData`、`runServerData` |

## 常用命令

将 `JAVA_HOME` 指向 JDK 25。Gradle 可自动探测 JDK 17 和 21，也可从下列变量读取路径。Legacy Forge ModDevGradle 另用 JDK 21 运行 NeoForm Runtime。根 `build` 在独立 Gradle 构建域中运行 Fabric 1.20.1，因为 Loom 1.10.5 与 Loom 1.18 加载在同一父项目下时无法执行重映射。

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

每个目标的主 JAR 位于 `targets/<目标>/build/libs/`，文件名形如 `Re-Avaritia-<加载器>-<游戏版本>-<模组版本>-<渠道>.jar`。带 `-sources`、`-javadoc` 或 `-api` 后缀的是部分目标提供的辅助产物。Fabric 26.3 使用无混淆的 Minecraft，主 JAR 由 `jar` 任务生成。Fabric 1.20.1 使用 `remapJar`。

[构建工作流](../.github/workflows/build.yml) 和根配置使用同一份[目标清单](../gradle/targets.tsv)。每个 CI 作业选择一个节点，构建、测试并归档 JAR 与 JUnit XML。历史发布工作流见[维护分支](#维护分支)。

## 源码归属

- `targets/<目标>/src/main` 放平台专用代码、资源、元数据与模板。`src/generated/resources` 放该目标使用的已提交生成资源或保留资源。`src/test` 放单元测试，部分目标另有 `src/gameTest`。
- [全局共享 Java](../shared/src/main/java/) 由七个目标直接编译。`shared/<组>/src/main/java/` 按目标清单分配给字节一致的实现，包含 `1.20.1`、`26.3`、`neoforge` 和 `neoforge-modern`（1.21.11、26.1.2、26.3）。在目标目录里维护有加载器或游戏接口差异的实现。
- [共享资源](../shared/src/main/resources/) 存放同路径、同字节的普通贴图和内嵌资源包贴图及动画元数据。运行时的内嵌资源包仍使用原路径，各目标保留自己的 `pack.mcmeta`、图集、模型和有差异的资源。[共享源检查](../gradle/shared-sources.gradle) 拒绝会覆盖共享文件的同路径副本。
- [Stonecutter 源码](../src/main/java/) 存放 Forge 和 Fabric 共用的 `ResourceUtil`。[控制器](../stonecutter.gradle.kts) 为 Fabric 26.3 生成 `Identifier` 版本。

## 数据生成

Forge 1.20.1 和 NeoForge 1.21.1 使用 `runData`。NeoForge 1.21.11、26.1.2 和 26.3 在客户端数据任务中注册生成器，由 `runClientData` 写出文件。`runServerData` 可验证服务端数据入口。Forge 与 NeoForge 数据任务默认写入所选目标的 `src/generated/resources/`，提交前先检查生成差异。

```bash
./gradlew -Ptarget=forge :forge:runData
./gradlew -Ptarget=neoforge-1.21.1 :neoforge-1.21.1:runData
./gradlew -Ptarget=neoforge-1.21.11 :neoforge-1.21.11:runClientData
```

### Fabric 数据导出

两个 Fabric 目标以 `src/main/resources/` 与 `src/generated/resources/` 内提交的资源为输入。[1.20.1 的 `ModDataGen`](../targets/fabric/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) 和 [26.3 的 `ModDataGen`](../targets/fabric-26.3/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) 会导出目标和共享目录中的 `assets`、`data` 及可用的 `pack.mcmeta`。在对应目标的 Gradle `main` 运行类路径上执行 `main`，传入目标目录和独立输出目录两个绝对路径。可以在源码目录外保存以下 Gradle 初始化脚本，命名为 `export-fabric.gradle`。

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

## Forge 附属模组依赖

为 Forge 1.20.1 的 ForgeGradle 附属模组配置仓库后，将 `avaritia_version` 设为完整产物版本，例如 `1.20.1-1.4.4-release`。

```groovy
repositories { maven { url 'https://maven.nova-committee.cn/releases' } }
dependencies { implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}") }
```

本地开发时运行 `./gradlew -Ptarget=forge :forge:publishToMavenLocal`，再加入 `mavenLocal()`。

## 维护分支

以下是合并构建树时导入的维护分支提交，原有独立工作流可在各提交的 `.github/` 查看。

- [Forge 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb/.github)
- [Fabric 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5/.github)
- [NeoForge 1.21.1](https://github.com/Nova-Committee/Re-Avaritia/tree/5543b95c138b2cd692f90764122a50652a5d3e8c/.github)
- [NeoForge 1.21.11](https://github.com/Nova-Committee/Re-Avaritia/tree/dbd9e5b60524e97dacfc6160ad87ea7f8cf3dccf/.github)
- [NeoForge 26.1.2](https://github.com/Nova-Committee/Re-Avaritia/tree/86c930fc47a386159afcb870830f4634927a6f04/.github)
- [Fabric 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/a44c1aae9558aa6b8b24c7a869b42c9716607711/.github)
- [NeoForge 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/71763cb63e3e1558d0e3bf3b1f122bcf704af96c/.github)
