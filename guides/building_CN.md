# 构建与测试 Re:Avaritia

[English](building.md) · [返回项目首页](../README_CN.md)

在仓库根目录运行 `bash build-targets.sh <目标>`。默认调用该目标的 Gradle `build`，其中包含单元测试。目标名后可以传 Gradle 任务与参数。七个目标各自维护 Wrapper 和构建依赖。

| 游戏和加载器 | 目标 | Gradle JDK / Java 工具链 | Wrapper | 主 JAR 任务 | 数据入口 |
| --- | --- | --- | --- | --- | --- |
| Forge 1.20.1 | [`forge`](../targets/forge/) | 17 / 17 | 8.12 | `jar` | `runData` |
| Fabric 1.20.1 | [`fabric`](../targets/fabric/) | 17 / 17 | 8.12 | `remapJar` | [保留资源导出](#fabric-数据导出) |
| NeoForge 1.21.1 | [`neoforge-1.21.1`](../targets/neoforge-1.21.1/) | 21 / 21 | 8.8 | `jar` | `runData` |
| NeoForge 1.21.11 | [`neoforge-1.21.11`](../targets/neoforge-1.21.11/) | 25 / 21 | 9.1.0 | `jar` | `runClientData`、`runServerData` |
| NeoForge 26.1.2 | [`neoforge-26.1.2`](../targets/neoforge-26.1.2/) | 25 / 25 | 9.1.0 | `jar` | `runClientData`、`runServerData` |
| Fabric 26.3 | [`fabric-26.3`](../targets/fabric-26.3/) | 25 / 25 | 9.7.1 | `jar` | [保留资源导出](#fabric-数据导出) |
| NeoForge 26.3 | [`neoforge-26.3`](../targets/neoforge-26.3/) | 25 / 25 | 9.1.0 | `jar` | `runClientData`、`runServerData` |

## 常用命令

构建全部目标时设置以下三个变量。构建单个目标也可以从 `JAVA_HOME` 或 `PATH` 取得相应 JDK。NeoForge 1.21.11 用 JDK 25 启动 Gradle，用 JDK 21 编译和运行游戏。未设 `AVARITIA_JAVA21_HOME` 时，由 Gradle 探测 Java 21 工具链。可设置 `GRADLE_USER_HOME` 让各目标复用同一缓存。

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

每个目标的主 JAR 位于 `targets/<目标>/build/libs/`，文件名形如 `Re-Avaritia-<加载器>-<游戏版本>-<模组版本>-<渠道>.jar`。带 `-sources`、`-javadoc` 或 `-api` 后缀的是部分目标提供的辅助产物。Fabric 26.3 使用无混淆的 Minecraft，主 JAR 由 `jar` 任务生成。Fabric 1.20.1 使用 `remapJar`。

[构建工作流](../.github/workflows/build.yml) 通过 `bash build-targets.sh --matrix` 读取目标与 JDK，逐目标构建和测试，归档 JAR 和 JUnit XML。独立版本分支的发布工作流见[维护分支历史](#维护分支)。

## 源码归属

- `targets/<目标>/src/main` 放平台专用代码、资源、元数据与模板。`src/generated/resources` 放该目标使用的已提交生成资源或保留资源。`src/test` 放单元测试，部分目标另有 `src/gameTest`。
- [共享 Java](../shared/src/main/java/) 维护 `PortableLayout`，[共享资源](../shared/src/main/resources/) 维护压缩箱主贴图。各版本的图集、模型和内嵌资源包仍由目标目录提供。[共享源检查](../gradle/shared-sources.gradle) 会拒绝覆盖这两份共享文件的同路径副本。

## 数据生成

Forge 1.20.1 和 NeoForge 1.21.1 使用 `runData`。NeoForge 1.21.11、26.1.2 和 26.3 在客户端数据任务中注册生成器，由 `runClientData` 写出文件。`runServerData` 可验证服务端数据入口。Forge 与 NeoForge 数据任务默认写入所选目标的 `src/generated/resources/`，提交前先检查生成差异。

```bash
bash build-targets.sh forge runData
bash build-targets.sh neoforge-1.21.1 runData
bash build-targets.sh neoforge-1.21.11 runClientData
```

### Fabric 数据导出

两个 Fabric 目标以 `src/main/resources/` 与 `src/generated/resources/` 内提交的资源为输入。[1.20.1 的 `ModDataGen`](../targets/fabric/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) 和 [26.3 的 `ModDataGen`](../targets/fabric-26.3/src/main/java/committee/nova/mods/avaritia/init/data/ModDataGen.java) 会导出目标资源及共享箱子贴图。在对应目标的 Gradle `main` 运行类路径上执行 `main`，传入目标目录和独立输出目录两个绝对路径。可以在源码目录外保存以下 Gradle 初始化脚本，命名为 `export-fabric.gradle`。

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

## Forge 附属模组依赖

为 Forge 1.20.1 的 ForgeGradle 附属模组配置仓库后，将 `avaritia_version` 设为完整产物版本，例如 `1.20.1-1.4.4-release`。

```groovy
repositories { maven { url 'https://maven.nova-committee.cn/releases' } }
dependencies { implementation fg.deobf("committee.nova.mods:avaritia-forge:${avaritia_version}") }
```

本地开发时运行 `bash build-targets.sh forge publishToMavenLocal`，再加入 `mavenLocal()`。

## 维护分支

以下是合并构建树时导入的维护分支提交，原有独立工作流可在各提交的 `.github/` 查看。

- [Forge 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb/.github)
- [Fabric 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5/.github)
- [NeoForge 1.21.1](https://github.com/Nova-Committee/Re-Avaritia/tree/5543b95c138b2cd692f90764122a50652a5d3e8c/.github)
- [NeoForge 1.21.11](https://github.com/Nova-Committee/Re-Avaritia/tree/dbd9e5b60524e97dacfc6160ad87ea7f8cf3dccf/.github)
- [NeoForge 26.1.2](https://github.com/Nova-Committee/Re-Avaritia/tree/86c930fc47a386159afcb870830f4634927a6f04/.github)
- [Fabric 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/a44c1aae9558aa6b8b24c7a869b42c9716607711/.github)
- [NeoForge 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/71763cb63e3e1558d0e3bf3b1f122bcf704af96c/.github)
