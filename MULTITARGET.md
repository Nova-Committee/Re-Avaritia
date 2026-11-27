# Re:Avaritia 单分支构建试点

试点沿用 Forge 1.20.1 的仓库历史。每个目标保留自己的 Wrapper、构建插件、模组元数据和 Gradle 任务。

| 根脚本目标 | 构建目录 | JDK | Gradle | 远端基线 |
| --- | --- | --- | --- | --- |
| `forge` | `targets/forge` | 17 | 8.12 | [Forge 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb) |
| `fabric` | `targets/fabric` | 17 | 8.12 | [Fabric 1.20.1](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5) |
| `fabric-26.3` | `targets/fabric-26.3` | 25 | 9.7.1 | [Fabric 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/a44c1aae9558aa6b8b24c7a869b42c9716607711) |
| `neoforge-26.3` | `targets/neoforge-26.3` | 25 | 9.1.0 | [NeoForge 26.3](https://github.com/Nova-Committee/Re-Avaritia/tree/71763cb63e3e1558d0e3bf3b1f122bcf704af96c) |

设置两个 JDK 安装目录后，可从根目录依次构建四个目标。单目标也可直接使用与之匹配的 `JAVA_HOME` 或 PATH 中的 Java。Gradle 缓存由继承的 `GRADLE_USER_HOME` 管理。

```bash
export AVARITIA_JAVA17_HOME=/path/to/jdk-17
export AVARITIA_JAVA25_HOME=/path/to/jdk-25
bash build-targets.sh all
bash build-targets.sh forge
bash build-targets.sh fabric
bash build-targets.sh fabric-26.3
bash build-targets.sh neoforge-26.3
```

默认任务为 `build`。目标名后可传入 Gradle 任务和参数，例如 `bash build-targets.sh fabric build --offline`。各目标目录可直接运行 `bash gradlew runClient` 或 `bash gradlew runServer`。正式 JAR 位于各目标的 `build/libs/`。原始 GitHub 工作流保存在各目标目录，供核对基线。

压缩箱贴图在 `shared/src/main/resources` 维护，两代渲染代码读取同一路径的 PNG，26.3 自己的图集声明仍在各目标的生成资源中。`PortableLayout.java` 按游戏版本分别在 `shared/1.20.1/src/main/java` 和 `shared/26.3/src/main/java` 维护。构建在编译和处理资源前检查各目标源集中的同路径文件，包含生成资源目录。数据生成应使用独立导出目录，共享贴图在 `shared` 中编辑。
