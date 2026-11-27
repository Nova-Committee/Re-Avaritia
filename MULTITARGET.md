# Re:Avaritia 单分支构建

各目标保留独立的 Gradle Wrapper、插件、模组元数据和构建任务。

| 根脚本目标 | 构建目录 | Gradle JDK | 编译 JDK | Gradle | 远端基线 |
| --- | --- | --- | --- | --- | --- |
| `forge` | `targets/forge` | 17 | 17 | 8.12 | [Forge 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb) |
| `fabric` | `targets/fabric` | 17 | 17 | 8.12 | [Fabric 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5) |
| `fabric-26.3` | `targets/fabric-26.3` | 25 | 25 | 9.7.1 | [Fabric 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/a44c1aae9558aa6b8b24c7a869b42c9716607711) |
| `neoforge-26.3` | `targets/neoforge-26.3` | 25 | 25 | 9.1.0 | [NeoForge 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/71763cb63e3e1558d0e3bf3b1f122bcf704af96c) |
| `neoforge-1.21.1` | `targets/neoforge-1.21.1` | 21 | 21 | 8.8 | [NeoForge 1.21.1](https://github.com/Nova-Committee/Re-Avaritia/tree/5543b95c138b2cd692f90764122a50652a5d3e8c) |
| `neoforge-1.21.11` | `targets/neoforge-1.21.11` | 25 | 21 | 9.1.0 | [NeoForge 1.21.11](https://github.com/Nova-Committee/Re-Avaritia/tree/dbd9e5b60524e97dacfc6160ad87ea7f8cf3dccf) |
| `neoforge-26.1.2` | `targets/neoforge-26.1.2` | 25 | 25 | 9.1.0 | [NeoForge 26.1.2](https://github.com/Nova-Committee/Re-Avaritia/tree/86c930fc47a386159afcb870830f4634927a6f04) |

从根目录依次构建七个目标时，分别指定三个 JDK。单目标也可从 `JAVA_HOME` 或 `PATH` 取得运行 Gradle 的 JDK。若 `AVARITIA_JAVA21_HOME` 未设置，NeoForge 1.21.11 由 Gradle 查找 Java 21 编译工具链。设置 `GRADLE_USER_HOME` 可共用 Gradle 缓存。

```bash
export AVARITIA_JAVA17_HOME=/path/to/jdk-17
export AVARITIA_JAVA21_HOME=/path/to/jdk-21
export AVARITIA_JAVA25_HOME=/path/to/jdk-25
bash build-targets.sh all
bash build-targets.sh neoforge-1.21.11
bash build-targets.sh fabric build --offline
```

默认任务为 `build`，目标名后可传入 Gradle 任务及参数。`bash build-targets.sh --matrix` 输出 GitHub Actions 使用的矩阵。CI 在分支推送、PR 或手动运行时构建、测试并归档各目标的 JAR 和测试 XML。原分支工作流保存在各目标目录供核对。

开发启动可在目标目录运行 `bash gradlew runClient` 和 `bash gradlew runServer`。NeoForge 1.21.1 使用 `runData`，三个较新的 NeoForge 目标使用 `runServerData` 和 `runClientData`。正式 JAR 位于各目标的 `build/libs/`。

压缩箱主贴图和 `PortableLayout.java` 分别在 `shared/src/main/resources` 与 `shared/src/main/java` 维护。共享布局只使用七端 Minecraft 均提供的 `ScreenRectangle` 构造器与边界方法。图集、模型以及内嵌资源包由目标各自提供。各目标编译或处理资源前都会检查主、生成和其他源集中的同路径副本。数据生成使用共享贴图作为现有资源。
