
## 2026-05-25 Gate-2 compile validation
- `./gradlew compileJava --no-daemon` still fails with javac's capped `100 errors` after import fixes. Remaining errors are missing Wave 4 classes/packages and were intentionally deferred per task constraints.
- LSP diagnostics could not run because `jdtls` is unavailable in this environment (`LSP server exited immediately with code 1`). Build output was used as verification evidence instead.

## Wave 3 Task 3.1 — 验证阻塞
- `lsp_diagnostics` 无法运行：环境缺少 `jdtls` 命令。
- `./gradlew compileJava` 失败原因是既有/其他任务未完成：缺少 `net.neoforged.neoforge.client.model.generators.*`、`ExistingFileHelper`、`ResourceLocation`（应迁移为 26.1.2 API），以及大量未迁移的 `com.avaritia.common.*` 类型。
- `./gradlew runData` 同样在编译阶段被上述非本任务问题阻塞，尚不能生成 `src/generated/resources/assets/avaritia/lang/en_us.json`。
