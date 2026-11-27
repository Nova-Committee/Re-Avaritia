# Re:Avaritia 1.20.1 双加载器试点

试点沿用 Forge 1.20.1 的仓库历史。`targets/forge` 的构建树从 [Forge 基线](https://github.com/Nova-Committee/Re-Avaritia/tree/a235038d46493225d2a80fc72ef115cff8e0fbbb) 移入，`targets/fabric` 的构建树由 [Fabric 基线](https://github.com/Nova-Committee/Re-Avaritia/tree/4926d67b23e8e31fe2ecc68205640bfffd6236e5) 导入。两个目录各自保留 Wrapper、构建插件、模组元数据和对应的 Gradle 任务。Fabric 构建继续使用随目标导入的许可证。

在根目录用 Java 17 调度两个独立的 Gradle 构建。

```bash
bash build-targets.sh all
bash build-targets.sh forge
bash build-targets.sh fabric
```

默认任务是 `build`，也可在目标名后传入 Gradle 任务和参数，例如 `bash build-targets.sh fabric build --offline`。可从各目标目录直接启动 `bash gradlew runClient` 或 `bash gradlew runServer`。正式 JAR 位于各目标的 `build/libs/`。两端各自的原始 GitHub 工作流保存在目标目录中，供核对基线。

`shared/src/main` 保存压缩箱贴图与 `PortableLayout.java` 的唯一源文件。修改共享内容后同时构建两个目标。两端在编译和处理资源前检查本地源集（包括生成资源）中有无同路径文件，发现重复时构建会报告具体文件。数据生成的导出目录请与源集分开，共享贴图在 `shared` 修改。
